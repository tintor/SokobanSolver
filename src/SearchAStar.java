import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import util.Human;
import base.Flags;
import base.Key;
import base.KeySet;
import base.KeyUtil;
import base.Stats;

public class SearchAStar extends Search {
	private volatile Result result;
	private final AtomicInteger closedKeysAtomic = new AtomicInteger();

	private PriorityQueue<Key> queue;
	private ConcurrentWorkPriorityQueue<Key> concurrentQueue;
	private KeySet keySet;

	@Override
	public int setSize() {
		return keySet.size();
	}

	@Override
	public void print() {
		print(keySet.size(), closedKeys, openKeys, null);
	}

	private static void print(int set, int closedKeys, int openKeys, Key lastKey) {
		synchronized (System.out) {
			System.out.printf("[set %s, closed %s, open %s", Human.human(set), Human.human(closedKeys),
					Human.human(openKeys));
			System.out.printf(", branching %.1f", (double) set / closedKeys);
			if (lastKey != null)
				System.out.printf(", key %s/%s", lastKey.pushes, lastKey.total);
			System.out.printf(", freemem %s", Human.human(Runtime.getRuntime().freeMemory()));
			System.out.print("]\n");
		}
	}

	@Override
	public Result run() {
		Stats.instance.set(new Stats());
		stats = Stats.instance.get();

		if (start.isGoal()) {
			lastKey = start;
			openKeys = queue.size();
			return Result.Solved;
		}

		Key[] pushes = new Key[start.agent.level.goals.length * 4];
		closedKeys = 0;

		keySet = new KeySet(16, 0.75f);
		keySet.add(start);

		queue = new PriorityQueue<Key>();
		queue.add(start);

		while (queue.size() > 0) {
			Key key = queue.poll();

			if (Flags.VerboseSearch) {
				System.out.println("=========");
				System.out.printf("total %s, pushes %s, vhash %s\n", key.total, key.pushes, key.virtualHashCode());
				System.out.print(key);
			}

			if (Flags.FindOptimalSolution && key.isGoal()) {
				lastKey = key;
				openKeys = queue.size();
				return Result.Solved;
			}

			// key.analyzeCorrals();

			int size = KeyUtil.computePushes(key, pushes);
			for (int i = 0; i < size; i++) {
				Key push = pushes[i];

				if (!keySet.add(push))
					continue;

				if (!Flags.FindOptimalSolution && push.isGoal()) {
					lastKey = push;
					openKeys = queue.size();
					return Result.Solved;
				}

				if (heuristic.update(push))
					continue;

				if (Flags.VerboseSearch) {
					System.out.printf("total %s, pushes %s, vhash %s\n", push.total, push.pushes,
							push.virtualHashCode());
					System.out.print(push);
				}
				queue.add(push);
			}

			closedKeys += 1;
			if (closedKeys % (1 << verbose) == 0) {
				print(keySet.size(), closedKeys, queue.size(), key);
			}
			if (keySet.size() >= maxSetSize) {
				lastKey = key;
				openKeys = queue.size();
				return Result.Timeout;
			}
		}

		openKeys = 0;
		return Result.NoSolution;
	}

	private class Runner extends Thread {
		Key[] pushes;
		int size;

		@Override
		public void run() {
			while (true) {
				try {
					raceStart.acquire();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				// Reset structures
				pushes = new Key[start.agent.level.goals.length * 4];
				size = 0;
				Stats.instance.set(new Stats());

				concurrentQueue.attach();
				try {
					while (true) {
						Key key = concurrentQueue.exchange(pushes, size);
						if (key == null)
							break;

						if (Flags.VerboseSearch) {
							System.out.println("=========");
							System.out.printf("total %s, pushes %s, vhash %s\n", key.total, key.pushes,
									key.virtualHashCode());
							System.out.print(key);
						}

						if (computePushes(key)) {
							concurrentQueue.abort();
							break;
						}

						if (Flags.VerboseSearch) {
							for (int i = 0; i < size; i++) {
								Key push = pushes[i];
								System.out.printf("total %s, pushes %s, vhash %s\n", push.total, push.pushes,
										push.virtualHashCode());
								System.out.print(push);
							}
						}
					}
				} finally {
					concurrentQueue.detach();
					synchronized (SearchAStar.this.stats) {
						SearchAStar.this.stats.add(Stats.instance.get());
					}
				}

				raceEnd.release();
			}
		}

		boolean computePushes(Key key) {
			KeyUtil.computePushes(key, pushes);

			// Register unique pushes and remove duplicates (while shortly
			// keeping the lock on keys.set)
			synchronized (keySet) {
				int w = 0;
				for (int i = 0; i < size; i++) {
					Key push = pushes[i];
					if (keySet.add(push))
						pushes[w++] = push;
				}
				size = w;
			}

			// Check if we've reached goal
			int w = 0;
			for (int i = 0; i < size; i++) {
				Key push = pushes[i];
				if (push.isGoal()) {
					synchronized (SearchAStar.this) {
						lastKey = push;
						result = Result.Solved;
					}
					return true;
				}

				if (heuristic.update(push))
					continue;

				pushes[w++] = push;
			}
			size = w;

			int closed = closedKeysAtomic.incrementAndGet();
			int setSize;
			synchronized (keySet) {
				setSize = keySet.size();
			}
			if (closed % (1 << verbose) == 0)
				print(setSize, closed, concurrentQueue.size(), key);
			if (setSize >= maxSetSize) {
				synchronized (SearchAStar.this) {
					if (result == Result.NoSolution) {
						lastKey = key;
						result = Result.Timeout;
					}
				}
				return true;
			}

			return false;
		}
	}

	private Runner[] runners;
	private Semaphore raceStart;
	private Semaphore raceEnd;

	@Override
	public Result runConcurrently() {
		if (Flags.FindOptimalSolution)
			throw new RuntimeException();

		stats = new Stats();

		keySet = new KeySet(16, 0.75f);
		keySet.add(start);

		concurrentQueue = new ConcurrentWorkPriorityQueue<Key>();
		concurrentQueue.put(start);

		result = Result.NoSolution;
		closedKeysAtomic.set(0);

		if (runners == null) {
			if (maxDOP == 0)
				throw new IllegalArgumentException();
			runners = new Runner[Math.min(Runtime.getRuntime().availableProcessors(), maxDOP)];
			raceStart = new Semaphore(0);
			raceEnd = new Semaphore(0);
			for (int i = 0; i < runners.length; i++) {
				Runner runner = runners[i] = new Runner();
				runner.setDaemon(true);
				runner.start();
			}
		}

		raceStart.release(runners.length);
		try {
			raceEnd.acquire(runners.length);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		openKeys = concurrentQueue.size();
		closedKeys = closedKeysAtomic.get();
		return result;
	}
}
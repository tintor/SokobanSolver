import java.util.Arrays;

import util.Stack;
import base.Flags;
import base.Key;
import base.KeySet;
import base.KeyUtil;
import base.Stats;

public class SearchIDA extends Search {
	enum IDAResult {
		FoundSolution, Deadlock, Cutoff
	}

	@Override
	public int setSize() {
		return -1;
	}

	@Override
	public void print() {
	}

	@Override
	public Result run() {
		Stats.instance.set(new Stats());
		stats = Stats.instance.get();

		pushes = new Key[start.agent.level.goals.length * 4];
		deadlocks = new KeySet(16, 0.75f);
		visited = new KeySet(16, 0.75f);
		visited.add(start);

		Stack<Key> stack = new Stack<Key>(100);
		// ArrayDeque<IDAResult> path = new ArrayDeque<IDAResult>();

		cutoff = heuristic.estimate(start);
		while (cutoff < Integer.MAX_VALUE) {
			if (Flags.VerboseSearch)
				System.out.printf("cutoff %s\n", cutoff);

			minEstimateAboveCutoff = Integer.MAX_VALUE;

			stack.clear();
			stack.push(start);

			while (stack.size() > 0) {
				Key key = stack.pop();

				// path.addLast(IDAResult.Deadlock);

				int size = computePushes(key);
				if (size > 0 && pushes[0].total == pushes[0].pushes) {
					lastKey = key;
					return Search.Result.Solved;
				}

				// Add pushes to stack in reverse order
				stack.ensure(size);
				while (size > 0)
					stack.push(pushes[--size]);

				// path.removeLast();
			}

			cutoff = minEstimateAboveCutoff;
		}

		return Result.NoSolution;
	}

	private IDAResult dfs(Key key, int cutoff) {
		if (key.isGoal())
			return IDAResult.FoundSolution;

		if (key.total > cutoff)
			return IDAResult.Cutoff;

		result = IDAResult.Deadlock;
		int size = computePushes(key);

		for (int i = 0; i < size; i++) {
			switch (dfs(pushes[i], cutoff)) {
			case FoundSolution:
				return IDAResult.FoundSolution;

			case Deadlock:
				deadlocks.add(pushes[i]);
				break;

			case Cutoff:
				result = IDAResult.Cutoff;
				break;
			}
		}

		return result;
	}

	private int minEstimateAboveCutoff;
	private KeySet deadlocks;
	private KeySet visited;

	private int cutoff;
	private Key[] pushes;
	private IDAResult result;

	private int computePushes(Key key) {
		int size = KeyUtil.computePushes(key, pushes);
		Stats stats = Stats.instance.get();

		int outSize = 0;
		for (int i = 0; i < size; i++) {
			Key push = pushes[i];

			if (deadlocks.contains(push)) {
				stats.liveDeadlocks += 1;
				continue;
			}

			if (!visited.add(push)) {
				stats.transpositions += 1;
				continue;
			}

			// compute estimate
			int estimate = heuristic.estimate(push);
			if (estimate == Integer.MAX_VALUE) {
				stats.estimatedDeadlock += 1;
				continue;
			}
			push.total = push.pushes + estimate;
			assert push.total >= 0;
			if (push.total > cutoff) {
				minEstimateAboveCutoff = Math.min(minEstimateAboveCutoff, push.total);
				result = IDAResult.Cutoff;
				continue;
			}

			pushes[outSize++] = push;
		}

		Arrays.sort(pushes, 0, outSize);
		return outSize;
	}

	@Override
	public Result runConcurrently() {
		throw new RuntimeException();
	}
}
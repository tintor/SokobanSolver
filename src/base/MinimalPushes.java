package base;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Queue;

final class BoxKey {
	final Cell agent;
	final Cell box;
	final int pushes;
	BoxKey nextInBucket;

	BoxKey(Cell agent, Cell box, int pushes) {
		this.agent = agent;
		this.box = box;
		this.pushes = pushes;
	}

	boolean equals(BoxKey key) {
		if (box != key.box)
			return false;
		if (agent == key.agent)
			return true;

		CellSearch2 search = agent.level.cellSearch2.get();
		for (Cell cell : search.init(agent, box)) {
			if (cell == key.agent)
				return true;
			search.addLinks(cell);
		}
		return false;
	}
}

final class BoxKeySet {
	private BoxKey table[];

	BoxKeySet(int cells) {
		table = new BoxKey[cells];
	}

	boolean add(BoxKey key) {
		int index = key.box.id;
		for (BoxKey e = table[index]; e != null; e = e.nextInBucket)
			if (key.equals(e))
				return false;

		assert key.nextInBucket == null;
		key.nextInBucket = table[index];
		table[index] = key;
		return true;
	}

	void clear() {
		for (int i = 0; i < table.length; i++)
			table[i] = null;
	}
}

final class MinimalPushes {
	private static void minimalPushesVisit(BoxKey key, CellSearch search, BoxKeySet keySet, Queue<BoxKey> keyQueue,
			Dir dir, Cell cell) {
		if (cell == null)
			return;

		if (key.box != cell) {
			search.add(cell);
			return;
		}

		Cell dest = cell.get(dir);
		if (dest == null || dest.isDead)
			return;

		BoxKey push = new BoxKey(cell, dest, key.pushes + 1);
		if (keySet.add(push))
			keyQueue.add(push);
	}

	static void compute(Level level) {
		CellSearch search = new CellSearch(level.cells.length);
		BoxKeySet keySet = new BoxKeySet(level.cells.length);
		Queue<BoxKey> keyQueue = new ArrayDeque<BoxKey>();

		for (Cell cell : level.cells) {
			cell.goalPushes = Integer.MAX_VALUE;
			cell.isDead = !cell.isGoal && cell.links() <= 2 && !cell.isHorizontal() && !cell.isVertical();
			if (cell.isDead)
				continue;

			BoxKey start = new BoxKey(cell, cell, 0);

			keySet.clear();
			keyQueue.clear();
			keySet.add(start);
			keyQueue.add(start);

			while (keyQueue.size() > 0) {
				BoxKey key = keyQueue.poll();

				if (cell.pushes.size() == 0)
					cell.pushes = new IdentityHashMap<Cell, Integer>();

				if (!cell.pushes.containsKey(key.box)) {
					cell.pushes.put(key.box, key.pushes);
					if (key.box.isGoal)
						cell.goalPushes = Math.min(cell.goalPushes, key.pushes);
				}

				for (Cell vcell : search.init(key.agent)) {
					minimalPushesVisit(key, search, keySet, keyQueue, Dir.Left, vcell.left);
					minimalPushesVisit(key, search, keySet, keyQueue, Dir.Right, vcell.right);
					minimalPushesVisit(key, search, keySet, keyQueue, Dir.Up, vcell.up);
					minimalPushesVisit(key, search, keySet, keyQueue, Dir.Down, vcell.down);
				}
			}

			cell.isDead = cell.goalPushes == Integer.MAX_VALUE;
		}
	}
}

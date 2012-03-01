package base;

import java.util.List;

import util.BitSet;

public class KeyUtil {
	static void computePushesVisit(Key key, List<Key> pushes, CellSearch search, Dir dir, Cell cell) {
		if (cell == null)
			return;

		if (!key.containsBox(cell)) {
			search.add(cell);
			return;
		}

		Key push = key.push(cell, cell.get(dir));
		if (push != null)
			pushes.add(push);
	}

	/*
	public static List<Key> computePushes(Key key, List<Key> pushes) {
		pushes.clear();
		CellSearch search = key.agent.level.computePushesCellSearch.get();
		for (Cell cell : search.init(key.agent)) {
			computePushesVisit(key, pushes, search, Dir.Left, cell.left);
			computePushesVisit(key, pushes, search, Dir.Right, cell.right);
			computePushesVisit(key, pushes, search, Dir.Up, cell.up);
			computePushesVisit(key, pushes, search, Dir.Down, cell.down);
		}
		return pushes;
	}
	*/

	public static int computePushes(Key key, Key[] pushes) {
		int size = 0;
		CellSearch search = key.agent.level.computePushesCellSearch.get();
		for (Cell cell : search.init(key.agent)) {
			if (cell.left != null)
				if (!key.containsBox(cell.left))
					search.add(cell.left);
				else {
					Key push = key.push(cell.left, cell.left.get(Dir.Left));
					if (push != null)
						pushes[size++] = push;
				}

			if (cell.right != null)
				if (!key.containsBox(cell.right))
					search.add(cell.right);
				else {
					Key push = key.push(cell.right, cell.right.get(Dir.Right));
					if (push != null)
						pushes[size++] = push;
				}

			if (cell.up != null)
				if (!key.containsBox(cell.up))
					search.add(cell.up);
				else {
					Key push = key.push(cell.up, cell.up.get(Dir.Up));
					if (push != null)
						pushes[size++] = push;
				}

			if (cell.down != null)
				if (!key.containsBox(cell.down))
					search.add(cell.down);
				else {
					Key push = key.push(cell.down, cell.down.get(Dir.Down));
					if (push != null)
						pushes[size++] = push;
				}
		}
		return size;
	}

	static int[] createBoxset(Cell... boxes) {
		Level level = boxes[0].level;
		int[] boxset = new int[(level.aliveCells() + 31) / 32];
		for (Cell box : boxes) {
			BitSet.setBit(boxset, box.id);
			assert level == box.level;
		}
		assert boxes.length == BitSet.bitCount(boxset);
		return boxset;
	}

	static long createBoxset64(Level level, Cell... boxes) {
		assert level.aliveCells() <= 64;
		long boxset = 0;
		for (Cell box : boxes) {
			boxset |= 1L << box.id;
			assert level == box.level;
		}
		assert boxes.length == BitSet.bitCount(boxset);
		return boxset;
	}

	static Cell[] getBoxes(Level level, int[] boxset) {
		Cell[] boxes = new Cell[BitSet.bitCount(boxset)];
		int w = 0;
		for (int i = 0; i < boxset.length; i++) {
			int b = boxset[i];
			if (b == 0)
				continue;

			for (int j = 0; j < Math.min(32, level.cells.length - i * 32); j++)
				if (((1 << j) & b) != 0)
					boxes[w++] = level.cells[(i << 5) + j];
		}
		assert w == boxes.length;
		return boxes;
	}

	static boolean containsBox(int[] boxset, Cell cell) {
		return BitSet.testBitExt(boxset, cell.id);
	}

	// Find agent reachable cell with smallest ID
	static Cell normalizeAgent(Cell agent, int[] boxset) {
		if (agent.id == 0)
			return agent;

		Cell normAgent = agent;
		CellSearch2 search = agent.level.cellSearch2.get();
		for (Cell cell : search.init(agent, boxset)) {
			if (cell.id < normAgent.id) {
				if (cell.id == 0)
					return cell;
				normAgent = cell;
			}
			search.addLinks(cell);
		}
		return normAgent;
	}

	static boolean reachable(Cell a, Cell b, int[] boxset) {
		if (a == b)
			return true;

		CellSearch2 search = a.level.cellSearch2.get();
		// Consider: Make it a guided search in case level is large.
		for (Cell cell : search.init(a, boxset)) {
			if (cell == b)
				return true;
			search.addLinks(cell);
		}
		return false;
	}

	static int computeHashCode(Cell[] boxes) {
		int hash = 0;
		for (Cell box : boxes)
			hash ^= Zobrist.box(box.id);
		return hash;
	}

	static int computeHashCode(Level level, int[] boxset) {
		int hash = 0;
		for (int i = 0; i < level.cells.length; i++)
			if (BitSet.testBit(boxset, i))
				hash ^= Zobrist.box(i);
		return hash;
	}

	static int computeHashCode(Cell agent, Cell[] boxes) {
		int hash = Flags.IncludeAgentInHashCode ? Zobrist.agent(agent.id) : 0;
		for (Cell box : boxes)
			hash ^= Zobrist.box(box.id);
		return hash;
	}

	static int computeHashCode(Cell agent, int[] boxset) {
		int hash = Flags.IncludeAgentInHashCode ? Zobrist.agent(agent.id) : 0;
		for (int i = 0; i < agent.level.cells.length; i++)
			if (i < boxset.length * 32 && BitSet.testBit(boxset, i))
				hash ^= Zobrist.box(i);
		return hash;
	}

	static int computeHashCode(Cell agent, long boxset) {
		int hash = Flags.IncludeAgentInHashCode ? Zobrist.agent(agent.id) : 0;
		for (int i = 0; i < agent.level.cells.length; i++)
			if ((boxset & (1L << i)) != 0)
				hash ^= Zobrist.box(i);
		return hash;
	}
}
package base;

import java.util.Arrays;

import util.BitSet;
import util.Human;

public final class Key implements Comparable<Key> {
	public final Cell agent;

	// private final long boxsetLow;
	// private final long boxsetHigh;
	private int[] boxset;

	public final Key prev;

	public final int pushes;
	public int total; // = pushes + estimated number of pushes to goal

	int hashCode;
	Key nextInBucket;

	public static Key create(Cell agent, Cell[] boxes) {
		int[] boxset = KeyUtil.createBoxset(boxes);
		Key key = new Key(agent, boxset, null);
		if (Flags.CacheHashCode)
			key.hashCode = KeyUtil.computeHashCode(agent, boxes);
		return key;
	}

	public static Key create(Cell agent, int[] boxset) {
		Key key = new Key(agent, boxset, null);
		if (Flags.CacheHashCode)
			key.hashCode = KeyUtil.computeHashCode(agent, boxset);
		return key;
	}

	public boolean canPush(Cell src, Dir dir) {
		// TODO check dest and frozen box deadlocks
		throw new RuntimeException();
	}

	public Key push(Cell box, Cell dest) {
		if (dest == null || containsBox(dest) || dest.isDead)
			return null;

		int[] newBoxset = Arrays.copyOf(boxset, boxset.length);
		BitSet.setBit(newBoxset, dest.id);
		BitSet.clearBit(newBoxset, box.id);
		Cell newAgent = box;

		if (Flags.CheckQuick2x2 && Deadlock.checkQuick2x2(newBoxset, dest))
			return null;

		if (Flags.CheckFrozenBoxes && Deadlock.frozenBoxes(newAgent, newBoxset))
			return null;

		if (Flags.CheckDeadlockDB && inDeadlockDB(newAgent, newBoxset, dest))
			return null;

		Key key = new Key(newAgent, newBoxset, this);

		if (Flags.CacheHashCode) {
			key.hashCode = hashCode ^ Zobrist.box(box.id) ^ Zobrist.box(dest.id);
			if (Flags.IncludeAgentInHashCode)
				key.hashCode = key.hashCode ^ Zobrist.agent(agent.id) ^ Zobrist.agent(newAgent.id);
		}

		return key;
	}

	Key(Cell agent, int[] boxset, Key prev) {
		this.agent = agent;
		this.boxset = boxset;
		this.prev = prev;
		this.pushes = prev != null ? prev.pushes + 1 : 0;
	}

	public static boolean inDeadlockDB(Cell agent, int[] boxset, Cell box) {
		for (Key key : box.deadlockDB)
			if (key.subkey(agent, boxset)) {
				Stats.instance.get().deadlockDB += 1;
				return true;
			}
		return false;
	}

	private boolean subkey(Cell agentS, int[] boxsetS) {
		assert boxset.length == boxsetS.length;
		for (int i = 0; i < boxset.length; i++)
			if ((boxset[i] | boxsetS[i]) != boxsetS[i])
				return false;
		return KeyUtil.reachable(agent, agentS, boxsetS);
	}

/*	public boolean frozenBoxes() {
		return KeyUtil.frozenBoxes(agent, boxset);
	}*/

	public boolean containsBox(Cell cell) {
		return KeyUtil.containsBox(boxset, cell);
	}

	@SuppressWarnings("unused")
	public boolean isGoal() {
		if (Flags.CacheHashCode)
			return agent.level.isGoal(Flags.IncludeAgentInHashCode ? hashCode ^ Zobrist.agent(agent.id) : hashCode,
					boxset);
		return agent.level.isGoal(boxset);
	}

	public Cell[] boxes() {
		return KeyUtil.getBoxes(agent.level, boxset);
	}

	public void printSolution() {
		if (prev != null)
			prev.printSolution();
		System.out.println(this);
	}

	public boolean equals(Key key) {
		if (Flags.CacheHashCode && hashCode != key.hashCode)
			return false;

		if (!Arrays.equals(boxset, key.boxset))
			return false;

		return KeyUtil.reachable(agent, key.agent, boxset);
	}

	@Override
	public boolean equals(Object key) {
		return equals((Key) key);
	}

	@Override
	public int hashCode() {
		return Flags.CacheHashCode ? hashCode : KeyUtil.computeHashCode(agent, boxset);
	}

	public int virtualHashCode() {
		return Flags.IncludeAgentInHashCode ? hashCode : (hashCode ^ Zobrist.agent(agent.id));
	}

	@SuppressWarnings("unused")
	private char toChar(Cell c) {
		if (c.marker != '\0')
			return c.marker;
		if (c.isGoal) {
			if (agent == c)
				return '+';
			if (containsBox(c))
				return '*';
			return '.';
		}
		if (agent == c)
			return '@';
		if (containsBox(c))
			return '$';
		if (Flags.PrintDeadCells && c.isDead)
			return 'x';
		return ' ';
	}

	@Override
	public String toString() {
		char[] map = new char[agent.level.cells.length];
		for (int i = 0; i < map.length; i++)
			map[i] = toChar(agent.level.cells[i]);
		return agent.level.printer.toString(map);
	}

	public int boxesOnGoals() {
		int result = 0;
		for (Cell c : agent.level.goals)
			if (containsBox(c))
				result += 1;
		return result;
	}

	@Override
	public int compareTo(Key other) {
		int t = total - other.total;
		if (t != 0)
			return t;

		int p = other.pushes - pushes;
		if (p != 0)
			return p;

		int q = other.boxesOnGoals() - boxesOnGoals();
		if (q != 0)
			return q;

		return virtualHashCode() - other.virtualHashCode();
	}
}
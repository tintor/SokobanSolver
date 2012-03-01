package base;

import java.util.Arrays;

import util.BitSet;

public final class AgentlessKey implements Comparable<AgentlessKey> {
	private int[] boxset;

	public final int pushes;
	public int total; // = pushes + estimated number of pushes to goal

	int hashCode;
	AgentlessKey nextInBucket;

	public static AgentlessKey create(Cell[] boxes) {
		int[] boxset = KeyUtil.createBoxset(boxes);
		AgentlessKey key = new AgentlessKey(boxset, null);
		key.hashCode = KeyUtil.computeHashCode(boxes);
		return key;
	}

	public AgentlessKey push(Cell src, Dir dir) {
		Cell box = src.get(dir);
		Cell dest = box.get(dir);

		assert !containsBox(src);
		assert containsBox(box);

		if (dest == null || containsBox(dest) || dest.isDead)
			return null;

		int[] newBoxset = Arrays.copyOf(boxset, boxset.length);
		BitSet.setBit(newBoxset, dest.id);
		BitSet.clearBit(newBoxset, box.id);

		if (Flags.CheckQuick2x2 && Deadlock.checkQuick2x2(newBoxset, dest))
			return null;

		if (Flags.CheckFrozenBoxes && Deadlock.frozenBoxes(box, newBoxset))
			return null;

		AgentlessKey key = new AgentlessKey(newBoxset, this);
		key.hashCode = hashCode ^ Zobrist.box(box.id) ^ Zobrist.box(dest.id);
		return key;
	}

	private AgentlessKey(int[] boxset, AgentlessKey prev) {
		this.boxset = boxset;
		this.pushes = prev != null ? prev.pushes + 1 : 0;
	}

	public boolean containsBox(Cell cell) {
		return BitSet.testBit(boxset, cell.id);
	}

	public boolean isGoal(Level level) {
		return level.isGoal(hashCode, boxset);
	}

	public boolean equals(AgentlessKey key) {
		return hashCode == key.hashCode && Arrays.equals(boxset, key.boxset);
	}

	@Override
	public boolean equals(Object key) {
		return equals((AgentlessKey) key);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public int compareTo(AgentlessKey other) {
		if (total != other.total)
			return total - other.total;

		if (pushes != other.pushes)
			return other.pushes - pushes;

		return hashCode - other.hashCode;
	}
}
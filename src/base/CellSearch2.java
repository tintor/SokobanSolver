package base;

import java.util.Iterator;

import util.BitSet;

public class CellSearch2 implements Iterable<Cell> {
	private int[] set; // bitset
	private Cell[] queue;
	private int head, tail;

	public CellSearch2(int cells) {
		set = new int[(cells + 31) / 32];
		queue = new Cell[cells + 1];
	}

	public CellSearch2(Cell start) {
		this(start.level.cells.length);
		add(start);
	}

	public CellSearch2 init() {
		for (int i = 0; i < set.length; i++)
			set[i] = 0;

		head = tail = 0;
		return this;
	}

	public CellSearch2 init(Cell cell) {
		for (int i = 0; i < set.length; i++)
			set[i] = 0;

		BitSet.setBit(set, cell.id);
		queue[0] = cell;
		head = 0;
		tail = 1;
		return this;
	}

	public CellSearch2 init(Cell cell, Cell box) {
		for (int i = 0; i < set.length; i++)
			set[i] = 0;

		BitSet.setBit(set, cell.id);
		BitSet.setBit(set, box.id);
		queue[0] = cell;
		head = 0;
		tail = 1;
		return this;
	}

	public CellSearch2 init(Cell cell, int[] initset) {
		for (int i = 0; i < initset.length; i++)
			set[i] = initset[i];
		for (int i = initset.length; i < set.length; i++)
			set[i] = 0;

		BitSet.setBit(set, cell.id);
		queue[0] = cell;
		head = 0;
		tail = 1;
		return this;
	}

	public void addLinks(Cell cell) {
		if (cell.left != null)
			add(cell.left);
		if (cell.right != null)
			add(cell.right);
		if (cell.up != null)
			add(cell.up);
		if (cell.down != null)
			add(cell.down);
	}

	public boolean add(Cell cell) {
		if (BitSet.testBit(set, cell.id))
			return false;

		BitSet.setBit(set, cell.id);
		queue[tail++] = cell;
		return true;
	}

	public boolean visited(Cell cell) {
		return BitSet.testBit(set, cell.id);
	}

	// add cell to queue (if not already)
	public void revisit(Cell cell) {
		assert BitSet.testBit(set, cell.id);

		// add if not already if the queue
		for (int i = head; i < tail; i++)
			if (queue[i] == cell)
				return;

		if (head > 0)
			queue[--head] = cell;
		else
			queue[tail++] = cell;
	}

	public boolean tryAdd(Cell cell) {
		if (cell == null || BitSet.testBit(set, cell.id))
			return false;

		BitSet.setBit(set, cell.id);
		queue[tail++] = cell;
		return true;
	}

	private Iterator<Cell> iterator = new Iterator<Cell>() {
		@Override
		public boolean hasNext() {
			return head != tail;
		}

		@Override
		public Cell next() {
			return queue[head++];
		}

		@Override
		public void remove() {
			throw new RuntimeException();
		}
	};

	@Override
	public Iterator<Cell> iterator() {
		return iterator;
	}
}
package base;

import java.util.Iterator;

public class CellSearch implements Iterable<Cell> {
	private int[] set;
	private Cell[] queue;
	private int head, tail;
	private int timestamp = 1;

	public CellSearch(int cells) {
		set = new int[cells];
		queue = new Cell[cells + 1];
	}

	public CellSearch(Cell start) {
		this(start.level.cells.length);
		add(start);
	}

	public CellSearch init() {
		if (++timestamp == 0) {
			for (int i = 0; i < set.length; i++)
				set[i] = 0;
			timestamp = 1;
		}

		head = tail = 0;
		return this;
	}

	public CellSearch init(Cell cell) {
		if (++timestamp == 0) {
			for (int i = 0; i < set.length; i++)
				set[i] = 0;
			timestamp = 1;
		}

		set[cell.id] = timestamp;
		queue[0] = cell;
		head = 0;
		tail = 1;
		return this;
	}

	public void setVisited(Cell cell) {
		set[cell.id] = timestamp;
	}

	public boolean visited(Cell cell) {
		return set[cell.id] == timestamp;
	}

	public boolean add(Cell cell) {
		if (set[cell.id] == timestamp)
			return false;

		set[cell.id] = timestamp;
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
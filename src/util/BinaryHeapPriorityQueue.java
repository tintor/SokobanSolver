package util;

import java.util.Arrays;

public class BinaryHeapPriorityQueue<T extends Comparable<T>> {
	private final int shardSizeBits;
	private final int mask;
	private int size;
	private int shardCount;
	private Object[][] shards;

	public BinaryHeapPriorityQueue(int initialShardsBits, int shardSizeBits) {
		this.shardSizeBits = shardSizeBits;
		mask = (1 << shardSizeBits) - 1;
		shards = new Object[1 << initialShardsBits][];
		shardCount = 0;
	}

	public void offer(T e) {
		if (shardCount >= shards.length) { 
			if (shardCount == shards.length)
				shards = Arrays.copyOf(shards, shards.length * 2);
			shards[shardCount++] = new Object[1 << shardSizeBits];
		}
		siftUp(size++, e);
	}

	@SuppressWarnings("unchecked")
	public T poll() {
		if (size == 0)
			return null;
		size -= 1;
		T result = (T) shards[0][0];
		T x = get(size);
		set(size, null);
		if (size != 0)
			siftDown(0, x);
		return result;
	}

	private void set(int index, T e) {
		shards[index >>> shardSizeBits][index & mask] = e;
	}

	@SuppressWarnings("unchecked")
	private T get(int index) {
		return (T) shards[index >>> shardSizeBits][index & mask];
	}

	private void siftDown(int k, T x) {
		int half = size >>> 1; // loop while a non-leaf
		while (k < half) {
			int child = (k << 1) + 1; // assume left child is least
			T c = get(child);
			int right = child + 1;
			if (right < size && c.compareTo(get(right)) > 0)
				c = get(child = right);
			if (x.compareTo(c) <= 0)
				break;
			set(k, c);
			k = child;
		}
		set(k, x);
	}

	private void siftUp(int k, T x) {
		while (k > 0) {
			int parent = (k - 1) >>> 1;
			T e = get(parent);
			if (x.compareTo(e) >= 0)
				break;
			set(k, e);
			k = parent;
		}
		set(k, x);
	}

	public int size() {
		return size;
	}
}
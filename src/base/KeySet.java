package base;

public final class KeySet {
	private int size;
	private float loadFactor;
	private int threshold;
	private Key table[];

	public KeySet(int cap, float loadFactor) {
		this.loadFactor = loadFactor;
		threshold = (int) ((1 << cap) * loadFactor);
		table = new Key[1 << cap];
	}

	public boolean add(Key key) {
		int hash = key.hashCode();
		int index = hash & (table.length - 1);
		for (Key e = table[index]; e != null; e = e.nextInBucket)
			if (key.equals(e))
				return false;

		assert key.nextInBucket == null;
		key.nextInBucket = table[index];
		table[index] = key;
		if (size++ >= threshold)
			resize(2 * table.length);
		return true;
	}

	public boolean contains(Key key) {
		int hash = key.hashCode();
		int index = hash & (table.length - 1);
		for (Key e = table[index]; e != null; e = e.nextInBucket)
			if (key.equals(e))
				return true;

		return false;
	}

	void resize(int newCapacity) {
		Key[] newTable = new Key[newCapacity];
		for (int j = 0; j < table.length; j++) {
			while (table[j] != null) {
				int index = table[j].hashCode() & (newCapacity - 1);

				Key next = table[j].nextInBucket;
				table[j].nextInBucket = newTable[index];
				newTable[index] = table[j];
				table[j] = next;
			}
		}
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
	}

	public void clear() {
		for (int i = 0; i < table.length; i++)
			table[i] = null;
	}

	public int size() {
		return size;
	}
}
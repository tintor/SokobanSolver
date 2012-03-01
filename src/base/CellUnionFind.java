package base;

public final class CellUnionFind {
	private int[] parent;
	private int[] rank;

	public CellUnionFind(int size) {
		parent = new int[size];
		rank = new int[size];
		for (int i = 0; i < size; i++)
			parent[i] = i;
	}

	public void clear() {
		for (int i = 0; i < rank.length; i++) {
			parent[i] = i;
			rank[i] = 0;
		}
	}

	public void union(Cell ca, Cell cb) {
		int a = group(ca.id);
		int b = group(cb.id);
		if (a == b)
			return;

		if (rank[a] < rank[b]) {
			parent[a] = b;
			return;
		}

		parent[b] = a;
		if (rank[a] > rank[b])
			return;
		rank[a] += 1;
	}

	public boolean connected(Cell a, Cell b) {
		return group(a.id) == group(b.id);
	}

	private int group(int a) {
		int p = parent[a];
		if (parent[p] == p)
			return p;

		return parent[a] = group(p);
	}
}

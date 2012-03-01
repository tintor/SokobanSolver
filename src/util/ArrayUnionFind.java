package util;

public class ArrayUnionFind {
	private int[] parent;
	private int[] rank;

	public ArrayUnionFind(int size) {
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

	public void union(int a, int b) {
		a = group(a);
		b = group(b);
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

	public boolean connected(int a, int b) {
		return group(a) == group(b);
	}

	private int group(int a) {
		int p = parent[a];
		if (parent[p] == p)
			return p;

		return parent[a] = group(p);
	}
}

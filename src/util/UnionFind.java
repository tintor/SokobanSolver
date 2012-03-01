package util;

public class UnionFind {
	private UnionFind parent;
	private int rank;

	public UnionFind() {
		parent = this;
	}

	public static void union(UnionFind a, UnionFind b) {
		a = a.group();
		b = b.group();
		if (a == b)
			return;

		if (a.rank > b.rank)
			b.parent = a;
		else if (a.rank < b.rank)
			a.parent = b;
		else {
			b.parent = a;
			a.rank += 1;
		}
	}

	public static boolean connected(UnionFind a, UnionFind b) {
		return a.group() == b.group();
	}

	private UnionFind group() {
		if (parent.parent == parent)
			return parent;

		return parent = parent.group();
	}
}
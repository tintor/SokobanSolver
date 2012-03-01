package base;

import java.util.Arrays;

class Articulations {
	int dfsCounter;
	int[] num;
	int[] level;
	int[] low;
	int rootChildren;

	Articulations(int size) {
		num = new int[size];
		level = new int[size];
		low = new int[size];

		Arrays.fill(low, Integer.MAX_VALUE);
	}

	void visit(Cell v, Cell x) {
		if (x == null)
			return;

		if (num[x.id] == 0) {
			level[x.id] = level[v.id] + 1;
			if (num[v.id] == 1)
				rootChildren += 1;

			dfs(x);

			low[v.id] = Math.min(low[v.id], low[x.id]);

			if (num[v.id] == 1) {
				// Root is an artic. point iff there are two or more
				// children
				if (rootChildren >= 2)
					v.isArticulation = true;
			} else if (low[x.id] >= num[v.id])
				// v is artic. point seperating x. That is, children of v
				// cannot climb higher than v without passing through v.
				v.isArticulation = true;

			return;
		}

		if (level[x.id] < level[v.id] - 1) {
			// x is at a lower level than the level of v's parent.
			low[v.id] = Math.min(low[v.id], num[x.id]);
		}
	}

	void dfs(Cell v) {
		// Set DFS info of vertex 'v'
		num[v.id] = ++dfsCounter;
		low[v.id] = num[v.id];

		visit(v, v.left);
		visit(v, v.right);
		visit(v, v.up);
		visit(v, v.down);
	}
}
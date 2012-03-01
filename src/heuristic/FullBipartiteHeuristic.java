package heuristic;

import java.util.Arrays;

import base.Key;

public final class FullBipartiteHeuristic extends Heuristic {
	private static int edmondsKarp(int[][] edges, int[][] capacity, int start, int end) {
		int n = capacity.length;
		// Residual capacity from u to v is C[u][v] - F[u][v]
		int[][] flow = new int[n][n];
		int[] prev = new int[n];
		int[] M = new int[n]; // Capacity of path to node
		int[] queue = new int[n];

		while (prev[end] != -1) {
			Arrays.fill(prev, -1);
			prev[start] = start;
			Arrays.fill(M, 0);
			M[start] = Integer.MAX_VALUE;

			// BFS queue
			int head = 0, tail = 0;
			queue[tail++] = start;
			LOOP: while (head < tail) {
				int u = queue[head++];
				for (int v : edges[u]) {
					// If v is not seen before in search
					if (prev[v] != -1)
						continue;

					// If here is available capacity
					int delta = capacity[u][v] - flow[u][v];
					if (delta <= 0)
						continue;

					prev[v] = u;
					M[v] = Math.min(M[u], delta);
					if (v != end)
						queue[tail++] = v;
					else {
						// Backtrack search, and write flow
						while (prev[v] != v) {
							u = prev[v];
							flow[u][v] += M[end];
							flow[v][u] -= M[end];
							v = u;
						}
						break LOOP;
					}
				}
			}
		}

		// We did not find a path to t
		int sum = 0;
		for (int f : flow[start])
			sum += f;
		return sum;
	}

	@Override
	public int estimate(Key key) {
		throw new RuntimeException();
	}
}
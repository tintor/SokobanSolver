package base;

import java.util.Random;

public class Zobrist {
	public static int agent(int i) {
		return agent[i];
	}

	public static int box(int i) {
		return box[i];
	}

	private static final int maxCells = 200;
	private static final int[] agent = new int[maxCells];
	private static final int[] box = new int[maxCells];

	static {
		Random rand = new Random(0);
		for (int i = 0; i < maxCells; i++) {
			agent[i] = rand.nextInt();
			box[i] = rand.nextInt();
		}
	}
}
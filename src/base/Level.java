package base;

public final class Level {
	public Cell[] cells;
	public Cell[] goals;

	public int goalhash;
	public int[] goalset;

	public int deadlockDbSize;
	
	public boolean isGoal(int boxhash, int[] boxset) {
		return boxhash == goalhash && isGoal(boxset);
	}

	public boolean isGoal(int[] boxset) {
		for (int i = 0; i < boxset.length; i++)
			if (boxset[i] != goalset[i])
				return false;
		return true;
	}

	public final ThreadLocal<CellSearch> computePushesCellSearch = new ThreadLocal<CellSearch>() {
		@Override
		protected CellSearch initialValue() {
			return new CellSearch(cells.length);
		}
	};

	public final ThreadLocal<CellSearch2> cellSearch2 = new ThreadLocal<CellSearch2>() {
		@Override
		protected CellSearch2 initialValue() {
			return new CellSearch2(cells.length);
		}
	};

	public final ThreadLocal<CellUnionFind> groups = new ThreadLocal<CellUnionFind>() {
		@Override
		protected CellUnionFind initialValue() {
			return new CellUnionFind(cells.length);
		}
	};

	public int pushes(Cell a, Cell b) {
		Integer pushes = a.pushes.get(b); 
		return pushes != null ? pushes : Integer.MAX_VALUE;
	}

	class Printer {
		private int[][] template;
		private static final int Wall = -1;
		private static final int CompressedTunnel = -2;
		private static final int Exterior = -3;

		Printer() {
			int xmin = Integer.MAX_VALUE, xmax = Integer.MIN_VALUE, ymin = Integer.MAX_VALUE, ymax = Integer.MIN_VALUE;
			for (Cell cell : cells) {
				if (cell.x < xmin)
					xmin = cell.x;
				if (cell.x > xmax)
					xmax = cell.x;
				if (cell.y < ymin)
					ymin = cell.y;
				if (cell.y > ymax)
					ymax = cell.y;
			}

			int w = xmax - xmin + 3, h = ymax - ymin + 3;
			template = new int[h][w];

			// First exterior
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++) {
					template[y][x] = Exterior;
				}

			// Then walls
			for (Cell cell : cells) {
				int x = cell.x - xmin + 1;
				int y = cell.y - ymin + 1;
				for (int dy = y - 1; dy <= y + 1; dy++)
					for (int dx = x - 1; dx <= x + 1; dx++) {
						template[dy][dx] = Wall;
					}
			}

			// Then cells and compressed tunnels
			for (Cell cell : cells) {
				template[cell.y - ymin + 1][cell.x - xmin + 1] = cell.id;

				Cell left = cell.left;
				if (left != null && cell.x - left.x > 1) {
					for (int x = left.x + 1; x < cell.x; x++)
						template[cell.y - ymin + 1][x - xmin + 1] = CompressedTunnel;
				}

				Cell up = cell.up;
				if (up != null && cell.y - up.y > 1) {
					for (int y = up.y + 1; y < cell.y; y++)
						template[y - ymin + 1][cell.x - xmin + 1] = CompressedTunnel;
				}
			}
		}

		private char toChar(int x, int y, char[] map) {
			int a = template[y][x];
			if (a >= 0)
				return map[a];
			if (a == Wall)
				return '#';
			if (a == CompressedTunnel)
				return 'T';
			return ' ';
		}

		public String toString(char[] map) {
			StringBuilder b = new StringBuilder();
			for (int y = 0; y < template.length; y++) {
				for (int x = 0; x < template[0].length; x++) {
					b.append(toChar(x, y, map));
				}
				b.append('\n');
			}
			return b.toString();
		}
	}

	public Printer printer;

	public int aliveCells() {
		int alive = 0;
		for (Cell c : cells)
			if (!c.isDead)
				alive += 1;
		return alive;
	}

	public double complexity() {
		double res = Math.log(cells.length - goals.length);
		int alive = aliveCells();
		for (int i = alive; i >= alive - goals.length; i--)
			res += Math.log(i);
		for (int i = goals.length; i >= 1; i--)
			res -= Math.log(i);
		return res;
	}
}
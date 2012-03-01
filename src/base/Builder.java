package base;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import util.BitSet;
import util.NotYetImplemented;

public class Builder {
	int id;
	Cell[][] grid;
	CellUnionFind groups;
	ArrayList<Cell> cells = new ArrayList<Cell>();
	ArrayList<Cell> boxes = new ArrayList<Cell>();
	ArrayList<Cell> goals = new ArrayList<Cell>();
	Cell agent;
	Level level = new Level();

	Builder(char[][] map) {
		int h = map.length;
		int w = 0;
		for (int y = 0; y < h; y++)
			if (map[y].length > w)
				w = map[y].length;

		groups = new CellUnionFind(h * w);

		// Parse grid
		grid = new Cell[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < map[y].length; x++)
				parseCell(x, y, map[y][x]);

		// Verify level
		if (agent == null)
			throw new RuntimeException("Agent is missing");
		if (boxes.size() != goals.size())
			throw new RuntimeException("Unequal number of boxes and goals.");
		if (boxes.size() == 0)
			throw new RuntimeException("Boxes are missing");
		for (Cell box : boxes)
			if (!groups.connected(box, agent))
				throw new RuntimeException("Not all boxes (not on goals) are reachable by agent.");
		for (Cell goal : goals)
			if (!groups.connected(goal, agent))
				throw new RuntimeException("Not all goals are reachable by agent.");

		// Create list of cells reachable by agent
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				if (grid[y][x] == null)
					continue;

				if (groups.connected(agent, grid[y][x]))
					cells.add(grid[y][x]);
				else {
					grid[y][x].id = -1;
					grid[y][x] = null;
				}
			}

		if (Flags.RemoveDeadEnds)
			removeDeadEnds();

		// init cells
		if (cells.size() >= 128)
			throw new UnsupportedLevelException();
		level.cells = cells.toArray(new Cell[cells.size()]);
		level.goals = goals.toArray(new Cell[goals.size()]);
		for (int i = 0; i < level.cells.length; i++)
			level.cells[i].id = i;

		// mark all articulation cells
		if (Flags.BuildArticulations)
			new Articulations(level.cells.length).dfs(agent);

		// compute minimal pushes
		MinimalPushes.compute(level);

		// find and mark tunnels
		if (Flags.BuildTunnels)
			findTunnels();

		// move all deads cells to the end of id range
		moveDeadCellsToEnd();

		level.printer = level.new Printer();

		// compute goalhash and goalset
		level.goalhash = 0;
		level.goalset = new int[(level.aliveCells() + 31) / 32];
		for (Cell goal : level.goals) {
			level.goalhash ^= Zobrist.box(goal.id);
			BitSet.setBit(level.goalset, goal.id);
		}

		sanityChecks();
	}

	private void sanityChecks() {
		for (Cell box : boxes)
			if (box.isDead)
				throw new RuntimeException();
		for (Cell goal : goals)
			if (goal.isDead)
				throw new RuntimeException();

		for (Cell c : goals) {
			if (c.left != null && c.left.right != c)
				throw new RuntimeException();
			if (c.right != null && c.right.left != c)
				throw new RuntimeException();
			if (c.up != null && c.up.down != c)
				throw new RuntimeException();
			if (c.down != null && c.down.up != c)
				throw new RuntimeException();
		}

		int alive = level.aliveCells();
		for (Cell c : cells)
			if (c.isDead && c.id < alive)
				throw new RuntimeException();
	}

	private Cell followTunnel(Cell a, Cell back) {
		for (Cell b : a)
			if (b.links() == 2 && b != back)
				return b;
		return null;
	}

	private void findTunnels() {
		boolean[] containsBox = new boolean[level.cells.length];
		for (Cell box : boxes)
			containsBox[box.id] = true;

		for (Cell a : level.cells) {
			if (a.isTunnel || a.links() != 2)
				continue;

			int twos = 0;
			for (Cell b : a)
				if (b.links() == 2)
					twos += 1;
			if (twos != 1)
				continue;

			// We've found a tunnel entrance
			processTunnel(a, containsBox);
		}
	}

	private void processTunnel(Cell start, boolean[] containsBox) {
		List<Cell> tunnel = new ArrayList<Cell>();
		tunnel.add(start);

		Cell curr = start, prev = null;
		boolean isDead = start.isDead;
		boolean containsAgent = start == agent;

		while (true) {
			Cell next = followTunnel(curr, prev);
			if (next == null)
				break;

			tunnel.add(next);
			prev = curr;
			curr = next;
			if (next.isDead)
				isDead = true;
			if (next == agent)
				containsAgent = true;

			if (curr.isGoal || containsBox[curr.id])
				break;
		}

		Cell end = curr;

		// tunnels composed of two corners are not interesting
		if (tunnel.size() == 2 && start.isCorner() && end.isCorner())
			return;

		// need to move agent outside of interior cells
		if (containsAgent) {
			if (!containsBox[start.id])
				agent = start;
			else if (!containsBox[end.id])
				agent = end;
			else
				return;
		}

		// TODO we may extend tunnel by one cell on each endpoint under special
		// conditions (links=3 and not T towards the tunnel)
		// TODO need to remove interior cells
		// BUG some tunnels appear broken into two for no reason

		int pushes = isDead ? 0 : (tunnel.size() - 1);
		for (Cell a : tunnel) {
			a.isTunnel = true;
			a.tunnelPushes = pushes;
			if (isDead && a != start && a != end)
				a.isDead = true;
		}
	}

	private void removeDeadEnds() {
		boolean[] containsBox = new boolean[grid.length * grid[0].length];
		for (Cell box : boxes)
			containsBox[box.id] = true;

		while (true) {
			int size = cells.size();
			Iterator<Cell> i = cells.iterator();
			while (i.hasNext()) {
				Cell a = i.next();
				if (a.links() != 1 || a.isGoal)
					continue;

				if (agent == a) {
					if (a.left != null && containsBox[a.left.id])
						continue;
					if (a.right != null && containsBox[a.right.id])
						continue;
					if (a.up != null && containsBox[a.up.id])
						continue;
					if (a.down != null && containsBox[a.down.id])
						continue;
				}

				i.remove();

				if (agent == a)
					for (Cell b : a)
						agent = b;

				if (a.left != null)
					a.left.right = null;
				if (a.right != null)
					a.right.left = null;
				if (a.up != null)
					a.up.down = null;
				if (a.down != null)
					a.down.up = null;
			}
			if (size == cells.size())
				break;
		}
	}

	private void moveDeadCellsToEnd() {
		int a = 0, d = level.cells.length - 1;
		main: while (a < d) {
			if (!level.cells[a].isDead) {
				a += 1;
				continue main;
			}

			if (level.cells[d].isDead) {
				d -= 1;
				continue main;
			}

			Cell w = level.cells[a];
			level.cells[a] = level.cells[d];
			level.cells[d] = w;

			level.cells[a].id = a;
			level.cells[d].id = d;

			a += 1;
			d -= 1;
		}
	}

	private void parseCell(int x, int y, char c) {
		if (c == '#')
			return;

		Cell cell = grid[y][x] = new Cell();
		cell.id = id++;
		cell.level = level;
		cell.x = x;
		cell.y = y;

		if (y > 0) {
			Cell up = grid[y - 1][x];
			if (up != null) {
				groups.union(cell, up);
				cell.up = up;
				up.down = cell;
			}
		}

		if (x > 0) {
			Cell left = grid[y][x - 1];
			if (left != null) {
				groups.union(cell, left);
				cell.left = left;
				left.right = cell;
			}
		}

		if (c == '@' || c == '+') {
			if (agent != null)
				throw new RuntimeException("Double agent");
			agent = cell;
		}

		cell.isGoal = c == '.' || c == '*' || c == '+';
		if (cell.isGoal)
			goals.add(cell);

		if (c == '$' || c == '*')
			boxes.add(cell);
	}

	private static boolean isPartOfLevel(String line) {
		if (line.trim().length() < 3)
			return false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c != '#' && c != '.' && c != ' ' && c != '@' && c != '$' && c != '*' && c != '+')
				return false;
		}

		return true;
	}

	public static Key build(String filename, int level) throws IOException {
		ArrayList<char[]> map = new ArrayList<char[]>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;

		for (int i = 1; i < level; i++) {
			do {
				line = reader.readLine();
			} while (line != null && !isPartOfLevel(line));

			do {
				line = reader.readLine();
			} while (line != null && isPartOfLevel(line));
		}

		do {
			line = reader.readLine();
		} while (line != null && !isPartOfLevel(line));

		if (line == null)
			return null;

		do {
			map.add(line.toCharArray());
			line = reader.readLine();
		} while (line != null && isPartOfLevel(line));

		return build(map);
	}

	public static Key build(ArrayList<char[]> map) throws IOException {
		Builder builder = new Builder(map.toArray(new char[map.size()][]));

		Cell[] boxes = builder.boxes.toArray(new Cell[builder.boxes.size()]);
		return Key.create(builder.agent, boxes);
	}

	Level addCell(Cell cell, int id) {
		throw new NotYetImplemented();
	}
}
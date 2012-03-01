package base;

class Coral {
	private static class Box {
		enum Type {
			I, F, E, B
		};

		Type type;
		Corral[] corrals;
	}

	private static class Corral {
		Box[] boxesI, boxesF;
	}

	public void analyzeCorrals(Key key) {
		Level level = key.agent.level;

		// Find groups of cells separated by boxes
		CellUnionFind groups = level.groups.get();
		groups.clear();

		Cell[] boxes = new Cell[level.goals.length];
		int bw = 0;

		for (Cell a : level.cells) {
			if (key.containsBox(a)) {
				boxes[bw++] = a;
				continue;
			}

			if (a.right != null && !key.containsBox(a.right))
				groups.union(a, a.right);

			if (a.down != null && !key.containsBox(a.down))
				groups.union(a, a.down);

			if (a.isTunnel) {
				if (a.left != null && !key.containsBox(a.left))
					groups.union(a, a.left);

				if (a.up != null && !key.containsBox(a.up))
					groups.union(a, a.up);
			}
		}

		// Find all Interior boxes
		for (Cell box : boxes) {

		}

		// Find all Exterior (and Blocked) boxes

		// Mark all remaining as Fence boxes

		// Create Corral objects and classify corrals

		// Print regular level
		// Print level with box classes
		// Print level with corral ownerships (no boxes / goals / agent),
		// corrals numbered from 1

		// TODO: write function to merge multiple maze linear strings into one
		// linear string of mazes horizontaly in the console:
		// ### # # #@#
		// #.$ $.$ * #
		// ### ### ###

		// if there are multiple PI-corrals pick the one with least number of
		// possible pushes
		// (if there are ties pick one deterministicaly)

		// Return pushes for selected PI-corral
	}
}

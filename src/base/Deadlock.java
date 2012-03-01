package base;

import java.util.Arrays;

import util.BitSet;

public class Deadlock {
	public static boolean checkQuick2x2(int[] boxset, Cell box) {
		if (box.isGoal || !checkQuick2x2priv(boxset, box))
			return false;
		
		Stats.instance.get().quick2x2 += 1;
		return true;
	}

	private static boolean livebox(int[] boxset, Cell cell) {
		return !cell.isGoal && BitSet.testBitExt(boxset, cell.id);
	}

	private static boolean wallbox(int[] boxset, Cell cell) {
		return cell == null || livebox(boxset, cell);
	}

	private static boolean checkQuick2x2priv(int[] boxset, Cell box) {
		if (box.up == null) {
			if (livebox(boxset, box.left) && wallbox(boxset, box.left.up))
				return true;
			if (livebox(boxset, box.right) && wallbox(boxset, box.right.up))
				return true;
		} else if (livebox(boxset, box.up)) {
			if (wallbox(boxset, box.left) && wallbox(boxset, box.up.left))
				return true;
			if (wallbox(boxset, box.right) && wallbox(boxset, box.up.right))
				return true;
		}

		if (box.down == null) {
			if (livebox(boxset, box.left) && wallbox(boxset, box.left.down))
				return true;
			if (livebox(boxset, box.right) && wallbox(boxset, box.right.down))
				return true;
		} else if (livebox(boxset, box.down)) {
			if (wallbox(boxset, box.left) && wallbox(boxset, box.down.left))
				return true;
			if (wallbox(boxset, box.right) && wallbox(boxset, box.down.right))
				return true;
		}

		return false;
	}

	private static void frozenBoxesVisit(CellSearch2 search, int[] boxset, Dir dir, Cell cell) {
		if (cell == null || !KeyUtil.containsBox(boxset, cell))
			return;

		Cell behind = cell.get(dir);
		if (behind != null && search.visited(behind) && !KeyUtil.containsBox(boxset, behind))
			search.revisit(behind);
	}

	// Simple single pass deadlock checker
	public static boolean frozenBoxes(Cell agent, int[] boxset) {
		// Idea: remove all reachable boxes that can be pushed (and do that in
		// single iteration).
		// If some of the remaining boxes is not on goal => deadlock

		int boxes = agent.level.goals.length;
		if (boxes <= 1)
			return false;
		boxset = Arrays.copyOf(boxset, boxset.length);
		CellSearch2 search = agent.level.cellSearch2.get();

		for (Cell cell : search.init(agent)) {
			if (cell.left != null) {
				Cell box = cell.left;
				if (KeyUtil.containsBox(boxset, box)) {
					Cell dest = box.left;
					if (dest != null && !dest.isDead && !KeyUtil.containsBox(boxset, dest)) {
						BitSet.clearBit(boxset, box.id);
						if (--boxes <= 1)
							return false;

						frozenBoxesVisit(search, boxset, Dir.Up, box.up);
						frozenBoxesVisit(search, boxset, Dir.Down, box.down);
						search.add(box);
					}
				} else {
					search.add(box);
				}
			}

			if (cell.right != null) {
				Cell box = cell.right;
				if (KeyUtil.containsBox(boxset, box)) {
					Cell dest = box.right;
					if (dest != null && !dest.isDead && !KeyUtil.containsBox(boxset, dest)) {
						BitSet.clearBit(boxset, box.id);
						if (--boxes <= 1)
							return false;

						frozenBoxesVisit(search, boxset, Dir.Up, box.up);
						frozenBoxesVisit(search, boxset, Dir.Down, box.down);
						search.add(box);
					}
				} else {
					search.add(box);
				}
			}

			if (cell.up != null) {
				Cell box = cell.up;
				if (KeyUtil.containsBox(boxset, box)) {
					Cell dest = box.up;
					if (dest != null && !dest.isDead && !KeyUtil.containsBox(boxset, dest)) {
						BitSet.clearBit(boxset, box.id);
						if (--boxes <= 1)
							return false;

						frozenBoxesVisit(search, boxset, Dir.Left, box.left);
						frozenBoxesVisit(search, boxset, Dir.Right, box.right);
						search.add(box);
					}
				} else {
					search.add(box);
				}
			}

			if (cell.down != null) {
				Cell box = cell.down;
				if (KeyUtil.containsBox(boxset, box)) {
					Cell dest = box.down;
					if (dest != null && !dest.isDead && !KeyUtil.containsBox(boxset, dest)) {
						BitSet.clearBit(boxset, box.id);
						if (--boxes <= 1)
							return false;

						frozenBoxesVisit(search, boxset, Dir.Left, box.left);
						frozenBoxesVisit(search, boxset, Dir.Right, box.right);
						search.add(box);
					}
				} else {
					search.add(box);
				}
			}
		}

		for (Cell cell : agent.level.cells)
			if (KeyUtil.containsBox(boxset, cell) && !cell.isGoal) {
				Stats.instance.get().frozenBoxes += 1;
				return true;
			}

		return false;
	}
}

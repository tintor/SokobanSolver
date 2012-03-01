package base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Cell implements Iterable<Cell> {
	public Level level;
	public int id;

	public int x, y; // for printing only

	public boolean isGoal;
	public boolean isArticulation;
	public boolean isDead; // = !isGoal && links <= 2 && !isTube();
	public int goalPushes = -1; // unknown -1, isDead Integer.MAX_VALUE, isGoal
								// 0, else >0
	private static final Map<Cell, Integer> emptyPushes = Collections.unmodifiableMap(new IdentityHashMap<Cell, Integer>(0));
	Map<Cell, Integer> pushes = emptyPushes;

	private static final List<Key> emptyDeadlockDB = Collections.unmodifiableList(new ArrayList<Key>(0));
	public List<Key> deadlockDB = emptyDeadlockDB;

	public Cell left;
	public Cell right;
	public Cell up;
	public Cell down;

	// Example of tunnel in which boxes can only be pushed from A to B.
	// A.pushes = 6
	// B.pushes = 0
	// ######
	// ###__#
	// A____#
	// ####_#
	// ####B#

	public boolean isTunnel;
	// public Dir tunnelExitDir;
	public int tunnelPushes;
	// Number of pushes to get box from this entrance to the outside of other
	// side of tunnel.
	// It is equal to length for straight tunnels.
	// It is equal to 0 if box can't be pushed from this side.

	public char marker = '\0';

	// public boolean isTunnel() {
	// return tunnelExitDir != null;
	// }

	public boolean isCorner() {
		return links() == 2 && !isVertical() && !isHorizontal();
	}

	public boolean isVertical() {
		return up != null && down != null && left == null && right == null;
	}

	public boolean isHorizontal() {
		return left != null && right != null && up == null && down == null;
	}

	public int links() {
		int links = 0;
		if (left != null)
			links += 1;
		if (right != null)
			links += 1;
		if (up != null)
			links += 1;
		if (down != null)
			links += 1;
		return links;
	}

	public Iterator<Cell> iterator() {
		return new Iterator<Cell>() {
			int nextDir = 0;

			@Override
			public boolean hasNext() {
				for (int i = nextDir; i < Dir.values().length; i++)
					if (get(Dir.values()[i]) != null)
						return true;
				return false;
			}

			@Override
			public Cell next() {
				for (int i = nextDir; i < Dir.values().length; i++) {
					Cell a = get(Dir.values()[i]);
					if (a != null) {
						nextDir = i + 1;
						return a;
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new RuntimeException();
			}
		};
	}

	public Cell get(Dir dir) {
		switch (dir) {
		case Left:
			return left;
		case Right:
			return right;
		case Up:
			return up;
		case Down:
			return down;
		}
		throw new RuntimeException();
	}

	@Override
	public String toString() {
		return "[" + id + " (" + x + " " + y + ")" + (isGoal ? " G" : "") + (isDead ? " D" : "")
				+ (isArticulation ? " A" : "") + (isTunnel ? " T" + tunnelPushes : "") + (left != null ? " L" + left.id : "")
				+ (right != null ? " R" + right.id : "") + (up != null ? " U" + up.id : "")
				+ (down != null ? " D" + down.id : "") + "]";
	}
}
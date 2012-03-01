package base;

import java.lang.reflect.Field;

public class Flags {
	public static final boolean PrintDeadCells = false;
	public static final boolean VerboseSearch = false;

	public static final boolean BuildDeadlockDB = true;
	public static final boolean CheckDeadlockDB = true;

	public static final boolean CheckQuick2x2 = true;
	public static final boolean CheckFrozenBoxes = true;

	public static final boolean RemoveDeadEnds = true;

	public static final boolean BuildArticulations = true;
	public static final boolean UseArticulations = false;

	public static final boolean BuildTunnels = true;
	public static final boolean UseTunnels = false;

	public static final boolean UseSimpleTunnels = false; // ie. with no
															// preprocessing

	public static final boolean IncludeAgentInHashCode = false;
	public static final boolean CacheHashCode = true;

	// if turned off then search for any solution, which may be faster
	public static final boolean FindOptimalSolution = true;

	public static void print() {
		synchronized (System.out) {
			for (Field field : Flags.class.getDeclaredFields())
				try {
					System.out.printf("%s %s\n", field.getName(), field.get(null));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
		}
	}
}
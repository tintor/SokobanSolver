import heuristic.Heuristic;
import base.Key;
import base.Stats;

public abstract class Search {
	enum Result {
		Solved, Timeout, NoSolution
	}

	public final int maxDOP = Integer.MAX_VALUE;

	// Inputs
	public volatile Key start;
	public Heuristic heuristic;
	public int maxSetSize = 10 * 1000 * 1000;
	public int verbose = 16; // print stats on (1 << verbose) closed keys

	// Outputs
	public volatile Key lastKey;
	public int closedKeys;
	public int openKeys;
	public Stats stats;

	public abstract void print();

	public abstract int setSize();

	public abstract Result run();

	public abstract Result runConcurrently();
}

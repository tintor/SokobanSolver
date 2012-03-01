import heuristic.SimpleBipartiteHeuristic;

import java.io.IOException;
import java.util.ArrayDeque;

import util.Human;
import base.Builder;
import base.Cell;
import base.DeadlockDB;
import base.Flags;
import base.Key;
import base.Level;
import base.Stats;
import base.UnsupportedLevelException;

public class Benchmark {
	static String levelset = "bin/levels/original.txt";
	// static String levelset = "bin/levels/microban.txt";
	// static String levelset = "bin/levels/sasquatch.txt";

	static int onlyLevel = 2;
	static boolean parseOnly = false;
	static boolean concurrent = false;

	static boolean printSolution = false;
	static boolean printArticulationsAndTunnels = false;

	static int totalClosed = 0;
	static int totalOpen = 0;
	static int totalSet = 0;
	static int totalPushes = 0;
	static Stats totalStats = new Stats();

	static int timeout = 0;
	static int noSolution = 0;
	static int unsupported = 0;

	static int simplestLevelNoSolutionId;
	static double simplestLevelNoSolutionComplexity = Double.POSITIVE_INFINITY;
	static Key simplestLevelNoSolutionKey;

	static int simplestLevelTimeoutId;
	static double simplestLevelTimeoutComplexity = Double.POSITIVE_INFINITY;
	static Key simplestLevelTimeoutKey;

	static int maxAlive;
	static Key maxAliveKey;

	public static void main(String[] args) throws IOException {
		Search search = new SearchAStar();
		search.heuristic = new SimpleBipartiteHeuristic();

		int levelNo = 0;
		while (true) {
			try {
				search.start = Builder.build(levelset, ++levelNo);
			} catch (UnsupportedLevelException e) {
				unsupported += 1;
				continue;
			}
			if (search.start == null)
				break;
			if (onlyLevel > 0 && levelNo != onlyLevel)
				continue;

			Level level = search.start.agent.level;
			System.out.printf("Level #%s [cells %s, boxes %s, alive %s, complexity %.1f]\n", levelNo,
					level.cells.length, level.goals.length, level.aliveCells(), level.complexity());
			System.out.println(search.start);

			if (Flags.BuildDeadlockDB)
				DeadlockDB.compute(level);

			if (Flags.BuildArticulations && printArticulationsAndTunnels) {
				// Print articulations
				for (Cell a : level.cells)
					if (a.isArticulation)
						a.marker = 'A';
				System.out.println(search.start);
				for (Cell a : level.cells)
					a.marker = '\0';
			}

			if (Flags.BuildTunnels && printArticulationsAndTunnels) {
				// Print tunnels
				for (Cell a : level.cells)
					if (a.isTunnel && a.tunnelPushes >= 10)
						a.marker = (char) ('A' + a.tunnelPushes - 10);
					else if (a.isTunnel)
						a.marker = (char) ('0' + a.tunnelPushes);
				System.out.println(search.start);
				for (Cell a : level.cells)
					a.marker = '\0';
			}

			if (level.aliveCells() > maxAlive) {
				maxAlive = level.aliveCells();
				maxAliveKey = search.start;
			}

			if (parseOnly)
				continue;

			Search.Result result = concurrent ? search.runConcurrently() : search.run();

			search.print();

			switch (result) {
			case Solved:
				totalSet += search.setSize();
				totalClosed += search.closedKeys;
				totalOpen += search.openKeys;
				totalPushes += search.lastKey.pushes;
				System.out.printf("Found solution in %s pushes.\n", search.lastKey.pushes);

				if (printSolution) {
					ArrayDeque<Key> path = new ArrayDeque<Key>();
					for (Key a = search.lastKey; a != null; a = a.prev)
						path.addFirst(a);

					for (Key a : path)
						System.out.println(a);
				}
				break;

			case Timeout:
				timeout++;
				double complexity = level.complexity();
				if (complexity < simplestLevelTimeoutComplexity) {
					simplestLevelTimeoutComplexity = complexity;
					simplestLevelTimeoutId = levelNo;
					simplestLevelTimeoutKey = search.start;
				}
				System.out.printf("Timed out after key: pushes %s, total %s\n", search.lastKey.pushes,
						search.lastKey.total);
				break;

			case NoSolution:
				noSolution++;
				complexity = level.complexity();
				if (complexity < simplestLevelNoSolutionComplexity) {
					simplestLevelNoSolutionComplexity = complexity;
					simplestLevelNoSolutionId = levelNo;
					simplestLevelNoSolutionKey = search.start;
				}
				System.out.printf("No solution.\n");
				break;
			}

			search.stats.print();
			totalStats.add(search.stats);

			System.out.println();
		}
		System.out.println("Summary:");
		System.out.printf("Levels %s (Timeout %s, NoSolution %s, Unsupported %s)\n", --levelNo, timeout, noSolution,
				unsupported);
		System.out.printf("TotalClosed %s, TotalOpened %s\n", Human.human(totalClosed), Human.human(totalOpen));
		totalStats.print();
		System.out.printf("TotalPushes %s\n", Human.human(totalPushes));
		System.out.printf("Heuristic %s\n", search.heuristic.getClass().getName());
		System.out.printf("Concurrent = %s\n", concurrent);
		if (simplestLevelTimeoutId != 0) {
			System.out.printf("Simplest level Timeout: %s\n", simplestLevelTimeoutId);
			System.out.println(simplestLevelTimeoutKey);
		}
		if (simplestLevelNoSolutionId != 0) {
			System.out.printf("Simplest level NoSolution: %s\n", simplestLevelNoSolutionId);
			System.out.println(simplestLevelNoSolutionKey);
		}
		System.out.printf("Max alive: %s\n", maxAlive);
		System.out.println(maxAliveKey);
		Flags.print();
	}
}
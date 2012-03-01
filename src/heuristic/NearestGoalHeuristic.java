package heuristic;

import base.Cell;
import base.Key;

public final class NearestGoalHeuristic extends Heuristic {
	public static final NearestGoalHeuristic instance = new NearestGoalHeuristic();

	@Override
	public int estimate(Key key) {
		long result = 0;
		for (Cell cell : key.agent.level.cells) {
			if (cell.isDead)
				break;

			if (key.containsBox(cell))
				result += cell.goalPushes;
		}
		return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
	}
}
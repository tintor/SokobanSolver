package heuristic;

import base.Cell;
import base.Level;
import base.Key;

public final class SimpleBipartiteHeuristic extends Heuristic {
	@Override
	public int estimate(Key key) {
		Level level = key.agent.level;

		int pairs = level.goals.length;
		Cell[] boxes = key.boxes();
		Cell[] goals = level.goals;

		// Pair up boxes and goals at random
		long result = 0;
		for (int i = 0; i < pairs; i++)
			result += level.pushes(boxes[i], goals[i]);

		// Optimize combinations of boxes and goals
		while (true) {
			boolean again = false;
			for (int a = 1; a < pairs; a++)
				for (int b = 0; b < a; b++) {
					long diff = (long) level.pushes(boxes[b], goals[a]) + level.pushes(boxes[a], goals[b])
							- level.pushes(boxes[a], goals[a]) - level.pushes(boxes[b], goals[b]);
					if (diff < 0) {
						result += diff;
						Cell w = boxes[a];
						boxes[a] = boxes[b];
						boxes[b] = w;
						again = true;
					}
				}
			if (!again)
				break;
		}
		return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
	}
}
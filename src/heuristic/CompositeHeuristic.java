package heuristic;

import base.Key;

public class CompositeHeuristic extends Heuristic {
	private final Heuristic first;
	private final Heuristic second;

	public CompositeHeuristic(Heuristic first, Heuristic second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public int estimate(Key key) {
		return Math.min(first.estimate(key), second.estimate(key));
	}
}
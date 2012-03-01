package heuristic;

import base.Key;

public abstract class Heuristic {
	public boolean update(Key key) {
		int e = estimate(key);
		assert e >= 0;
		assert key.isGoal() == (e == 0); 
		if (e == Integer.MAX_VALUE)
			return true;
		key.total = key.pushes + e;
		assert key.total >= 0;
		return false;
	}

	public abstract int estimate(Key key);
}
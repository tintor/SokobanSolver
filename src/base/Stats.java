package base;

import java.lang.reflect.Field;

import util.Human;

public class Stats {
	public int deadlockDB;
	public int frozenBoxes;
	public int quick2x2;

	public int liveDeadlocks;
	public int transpositions;
	public int estimatedDeadlock;

	public static ThreadLocal<Stats> instance = new ThreadLocal<Stats>() {
		@Override
		public Stats initialValue() {
			return new Stats();
		}
	};

	public void add(Stats stats) {
		try {
			for (Field field : Stats.class.getDeclaredFields())
				field.set(this, (Integer) field.get(stats) + (Integer) field.get(stats));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void print() {
		synchronized (System.out) {
			try {
				for (Field field : Stats.class.getDeclaredFields())
					System.out.printf("%s %s\n", field.getName(), Human.human((Integer) field.get(this)));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
package base;

import heuristic.NearestGoalHeuristic;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class DeadlockDB {
	public static void compute(Level level) {
		// Assume all dead cells are at the end
		int alive = level.aliveCells();
		for (Cell c : level.cells)
			if (c.isDead && c.id < alive)
				throw new RuntimeException();

		List<Cell> agents = new ArrayList<Cell>();

		if (level.goals.length > 2)
			for (int a = 0; a < alive; a++)
				for (int b = a + 1; b < alive; b++)
					innerLoop(agents, level.cells[a], level.cells[b]);

		if (level.goals.length > 3)
			for (int a = 0; a < alive; a++)
				for (int b = a + 1; b < alive; b++)
					for (int c = b + 1; c < alive; c++)
						innerLoop(agents, level.cells[a], level.cells[b], level.cells[c]);

		if (level.goals.length > 4)
			for (int a = 0; a < alive; a++)
				for (int b = a + 1; b < alive; b++)
					for (int c = b + 1; c < alive; c++)
						for (int d = c + 1; d < alive; d++)
							innerLoop(agents, level.cells[a], level.cells[b], level.cells[c], level.cells[d]);
	}

	private static boolean wallbox(int[] boxset, Cell cell) {
		return cell == null || KeyUtil.containsBox(boxset, cell);
	}

	private static void addAgent(List<Cell> agents, int[] boxset, Cell agent) {
		// ignore if agent is on top of box
		if (KeyUtil.containsBox(boxset, agent))
			return;

		// ignore if agent is completely surrounded by boxes and walls
		if (wallbox(boxset, agent.left) && wallbox(boxset, agent.right) && wallbox(boxset, agent.up)
				&& wallbox(boxset, agent.down))
			return;

		// ignore if agent can reach some other agent
		for (Cell a : agents)
			if (KeyUtil.reachable(a, agent, boxset))
				return;

		agents.add(agent);
	}

	private static boolean isDeadlock(Key start) {
		if (NearestGoalHeuristic.instance.estimate(start) == 0)
			return false;

		KeySet keySet = new KeySet(10, 0.75f);
		keySet.add(start);

		Key[] pushes = new Key[start.agent.level.goals.length * 4];

		PriorityQueue<Key> queue = new PriorityQueue<Key>();
		queue.add(start);

		while (queue.size() > 0) {
			int size = KeyUtil.computePushes(queue.poll(), pushes);
			for (int i = 0; i < size; i++) {
				Key push = pushes[i];

				if (!keySet.add(push))
					continue;

				if (NearestGoalHeuristic.instance.update(push))
					continue;

				if (push.total == push.pushes)
					return false;

				queue.add(push);
			}
		}

		start.agent.level.deadlockDbSize += 1;
		return true;
	}

	private static void innerLoop(List<Cell> agents, Cell... boxes) {
		int[] boxset = KeyUtil.createBoxset(boxes);

		agents.clear();
		for (Cell box : boxes)
			for (Cell agent : box)
				addAgent(agents, boxset, agent);

		loop: for (Cell agent : agents) {
			if (Deadlock.frozenBoxes(agent, boxset))
				continue;

			for (Cell box : boxes)
				if (Key.inDeadlockDB(agent, boxset, box))
					continue loop;

			Key key = Key.create(agent, boxset);
			if (isDeadlock(key)) {
				synchronized (System.out) {
					System.out.print(key);
					System.out.print("compute");
					for (Cell box : boxes) {
						System.out.print(" ");
						System.out.print(box.id);
					}
					System.out.printf(", dbsize %s\n", boxes[0].level.deadlockDbSize);
				}
				for (Cell box : boxes) {
					if (box.deadlockDB.size() == 0)
						box.deadlockDB = new ArrayList<Key>();
					box.deadlockDB.add(key);
				}
			}
		}
	}
}
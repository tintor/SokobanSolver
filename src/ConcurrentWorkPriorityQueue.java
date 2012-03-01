import java.util.List;

import tintor.devel.heap.XLiteAuxTwoPassPairingHeap;

public final class ConcurrentWorkPriorityQueue<T extends Comparable<T>> {
	private static class Item<T extends Comparable<T>> extends XLiteAuxTwoPassPairingHeap.Node<Item<T>> {
		T value;

		@Override
		public int compareTo(Item<T> other) {
			return value.compareTo(other.value);
		}
	}

	private XLiteAuxTwoPassPairingHeap<Item<T>> heap = new XLiteAuxTwoPassPairingHeap<ConcurrentWorkPriorityQueue.Item<T>>();
	private int queuedItems;
	private int activeWorkers;
	private boolean aborted;

	public synchronized int size() {
		return queuedItems;
	}

	public synchronized void put(T e) {
		Item<T> item = new Item<T>();
		item.value = e;
		heap.offer(item);
		queuedItems += 1;
		notify();
	}

	public synchronized void attach() {
		activeWorkers += 1;
	}

	public synchronized T exchange(T[] list, int size) {
		for (int i = 0; i < size; i++) {
			Item<T> item = new Item<T>();
			item.value = list[i];
			heap.offer(item);
			notify();
		}
		queuedItems += size;
		activeWorkers -= 1;

		while (true) {
			if (aborted)
				return null;

			if (queuedItems > 0) {
				queuedItems -= 1;
				activeWorkers += 1;
				return heap.poll().value;
			}

			if (activeWorkers > 0)
				try {
					// Queue is empty, but as there are active workers, some
					// of them may still new items to the queue so just wait.
					wait();
					continue;
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

			aborted = true;
			notifyAll();
			return null;
		}
	}

	public synchronized void detach() {
		activeWorkers -= 1;
		notifyAll();
	}

	public synchronized void abort() {
		aborted = true;
		notifyAll();
	}
}
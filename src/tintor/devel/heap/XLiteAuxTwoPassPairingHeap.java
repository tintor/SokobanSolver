package tintor.devel.heap;

import java.util.AbstractQueue;
import java.util.Iterator;

public final class XLiteAuxTwoPassPairingHeap<T extends XLiteAuxTwoPassPairingHeap.Node<T>> extends AbstractQueue<T> {
	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return min == null;
	}

	@Override
	public boolean offer(final T a) {
		assert a.next == null && a.child == null;
		aux = cons(a, aux);
		if (min == null || a.compareTo(min) < 0)
			min = a;
		return true;
	}

	@Override
	public T peek() {
		return min;
	}

	@Override
	public T poll() {
		if (min == null)
			return null;

		final T m = min;
		compressAux();
		min = root = twopass(root.child);
		return m;
	}

	public void merge(final XLiteAuxTwoPassPairingHeap<T> heap) {
		heap.compressAux();
		if (heap.root == null)
			return;

		aux = cons(heap.root, aux);
		if (min == null || heap.root.compareTo(min) < 0)
			min = heap.root;

		heap.root = null;
		heap.min = null;
	}

	public static abstract class Node<T> implements Comparable<T> {
		T child, next;
	}

	private T min, root, aux;

	private void compressAux() {
		if (aux != null)
			root = root == null ? auxMultipass() : link(auxMultipass(), root);
	}

	private static <T extends Node<T>> T link(final T a, final T b) {
		if (a.compareTo(b) < 0) {
			a.next = null;
			b.next = a.child;
			a.child = b;
			return a;
		}

		b.next = null;
		a.next = b.child;
		b.child = a;
		return b;
	}

	private static <T extends Node<T>> T cons(final T a, final T tail) {
		a.next = tail;
		return a;
	}

	private static <T extends Node<T>> T twopass(T listA) {
		if (listA == null)
			return null;
		T listB = null;

		// left to right
		while (listA != null && listA.next != null) {
			final T a = listA, b = listA.next;
			listA = b.next;
			listB = cons(link(a, b), listB);
		}
		if (listA != null)
			listB = cons(listA, listB);

		// right to left
		while (listB.next != null) {
			final T a = listB, b = a.next;
			listB = b.next;
			listB = cons(link(a, b), listB);
		}
		return listB;
	}

	private T auxMultipass() {
		assert aux != null;

		if (aux.next == null) {
			final T a = aux;
			aux = null;
			return a;
		}

		T last = aux;
		while (last.next != null)
			last = last.next;

		while (true) {
			final T a = aux, b = a.next;
			aux = b.next;

			final T c = link(a, b);
			if (aux == null)
				return c;
			last = last.next = c;
		}
	}
}
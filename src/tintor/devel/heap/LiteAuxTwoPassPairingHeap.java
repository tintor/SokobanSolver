package tintor.devel.heap;

import java.util.AbstractQueue;
import java.util.Iterator;

public class LiteAuxTwoPassPairingHeap<T extends Comparable<T>> extends AbstractQueue<T> {
	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean offer(final T e) {
		if (e == null)
			throw new IllegalArgumentException();

		final Node<T> a = new Node<T>(e);
		aux = cons(a, aux);
		if (min == null || e.compareTo(min.element) < 0)
			min = a;
		return true;
	}

	@Override
	public T peek() {
		return min != null ? min.element : null;
	}

	@Override
	public T poll() {
		if (min == null)
			return null;

		final T m = min.element;
		compressAux();
		min = root = twopass(root.child);
		return m;
	}

	public void merge(final LiteAuxTwoPassPairingHeap<T> heap) {
		heap.compressAux();
		if (heap.root == null)
			return;

		aux = cons(heap.root, aux);
		if (min == null || heap.root.element.compareTo(min.element) < 0)
			min = heap.root;

		heap.root = null;
		heap.min = null;
	}

	public static class Node<T> {
		public final T element;
		Node<T> child, next;

		Node(final T element) {
			this.element = element;
		}
	}

	private Node<T> min;
	private Node<T> root;
	private Node<T> aux;

	private static <T extends Comparable<T>> Node<T> link(final Node<T> a, final Node<T> b) {
		if (a == null) {
			if (b != null)
				b.next = null;
			return b;
		}
		if (b == null) {
			a.next = null;
			return a;
		}

		if (a.element.compareTo(b.element) < 0) {
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

	private static <T> Node<T> cons(final Node<T> a, final Node<T> tail) {
		a.next = tail;
		return a;
	}

	private static <T> Node<T> last(Node<T> a) {
		while (a.next != null)
			a = a.next;
		return a;
	}

	private static <T extends Comparable<T>> Node<T> twopass(Node<T> listA) {
		if (listA == null)
			return null;
		Node<T> listB = null;

		// left to right
		while (listA != null && listA.next != null) {
			final Node<T> a = listA, b = listA.next;
			listA = b.next;
			listB = cons(link(a, b), listB);
		}
		if (listA != null)
			listB = cons(listA, listB);

		// right to left
		while (listB.next != null) {
			final Node<T> a = listB, b = a.next;
			listB = b.next;
			listB = cons(link(a, b), listB);
		}
		return listB;
	}

	private void compressAux() {
		if (aux != null)
			root = link(auxMultipass(), root);
	}

	private Node<T> auxMultipass() {
		assert aux != null;

		if (aux.next == null) {
			final Node<T> a = aux;
			aux = null;
			return a;
		}

		Node<T> last = last(aux);
		while (true) {
			final Node<T> a = aux, b = a.next;
			aux = b.next;

			final Node<T> c = link(a, b);
			if (aux == null)
				return c;
			last = last.next = c;
		}
	}
}
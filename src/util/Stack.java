package util;

import java.util.Arrays;

public final class Stack<T> {
	private T[] array;
	private int size;

	@SuppressWarnings("unchecked")
	public Stack(int capacity) {
		array = (T[]) new Object[capacity];
	}

	public int size() {
		return size;
	}

	public void ensure(int space) {
		if (size + space >= array.length)
			array = Arrays.copyOf(array, Math.max(size + space, array.length * 2));
	}

	public void clear() {
		size = 0;
	}

	public void push(T value) {
		array[size++] = value;
	}

	public T pop() {
		assert size > 0;
		return array[--size];
	}
}
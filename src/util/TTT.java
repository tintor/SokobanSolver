package util;

import java.util.Arrays;
import java.util.Random;

public class TTT {
	public static void main(String[] args) {
		Random rand = new Random();
		int[] a = new int[1000000];
		for (int i = 0; i < a.length; i++)
			a[i] = rand.nextInt();

		BinaryHeapPriorityQueue<Integer> queue = new BinaryHeapPriorityQueue<Integer>(0, 12);
		for (int i : a)
			queue.offer(i);

		Arrays.sort(a);

		for (int i : a)
			if (i != queue.poll())
				throw new RuntimeException();
	}
}
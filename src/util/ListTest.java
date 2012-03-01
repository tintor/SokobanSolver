package util;

import java.util.ArrayList;
import java.util.List;

public class ListTest {
	static Integer[] m = new Integer[20];

	static void computePushes1(List<Integer> list) {
		for (int i = 0; i < 15; i++)
			if (i % 2 == 0)
				list.add(Integer.valueOf(i));
	}

	static void computePushes2(ArrayList<Integer> list) {
		for (int i = 0; i < 15; i++)
			if (i % 2 == 0)
				list.add(Integer.valueOf(i));
	}

	static int computePushes3(Integer[] list) {
		int size = 0;
		for (int i = 0; i < 15; i++)
			if (i % 2 == 0)
				list[size++] = Integer.valueOf(i);
		return size;
	}

	public static void main(String[] args) {
		for (int i = 0; i < m.length; i++)
			m[i] = i;

		ArrayList<Integer> array = new ArrayList<Integer>();
		Integer[] arrayi = new Integer[20];
		int n = 10000000;
		while (true) {
			System.gc();
			long a = System.nanoTime();
			for (int i = 0; i < n; i++) {
				array.clear();
				computePushes1(array);
			}
			long b = System.nanoTime();
			for (int i = 0; i < n; i++) {
				array.clear();
				computePushes2(array);
			}
			long c = System.nanoTime();
			for (int i = 0; i < n; i++) {
				computePushes3(arrayi);
			}
			long d = System.nanoTime();
			System.out.printf("%.0f %.0f %.0f\n", (double) (b - a) / n, (double) (c - b) / n, (double) (d - c) / n);
		}
	}
}
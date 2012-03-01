package util;


class Key {
	Object agent;
	long[] boxset;

	Object prev;

	int pushes;
	int total;

	int hashCode;
	Object nextInBucket;
}

// with single inline long 46.2 -> pushes and total to short 46.2, -> without hashCode 39.6
// with two inline long 52.9, -> pushes and total to short -> 46.2, -> without hashCode 46.2
// with three inline long 62.8
class Key2 {
	Object agent; //12
	long boxset; //
	long boxset2;

	Object prev; // 12

	short pushes; //
	short total; //

	//int hashCode;
	Object nextInBucket; //
}

/*
1 63.1
2 69.4 6.3
3 78.9 9.5
4 85.1 6.2
*/

/*
1 63.1
2 63.1
3 69.4
4 69.4
5 78.9
6 78.9
7 85.1
8 85.2
*/

public class Memtest {
	public static void main(String[] args) {
		int b = 1;
		while (true) {
			Object[] obj = new Object[100000];
			long before = Runtime.getRuntime().freeMemory();
			for (int i = 0; i < obj.length; i++) {
				Key2 a = new Key2();
				//a.boxset = new long[b];
				obj[i] = a;
			}
			long after = Runtime.getRuntime().freeMemory();
			System.gc();
			System.out.printf("%s %.1f\n", b, (double) (before - after) / obj.length);

			b += 1;
			if (b > 4)
				b = 1;
		}
	}
}

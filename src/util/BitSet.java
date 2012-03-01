package util;

public class BitSet {
	public static int bitCount(int[] set) {
		int size = 0;
		for (int a : set) {
			size += bitCount(a);
		}
		return size;
	}

	public static int bitCount(long set) {
		int size = 0;
		while (set > 0) {
			size += set & 1;
			set >>>= 1;
		}
		return size;
	}

	public static boolean zero(int[] set) {
		for (int a : set)
			if (a != 0)
				return false;
		return true;
	}

	public static boolean testBitExt(int[] set, int i) {
		return i < set.length * 32 && testBit(set, i);
	}

	public static boolean testBit(int[] set, int i) {
		return (set[i >>> 5] & (1 << (i & 31))) != 0;
	}

	public static void setBit(int[] set, int i) {
		set[i >>> 5] |= 1 << (i & 31);
	}

	public static void clearBit(int[] set, int i) {
		set[i >>> 5] &= ~(1 << (i & 31));
	}

	public static long setBit(long set, int i) {
		return set | (1 << (i & 31));
	}

	public static long clearBit(long set, int i) {
		return set & ~(1 << (i & 31));
	}

	private static int bitCount(int val) {
		val -= (0xaaaaaaaa & val) >>> 1;
		val = (val & 0x33333333) + ((val >>> 2) & 0x33333333);
		val = val + (val >>> 4) & 0x0f0f0f0f;
		val += val >>> 8;
		val += val >>> 16;
		return val & 0xff;
	}
}

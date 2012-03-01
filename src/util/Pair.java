package util;

public class Pair<A, B> {
	public final A first;
	public final B second;

	public Pair(A f, B s) {
		if (f == null || s == null)
			throw new NullPointerException();
		first = f;
		second = s;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	@Override
	public int hashCode() {
		return first.hashCode() ^ second.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		Pair<A, B> p = (Pair<A, B>) other;
		if (p == null)
			return false;
		return equals(this, p);
	}

	private boolean equals(Pair<A, B> a, Pair<A, B> b) {
		return a.first.equals(b.first) && a.second.equals(b.second);
	}
}
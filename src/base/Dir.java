package base;

public enum Dir {
	Up(2), Left(3), Down(0), Right(1);

	public Dir reverse() {
		return Dir.values()[reverse];
	}

	public final int reverse;

	private Dir(int reverse) {
		this.reverse = reverse;
	}
}
package util;

public class Statistics {
	private int count;
	private double sum;
	private double sumOfSquares;
	private double max;

	public void add(double value) {
		count += 1;
		sum += value;
		sumOfSquares += value * value;
	}

	public void add(double value, int count) {
		this.count += count;
		sum += value * count;
		sumOfSquares += value * value * count;
	}

	public void increase(double value, double delta) {
		if (delta < 0)
			throw new IllegalArgumentException();
		sum += delta;
		sumOfSquares += (2 * value + delta) * delta;
		max = Math.max(max, value + delta);
	}

	public int count() {
		return count;
	}

	public double avg() {
		return sum / count;
	}

	public double stdev() {
		return Math.sqrt((sumOfSquares - sum * sum / count) / (count - 1));
	}

	public double max() {
		return max;
	}
}
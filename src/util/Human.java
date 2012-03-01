package util;

public class Human {
	public static String human(long count) {
		if (count >= 1000000000)
			return String.format("%.1fB", (count + 50000000) / 1000000000.0);
		if (count >= 10000000)
			return String.format("%.1fM", (count + 50000) / 1000000.0);
		if (count >= 1000000)
			return String.format("%.2fM", (count + 5000) / 1000000.0);
		if (count >= 100000)
			return String.format("%sK", (count + 500) / 1000);
		if (count >= 10000)
			return String.format("%.1fK", (count + 50) / 1000.0);
		if (count >= 1000)
			return String.format("%.1fK", (count + 50) / 1000.0);
		return count + "";
	}
}

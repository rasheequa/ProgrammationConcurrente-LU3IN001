package pc;

public class Repeat {

	public static String repeat(char c, int n) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < n; i++) {
			s.append(c);
		}
		
		return s.toString();
	}
		
}
	

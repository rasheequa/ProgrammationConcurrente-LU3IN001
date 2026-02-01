package pc;

public class Repeat {

	public static String repeat(char c, int n) {
		StringBuilder s = new StringBuilder(); // Modif du type en StringBuilder
		for (int i = 0; i < n; i++) {
			s.append(c);
		}
		
		return s.toString(); // Convertit le StringBuilder en String avec la methode toString()
	}
		
}
	

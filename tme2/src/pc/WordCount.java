package pc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class WordCount extends Thread {
	private String fichier;
	private int [] tableau;
	private int id;

   
    public WordCount(String arg,int [] tab, int id) {
    	this.fichier=arg;
    	this.tableau=tab;
    	this.id=id;
    }
    
    public void run(){
        try {
        	int count=countWords(fichier);
        	tableau[id]=count;
			System.out.println("---------------Thread "+id+"---------------\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public static int countWords(String filename) throws IOException {
		long startTime = System.currentTimeMillis();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			int total = 0; 
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				total += line.split("\\s+").length;
			}
			System.out.println("Time for file "+filename+" : "+(System.currentTimeMillis()-startTime) + " ms for "+ total + " words \n");
			return total;
			
		}
	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		int [] wordCount = new int[args.length];
		
		Thread [] th = new Thread[args.length];
		
		for (int i = 0; i < args.length; i++) {
			th[i]= new WordCount(args[i],wordCount,i);
			th[i].start();

		}
		
		for (int j = 0; j < args.length; j++) {
			try {
				th[j].join();
				
			} catch (InterruptedException e) {
		        e.printStackTrace();
		    }
		}
		
		System.out.println("\n---------------------------------------\n");
		
		System.out.println("Word count:" + Arrays.toString(wordCount));
		int total = 0;
		for (int count : wordCount) {
			total += count;
		}
		System.out.println("Total word count:" + total);
		System.out.println("Total time "+(System.currentTimeMillis()-startTime) + " ms");
	}
}

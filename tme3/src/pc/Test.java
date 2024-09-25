package pc;
import pc.IList;
import pc.iter.SimpleList;
import pc.iter.SimpleListSync;
import pc.rec.SimpleListRec;
import pc.rec.SimpleListRecSync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;


public class Test{
	
	public static int N = 100;
	public static int M = 100;
			
	
	
	public static void runConcurrentTest(IList<Object> liste, int N, int M) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		
		long start = System.nanoTime();
		
		for(int i = 0; i<N;i++) {
			Thread t = new Thread (() -> {
				for(int j = 0; j<M;j++) {
						liste.add(j);
				}
			});
			
			t.start();
			threads.add(t);
		}
		
		for(Thread t : threads) {
			t.join();
		}
		
		long end = System.nanoTime();
		long time=end-start;
		System.out.println("Temps écoulé runConcurrentTest add " + N+"x"+M+ " : "+time + " ns\n");
		
		
		threads.clear();
		
		start = System.nanoTime();
		
		for(int i = 0; i<N;i++) {
			Thread t = new Thread (()->{
					for(int j = 0; j<M;j++) {
						assertFalse(liste.contains(j+M));;
					}
			});
			
			t.start();
			threads.add(t);
		}
		
		for(Thread t : threads) {
			t.join();
		}
		
		end = System.nanoTime();
		time=end-start;
		System.out.println("Temps écoulé runConcurrentTest contains " + N+"x"+M+ " : "+time + " ns\n");
		
		System.out.println("Taille liste : "+liste.size()+"\n");
		//assertEquals(N*M,liste.size()); produit des erreurs pour les listes non synchronized a cause du parallélisme 
	}
	
	
	// -------------------- Test-------------------- 

	public static void main(String[] args) {
		
		IList<Object> simplesync = new SimpleListSync<Object>();
		IList<Object> simple = new SimpleList<Object>();
		IList<Object> simplerec = new SimpleListRec<Object>();
		IList<Object> simplesyncrec = new SimpleListRecSync<Object>();
		
		try {
			System.out.println("------------------ Test avec SimpleList ------------------\n");
			runConcurrentTest(simple,N,M);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println("------------------ Test avec SimpleListSync ------------------\n");
			runConcurrentTest(simplesync,N,M);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println("------------------ Test avec SimpleListRec ------------------\n");
			runConcurrentTest(simplerec,N,M);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println("------------------ Test avec SimpleListRecSync ------------------\n");
			runConcurrentTest(simplesyncrec,N,M);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

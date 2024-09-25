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
	
	public static void runConcurrentTest(IList<Object> liste, int N, int M) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		
		for(int i = 0; i<N;i++) {
			Thread t = new Thread (new Runnable() {
				public void run() {
					for(int j = 0; j<M;j++) {
						liste.add(j);
						assertFalse(liste.contains(j+M));
					}
				}
			});
			t.start();
			threads.add(t);
		}
		
		for(Thread t : threads) {
			t.join();
		}
		
		assertEquals(N*M,liste.size());
	}
	
	
	
	public static void main(String [] args) {
		
		IList<Object> simple = new SimpleListSync<Object>();
		IList<Object> simplesync = new SimpleList<Object>();
		
		IList<Object> simplerec = new SimpleListRec<Object>();
		IList<Object> simplesyncrec = new SimpleListRecSync<Object>();

	}
}

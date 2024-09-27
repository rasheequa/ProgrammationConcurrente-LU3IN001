package pc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import pc.IList;
import pc.iter.SimpleList;
import pc.rec.SimpleListRec;

public class TestList {

	@Test
	public void testSimpleList() {
		IList<String> list = new SimpleList<>();

		runConcurrentTest(list, 10, 1000);
	}

	@Test
	public void testSimpleListRec() {
		IList<String> list = new SimpleListRec<>();

		runConcurrentTest(list, 10, 1000);
	}
	
	@Test
	public void testClear() {
		IList<String> list = new SimpleListRec<>();

		ClearTask(list);
	}

	public static void testList(IList<String> list) {
		// Tests des versions itératives
		list.add("Hello");
		list.add("World");
		System.out.println("Taille : " + list.size());
		assertEquals(2, list.size());
		System.out.println("Contient 'World' : " + list.contains("World"));
		assertEquals(true, list.contains("World"));
		assertEquals(false, list.contains("Master"));
		
		list.clear();
		assertEquals(0, list.size());
		System.out.println("Taille après clear : " + list.size());
	}
	

	private void runConcurrentTest(IList<String> list, int N, int M) {
		System.out.println("Running test of "+list.getClass().getSimpleName());
		testList(list);

		long startTime = System.currentTimeMillis();

		List<Thread> threads = new ArrayList<>();

		// Create threads to add elements to the list
		for (int i = 0; i < N; i++) {
            Thread t = new Thread(new AddTask(list, M));
            threads.add(t);
         // Start all threads
            t.start(); 
        }

		for(Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Check that the list size is N * M
		assertEquals(N * M, list.size());
		
		// Clear thread list for the next task
        threads.clear();
        
        for (int i = 0; i < N; i++) {
            Thread t = new Thread(new ContainsTask(list, M));
            threads.add(t);
            t.start();
        }

        // Wait for all threads to finish
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
		// assertEquals("List size should be N * M", N * M, list.size());
		assertEquals(N*M,list.size());

		long endTime = System.currentTimeMillis();
		System.out.println("Test completed in " + (endTime - startTime) + " milliseconds");
	}

	// TODO support pour les threads
	static class AddTask implements Runnable {
		private final IList<String> list;
        private final int M;
        
        public AddTask(IList<String> list, int M) {
        	this.list = list;
        	this.M=M;
        }
        
		@Override
		public void run() {
			for(int j = 0; j<M;j++) {
				list.add(Integer.toString(j));
			}
			
		}

	}
	
	static class ContainsTask implements Runnable {
		private final IList<String> list;
        private final int M;
        
        public ContainsTask(IList<String> list, int M) {
        	this.list = list;
        	this.M=M;
        }
        
		@Override
		public void run() {
			for(int j = 0; j<M;j++) {
				assertFalse(list.contains(Integer.toString(j+M)));
			}
			
		}

	}
	
	static class ClearTask implements Runnable {
		private final IList<String> list;
 
        
        public ClearTask(IList<String> list) {
        	this.list = list;
        }
        
		@Override
		public void run() {
			list.clear();

		}

	}

}


package pc.crawler;

import java.util.concurrent.atomic.AtomicInteger;

public class ActivityMonitor {
	private AtomicInteger compteur;
	
	public ActivityMonitor() {
		compteur=new AtomicInteger(0);
	}
	
	public synchronized void taskStarted() {
		compteur.addAndGet(1);
	}
	
	public synchronized void taskCompleted() {
		if(compteur.addAndGet(-1)==0) {
			notifyAll();
		}
		
	}
	
	public synchronized void awaitCompletion() throws InterruptedException {
		while(compteur.get()!=0) {
			wait();
			
		}
	}
	

}

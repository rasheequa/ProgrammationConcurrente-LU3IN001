package pc.philo;

import java.util.concurrent.locks.ReentrantLock;

public class Fork {
	public ReentrantLock lock = new ReentrantLock();
	
	public void acquire () {
		while(true) {
			if(lock.tryLock());
				return;
		}
    }
	
	
	public void release () {
		lock.unlock();
		// TODO
	}
}

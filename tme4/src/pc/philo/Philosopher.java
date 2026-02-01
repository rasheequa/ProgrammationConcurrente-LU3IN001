package pc.philo;

import java.util.concurrent.ThreadLocalRandom;

public class Philosopher implements Runnable {
	private Fork left;
	private Fork right;
	private int id;

	public Philosopher(Fork left, Fork right,int id) {
		this.left = left;
		this.right = right;
		this.id=id;
	}

	   @Override
	   public void run() {

	        while (!Thread.currentThread().isInterrupted()) {
	            think();
	            boolean right_ack = false;
	            boolean left_ack = false;
	            
	            if (id == 4) {
	            	
	            	right_ack = right.lock.tryLock();
	            	if(right_ack) {
	            		System.out.println("Philosophe " + id + " a pris la baguette de droite");
	            		left_ack = left.lock.tryLock();
		            	if(!left_ack) {
		            		right.release();
		            		System.out.println("Philosophe " + id + " a reposé la baguette de droite");
		            	}
		            	else {
		            		System.out.println("Philosophe " + id + " a pris la baguette de gauche");
		            	}
	            	}	
	            }
	            
	            else {
	            	
	            	left_ack = left.lock.tryLock();
	            	if(left_ack) {
	            		System.out.println("Philosophe " + id + " a pris la baguette de gauche");
	            		right_ack = right.lock.tryLock();
	            		if(!right_ack) {
	            			left.release();
		            		System.out.println("Philosophe " + id + " a reposé la baguette de gauche");
	            		}
	            		else {
		            		System.out.println("Philosophe " + id + " a pris la baguette de droite");
		            	}

	            	}
	     
	            }
	            
	            if(left_ack && right_ack) {
		            eat();
		            right.release();
		            left.release();
		            System.out.println("Philosophe " + id + " a reposé les baguettes.");
		        }  
	        }
	    }

	private void eat() {
		System.out.println("Philosophe "+id+" is eating");
		try {
			Thread.sleep(ThreadLocalRandom.current().nextInt(0, 10) * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.currentThread().interrupt();
		}
	}

	private void think() {
		System.out.println("Philosophe "+id+" is thinking");
		try {
			Thread.sleep(ThreadLocalRandom.current().nextInt(0, 10) * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.currentThread().interrupt();
		}
	}
}

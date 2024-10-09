package pc.philo;

public class TestPhilo {

	public static void main (String [] args) {
		
		final int NB_PHIL = 5;
		Thread [] tPhil = new Thread[NB_PHIL];
		Fork [] tChop = new Fork[NB_PHIL];

		for(int i = 0; i<NB_PHIL; i++) {
			tChop[i] = new Fork();
		}
		
		for(int i = 0; i<NB_PHIL; i++) {
			if (i == NB_PHIL - 1) {
				Philosopher p_last = new Philosopher(tChop[0],tChop[i],i );
				tPhil[i] = new Thread(p_last, "Philosophe "+i);
			}
			else {
				Philosopher p = new Philosopher(tChop[i], tChop[(i + 1) % NB_PHIL],i);
				tPhil[i] = new Thread(p, "Philosophe "+i);
			}
			tPhil[i].start();
		}
		
		try {
            Thread.sleep(5000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
		
		for(Thread t : tPhil) {
			t.interrupt();
		}

		
		try {
			for(Thread t : tPhil) {
				t.join();
			}
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.currentThread().interrupt();
		}
		// TODO
		
		System.out.println("Fin du programme");
		
		//Quel problème peut-on rencontrer à l’exécution de ce programme ?
		// Le problème classique que l'on peut rencontrer à l'exécution du programme des philosophes est le deadlock :
		// Comme toutes les baguettes sont prises par les philosophes à leur gauche, personne ne peut obtenir sa deuxième baguette, et donc personne ne peut manger.
		// Cela crée une situation où tous les threads sont bloqués, attendant chacun une ressource détenue par un autre philosophe.

//		Si un philosophe est assis entre deux baguettes numérotées i et  i+1, il devrait d'abord prendre la baguette i (la plus petite) puis la baguette i+1 (la plus grande).
//		Cependant, pour le dernier philosophe (celui numéroté N	− 1
//		N−1), il se trouve entre la baguette N-1 et la baguette 0
//		(à cause de la nature circulaire de la table).
		
//		Un deadlock pourrait survenir si tous les philosophes prennent la baguette de gauche en même temps et attendent ensuite la baguette de droite, ce qui entraîne un blocage
//		mutuel.
	}
}
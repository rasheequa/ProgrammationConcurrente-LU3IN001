package carlvbn.raytracing.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class ThreadPool {
	// File d'attente bloquante pour stocker les tâches à exécuter
	// Utilise une structure thread-safe qui permet de gérer l'ajout et le retrait
	// de tâches
	// entre plusieurs threads sans risque de conditions de course
	ArrayBlockingQueue<Runnable> jobs;

	// Liste pour stocker les threads travailleurs du pool
	// Permet de garder une trace de tous les threads créés et de pouvoir les
	// arrêter
	List<Thread> workers;

	// Classe interne privée représentant un travailleur (worker) qui va exécuter
	// les tâches
	// Implémente Runnable pour définir le comportement de chaque thread du pool
	private class PoolWorker implements Runnable {
		@Override
		public void run() {
			try {
				// Boucle infinie qui attend et exécute des tâches
				// Utilise Thread.interrupted() pour permettre un arrêt propre du thread
				while (!Thread.interrupted()) {
					// take() est bloquant : si la file est vide, le thread attend qu'une tâche
					// arrive
					// Cela évite le busy waiting et économise des ressources CPU
					Runnable job = jobs.take();
					job.run(); // Exécute la tâche
				}
			} catch (InterruptedException e) {
				// Capture l'exception quand le thread est interrompu (arrêté)
				// Permet une terminaison propre du thread
				System.out.println("Thread " + Thread.currentThread().getName() + " interrupted.");
			}
		}
	}

	// Constructeur du ThreadPool
	// Paramètres:
	// - qsize : taille maximale de la file d'attente des tâches
	// - nbWorker : nombre de threads travailleurs dans le pool
	public ThreadPool(int qsize, int nbWorker) {
		// Initialisation de la file d'attente avec ArrayBlockingQueue
		// Permet de limiter le nombre de tâches en attente et de gérer leur
		// ajout/retrait de manière thread-safe
		jobs = new ArrayBlockingQueue<>(qsize);

		// Initialisation de la liste des threads
		workers = new ArrayList<>();

		// Crée et démarre les threads travailleurs
		for (int i = 0; i < nbWorker; i++) {
			Thread t = new Thread(new PoolWorker()); // Crée un nouveau thread pour exécuter PoolWorker
			workers.add(t); // Ajoute le thread à la liste
			t.start(); // Démarre le thread
		}
	}

	// Méthode pour soumettre une tâche à exécuter
	// Cette méthode ajoute une tâche à la file d'attente (si la file n'est pas
	// pleine)
	// put() est bloquant : si la file est pleine, le thread qui soumet attend
	// qu'une place se libère
	public void submit(Runnable job) throws InterruptedException {
		jobs.put(job); // Ajoute la tâche à la file d'attente
	}

	// Méthode pour arrêter tous les threads du pool
	public void shutdown() {
		// Interrompt chaque thread du pool
		for (Thread t : workers) {
			t.interrupt(); // Signale l'interruption
			try {
				t.join(); // Attend que chaque thread termine son exécution
			} catch (InterruptedException e) {
				e.printStackTrace(); // Gère l'exception si l'interruption échoue
			}
		}

		// Nettoie les ressources
		workers.clear(); // Vide la liste des travailleurs
		jobs.clear(); // Vide la file d'attente des tâches

	}
}

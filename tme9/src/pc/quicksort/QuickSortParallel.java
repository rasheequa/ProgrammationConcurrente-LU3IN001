package pc.quicksort;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class QuickSortParallel extends RecursiveAction{
	private static final int SEQUENTIAL_THRESHOLD = 1000; // Seuil pour basculer vers un tri séquentiel

	private int[] array;
    private int low;
    private int high;
    
    public QuickSortParallel(int[] array, int low, int high) {
    	this.array = array;
        this.low = low;
        this.high = high;    	
    }
    
    public static void QuickSortParallelTask(int[] array, int low, int high, int nbThread) {
    	ForkJoinPool pool = new ForkJoinPool(nbThread); // Utilisation d'un pool de threads avec un nombre spécifié de threads
        QuickSortParallel task = new QuickSortParallel(array, low, high);
        pool.invoke(task); // Lancer la tâche
    }
    
	@Override
	protected void compute() {
		// TODO Auto-generated method stub
		if (high - low < SEQUENTIAL_THRESHOLD) {
            // Si la taille de la sous-liste est petite, utiliser le tri séquentiel
            QuickSort.quickSort(array, low, high);
        } else {
            if (low < high) {
                int pi = QuickSort.partition(array, low, high);
                
                // Créer deux sous-tâches pour le tri parallèle
                QuickSortParallel leftTask = new QuickSortParallel(array, low, pi);
                QuickSortParallel rightTask = new QuickSortParallel(array, pi + 1, high);
                
                // Exécuter les sous-tâches en parallèle
                leftTask.fork();  // Fork la tâche gauche
                rightTask.fork(); // Fork la tâche droite
                
                // Attendre que les deux tâches soient terminées
                leftTask.join();
                rightTask.join();
            }
        }
    }
	
	
	public static void main(String[] args) {
        // Taille du tableau à trier
        int[] sizes = {1000, 5000, 10000, 50000, 1000000};
        // Nombre de threads à tester
        int[] nbThreads = {1, 2, 4, 8}; // Test pour 1, 2, 4, 8 threads

        System.out.println("---------------------------------------------------\n");
        
        // Tester pour différentes tailles de tableaux
        for (int size : sizes) {
            // Générer un tableau aléatoire
            int[] array = QuickSort.generateRandomArray(size);
            
            // Tester le tri parallèle avec différents nombres de threads
            for (int th : nbThreads) {
                System.out.println("Taille du tableau: " + size + ", Nombre de threads: " + th);
                
                // Dupliquer le tableau pour le test
                int[] arrayToSort = Arrays.copyOf(array, array.length);
                
                // Mesurer le temps de tri parallèle
                long startTime = System.currentTimeMillis();
                QuickSortParallelTask(arrayToSort, 0, arrayToSort.length - 1, th);
                long endTime = System.currentTimeMillis();
                
                System.out.println("Temps d'exécution avec ForkJoin (parallèle) : " + (endTime - startTime) + " ms");
                
                
                
                // Vérifier que le tableau est trié
                if (isSorted(arrayToSort)) {
                    System.out.println("Le tableau a été trié correctement.\n");
                } else {
                    System.out.println("Erreur : Le tableau n'est pas trié.\n");
                }
            }
            
            System.out.println("---------------------------------------------------\n");
        }
       
    }

    // Fonction pour vérifier si un tableau est trié
    public static boolean isSorted(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }
        return true;
    }
}
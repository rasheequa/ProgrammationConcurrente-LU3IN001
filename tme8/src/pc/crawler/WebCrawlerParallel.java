package pc.crawler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawlerParallel {

    private static final int NB_THREADS = 10;
    private static final WebCrawlerParallel POISON_PILL = new WebCrawlerParallel(null, 0);

    private final String url;
    private final int depth;

    public WebCrawlerParallel(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public String getURL() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    public static void main(String[] args) {
        int depth = 4;
        String baseUrl = "https://www-licence.ufr-info-p6.jussieu.fr/lmd/licence/2023/ue/LU3IN001-2023oct/index.php";
        Path outputDir = Paths.get("/tmp/crawler/");
        
        try {
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("Created output directory: " + outputDir.toAbsolutePath());
            }

            BlockingQueue<WebCrawlerParallel> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Boolean> visitedUrls = new ConcurrentHashMap<>();
            ActivityMonitor monitor = new ActivityMonitor();

            // Chronométrage - début
            long startTime = System.currentTimeMillis();
            
            // Ajouter l'URL de départ et incrémenter le compteur
            monitor.taskStarted();
            queue.put(new WebCrawlerParallel(baseUrl, depth));
            

            ExecutorService executor = Executors.newFixedThreadPool(NB_THREADS);

            // Lancer les threads
            for (int i = 0; i < NB_THREADS; i++) {
                executor.submit(new Worker(queue, outputDir, visitedUrls, monitor));
            }

            // Attendre que toutes les tâches soient terminées
            monitor.awaitCompletion();

            // Insérer des poison pills pour arrêter les threads
            for (int i = 0; i < NB_THREADS; i++) {
                queue.put(POISON_PILL);
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

            System.out.println("Crawling completed successfully.");
            
            // Chronométrage - fin
            long endTime = System.currentTimeMillis();

            System.out.println("Depth: " + depth + ", Threads: " + NB_THREADS + ", Time: " + (endTime - startTime) + " ms");

            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execution interrupted.");
        }
    }

    private static class Worker implements Runnable {

        private final BlockingQueue<WebCrawlerParallel> queue;
        private final Path outputDir;
        private final ConcurrentHashMap<String, Boolean> visitedURLs;
        private final ActivityMonitor monitor;

        public Worker(BlockingQueue<WebCrawlerParallel> queue, Path outputDir, ConcurrentHashMap<String, Boolean> visitedURLs, ActivityMonitor monitor) {
            this.queue = queue;
            this.outputDir = outputDir;
            this.visitedURLs = visitedURLs;
            this.monitor = monitor;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    WebCrawlerParallel task = queue.take();
                    String url = task.getURL();
                    int depth = task.getDepth();

                    // Arrêter le thread si une poison pill est rencontrée
                    if (task == POISON_PILL) {
                        break;
                    }

                    if (depth >= 0 && visitedURLs.putIfAbsent(url, true) == null) {
                        System.out.println("Processing (Depth " + depth + "): " + url);
                        List<String> extractedUrls = Collections.emptyList();

                        try {
                            extractedUrls = WebCrawlerUtils.processUrl(url, url, outputDir);
                        } catch (URISyntaxException | IOException e) {
                            System.err.println("Error during crawling: " + e.getMessage());
                        }

                        // Ajouter les URLs extraites à la file et incrémenter le compteur
                        for (String extrURL : extractedUrls) {
                            queue.put(new WebCrawlerParallel(extrURL, depth-1));
                            monitor.taskStarted();
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    // Indiquer que la tâche est terminée après avoir ajouté les nouvelles tâches
                    monitor.taskCompleted();
                }
            }
        }
    }
}
package pc.mandelbrot;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import javax.imageio.ImageIO;

public class MandelbrotTask extends RecursiveAction {
	private BoundingBox boundingBox;
	private int maxIterations;
	private int[] imageBuffer ;
	private final int start;
    private final int end;
    private static final int THRESHOLD = 5000;
    
    
    public MandelbrotTask(BoundingBox boundingBox, int maxIterations, int[] imageBuffer, int start, int end) {
        this.boundingBox = boundingBox;
        this.maxIterations = maxIterations;
        this.imageBuffer = imageBuffer;
        this.start = start;
        this.end = end;
   }
	
	

	@Override
	protected void compute() {
		
		//long time = System.currentTimeMillis();
		
		int pixel = (end - start) * boundingBox.width;
		
        // Vérifier si la tâche est assez petite pour un calcul direct
        if (pixel <= THRESHOLD) {
        	
    		// Iterate over each pixel
    		for (int py = start; py < end; py++) {
    			for (int px = 0; px < boundingBox.width; px++) {
    				int color = computePixelColor(boundingBox, maxIterations, px, py);

    				// Set the pixel in the image buffer
    				imageBuffer[py * boundingBox.width + px] = color;
    			}
    		}
    	
        }
        
        else {
            // Diviser en deux sous-tâches
            int mid = (start + end) / 2;
            MandelbrotTask top = new MandelbrotTask(boundingBox, maxIterations, imageBuffer, start, mid);
            MandelbrotTask bottom = new MandelbrotTask(boundingBox, maxIterations, imageBuffer, mid, end);

            invokeAll(top, bottom);
        } 
        
        //System.out.println("Rendered image in " + (System.currentTimeMillis() - time) + " ms");
		
	}
	
	public static void parCompute(BoundingBox boundingBox, int maxIterations, int[] imageBuffer) {
		//long time = System.currentTimeMillis();
		// TODO !!
		ForkJoinPool pool = ForkJoinPool.commonPool();
        MandelbrotTask task = new MandelbrotTask(boundingBox, maxIterations, imageBuffer, 0, boundingBox.height);
        pool.invoke(task);
        
	    //System.out.println("Rendered image in " + (System.currentTimeMillis() - time) + " ms");
	}
	
	
	
	
	
	public static int computePixelColor(BoundingBox boundingBox, int maxIterations, int px, int py) {
		int width = boundingBox.width;
		int height = boundingBox.height;

		// Map pixel to complex plane with inverted y-axis
		double x0 = boundingBox.xmin + px * (boundingBox.xmax - boundingBox.xmin) / width;
		double y0 = boundingBox.ymax - py * (boundingBox.ymax - boundingBox.ymin) / height;

		double x = 0.0;
		double y = 0.0;
		int iteration = 0;

		while (x * x + y * y <= 4 && iteration < maxIterations) {
			double xtemp = x * x - y * y + x0;
			y = 2 * x * y + y0;
			x = xtemp;
			iteration++;
		}

		if (iteration < maxIterations) {
			// Compute smooth iteration count
			double logZn = Math.log(x * x + y * y) / 2;
			double nu = Math.log(logZn / Math.log(2)) / Math.log(2);
			double smoothIteration = iteration + 1 - nu;

			// Normalize smooth iteration
			float hue = (float) (0.95f + 10 * smoothIteration / maxIterations);
			float saturation = 0.6f;
			float brightness = 1.0f;

			// Wrap hue to [0,1] to prevent overflow
			hue = hue - (float) Math.floor(hue);

			return Color.HSBtoRGB(hue, saturation, brightness);
		} else {
			return Color.BLACK.getRGB();
		}
	}
	
	private static void saveImage(int[] imageBuffer, int width, int height, String fileName) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, imageBuffer, 0, width);

        File outputFile = new File(fileName);
        try {
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved as: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving image: " + fileName);
            e.printStackTrace();
        }
    }

	public static void main(String[] args) {
        int[] sizes = {500, 1000, 2000};
        int[] maxIterations = {100, 500, 1000, 5000};
        int[] nbThread = {2, 4, 8, 16};

        for (int size : sizes) {
            for (int iteration : maxIterations) {
                for (int th : nbThread) {
                    System.out.println("Testing size: " + size + "x" + size +
                            ", maxIterations: " + iteration +
                            ", threads: " + th);

                    BoundingBox boundingBox = new BoundingBox(-2.0, 1.0, -1.5, 1.5, size, size);
                    int[] imageBuffer = new int[size * size];

                    ForkJoinPool pool = new ForkJoinPool(th);

                    // Test parallel implementation
                    long start = System.currentTimeMillis();
                    MandelbrotTask task = new MandelbrotTask(boundingBox, iteration, imageBuffer, 0, size);
                    pool.invoke(task);
                    long end = System.currentTimeMillis();

                    System.out.println("Parallel Execution Time: " + (end - start) + " ms");
                    
                    saveImage(imageBuffer, size, size, "mandelbrot_parallel_" +
                            size + "x" + size + "_iter" + iteration+".png");
                    
                    // Test sequential implementation
                    start = System.currentTimeMillis();
                    MandelbrotCalculator.compute(boundingBox, iteration, imageBuffer);
                    end = System.currentTimeMillis();

                    System.out.println("Sequential Execution Time: " + (end - start) + " ms");
                    System.out.println();
                }
            }
        }
    }

}

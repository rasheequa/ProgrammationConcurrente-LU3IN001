package carlvbn.raytracing.rendering;

import carlvbn.raytracing.pixeldata.Color;
import carlvbn.raytracing.pixeldata.GaussianBlur;
import carlvbn.raytracing.pixeldata.PixelBuffer;
import carlvbn.raytracing.pixeldata.PixelData;
import carlvbn.raytracing.math.*;
import carlvbn.raytracing.solids.Solid;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Renderer {
	// Constantes pour le rendu et les effets
	private static final float GLOBAL_ILLUMINATION = 0.3F;
	private static final float SKY_EMISSION = 0.5F;
	private static final int MAX_REFLECTION_BOUNCES = 5;
	private static final boolean SHOW_SKYBOX = true;

	// Configuration des effets visuels
	public static float bloomIntensity = 0.5F;
	public static int bloomRadius = 10;

	// Pool de threads personnalisé pour le rendu parallèle
	// Permet de gérer efficacement l'exécution des tâches de ray tracing
	private static final ThreadPool threadPool = new ThreadPool(1000, 200);
	// Executor service standard de Java pour comparaison
	private static final ExecutorService exec = Executors.newSingleThreadExecutor();

	/**
	 * Renders the scene to a Pixel buffer
	 * 
	 * @param scene  The scene to Render
	 * @param width  The width of the desired output
	 * @param height The height of the desired output
	 * @return The rendered PixelBuffer
	 */
	public static PixelBuffer renderScene(Scene scene, int width, int height) {
		PixelBuffer pixelBuffer = new PixelBuffer(width, height);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				float[] screenUV = getNormalizedScreenCoordinates(x, y, width, height);

				pixelBuffer.setPixel(x, y, computePixelInfo(scene, screenUV[0], screenUV[1]));
			}
		}

		return pixelBuffer;
	}

	/**
	 * Fills a rectangle area of a BufferedImage with a specified color. This
	 * function is thread-safe as long as no two threads write to the same pixels.
	 *
	 * @param image  The BufferedImage to modify
	 * @param x      The x-coordinate of the rectangle's top-left corner
	 * @param y      The y-coordinate of the rectangle's top-left corner
	 * @param width  The width of the rectangle
	 * @param height The height of the rectangle
	 * @param color  The color to fill with
	 */
	public static void fillColorRect(BufferedImage image, int x, int y, int width, int height, Color color) {
		int rgb = color.getRGB();
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();

		// Ensure the rectangle is within the image bounds
		int startX = Math.max(0, x);
		int startY = Math.max(0, y);
		int endX = Math.min(x + width, imgWidth);
		int endY = Math.min(y + height, imgHeight);

		if (startX >= endX || startY >= endY) {
			// Rectangle is outside the image bounds; nothing to fill
			return;
		}

		// Get the pixel data array
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		// Fill the rectangle area
		for (int row = startY; row < endY; row++) {
			int offset = row * imgWidth + startX;
			int length = endX - startX;
			Arrays.fill(pixels, offset, offset + length, rgb);
		}
	}

	/**
	 * Renders the scene to a java.awt.Graphics object
	 * 
	 * @param scene      The scene to Render
	 * @param width      The width of the desired output
	 * @param height     The height of the desired output
	 * @param resolution (Floating point greater than 0 and lower or equal to 1)
	 *                   Controls the number of rays traced. (1 = Every pixel is
	 *                   ray-traced)
	 */
	public static void renderScene1(Scene scene, Graphics gfx, int width, int height, float resolution) {
		int blockSize = (int) (1 / resolution);
		long start = System.currentTimeMillis();

		for (int x = 0; x < width; x += blockSize) {
			for (int y = 0; y < height; y += blockSize) {
				float[] uv = getNormalizedScreenCoordinates(x, y, width, height);
				PixelData pixelData = computePixelInfo(scene, uv[0], uv[1]);

				gfx.setColor(pixelData.getColor().toAWTColor());
				gfx.fillRect(x, y, blockSize, blockSize);
			}
		}

		System.out.println("Rendered in " + (System.currentTimeMillis() - start) + "ms");
	}

	// Question 3 : parcours par pixel
	// Méthodes de rendu avec différentes stratégies de parallélisme
	// Version 2 : Un thread par pixel
	public static void renderScene2(Scene scene, Graphics gfx, int width, int height, float resolution) {
		// Résolution réduite pour éviter de créer trop de threads
		resolution = 0.1f;
		int blockSize = (int) (1 / resolution);
		long start = System.currentTimeMillis();

		// Liste pour stocker tous les threads
		List<Thread> th = new ArrayList<>();

		// Double boucle pour parcourir l'image
		for (int x = 0; x < width; x += blockSize) {
			for (int y = 0; y < height; y += blockSize) {
				final int x2 = x;
				final int y2 = y;

				// Création d'un thread par pixel/bloc
				// Chaque thread calcule et dessine un bloc de l'image
				Thread t = new Thread(() -> {
					float[] uv = getNormalizedScreenCoordinates(x2, y2, width, height);
					PixelData pixelData = computePixelInfo(scene, uv[0], uv[1]);

					// Synchronisation sur l'objet Graphics pour éviter les conflits
					synchronized (gfx) { // Question 4 : gfx -> data race donc synchronized sur la variable
						gfx.setColor(pixelData.getColor().toAWTColor());
						gfx.fillRect(x2, y2, blockSize, blockSize);
					}
				});
				th.add(t);
				t.start();
			}
		}

		// Attente de la fin de tous les threads
		for (Thread t : th) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Rendered in " + (System.currentTimeMillis() - start) + "ms");
	}

	// Question 5: version précedente crée trop de thread qui veulent accéder gfx
	// Version 3 : Un thread par colonne
	public static void renderScene3(Scene scene, Graphics gfx, int width, int height, float resolution) {
		// avec un for dans le thread
		System.out.println("prout");

		resolution = 0.1f;
		int blockSize = (int) (1 / resolution);
		long start = System.currentTimeMillis();

		List<Thread> th = new ArrayList<>();
		for (int x = 0; x < width; x += blockSize) {
			final int x2 = x;

			// Un seul thread par colonne, qui gère toute la hauteur
			Thread t = new Thread(() -> {
				for (int y = 0; y < height; y += blockSize) {
					float[] uv = getNormalizedScreenCoordinates(x2, y, width, height);
					PixelData pixelData = computePixelInfo(scene, uv[0], uv[1]);

					// Synchronisation pour éviter les conflits sur Graphics
					synchronized (gfx) {
						gfx.setColor(pixelData.getColor().toAWTColor());
						gfx.fillRect(x2, y, blockSize, blockSize);
					}
				}
			});
			th.add(t);
			t.start();
		}

		// Attente de la fin de tous les threads
		for (Thread t : th) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Rendered in " + (System.currentTimeMillis() - start) + "ms");
	}

	// Question 7
	// Version 4 : Utilisation d'un ThreadPool personnalisé avec CountDownLatch
	public static void renderScene4(Scene scene, Graphics gfx, int width, int height, float resolution) {

		// resolution = 0.1f;
		int blockSize = (int) (1 / resolution);
		long start = System.currentTimeMillis();
		// Calcul du nombre total de tâches
		int numTasks = (width / blockSize) * (height / blockSize);

		// Latch pour synchroniser la fin de toutes les tâches
		CountDownLatch latch = new CountDownLatch(numTasks);

		for (int x = 0; x < width; x += blockSize) {
			final int xf = x;

			// Création d'une tâche Runnable pour chaque colonne
			Runnable rn = () -> {
				for (int y = 0; y < height; y += blockSize) {
					float[] uv = getNormalizedScreenCoordinates(xf, y, width, height);
					PixelData pixelData = computePixelInfo(scene, uv[0], uv[1]);

					// Synchronisation sur Graphics
					synchronized (gfx) {
						gfx.setColor(pixelData.getColor().toAWTColor());
						gfx.fillRect(xf, y, blockSize, blockSize);
					}
					// Décrémente le latch pour signaler la fin d'une tâche
					latch.countDown();
				}
			};
			try {
				// Soumet la tâche au pool de threads personnalisé
				threadPool.submit(rn);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			// Attend que toutes les tâches soient terminées
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Rendered in " + (System.currentTimeMillis() - start) + "ms");
	}

	// Question 8
	// Version 5 : Utilisation d'un ExecutorService standard
	public static void renderScene5(Scene scene, Graphics gfx, int width, int height, float resolution) {

		resolution = 0.1f;
		int blockSize = (int) (1 / resolution);
		long start = System.currentTimeMillis();

		// Calcul du nombre total de tâches
		int numTasks = (width / blockSize) * (height / blockSize);

		// Latch pour synchroniser la fin de toutes les tâches
		CountDownLatch latch = new CountDownLatch(numTasks);

		// Crée une image buffer pour éviter les conflits de synchronisation
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x += blockSize) {
			final int xf = x;
			// Tâche Runnable pour chaque colonne
			Runnable rn = () -> {
				for (int y = 0; y < height; y += blockSize) {
					float[] uv = getNormalizedScreenCoordinates(xf, y, width, height);
					PixelData pixelData = computePixelInfo(scene, uv[0], uv[1]);
					// Utilise la méthode fillColorRect thread-safe
					fillColorRect(image, xf, y, blockSize, blockSize, pixelData.getColor());
					latch.countDown();
				}
			};
			// Soumet la tâche à l'ExecutorService
			exec.submit(rn);

		}
		try {
			// Attend que toutes les tâches soient terminées
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Dessine l'image complète
		gfx.drawImage(image, 0, 0, null);

		System.out.println("Rendered in " + (System.currentTimeMillis() - start) + "ms");
	}

	// Question 8
	public static void renderScene(Scene scene, Graphics gfx, int width, int height, float resolution) {

		// resolution = 0.1f;
		int blockSize = (int) (1 / resolution);
		long start = System.currentTimeMillis();

		int numTasks = (width / blockSize) * (height / blockSize);
		CountDownLatch latch = new CountDownLatch(numTasks);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x += blockSize) {
			final int xf = x;
			Runnable rn = () -> {
				for (int y = 0; y < height; y += blockSize) {
					float[] uv = getNormalizedScreenCoordinates(xf, y, width, height);
					PixelData pixelData = computePixelInfo(scene, uv[0], uv[1]);
					fillColorRect(image, xf, y, blockSize, blockSize, pixelData.getColor());
					latch.countDown();
				}
			};
			exec.submit(rn);

		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		gfx.drawImage(image, 0, 0, null);

		System.out.println("Rendered in " + (System.currentTimeMillis() - start) + "ms");
	}

	/** Same as the above but applies Post-Processing effects before drawing. */
	public static void renderScenePostProcessed(Scene scene, Graphics gfx, int width, int height, float resolution) {
		int bufferWidth = Math.round(width * resolution + 0.49F);
		int bufferHeight = Math.round(height * resolution + 0.49F);
		PixelBuffer pixelBuffer = renderScene(scene, bufferWidth, bufferHeight);

		PixelBuffer emissivePixels = pixelBuffer.clone(); // The width of this buffer has to remain constant to keep the
															// blur factor the same for all sizes
		emissivePixels.filterByEmission(0.1F);
		GaussianBlur blur = new GaussianBlur(emissivePixels);
		blur.blur(bloomRadius, 1);
		pixelBuffer.add(blur.getPixelBuffer().multiply(bloomIntensity).resize(bufferWidth, bufferHeight, true));

		float blockSize = 1 / resolution;
		for (int x = 0; x < bufferWidth; x++) {
			for (int y = 0; y < bufferHeight; y++) {
				PixelData pixel = pixelBuffer.getPixel(x, y);
				gfx.setColor(pixel.getColor().toAWTColor());
				gfx.fillRect((int) (x * blockSize), (int) (y * blockSize), (int) blockSize + 1, (int) blockSize + 1);
			}
		}

	}

	/**
	 * Get OpenGL style screen coordinates where (0,0) is the center of the screen
	 * from x, y coordinates that start from the top-left corner of the screen
	 */
	public static float[] getNormalizedScreenCoordinates(int x, int y, int width, int height) {
		float u = 0, v = 0;
		if (width > height) {
			u = (float) (x - width / 2 + height / 2) / height * 2 - 1;
			v = -((float) y / height * 2 - 1);
		} else {
			u = (float) x / width * 2 - 1;
			v = -((float) (y - height / 2 + width / 2) / width * 2 - 1);
		}

		return new float[] { u, v };
	}

	public static PixelData computePixelInfo(Scene scene, float u, float v) {
		Vector3 eyePos = new Vector3(0, 0, (float) (-1 / Math.tan(Math.toRadians(scene.getCamera().getFOV() / 2))));
		Camera cam = scene.getCamera();

		Vector3 rayDir = new Vector3(u, v, 0).subtract(eyePos).normalize().rotateYP(cam.getYaw(), cam.getPitch());
		RayHit hit = scene.raycast(new Ray(eyePos.add(cam.getPosition()), rayDir));
		if (hit != null) {
			return computePixelInfoAtHit(scene, hit, MAX_REFLECTION_BOUNCES);
		} else if (SHOW_SKYBOX) {
			Color sbColor = scene.getSkybox().getColor(rayDir);
			return new PixelData(sbColor, Float.POSITIVE_INFINITY, sbColor.getLuminance() * SKY_EMISSION);
		} else {
			return new PixelData(Color.BLACK, Float.POSITIVE_INFINITY, 0);
		}
	}

	private static PixelData computePixelInfoAtHit(Scene scene, RayHit hit, int recursionLimit) {
		Vector3 hitPos = hit.getPosition();
		Vector3 rayDir = hit.getRay().getDirection();
		Solid hitSolid = hit.getSolid();
		Color hitColor = hitSolid.getTextureColor(hitPos.subtract(hitSolid.getPosition()));
		float brightness = getDiffuseBrightness(scene, hit);
		float specularBrightness = getSpecularBrightness(scene, hit);
		float reflectivity = hitSolid.getReflectivity();
		float emission = hitSolid.getEmission();

		PixelData reflection;
		Vector3 reflectionVector = rayDir.subtract(hit.getNormal().multiply(2 * Vector3.dot(rayDir, hit.getNormal())));
		Vector3 reflectionRayOrigin = hitPos.add(reflectionVector.multiply(0.001F)); // Add a little to avoid hitting
																						// the same solid again
		RayHit reflectionHit = recursionLimit > 0 ? scene.raycast(new Ray(reflectionRayOrigin, reflectionVector))
				: null;
		if (reflectionHit != null) {
			reflection = computePixelInfoAtHit(scene, reflectionHit, recursionLimit - 1);
		} else {
			Color sbColor = scene.getSkybox().getColor(reflectionVector);
			reflection = new PixelData(sbColor, Float.POSITIVE_INFINITY, sbColor.getLuminance() * SKY_EMISSION);
		}

		Color pixelColor = Color.lerp(hitColor, reflection.getColor(), reflectivity) // Reflected color
				.multiply(brightness) // Diffuse lighting
				.add(specularBrightness) // Specular lighting
				.add(hitColor.multiply(emission)) // Object emission
				.add(reflection.getColor().multiply(reflection.getEmission() * reflectivity)); // Indirect illumination

		return new PixelData(pixelColor, Vector3.distance(scene.getCamera().getPosition(), hitPos),
				Math.min(1, emission + reflection.getEmission() * reflectivity + specularBrightness));
	}

	private static float getDiffuseBrightness(Scene scene, RayHit hit) {
		Light sceneLight = scene.getLight();

		// Raytrace to light to check if something blocks the light
		RayHit lightBlocker = scene.raycast(
				new Ray(sceneLight.getPosition(), hit.getPosition().subtract(sceneLight.getPosition()).normalize()));
		if (lightBlocker != null && lightBlocker.getSolid() != hit.getSolid()) {
			return GLOBAL_ILLUMINATION; // GOBAL_ILLUMINATION = Minimum brightness
		} else {
			return Math.max(GLOBAL_ILLUMINATION,
					Math.min(1, Vector3.dot(hit.getNormal(), sceneLight.getPosition().subtract(hit.getPosition()))));
		}
	}

	private static float getSpecularBrightness(Scene scene, RayHit hit) {
		Vector3 hitPos = hit.getPosition();
		Vector3 cameraDirection = scene.getCamera().getPosition().subtract(hitPos).normalize();
		Vector3 lightDirection = hitPos.subtract(scene.getLight().getPosition()).normalize();
		Vector3 lightReflectionVector = lightDirection
				.subtract(hit.getNormal().multiply(2 * Vector3.dot(lightDirection, hit.getNormal())));

		float specularFactor = Math.max(0, Math.min(1, Vector3.dot(lightReflectionVector, cameraDirection)));
		return (float) Math.pow(specularFactor, 2) * hit.getSolid().getReflectivity();
	}
}

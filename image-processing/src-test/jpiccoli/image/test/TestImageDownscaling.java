package jpiccoli.image.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import jpiccoli.image.AreaAveragingDownscaling;
import jpiccoli.image.MultiThreadedAreaAveragingDownscaler;

public class TestImageDownscaling {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		
		runTest(new File("testing-images", "world-map.jpg"), 0.52d);
		System.out.println();
		System.out.println("-------");
		System.out.println();
		runTest(new File("testing-images", "text.jpg"), 0.26d);
		
	}
	
	private static void runTest(final File imageFile, final double scaleFactor) throws IOException, InterruptedException, ExecutionException {
		
		System.out.println("Reading file " + imageFile.getAbsolutePath());
		BufferedImage source = ImageIO.read(imageFile);
		double dimensionFactor = Math.sqrt(scaleFactor);
		int scaledWidth = (int) (source.getWidth() * dimensionFactor);
		int scaledHeight = (int) (source.getHeight() * dimensionFactor);
		
		System.out.println("Scaling from " + source.getWidth() + "x" + source.getHeight() + " to " + scaledWidth + "x" + scaledHeight);
		System.out.println("Loading rgb data");
		int rgb[] = source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth());
		
		System.out.println("Running single-threaded scaling algorithm");
		long timestamp1 = System.currentTimeMillis();
		int[] scaledRgb = AreaAveragingDownscaling.scale(rgb, source.getWidth(), source.getHeight(), scaledWidth, scaledHeight);
		long timestamp2 = System.currentTimeMillis();
		
		File outputFile = new File(imageFile.getParent(), removeExtension(imageFile.getName()) + "-scaled.jpg");
		System.out.println("Outputting resulting image to file " + outputFile.getAbsolutePath());
		outputImage(scaledWidth, scaledHeight, scaledRgb, outputFile);
		
		int numOfThreads = Runtime.getRuntime().availableProcessors();
		System.out.println("Running multi-threaded scaling algorithm with " + numOfThreads + " threads");
		long timestamp3 = System.currentTimeMillis();
		Future<int[]> future = MultiThreadedAreaAveragingDownscaler.scale(rgb, source.getWidth(), source.getHeight(), scaledWidth, scaledHeight);
		scaledRgb = future.get();
		long timestamp4 = System.currentTimeMillis();
		
		outputFile = new File(imageFile.getParent(), removeExtension(imageFile.getName()) + "-mt-scaled.jpg");
		System.out.println("Outputting resulting image to file " + outputFile.getAbsolutePath());
		outputImage(scaledWidth, scaledHeight, scaledRgb, outputFile);
		
		System.out.println();
		System.out.println("Times for " + imageFile.getName());
		System.out.println("Single threaded: " + (timestamp2 - timestamp1) + " ms");
		System.out.println("Multi threaded: " + (timestamp4 - timestamp3) + " ms");
		
	}
	
	private static void outputImage(int width, int height, int[] rgb, File file) throws IOException {
		BufferedImage scaledBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		scaledBufferedImage.setRGB(0, 0, width, height, rgb, 0, width);
		ImageIO.write(scaledBufferedImage, "JPEG", file);
	}
	
	private static String removeExtension(final String extension) {
		int dotIndex = extension.lastIndexOf('.');
		return extension.substring(0, dotIndex);
	}
	
}

package jpiccoli.image;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a multi-threaded version of the area-average downscaling algorithm.
 * 
 * @author Juliano Piccoli
 *
 */
public class MultiThreadedAreaAveragingDownscaler {
	
	private int sourceWidth;
	private int sourceHeight;
	private int scaledWidth;
	private int scaledHeight;
	private int[] rgb;
	private int[] scaledRgb;
	
	private Executor executor;
	private GridMapping[] horizontalGridMappings;
	private AtomicInteger remainingRows;
	private float inverseScaledPixelArea;
	private CompletableFuture<int[]> future;
	
	/**
	 * Constructor.
	 * @param rgb 	Array containing the RGB pixel values of the image formatted as packed 32 bit integers
	 * 				containing RGB values in big-endian order (the most significant 8 bits are ignored).
	 * @param sourceWidth	Width of the original image.
	 * @param sourceHeight	Height of the original image.
	 * @param scaledWidth	Target width for the scaled image.
	 * @param scaledHeight	Target height for the scaled image.
	 * @param executor		The executor on which the workers Runnables are going to be executed.
	 */
	private MultiThreadedAreaAveragingDownscaler(final int[] rgb, final int sourceWidth, final int sourceHeight, final int scaledWidth, final int scaledHeight, final Executor executor) {
		this.rgb = rgb;
		this.sourceWidth = sourceWidth;
		this.sourceHeight = sourceHeight;
		this.scaledWidth = scaledWidth;
		this.scaledHeight = scaledHeight;
		this.executor = executor;
	}
	
	/**
	 * Starts the downscaling process.
	 * @return A CompletableFuture which resolves with the resulting RGB data.
	 */
	private CompletableFuture<int[]> start() {
		this.future = new CompletableFuture<>();
		executor.execute(() -> setup());
		return future;
	}
	
	/**
	 * Executes the initialization of the downscaling process and schedules the workers on the executor.
	 */
	private void setup() {
		float scaledPixelArea = (sourceWidth / (float) scaledWidth) * (sourceHeight / (float) scaledHeight);
		this.scaledRgb = new int[scaledWidth * scaledHeight];
		this.remainingRows = new AtomicInteger(scaledHeight);
		this.inverseScaledPixelArea = 1.0f / scaledPixelArea;
		this.horizontalGridMappings = AreaAveragingDownscaling.createDimensionGridMapping(sourceWidth, scaledWidth, 1, 0, scaledWidth);
		for (int i = 0; i < scaledHeight; i++) {
			final int index = i;
			executor.execute(() -> work(index, index + 1));
		}
	}
	
	/**
	 * Executes the downscaling algorithm for the specified rows of the scaled image.
	 * @param y1 First row index (inclusive).
	 * @param y2 Last row index (exclusive).
	 */
	private void work(int y1, int y2) {
		GridMapping[] verticalGridMappings = AreaAveragingDownscaling.createDimensionGridMapping(sourceHeight, scaledHeight, sourceWidth, y1, y2);
		for (int y = y1; y < y2; y++) {
			int offset = y * horizontalGridMappings.length;
			for (int x = 0; x < horizontalGridMappings.length; x++) {
				scaledRgb[offset + x] = AreaAveragingDownscaling.calcAveragePixelColor(rgb, horizontalGridMappings[x].pixelMappingArray, verticalGridMappings[y - y1].pixelMappingArray, inverseScaledPixelArea);
			}
		}
		long activeThreads = remainingRows.decrementAndGet();
		if (activeThreads == 0) {
			future.complete(scaledRgb);
		}
	}

	/**
	 * Scales down the specified image. This version of the scale method will execute the downscaling process
	 * using a fixed thread pool containing one thread per CPU core.
	 * @see MultiThreadedAreaAveragingDownscaler.scale
	 */
	public static CompletableFuture<int[]> scale(final int[] rgb, int sourceWidth, int sourceHeight, int scaledWidth, int scaledHeight) {
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		CompletableFuture<int[]> future = scale(rgb, sourceWidth, sourceHeight, scaledWidth, scaledHeight, executorService);
		return future.thenApply((value) -> {
			executorService.shutdownNow();
			return value;
		});
	}
	
	/**
	 * Scales down the specified image.
	 * @param rgb 	Array containing the RGB pixel values of the image formatted as packed 32 bit integers
	 * 				containing RGB values in big-endian order (the most significant 8 bits are ignored).
	 * @param sourceWidth	Width of the original image.
	 * @param sourceHeight	Height of the original image.
	 * @param scaledWidth	Target width for the scaled image.
	 * @param scaledHeight	Target height for the scaled image.
	 * @param executor		The executor on which the multi-threaded downscaling algorithm will be executed.
	 * @return The RGB array containing the resulting pixels of the scaled image formatted as packed 32 bit integers
	 * 	containing RGB values in big-endian order (the most significant 8 bits of each integer should be ignored).
	 */
	public static CompletableFuture<int[]> scale(final int[] rgb, int sourceWidth, int sourceHeight, int scaledWidth, int scaledHeight, Executor executor) {
		MultiThreadedAreaAveragingDownscaler scaler = new MultiThreadedAreaAveragingDownscaler(rgb, sourceWidth, sourceHeight, scaledWidth, scaledHeight, executor);
		return scaler.start();
	}
	
}

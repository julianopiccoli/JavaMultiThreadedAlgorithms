package jpiccoli.image;

/**
 * Provides an algorithm for downscaling images using the area-average approach.
 * 
 * @author Juliano Piccoli
 *
 */
public class AreaAveragingDownscaling {
	
	/*
	 * This class provides only static methods and is not intended for being instantiated,
	 * so we declare a single private constructor for preventing client code to calling it.
	 */
	private AreaAveragingDownscaling() {
		//
	}
	
	/**
	 * Scales down the specified image.
	 * @param rgb 	Array containing the RGB pixel values of the image formatted as packed 32 bit integers
	 * 				containing RGB values in big-endian order (the most significant 8 bits are ignored).
	 * @param sourceWidth	Width of the original image.
	 * @param sourceHeight	Height of the original image.
	 * @param scaledWidth	Target width for the scaled image.
	 * @param scaledHeight	Target height for the scaled image.
	 * @return The RGB array containing the resulting pixels of the scaled image formatted as packed 32 bit integers
	 * 	containing RGB values in big-endian order (the most significant 8 bits of each integer should be ignored).
	 */
	public static int[] scale(final int[] rgb, int sourceWidth, int sourceHeight, int scaledWidth, int scaledHeight) {
		float scaledPixelArea = (sourceWidth / (float) scaledWidth) * (sourceHeight / (float) scaledHeight);
		float inverseScaledPixelArea = 1.0f / scaledPixelArea;
		GridMapping[] verticalGridMappings = createDimensionGridMapping(sourceHeight, scaledHeight, sourceWidth);
		GridMapping[] horizontalGridMappings = createDimensionGridMapping(sourceWidth, scaledWidth, 1);
		int[] scaledRgb = new int[scaledWidth * scaledHeight];
		for (int y = 0; y < verticalGridMappings.length; y++) {
			int offset = y * scaledWidth;
			for (int x = 0; x < horizontalGridMappings.length; x++) {
				scaledRgb[offset + x] = calcAveragePixelColor(rgb, verticalGridMappings[y].pixelMappingArray, horizontalGridMappings[x].pixelMappingArray, inverseScaledPixelArea);
			}
		}
		return scaledRgb;
	}
	
	/**
	 * Calculates the weighted average value of the specified group of pixels.
	 * @param rgb Array containing the RGB pixel values of the source image
	 * @param verticalPixelMappings Sequence of rows of the original image which should be considered for the average value calculation.
	 * @param horizontalPixelMappings Sequence of columns of the original image which should be considered for the average value calculation.
	 * @param inverseScaledPixelArea The inverse of the "area" of each pixel of the scaled image relative to the original image pixels.
	 * @return The weighted average pixel value of the specified area of the source image.
	 */
	static int calcAveragePixelColor(final int[] rgb, final PixelMapping[] verticalPixelMappings, final PixelMapping[] horizontalPixelMappings, float inverseScaledPixelArea) {
		Color colorSum = new Color();
		Color pixelColor = new Color();
		for (int y = 0; y < verticalPixelMappings.length; y++) {
			for (int x = 0; x < horizontalPixelMappings.length; x++) {
				pixelColor.importFromRgb(rgb[verticalPixelMappings[y].position + horizontalPixelMappings[x].position]);
				pixelColor.multiply(verticalPixelMappings[y].multiplier * horizontalPixelMappings[x].multiplier);
				colorSum.sum(pixelColor);
			}
		}
		colorSum.multiply(inverseScaledPixelArea);
		return colorSum.toRGB();
	}
	
	/**
	 * Calculates a grid mapping for one of the image's dimensions.
	 * @see AreaAveragingDownscaling.createDimensionGridMapping
	 */
	static GridMapping[] createDimensionGridMapping(final int originalImageDimension, final int scaledImageDimension, final int positionIncrement) {
		return createDimensionGridMapping(originalImageDimension, scaledImageDimension, positionIncrement, 0, scaledImageDimension);
	}
	
	/**
	 * Calculates the position mapping between one of the dimensions of the scaled version of the image to the original image.
	 * This is done by attributing a "length" to each pixel of the scaled image expressed as pixel units of the source image.
	 * For example, when scaling an image which is 800 pixels wide to a target width of 400 pixels, each scaled pixel width would
	 * correspond to a "length" of 2 pixels in the original image. This is used to define which pixels of the original image should
	 * be used when calculating the average for composing the scaled image. It also attributes a "weight" to each mapping according
	 * to the intersection between the scaled image pixel area to the original image pixels.
	 * @param originalImageDimension Dimension (width or height) of the source image.
	 * @param scaledImageDimension Corresponding dimension (width or height) of the scaled image.
	 * @param positionIncrement Should be equal to "1" when calculating the width grid mapping and to the original image width when calculating the height grid mapping.
	 * @param startIndex Defines the first (including) grid mapping to generate (first line or first column depending on the dimension being calculated).
	 * @param endIndex Defines the last (excluding) grid mapping (last line or last column depending on the dimension being calculated).
	 * @return The resulting calculated grid mapping array.
	 */
	static GridMapping[] createDimensionGridMapping(final int originalImageDimension, final int scaledImageDimension, final int positionIncrement, final int startIndex, final int endIndex) {
		int length = endIndex - startIndex;
		final GridMapping[] gridMappings = new GridMapping[length];
		float ratio = originalImageDimension / (float) scaledImageDimension;
		float position = ratio * startIndex;
		for (int index = 0; index < length; index++, position += ratio) {
			float nextPosition = position + ratio;
			int start = (int) position;
			int end = (int) Math.min(Math.ceil(nextPosition), originalImageDimension);
			PixelMapping[] pixelMapping = new PixelMapping[end - start];
			pixelMapping[0] = new PixelMapping(positionIncrement * start, 1.0f - (position - start));
			for (int pixelMappingIndex = 1; pixelMappingIndex < pixelMapping.length - 1; pixelMappingIndex++) {
				pixelMapping[pixelMappingIndex] = new PixelMapping(positionIncrement * (start + pixelMappingIndex), 1.0f);
			}
			if (pixelMapping.length > 1) {
				pixelMapping[pixelMapping.length - 1] = new PixelMapping(positionIncrement * (start + pixelMapping.length - 1), 1.0f - (end - nextPosition));
			}
			gridMappings[index] = new GridMapping(pixelMapping);
		}
		return gridMappings;
	}
	
}
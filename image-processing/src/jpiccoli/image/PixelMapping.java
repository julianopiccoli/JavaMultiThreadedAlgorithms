package jpiccoli.image;

/**
 * Utility class which provides information for mapping each pixel of the source image
 * to a pixel of the scaled image, along with its "weight" information.
 * 
 * @author Juliano Piccoli
 *
 */
class PixelMapping {
	
	int position;
	float multiplier;
	
	PixelMapping(final int position, final float multiplier) {
		this.position = position;
		this.multiplier = multiplier;
	}

}

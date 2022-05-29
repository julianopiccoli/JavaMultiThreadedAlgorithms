package jpiccoli.image;

/**
 * Utility class for holding all the pixel mappings for a specific dimension of the image (width or height).
 * 
 * @author Juliano Piccoli
 *
 */
class GridMapping {
	
	PixelMapping[] pixelMappingArray;
	
	GridMapping(final PixelMapping[] pixelMappingArray) {
		this.pixelMappingArray = pixelMappingArray;
	}

}

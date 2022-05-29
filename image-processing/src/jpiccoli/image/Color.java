package jpiccoli.image;

/**
 * Auxiliary class providing utility functions for dealing with pixel's colors.
 * 
 * @author Juliano Piccoli
 *
 */
class Color {

	float red;
	float green;
	float blue;
	
	/**
	 * Imports data from the specified pixel RGB value (represented as a packed 32 bits integer
	 * containing RGB data in big-endian order).
	 * @param rgb The pixel color value.
	 */
	void importFromRgb(int rgb) {
		this.red = (rgb & 0xFF0000) >> 16;
		this.green = (rgb & 0xFF00) >> 8;
		this.blue = rgb & 0xFF;
	}
	
	/**
	 * Multiply this color by the specified multiplier.
	 * @param multiplier Multiplication factor to apply to the color.
	 */
	void multiply(final float multiplier) {
		this.red *= multiplier;
		this.green *= multiplier;
		this.blue *= multiplier;
	}
	
	/**
	 * Sum this color components to the specified color's components.
	 * @param other Color to sum.
	 */
	void sum(final Color other) {
		this.red += other.red;
		this.green += other.green;
		this.blue += other.blue;
	}
	
	/**
	 * Convert this color's components values to a packed 32 bits integer RGB value.
	 * @return The packed 32 bits integer RGB value correspondent to this color.
	 */
	int toRGB() {
		int integerRed = (int) Math.round(red);
		int integerGreen = (int) Math.round(green);
		int integerBlue = (int) Math.round(blue);
		return integerRed << 16 | integerGreen << 8 | integerBlue;
	}
	
}

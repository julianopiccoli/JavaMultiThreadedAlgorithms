package jpiccoli.mt.sort;

/**
 * Interface for sorting algorithms.
 * 
 * @author Juliano Piccoli
 *
 * @param <T> The type of element that this algorithm is able to sort
 */
@FunctionalInterface
public interface SortingAlgorithm<T> {
	
	/**
	 * Sorts the contents of the specified interval of the 'elements' array.
	 * @param elements Array containing the elements to be sorted
	 * @param position Index of the first element of the source array to be sorted
	 * @param length Length of the elements of the source array to be sorted
	 */
	public void sort(final T[] elements, final int position, final int length);
	
	/**
	 * Sorts the full contents of the specified array.
	 * @param source Array containing the elements to be sorted
	 */
	default void sort(final T[] elements) {
		sort(elements, 0, elements.length);
	}

}

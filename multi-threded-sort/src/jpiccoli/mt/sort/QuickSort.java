package jpiccoli.mt.sort;

import java.util.Comparator;

/**
 * Implementation of the quick sort algorithm.
 *
 * @author Juliano Piccoli
 *
 */
public class QuickSort<T> extends AbstractSortingAlgorithm<T> {
	
	public QuickSort(final Comparator<T> comparator) {
		super(comparator);
	}

	@Override
	public void sort(final T[] elements, final int position, final int length) {
		int pivotPosition = partition(elements, position, length);
		int lowerHalfLength = pivotPosition - position;
		int higherHalfLength = position + length - pivotPosition - 1;
		if (lowerHalfLength > 0) {
			sort(elements, position, lowerHalfLength);
		}
		if (higherHalfLength > 0) {
			sort(elements, pivotPosition + 1, higherHalfLength);
		}
	}
	
	/**
	 * Selects the first element as pivot and split the array region into partitions according
	 * to this pivot.
	 * 
	 * @param elements Array containing the elements to be sorted.
	 * @param position Index of the first element to be sorted.
	 * @param length Number of elements that should be sorted.
	 * @return The final position of the pivot.
	 */
	private int partition(final T[] elements, final int position, final int length) {
		T pivot = elements[position];
		int lowerIndex = position;
		int higherIndex = position + length - 1;
		while(lowerIndex < higherIndex) {
			for (; higherIndex > lowerIndex; higherIndex--) {
				T element = elements[higherIndex];
				if (comparator.compare(pivot, element) > 0) {
					elements[higherIndex] = pivot;
					elements[lowerIndex] = element;
					lowerIndex++;
					break;
				}
			}
			for (; lowerIndex < higherIndex; lowerIndex++) {
				T element = elements[lowerIndex];
				if (comparator.compare(pivot, element) < 0) {
					elements[lowerIndex] = pivot;
					elements[higherIndex] = element;
					higherIndex--;
					break;
				}
			}
		}
		return lowerIndex;
	}
	
}

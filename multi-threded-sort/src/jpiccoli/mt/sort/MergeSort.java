package jpiccoli.mt.sort;

import java.util.Comparator;

/**
 * Implementation of the bottom-up merge sort algorithm.
 * 
 * @author Juliano Piccoli
 */
public class MergeSort<T> extends AbstractSortingAlgorithm<T> {
	
	public MergeSort(final Comparator<T> comparator) {
		super(comparator);
	}
	
	/**
	 * Merge the contents of the two ordered sub-arrays contained in the source argument and stores
	 * the resulting sorted array in the destination argument.
	 * 
	 * @param source Array containing the two source sorted arrays to be merged
	 * @param destination Array where the merged contents of the two source arrays will be stored
	 * @param position Index in the source array where the two sorted sub-arrays start.
	 * @param length1 Length of the first sub-array
	 * @param length2 length of the second sub-array
	 */
	void merge(final T[] source, final T[] destination, final int position, int length1, final int length2) {
		
		final int indexLimit1 = position + length1;
		final int indexLimit2 = indexLimit1 + length2;
		int sourceIndex1 = position;
		int sourceIndex2 = indexLimit1;
		int destinationIndex = position;
		
		while(sourceIndex1 < indexLimit1 && sourceIndex2 < indexLimit2) {
			if (comparator.compare(source[sourceIndex1], source[sourceIndex2]) > 0) {
				destination[destinationIndex] = source[sourceIndex2];
				sourceIndex2++;
			} else {
				destination[destinationIndex] = source[sourceIndex1];
				sourceIndex1++;
			}
			destinationIndex++;
		}
		while(sourceIndex1 < indexLimit1) {
			destination[destinationIndex] = source[sourceIndex1];
			sourceIndex1++;
			destinationIndex++;
		}
		while(sourceIndex2 < indexLimit2) {
			destination[destinationIndex] = source[sourceIndex2];
			sourceIndex2++;
			destinationIndex++;
		}
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void sort(final T[] elements, final int position, final int length) {

		T[] source = elements;
		T[] aux = (T[]) new Object[length];
		
		// Avoiding an arraycopy when the full array is being sorted.
		if (position != 0 || length != elements.length) {
			source = (T[]) new Object[length];
			System.arraycopy(elements, position, source, 0, length);
		}
		
		int arrayLength = 1;
		int segmentLength = 2;
		while(segmentLength <= length) {
			int index = 0;
			int nextIndex = index + segmentLength;
			for (; nextIndex <= length; index += segmentLength, nextIndex += segmentLength) {
				merge(source, aux, index, arrayLength, arrayLength);
			}
			// When the length parameter is not a power of two, there will be a number
			// of remaining elements at the end of the sorting interval that is less than
			// a segment length and thus cannot be directly merged. In this case, we merge them with the last sorted segment.
			int remaining = length - index;
			if (remaining > 0) {
				int lastSortedSegmentIndex = index - segmentLength;
				System.arraycopy(aux, lastSortedSegmentIndex, source, lastSortedSegmentIndex, segmentLength);
				merge(source, aux, lastSortedSegmentIndex, segmentLength, remaining);
			}
			T[] temp = source;
			source = aux;
			aux = temp;
			arrayLength *= 2;
			segmentLength = arrayLength * 2;
		}
		
		if (source != elements) {
			System.arraycopy(source, 0, elements, position, source.length);
		}
		
	}

}

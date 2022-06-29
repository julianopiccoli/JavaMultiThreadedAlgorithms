package jpiccoli.mt.sort;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Implementation of SortingAlgorithm that uses the Java default Arrays.sort method.
 * 
 * @author Juliano Piccoli
 * 
 */
public class DefaultArraySort<T> extends AbstractSortingAlgorithm<T> {

	public DefaultArraySort(final Comparator<T> comparator) {
		super(comparator);
	}

	@Override
	public void sort(final T[] elements, final int position, final int length) {
		Arrays.sort(elements, position, position + length, comparator);
	}
	
}

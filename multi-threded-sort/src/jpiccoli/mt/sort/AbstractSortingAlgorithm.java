package jpiccoli.mt.sort;

import java.util.Comparator;

/**
 * Base class for sorting algorithms that rely on a {@link Comparator} for
 * sorting array elements.
 * 
 * @author Juliano Piccoli
 */
public abstract class AbstractSortingAlgorithm<T> implements SortingAlgorithm<T> {
	
	protected Comparator<T> comparator;
	
	public AbstractSortingAlgorithm(final Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
	/**
	 * Retrieves the comparator used by the algorithm to define the ordering relation
	 * while sorting.
	 * @return The comparator.
	 */
	protected Comparator<T> getComparator() {
		return comparator;
	}

}

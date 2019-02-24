package jpiccoli.mt.sort;

/**
 * MultiThreaded bottom-up merge sort implementation.
 * @author Juliano Piccoli
 *
 */
public class MTMergeSort {

	/**
	 * Merge the contents of the two ordered sub-arrays contained in the source argument and stores
	 * the resulting sorted array in the destination argument.
	 * @param source Array containing the two source sorted arrays to be merged
	 * @param destination Array where the merged contents of the two source arrays will be stored
	 * @param position Index in the source array where the two sorted sub-arrays start.
	 * @param length1 Length of the first sub-array
	 * @param length2 length of the second sub-array
	 */
	private static <T extends Comparable<T>> void merge(final T[] source, final T[] destination, final int position, int length1, final int length2) {
		
		final int indexLimit1 = position + length1;
		final int indexLimit2 = indexLimit1 + length2;
		int sourceIndex1 = position;
		int sourceIndex2 = indexLimit1;
		int destinationIndex = position;
		
		while(sourceIndex1 < indexLimit1 && sourceIndex2 < indexLimit2) {
			if (source[sourceIndex1].compareTo(source[sourceIndex2]) > 0) {
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
	
	/**
	 * Sorts the contents of the specified interval of the source array using the merge sort algorithm.
	 * The returned array will contain the resulting sorted elements.
	 * This method can change the order of the elements in the source array.
	 * The returned array may be the source array itself (with the contents
	 * arranged in ascending order) or the aux array (also the the source contents
	 * arranged in ascending order).
	 * @param source Array containing the elements to be sorted in ascending order
	 * @param aux Array used to store elements during the algorithm execution (must have the same length as the source array)
	 * @param position Index of the first element of the source array to be sorted
	 * @param length Length of the elements of the source array to be sorted
	 * @return The resulting sorted array
	 */
	public static <T extends Comparable<T>> T[] sort(T[] source, T[] aux, final int position, final int length) {

		final int limit = position + length;
		int arrayLength = 1;
		int segmentLength = 2;
		while(segmentLength <= length) {
			int index = position;
			int nextIndex = index + segmentLength;
			for (; nextIndex <= limit; index += segmentLength, nextIndex += segmentLength) {
				merge(source, aux, index, arrayLength, arrayLength);
			}
			// When the length parameter is not a power of two, there will be a number
			// of remaining elements at the end of the sorting interval that is less than
			// a segment length and thus cannot be directly merged. In this case, we merge them with the last sorted segment.
			int remaining = limit - index;
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
		return source;
		
	}
	
	/**
	 * Sorts the full contents of the source array using the merge sort algorithm.
	 * This method behaves almost exactly the same as the sort method with four parameters,
	 * except for the fact that it sorts the source array fully.
	 * @param source Array containing the elements to be sorted in ascending order
	 * @param aux Array used to store elements during the algorithm execution (must have the same length as the source array)
	 * @return The resulting sorted array
	 */
	public static <T extends Comparable<T>> T[] sort(T[] source, T[] aux) {
		return sort(source, aux, 0, source.length);
	}
	
	/**
	 * Sorts the contents of the source array using a multi-threaded version of the bottom-up
	 * merge sort algorithm. The elements of the source array must implement the Comparable interface
	 * and will be sorted in ascending order.
	 * @param source Array containing the elements to be sorted in ascending order
	 * @param threads Number of threads which will execute the algorithm.
	 */
	public static void mtSort(final Object[] source, int threads) {
		
		final Comparable[] aux = new Comparable[source.length];
		final Object[][] sortOutput = new Object[1][];
		int position = 0;
		
		// The source array is divided in partitions. The partitions will be ordered in parallel by multiple threads
		// using the bottom-up merge-sort algorithm.
		int partitionLength = source.length / threads;
		Thread[] t = new Thread[threads];
		for (int i = 0; i < t.length - 1; i++) {
			final int threadPartitionLength = partitionLength;
			final int threadPartitionPosition = position;
			t[i] = new Thread() {
				public void run() {
					sortOutput[0] = sort((Comparable[]) source, aux, threadPartitionPosition, threadPartitionLength);
				};
			};
			position += partitionLength;
		}

		// If the source array length is not exactly divisible by the number of threads,
		// the last partition will include the remaining items of the array.
		int lastPartitionLength = source.length - position;
		
		{
			final int threadPartitionLength = lastPartitionLength;
			final int threadPartitionPosition = position;
			t[t.length - 1] = new Thread() {
				public void run() {
					sortOutput[0] = sort((Comparable[]) source, aux, threadPartitionPosition, threadPartitionLength);
				};
			};
		}
		
		for (int i = 0; i < t.length; i++) {
			t[i].start();
		}
		
		// Await for the threads to complete the sorting procedure of each partition.
		for (int i = 0; i < t.length; i++) {
			try {
				t[i].join();
			} catch (InterruptedException e) {}
		}
		
		// The resulting partitions must be merged. Each pair of partitions can be merged by a single
		// thread. So, this procedure can be done using half the initial number of threads.
		// We keep doing this merge procedure until there is only one partition (the fully sorted array).
		// In each iteration, the number of threads is halved.
		
		Object[] array1 = sortOutput[0] == source ? source : aux;
		Object[] array2 = sortOutput[0] == source ? aux : source;
		
		while(threads > 1) {
			threads = threads / 2;
			position = 0;
			final Object[] localArray1 = array1;
			final Object[] localArray2 = array2;
			t = new Thread[threads];
			for (int i = 0; i < t.length - 1; i++) {
				final int threadPartitionLength = partitionLength;
				final int threadPartitionPosition = position;
				t[i] = new Thread() {
					public void run() {
						merge((Comparable[]) localArray1, (Comparable[]) localArray2, threadPartitionPosition, threadPartitionLength, threadPartitionLength);
					};
				};
				position += partitionLength * 2;
			}
			
			final int threadPartitionLength = partitionLength;
			final int threadLastPartitionLength = lastPartitionLength;
			final int threadPartitionPosition = position;
			final int lastTwoPartitionsIndex = source.length - lastPartitionLength - partitionLength;
			t[t.length - 1] = new Thread() {
				public void run() {
					// When the number of threads is not a power of two, the algorithm will eventually
					// end up with an odd number of partitions. In this case, the last thread will
					// execute two merge operations. The first one will merge the n-3 and n-2 partitions.
					// The second will merge the last (n-1) partition with the partition resulting from the first merge.
					if (lastTwoPartitionsIndex > threadPartitionPosition) {
						merge((Comparable[]) localArray1, (Comparable[]) localArray2, threadPartitionPosition, threadPartitionLength, threadPartitionLength);
						System.arraycopy(localArray2, threadPartitionPosition, localArray1, threadPartitionPosition, threadPartitionLength * 2);
						merge((Comparable[]) localArray1, (Comparable[]) localArray2, threadPartitionPosition, threadPartitionLength * 2, threadLastPartitionLength);
					} else {
						merge((Comparable[]) localArray1, (Comparable[]) localArray2, threadPartitionPosition, threadPartitionLength, threadLastPartitionLength);
					}
				};
			};
			
			for (int i = 0; i < t.length; i++) {
				t[i].start();
			}
			
			for (int i = 0; i < t.length; i++) {
				try {
					t[i].join();
				} catch (InterruptedException e) {}
			}
			
			if (lastTwoPartitionsIndex > position) {
				lastPartitionLength += partitionLength * 2;
			} else {
				lastPartitionLength += partitionLength;
			}
			partitionLength *= 2;
			
			// The algorithm change the roles of the two arrays at each iteration.
			// In the first step, it stores in the aux array the results of the merges
			// of the partitions contained in the source array. In the next iteration,
			// it stores in the source array the results of the merges of the partitions
			// contained in the aux array. This keeps going until the end of the algorithm.
			Object[] temp = array1;
			array1 = array2;
			array2 = temp;

		}
		
		// If the results are stored in the aux array, the contents are copied to the source array.
		if (array1 != source) {
			System.arraycopy(array1, 0, source, 0, array1.length);
		}
		
	}

}

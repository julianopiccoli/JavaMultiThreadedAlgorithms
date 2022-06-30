package jpiccoli.mt.sort;

import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * Sorting algorithm capable of using multiple threads to execute the sorting
 * procedure. Relies on a provided sorting algorithm for executing the job.
 *
 * @author Juliano Piccoli
 */
public class MultiThreadedSort<T> extends AbstractSortingAlgorithm<T> {

    private final SortingAlgorithm<T> sortingAlgorithm;
    private final Executor executor;

    public MultiThreadedSort(final AbstractSortingAlgorithm<T> sortingAlgorithm, final Executor executor) {
        this(sortingAlgorithm, sortingAlgorithm.getComparator(), executor);
    }

    public MultiThreadedSort(final SortingAlgorithm<T> sortingAlgorithm, final Comparator<T> comparator, final Executor executor) {
        super(comparator);
        this.sortingAlgorithm = sortingAlgorithm;
        this.executor = executor;
    }

    /**
     * Sorts the specified array using a number of slices that is equal to the number
     * of logical processing cores available to the JVM.
     *
     * @see {@link #sort(Object[], int, int, int)}
     */
    public void sort(final T[] source, final int position, final int length) {
        sort(source, position, length, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Sorts the specified array region using multiple threads. The array is divided into
     * the provided number of slices and each slice is sorted independently using the
     * underlying sorting algorithm provided in the constructor. The ordered slices are
     * then merged using the same approach as the merge-sort algorithm.
     *
     * @param source   Array containing the elements to be sorted.
     * @param position Index of the first element to be sorted.
     * @param length   Number of elements to be sorted.
     * @param slices   Number of slices in which the array will be divided.
     * @see {@link MergeSort#merge(Object[], Object[], int, int, int)}
     */
    public void sort(final T[] source, final int position, final int length, final int slices) {
        // The source array is divided in partitions. The partitions will be ordered in parallel by multiple threads.
        final int partitionLength = length / slices;
        CountDownLatch countDownLatch = new CountDownLatch(slices);
        for (int sliceIndex = 0; sliceIndex < slices - 1; sliceIndex++) {
            final int slicePosition = position + sliceIndex * partitionLength;
            executor.execute(new RunnableWrapper(countDownLatch, () ->
                    sortingAlgorithm.sort(source, slicePosition, partitionLength)
            ));
        }
        // The last partition will include the remaining items of the array.
        // This is necessary because the last partition will contain a different number of items
        // when the total length of the source array is not exactly divisible by the number of slices.
        if (slices > 1) {
            final int lastSlicePosition = position + (slices - 1) * partitionLength;
            executor.execute(new RunnableWrapper(countDownLatch, () ->
                    sortingAlgorithm.sort(source, lastSlicePosition, source.length - lastSlicePosition)
            ));
        }

        try {

            countDownLatch.await();

            mergeSlices(source, position, length, slices);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SortingException(e);
        }

    }

    @SuppressWarnings("unchecked")
    private void mergeSlices(final T[] elements, final int position, final int length, final int slices) throws InterruptedException {

        final MergeSort<T> mergeSort = new MergeSort<>(comparator);

        T[] source = elements;
        T[] auxiliary = (T[]) new Object[length];

        // Avoiding an array copy when the full array is being sorted.
        if (position != 0 || length != elements.length) {
            source = (T[]) new Object[length];
            System.arraycopy(elements, position, source, 0, length);
        }

        // The resulting partitions must be merged. Each pair of partitions can be merged by a single
        // thread. So, this procedure can be done using half the initial number of threads.
        // We keep doing this merge procedure until there is only one partition (the fully sorted array).
        // In each iteration, the number of threads is halved.

        int tasks = slices;
        int partitionLength = elements.length / tasks;
        int lastPartitionLength = elements.length - (tasks - 1) * partitionLength;
        while (tasks > 1) {
            tasks = tasks / 2;
            int localPosition = 0;
            final T[] localArray1 = source;
            final T[] localArray2 = auxiliary;
            final CountDownLatch countDownLatch = new CountDownLatch(tasks);
            for (int taskIndex = 0; taskIndex < tasks - 1; taskIndex++) {
                final int threadPartitionLength = partitionLength;
                final int threadPartitionPosition = localPosition;
                executor.execute(new RunnableWrapper(countDownLatch, () -> mergeSort.merge(localArray1, localArray2, threadPartitionPosition, threadPartitionLength, threadPartitionLength)));
                localPosition += partitionLength * 2;
            }

            final int threadPartitionLength = partitionLength;
            final int threadLastPartitionLength = lastPartitionLength;
            final int threadPartitionPosition = localPosition;
            final int lastTwoPartitionsIndex = source.length - lastPartitionLength - partitionLength;
            // When the number of threads is not a power of two, the algorithm will eventually
            // end up with an odd number of partitions. In this case, the last thread will
            // execute two merge operations. The first one will merge the n-3 and n-2 partitions.
            // The second will merge the last (n-1) partition with the partition resulting from the first merge.
            if (lastTwoPartitionsIndex > threadPartitionPosition) {
                executor.execute(new RunnableWrapper(countDownLatch, () -> {
                    mergeSort.merge(localArray1, localArray2, threadPartitionPosition, threadPartitionLength, threadPartitionLength);
                    System.arraycopy(localArray2, threadPartitionPosition, localArray1, threadPartitionPosition, threadPartitionLength * 2);
                    mergeSort.merge(localArray1, localArray2, threadPartitionPosition, threadPartitionLength * 2, threadLastPartitionLength);
                }));
                lastPartitionLength += partitionLength * 2;
            } else {
                executor.execute(new RunnableWrapper(countDownLatch, () -> mergeSort.merge(localArray1, localArray2, threadPartitionPosition, threadPartitionLength, threadLastPartitionLength)));
                lastPartitionLength += partitionLength;
            }

            countDownLatch.await();

            partitionLength *= 2;

            // The algorithm change the roles of the two arrays at each iteration.
            // In the first step, it stores in the aux array the results of the merges
            // of the partitions contained in the source array. In the next iteration,
            // it stores in the source array the results of the merges of the partitions
            // contained in the aux array. This keeps going until the end of the algorithm.
            T[] temp = source;
            source = auxiliary;
            auxiliary = temp;

        }

        if (source != elements) {
            System.arraycopy(source, 0, elements, position, source.length);
        }

    }

    /**
     * Runnable that decrement a CountDownLatch count by one when the underlying
     * runnable finishes.
     */
    private static class RunnableWrapper implements Runnable {

        private final CountDownLatch countDownLatch;
        private final Runnable runnable;

        private RunnableWrapper(final CountDownLatch countDownLatch, final Runnable runnable) {
            this.countDownLatch = countDownLatch;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } finally {
                countDownLatch.countDown();
            }
        }

    }

}

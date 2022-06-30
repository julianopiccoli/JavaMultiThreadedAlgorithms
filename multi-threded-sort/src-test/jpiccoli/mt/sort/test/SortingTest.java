package jpiccoli.mt.sort.test;

import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jpiccoli.mt.sort.DefaultArraySort;
import jpiccoli.mt.sort.MergeSort;
import jpiccoli.mt.sort.MultiThreadedSort;
import jpiccoli.mt.sort.QuickSort;
import jpiccoli.mt.sort.SortingAlgorithm;

public class SortingTest {

    private static final int THREADS_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int ARRAY_SIZE = 16111117;

    private static void testSortingAlgorithm(final SortingAlgorithm<Integer> sortingAlgorithm) {
        System.out.println("Filling integer array with random values");
        Integer[] source = new Integer[ARRAY_SIZE];
        final Random random = new Random();
        for (int i = 0; i < source.length; i++) {
            source[i] = random.nextInt();
        }
        System.out.println("Sorting");
        long startTimestamp = System.currentTimeMillis();
        sortingAlgorithm.sort(source);
        long endTimestamp = System.currentTimeMillis();
        System.out.println("Finished");
        System.out.println("Ellapsed time: " + (endTimestamp - startTimestamp));
        System.out.println("Verifying");
        for (int i = 1; i < source.length; i++) {
            if (source[i].compareTo(source[i - 1]) < 0) {
                System.out.println(">>> Incorrect order!");
                break;
            }
        }
        System.out.println("Array verified");
    }

    public static void main(String[] args) {

        System.out.println("---- Single threaded merge sort ----");
        testSortingAlgorithm(new MergeSort<Integer>(Comparator.naturalOrder()));
        System.out.println();

        System.out.println("---- Single threaded Arrays.sort ----");
        testSortingAlgorithm(new DefaultArraySort<Integer>(Comparator.naturalOrder()));
        System.out.println();

        System.out.println("---- Single threaded quick sort ----");
        testSortingAlgorithm(new QuickSort<Integer>(Comparator.naturalOrder()));
        System.out.println();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS_COUNT);

        System.out.println("---- Multi threaded merge sort ----");
        testSortingAlgorithm(new MultiThreadedSort<>(new MergeSort<Integer>(Comparator.naturalOrder()), executor));
        System.out.println();

        System.out.println("---- Multi threaded Arrays.sort ----");
        testSortingAlgorithm(new MultiThreadedSort<>(new DefaultArraySort<Integer>(Comparator.naturalOrder()), executor));
        System.out.println();

        System.out.println("---- Multi threaded quick sort ----");
        testSortingAlgorithm(new MultiThreadedSort<>(new QuickSort<Integer>(Comparator.naturalOrder()), executor));
        System.out.println();

        executor.shutdownNow();

    }

}

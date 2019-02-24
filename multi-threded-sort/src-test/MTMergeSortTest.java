import java.util.Arrays;
import java.util.Random;

import jpiccoli.mt.sort.MTMergeSort;

public class MTMergeSortTest {

	private static int ARRAY_SIZE = 16777216;
	
	public static void mergeSort() {
		
		System.out.println("Filling integer array with random values");
		Integer[] source = new Integer[ARRAY_SIZE];
		Integer[] destination = new Integer[ARRAY_SIZE];
		final Random random = new Random();
		for (int i = 0; i < source.length; i++) {
			source[i] = new Integer(random.nextInt());
		}
		System.out.println("Sorting");
		long startTimestamp = System.currentTimeMillis();
		destination = MTMergeSort.sort(source, destination);
		long endTimestamp = System.currentTimeMillis();
		System.out.println("Finished");
		System.out.println("Ellapsed time: " + (endTimestamp - startTimestamp));
		System.out.println("Verifying");
		for (int i = 1; i < destination.length; i++) {
			if (destination[i].compareTo(destination[i - 1]) < 0) {
				System.out.println("Incorrect order!");
				break;
			}
		}
		System.out.println("Array verified");
	}
	
	public static void defaultArraysSort() {
		
		System.out.println("Filling integer array with random values");
		Integer[] source = new Integer[ARRAY_SIZE];
		final Random random = new Random();
		for (int i = 0; i < source.length; i++) {
			source[i] = new Integer(random.nextInt());
		}
		System.out.println("Sorting");
		long startTimestamp = System.currentTimeMillis();
		Arrays.sort(source);
		long endTimestamp = System.currentTimeMillis();
		System.out.println("Finished");
		System.out.println("Ellapsed time: " + (endTimestamp - startTimestamp));
		System.out.println("Verifying");
		for (int i = 1; i < source.length; i++) {
			if (source[i].compareTo(source[i - 1]) < 0) {
				System.out.println("Incorrect order!");
				break;
			}
		}
		System.out.println("Array verified");
		
	}

	public static void multiThreadedMergeSort() {
		
		System.out.println("Filling integer array with random values");
		Integer[] source = new Integer[ARRAY_SIZE];
		final Random random = new Random();
		for (int i = 0; i < source.length; i++) {
			source[i] = new Integer(random.nextInt());
		}
		System.out.println("Sorting");
		long startTimestamp = System.currentTimeMillis();
		MTMergeSort.mtSort(source, Runtime.getRuntime().availableProcessors());
		long endTimestamp = System.currentTimeMillis();
		System.out.println("Finished");
		System.out.println("Ellapsed time: " + (endTimestamp - startTimestamp));
		System.out.println("Verifying");
		for (int i = 1; i < source.length; i++) {
			if (source[i].compareTo(source[i - 1]) < 0) {
				System.out.println("Incorrect order!");
				break;
			}
		}
		System.out.println("Array verified");		
	
	}
	
	public static void main(String[] args) {
		
		System.out.println("---- Single threaded merge sort ----");
		mergeSort();
		System.out.println();
		System.out.println("---- Arrays.sort ----");
		defaultArraysSort();
		System.out.println();
		System.out.println("---- Multi threaded merge sort ----");
		multiThreadedMergeSort();
		
	}
	
}

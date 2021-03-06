import java.lang.reflect.Array;
import java.util.*;

/**
 * Note that every sorting algorithm takes in an argument k. The sorting 
 * algorithm should sort the array from index 0 to k. This argument could
 * be useful for some of your sorts.
 *
 * Class containing all the sorting algorithms from 61B to date.
 *
 * You may add any number instance variables and instance methods
 * to your Sorting Algorithm classes.
 *
 * You may also override the empty no-argument constructor, but please
 * only use the no-argument constructor for each of the Sorting
 * Algorithms, as that is what will be used for testing.
 *
 * Feel free to use any resources out there to write each sort,
 * including existing implementations on the web or from DSIJ.
 *
 * All implementations except Counting Sort adopted from Algorithms,
 * a textbook by Kevin Wayne and Bob Sedgewick. Their code does not
 * obey our style conventions.
 */
public class MySortingAlgorithms {

    /**
     * Java's Sorting Algorithm. Java uses Quicksort for ints.
     */
    public static class JavaSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            Arrays.sort(array, 0, k);
        }

        @Override
        public String toString() {
            return "Built-In Sort (uses quicksort for ints)";
        }
    }

    /** Insertion sorts the provided data. */
    public static class InsertionSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            for (int i = 1; i < k; i += 1) {
                int toSwap = i;
                while(toSwap > 0 && array[toSwap] < array[toSwap - 1]) {
                    swap(array, toSwap, toSwap - 1);
                    toSwap -= 1;
                }
            }
        }

        @Override
        public String toString() {
            return "Insertion Sort";
        }
    }

    /**
     * Selection Sort for small K should be more efficient
     * than for larger K. You do not need to use a heap,
     * though if you want an extra challenge, feel free to
     * implement a heap based selection sort (i.e. heapsort).
     */
    public static class SelectionSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            for (int i = 0; i < k; i += 1) {
                int minIndex = i;
                int minEl = array[i];
                for (int j = i + 1; j < k; j += 1) {
                    if (Math.min(minEl, array[j]) == array[j]) {
                        minEl = array[j];
                        minIndex = j;
                    }
                }
                swap(array, i, minIndex);
            }
        }

        @Override
        public String toString() {
            return "Selection Sort";
        }
    }

    /** Your mergesort implementation. An iterative merge
      * method is easier to write than a recursive merge method.
      * Note: I'm only talking about the merge operation here,
      * not the entire algorithm, which is easier to do recursively.
      */
    public static class MergeSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            int [] merged = mergeSort(Arrays.copyOfRange(array, 0, k));
            for (int i = 0; i < merged.length; i += 1) {
                array[i] = merged[i];
            }
        }
        public int[] mergeSort(int[] array) {
            if (array.length <= 1) {
                return array;
            } else {
                int [] L = Arrays.copyOfRange(array, 0, (array.length)/2);
                int [] R = Arrays.copyOfRange(array, (array.length) / 2, array.length);
                L = mergeSort(L);
                R = mergeSort(R);
                return merge(L, R);
            }
        }
        public int[] merge(int[] arr1, int[] arr2) {
            int[] merged = new int[arr1.length + arr2.length];
            int mergedIndex = 0;
            int arr1Index = 0;
            int arr2Index = 0;
            while(arr1Index < arr1.length && arr2Index < arr2.length) {
                if (arr1[arr1Index] < arr2[arr2Index]) {
                    merged[mergedIndex] = arr1[arr1Index];
                    mergedIndex += 1;
                    arr1Index += 1;
                } else {
                    merged[mergedIndex] = arr2[arr2Index];
                    mergedIndex += 1;
                    arr2Index += 1;
                }
            }
            while (arr1Index < arr1.length) {
                merged[mergedIndex] = arr1[arr1Index];
                mergedIndex += 1;
                arr1Index += 1;
            }
            while (arr2Index < arr2.length) {
                merged[mergedIndex] = arr2[arr2Index];
                mergedIndex += 1;
                arr2Index += 1;
            }
            return merged;
        }
        // may want to add additional methods

        @Override
        public String toString() {
            return "Merge Sort";
        }

    }

    /**
     * Your Counting Sort implementation.
     * You should create a count array that is the
     * same size as the value of the max digit in the array.
     */
    public static class CountingSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            // FIXME: to be implemented
        }

        // may want to add additional methods

        @Override
        public String toString() {
            return "Counting Sort";
        }
    }

    /** Your Heapsort implementation.
     */
    public static class HeapSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            // FIXME
        }

        @Override
        public String toString() {
            return "Heap Sort";
        }
    }

    /** Your Quicksort implementation.
     */
    public static class QuickSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            // FIXME
        }

        @Override
        public String toString() {
            return "Quicksort";
        }
    }

    /* For radix sorts, treat the integers as strings of x-bit numbers.  For
     * example, if you take x to be 2, then the least significant digit of
     * 25 (= 11001 in binary) would be 1 (01), the next least would be 2 (10)
     * and the third least would be 1.  The rest would be 0.  You can even take
     * x to be 1 and sort one bit at a time.  It might be interesting to see
     * how the times compare for various values of x. */

    /**
     * LSD Sort implementation.
     */
    public static class LSDSort implements SortingAlgorithm {
        @Override
        public void sort(int[] a, int k) {
            LinkedList<Integer>[] buckets = new LinkedList[10];
            for (int i = 0; i < buckets.length; i += 1) {
                buckets[i] = new LinkedList<Integer>();
            }
            boolean allProcessed = false;
            int exp = 1;

            while (!allProcessed) {
                allProcessed = true;
                for (int i = 0; i < k; i += 1) {
                    int bucket = (a[i]/exp) % 10;
                    if (allProcessed && bucket > 0) {
                        allProcessed = false;
                    }
                    buckets[bucket].add(a[i]);
                }

                exp *= 10;
                int newInd = 0;
                for (int i = 0; i < buckets.length; i += 1) {
                    while (!buckets[i].isEmpty()) {
                        a[newInd] = buckets[i].removeFirst();
                        newInd += 1;
                    }
                }
            }
        }
        @Override
        public String toString() {
            return "LSD Sort";
        }
    }

    /**
     * MSD Sort implementation.
     */
    public static class MSDSort implements SortingAlgorithm {
        @Override
        public void sort(int[] a, int k) {
            // FIXME
        }

        @Override
        public String toString() {
            return "MSD Sort";
        }
    }

    /** Exchange A[I] and A[J]. */
    private static void swap(int[] a, int i, int j) {
        int swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

}

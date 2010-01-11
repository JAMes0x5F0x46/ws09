package at.tuwien.ifs.somtoolbox.util;

import java.util.Arrays;

/**
 * Class gathering utilities related to Arrays.
 * 
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class ArrayUtils {
    /** Initialises an array of the given size, with the value at each index corresponding to the index, i.e. 0, 1, 2, .... */
    public static int[] getLinearArray(int dim) {
        int[] columnOrder = new int[dim];
        for (int i = 0; i < columnOrder.length; i++) {
            columnOrder[i] = i;
        }
        return columnOrder;
    }

    /** Gets a string representation just as {@link Arrays#toString(int[])}, but at most until the given max amount of values. */
    public static String toString(int[] a, int maxValues) {
        if (a == null || a.length < maxValues) {
            return Arrays.toString(a);
        }
        StringBuilder b = new StringBuilder(maxValues * 7);
        b.append("[");
        for (int i = 0; i < maxValues; i++) {
            b.append(a[i]);
            b.append(", ");
        }
        return b.append(" ...]").toString();
    }

    /** Counts the number of occurrences of the given string in the given array */
    public static int countOccurrences(String s, String[] array) {
        int num = 0;
        for (String element : array) {
            if (s.equals(element)) {
                num++;
            }
        }
        return num;
    }

}

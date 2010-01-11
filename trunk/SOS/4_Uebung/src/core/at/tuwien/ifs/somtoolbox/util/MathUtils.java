package at.tuwien.ifs.somtoolbox.util;

/**
 * A collection of math-related utility methods.
 * 
 * @author Rudolf Mayer
 * @version $Id: MathUtils.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MathUtils {

    public static int min(int... arguments) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] < min) {
                min = arguments[i];
            }
        }
        return min;
    }

    public static int constrainWithin(int i, int lower, int upper) {
        if (i < lower) {
            return lower;
        } else if (i > upper) {
            return upper;
        }
        return i;
    }

    public static int cap(int i, int cap) {
        if (i > cap) {
            return cap;
        } else {
            return i;
        }
    }

    public static long cap(long i, long cap) {
        if (i > cap) {
            return cap;
        } else {
            return i;
        }
    }

    /**
     * sums up the values in the array and returns the sum
     * 
     * @param in the array over which the sum shall be calculated
     * @return the sum of all values in the array
     */
    public static double getSumOf(double[] in) {
        double out = 0;
        for (double element : in) {
            out += element;
        }
        return out;
    }

    /**
     * sums up the values in the array and returns the sum
     * 
     * @param in the array over which the sum shall be calculated
     * @return the sum of all values in the array
     */
    public static int getSumOf(int[] in) {
        int out = 0;
        for (int element : in) {
            out += element;
        }
        return out;
    }
}

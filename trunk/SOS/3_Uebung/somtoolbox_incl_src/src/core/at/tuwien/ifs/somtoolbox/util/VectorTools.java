package at.tuwien.ifs.somtoolbox.util;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;

import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * This class provides several utility methods to operate on Vectors.
 * 
 * @author Angela Roiger
 * @author Rudolf Mayer
 * @version $Id: VectorTools.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VectorTools {

    private static final String SPACING = "  ";

    public static final NumberFormat myFormat = NumberFormat.getNumberInstance();

    private static final FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD);

    public static double[] subtract(double[] a, double[] b) {
        if (a.length != b.length) {
            try {
                throw new MetricException("Oops ... tried to subtract vectors with different dimensionalities.");
            } catch (MetricException e) {
                e.printStackTrace();
            }
        }
        double[] diff = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            diff[i] = a[i] - b[i];
        }
        return diff;
    }

    public static int[] subtract(int[] a, int[] b) {
        if (a.length != b.length) {
            try {
                throw new MetricException("Oops ... tried to subtract vectors with different dimensionalities.");
            } catch (MetricException e) {
                e.printStackTrace();
            }
        }
        int[] diff = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            diff[i] = a[i] - b[i];
        }
        return diff;
    }

    public static double[] add(double[] a, double[] b) {
        if (a.length != b.length) {
            try {
                throw new MetricException("Oops ... tried to add vectors with different dimensionalities.");
            } catch (MetricException e) {
                e.printStackTrace();
            }
        }
        double[] add = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            add[i] = a[i] + b[i];
        }
        return add;
    }

    public static int[] add(int[] a, int[] b) {
        if (a.length != b.length) {
            try {
                throw new MetricException("Oops ... tried to add vectors with different dimensionalities.");
            } catch (MetricException e) {
                e.printStackTrace();
            }
        }
        int[] add = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            add[i] = a[i] + b[i];
        }
        return add;
    }

    public static double[] multiply(double[] a, double[] b) throws MetricException {
        if (a.length != b.length) {
            throw new MetricException("Oops ... tried to add vectors with different dimensionalities.");
        }
        double[] mukt = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            mukt[i] = a[i] * b[i];
        }
        return mukt;
    }

    public static double[] multiply(double[] a, double b) {
        double[] diff = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            diff[i] = a[i] * b;
        }
        return diff;
    }

    public static double[] divide(double[] a, double b) {
        double[] diff = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            diff[i] = a[i] / b;
        }
        return diff;
    }

    /**
     * Generates a random double[] with the given dimensionality, using a new random number generator.
     * 
     * @param dim the dimensionality of the double[]
     * @return a random double[]
     */
    public static double[] generateRandomVector(int dim) {
        return generateRandomVector(new Random(), dim);
    }

    /**
     * Generates a random double[] with the given dimensionality, using the given random number generator.
     * 
     * @param rand a random number generator
     * @param dim the dimensionality of the double[]
     * @return a random double[]
     */
    public static double[] generateRandomVector(Random rand, int dim) {
        return generateRandomVector(rand, dim, false);
    }

    /**
     * Generates a random double[] with the given dimensionality, using a new random number generator.
     * 
     * @param dim the dimensionality of the double[]
     * @param generateNegativeNumbers whether to generate negative numbers or not.
     * @return a random double[]
     */
    public static double[] generateRandomVector(int dim, boolean generateNegativeNumbers) {
        return generateRandomVector(new Random(), dim, generateNegativeNumbers);
    }

    /**
     * Generates a random double[] with the given dimensionality, using the given random number generator.
     * 
     * @param rand a random number generator
     * @param dim the dimensionality of the double[]
     * @param generateNegativeNumbers whether to generate negative numbers or not.
     * @return a random double[]
     */
    public static double[] generateRandomVector(Random rand, int dim, boolean generateNegativeNumbers) {
        Random sign = new Random();
        double[] randomVector = new double[dim];
        for (int i = 0; i < randomVector.length; i++) {
            randomVector[i] = rand.nextDouble();
            if (generateNegativeNumbers && sign.nextBoolean()) {
                randomVector[i] = randomVector[i] * -1;
            }
        }
        return randomVector;
    }

    public static DoubleMatrix1D generateRandomDoubleMatrix1D(Random rand, int dim, boolean generateNegativeNumbers) {
        return new DenseDoubleMatrix1D(generateRandomVector(rand, dim, generateNegativeNumbers));
    }

    public static DoubleMatrix1D generateRandomDoubleMatrix1D(int dim, boolean generateNegativeNumbers) {
        return new DenseDoubleMatrix1D(generateRandomVector(new Random(), dim, generateNegativeNumbers));
    }

    public static DoubleMatrix1D generateRandomDoubleMatrix1D(int dim) {
        return new DenseDoubleMatrix1D(generateRandomVector(dim, false));
    }

    /**
     * Normalises a matrix.
     * 
     * @param matrix the matrix to normalise.
     */
    public static void normalise(DoubleMatrix2D matrix) {
        final double minUHeight = matrix.aggregate(Functions.min, Functions.identity);
        final double maxUHeight = matrix.aggregate(Functions.max, Functions.identity);
        matrix.assign(new DoubleFunction() {
            public double apply(double argument) {
                return (argument - minUHeight) / (maxUHeight - minUHeight);
            }
        });
    }

    /** Normalises vector elements by the length of the vector, i.e. the length of the vector will become 1. */
    public static double[] normaliseByLength(double[] vector) {
        return normaliseByLength(vector, 1);
    }

    public static double vectorLength(double[] vector) {
        double length = 0;
        for (int i = 0; i < vector.length; i++) {
            length += vector[i] * vector[i];
        }
        length = Math.sqrt(length);
        return length;
    }

    public static double[] normaliseByLength(double[] vector, double length) {
        double vectorLength = vectorLength(vector);

        double[] result = new double[vector.length];
        // normalising - dividing each component by the vector length
        if (vectorLength > 0.0D) { // optimisation - only for vectors which do not have not only 0 values
            for (int i = 0; i < vector.length; i++) {
                result[i] = vector[i] / vectorLength * length;
            }
        }

        return result;
    }

    public static InputDatum normaliseByLength(InputDatum datum) {
        return normaliseByLength(datum, 1);
    }

    public static InputDatum normaliseByLength(InputDatum datum, double length) {
        // vectorLength = sqrt(SUM(x[i] * x[i])
        double vectorLength = Math.sqrt(datum.getVector().aggregate(Functions.plus, Functions.square));

        /*
         * normx[i] = (x[i] / vectorLength) length normx[i] = x[i] (length / vectorLength)
         */
        double x = length / vectorLength;
        DenseDoubleMatrix1D xs = new DenseDoubleMatrix1D(datum.getVector().size());
        xs.assign(x);
        datum.getVector().assign(xs, Functions.mult);

        return datum;
    }

    /**
     * Calculates the median of a vector.
     * 
     * @param values the vector elements
     * @return the median value
     */
    public static double median(double[] values) {
        double res = 0;
        Arrays.sort(values);
        if (values.length % 2 == 0) {
            int idx = values.length / 2;
            res = (values[idx - 1] + values[idx]) / 2;
        } else {
            int idx = values.length / 2;
            res = values[idx];
        }
        return res;
    }

    /**
     * @param a an array
     * @return the standard deviation
     */
    public static double standard_deviation(double[] a) {
        return Math.sqrt(StatUtils.variance(a));
    }

    public static boolean[] createBooleanArray(int dim, boolean initialValue) {
        boolean[] presentTerms = new boolean[dim];
        for (int l = 0; l < presentTerms.length; l++) {
            presentTerms[l] = initialValue;
        }
        return presentTerms;
    }

    /** Creates a {@link Map} with an inversed mapping of the original map, i.e. a valu e-&gt; key mapping. */
    public static HashMap<Object, Integer> reverseHashMap(Map<Integer, Object> map) {
        HashMap<Object, Integer> reversed = new HashMap<Object, Integer>();
        for (Integer i : map.keySet()) {
            reversed.put(map.get(i), i);
        }
        return reversed;
    }

    /** Calculates the mean vector from the given array of vectors. */
    public static double[] meanVector(double[][] a) {
        double[] meanVector = new double[a[0].length];
        for (int i = 0; i < a[0].length; i++) {
            double sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += a[j][i];
            }
            meanVector[i] = sum / a.length;
        }
        return meanVector;
    }

    public static double[] medianVector(double[][] a) {
        double[] medianVector = new double[a[0].length];
        for (int i = 0; i < a[0].length; i++) {
            medianVector[i] = median(slice(a, i));
        }
        return medianVector;
    }

    public static String printMatrix(double[][] matrix) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < matrix.length; i++) {
            b.append(printVector(matrix[i]));
            b.append('\n');
        }
        return b.toString();
    }

    public static String printMatrix(Object[][] matrix) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < matrix.length; i++) {
            b.append(printVector(matrix[i]));
            b.append('\n');
        }
        return b.toString();
    }

    public static String printMatrix(int[][] matrix) {
        StringBuffer b = new StringBuffer();
        for (int j = 0; j < matrix[0].length; j++) {
            for (int i = 0; i < matrix.length; i++) {
                b.append(matrix[i][j]).append(SPACING);
            }
            b.append('\n');
        }
        return b.toString();
    }

    /**
     * Method for printing a double float matrix <br>
     * Based on ER Harold, "Java I/O", O'Reilly, around p. 473.
     * 
     * @param m input matrix values, double
     * @param d display precision, number of decimal places
     * @param w display precision, total width of floating value
     */
    public static String printMatrix(double[][] m, int d, int w) {
        StringBuffer b = new StringBuffer();
        // Some definitions for handling output formating
        myFormat.setMaximumIntegerDigits(d);
        myFormat.setMaximumFractionDigits(d);
        myFormat.setMinimumFractionDigits(d);
        for (int i = 0; i < m[0].length; i++) {
            // Print each row, elements separated by spaces
            for (int j = 0; j < m.length; j++) {
                String valString = myFormat.format(m[i][j], new StringBuffer(), fp).toString();
                valString = getSpaces(w - fp.getEndIndex()) + valString;
                b.append(valString);
            }
            // Start a new line at the end of a row
            b.append("\n");
        }
        return b.toString();
    }

    /**
     * Method printVect for printing a double float vector <br>
     * Based on ER Harold, "Java I/O", O'Reilly, around p. 473.
     * 
     * @param m input vector of length m.length
     * @param d display precision, number of decimal places
     * @param w display precision, total width of floating value
     */
    public static String printVect(double[] m, int d, int w) {
        StringBuffer b = new StringBuffer();
        // Some definitions for handling output formating
        myFormat.setMaximumIntegerDigits(d);
        myFormat.setMaximumFractionDigits(d);
        myFormat.setMinimumFractionDigits(d);
        int len = m.length;
        for (int i = 0; i < len; i++) {
            String valString = myFormat.format(m[i], new StringBuffer(), fp).toString();
            valString = getSpaces(w - fp.getEndIndex()) + valString;
            b.append(valString);
        }
        return b.toString();
    }

    // Little method for helping in output formating
    public static String getSpaces(int n) {
        StringBuffer sb = new StringBuffer(Math.max(0, n));
        for (int i = 0; i < n; i++)
            sb.append(' ');
        return sb.toString();
    }

    // FIXME replace calls with ArrayUtils (and a couple of more I suppose)
    public static StringBuffer printVector(double[] vector) {
        StringBuffer b = new StringBuffer();
        b.append('[');
        for (int i = 0; i < vector.length; i++) {
            b.append(vector[i]);
            if (i < (vector.length - 1)) {
                b.append(", ");
            }
        }
        b.append(']');
        return b;
    }

    public static StringBuffer printVector(int[] vector) {
        return printVector(vector, ", ");
    }

    public static StringBuffer printVector(int[] vector, String spacing) {
        StringBuffer b = new StringBuffer();
        b.append('[');
        for (int i = 0; i < vector.length; i++) {
            b.append(vector[i]);
            if (i < (vector.length - 1)) {
                b.append(spacing);
            }
        }
        b.append(']');
        return b;
    }

    public static StringBuffer printVector(Object[] vector) {
        return printVector(vector, ", ");
    }

    public static StringBuffer printVector(Object[] vector, String spacing) {
        StringBuffer b = new StringBuffer();
        b.append('[');
        for (int i = 0; i < vector.length; i++) {
            b.append(vector[i]);
            if (i < (vector.length - 1)) {
                b.append(spacing);
            }
        }
        b.append(']');
        return b;
    }

    public static String printMatrixComparison(double[][] matrix, double[][] otherMatrix) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < otherMatrix.length; i++) {
            for (int j = 0; j < otherMatrix.length; j++) {
                if (otherMatrix[j][i] == matrix[j][i]) {
                    buffer.append("[ok] ");
                } else {
                    buffer.append("[no] ");
                }
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }

    public static boolean equals(double[][] distanceMatrix, double[][] distanceMatrixFromFile) {
        for (int i = 0; i < distanceMatrix.length; i++) {
            if (!Arrays.equals(distanceMatrix[i], distanceMatrixFromFile[i])) {
                return false;
            }
        }
        return true;
    }

    public static double[] generateOneVector(int dim) {
        return generateVectorWithValue(dim, 1);
    }

    public static double[] generateVectorWithValue(int dim, int value) {
        double[] res = new double[dim];
        for (int i = 0; i < res.length; i++) {
            res[i] = value;
        }
        return res;
    }

    public static double[][] transpose(double[][] matrix) {
        double[][] res = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix[0].length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                res[i][j] = matrix[j][i];
            }
        }
        return res;
    }

    /** main method for testing purposes only. */
    public static void main(String[] args) {
        double[] a1 = new double[] { 1, 2, 3, 4 };
        double[] a2 = new double[] { 2, 2, 3, 5 };
        double[] a3 = new double[] { 5, 8, 9, 2 };
        double[][] aa = new double[][] { a1, a2, a3 };
        System.out.println(printMatrix(aa));
        System.out.println(printMatrix(transpose(aa)));
        System.out.println("====================");
        for (int i = 0; i < aa[0].length; i++) {
            System.out.println(printVector(slice(aa, i)));
        }
        System.out.println("====================");
        System.out.println(printVector(meanVector(aa)));
        System.out.println(printVector(medianVector((aa))));

        System.out.println("====================");

        double[][] m = { { 1, 2 }, { 3, 4 } };
        double[] vec = { 1, 2 };
        System.out.println(printVector(multiply(m, vec)));
        System.out.println(printVector(multiply(vec, m)));

        double[] vec2 = { 99, 1, 2, 3, 4, 5, 6, .5d };
        System.out.println(ArrayUtils.toString(vec2));
        int toRemove = 4;
        int numOfMaxIndices = 3;
        double[] shrinked = removeIndex(vec2, toRemove);
        System.out.println("Removed index: " + toRemove + ":\n" + ArrayUtils.toString(shrinked));
        System.out.println("Max index is: " + getIndexOfMaxValue(vec2));
        System.out.println("Max indices for: " + numOfMaxIndices + " are:\n" + ArrayUtils.toString(getIndicesOfMaxValues(vec2, numOfMaxIndices)));

        Point3d a = new Point3d(1, 2, -3);
        Point3d b = new Point3d(5, 6, 0);
        System.out.println(ArrayUtils.toString(a));
        System.out.println(ArrayUtils.toString(b));
        System.out.println(ArrayUtils.toString(VectorTools.crossProduct(a, b)));

        double[] x = new double[] { 12, 12, 5, 2 };
        System.out.println(ArrayUtils.toString(x));
        System.out.println(ArrayUtils.toString(getIndicesOfMaxValues(x, 3)));

    }

    public static double[] multiply(double[][] matrix, double[] vec) {
        double[] res = new double[vec.length];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res.length; j++) {
                res[i] += matrix[i][j] * vec[j];
            }
        }
        return res;
    }

    public static double[] multiply(double[] vec, double[][] matrix) {
        double[] res = new double[vec.length];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res.length; j++) {
                res[i] += matrix[j][i] * vec[j];
            }
        }
        return res;
    }

    /**
     * Gets the index of the max value in an array.
     * 
     * @param array
     * @return index of the maximum value in the array
     */
    public static int getIndexOfMaxValue(int[] array) {
        int max = Integer.MIN_VALUE;
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] >= max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * Gets the index of the max value in an array.
     * 
     * @param array
     * @return index of the maximum value in the array
     */
    public static int getMaxValue(int[] array) {
        int max = 0;
        for (int i : array) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }

    /**
     * Gets the index of the max value in an array (for a double array his time).
     * 
     * @param array
     * @return index of the maximum value in the array
     * @deprecated not used anymore, marked for removal
     */
    public static int getIndexOfMaxValue(double[] array) {
        double max = Double.MIN_VALUE;
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] >= max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * get the max indices of the numberOfIndices largest values in an array
     * 
     * @param array
     * @param numberOfIndices
     * @return
     */
    public static int[] getIndicesOfMaxValues(double[] array, int numberOfIndices) {
        int[] indices = new int[numberOfIndices];

        // sort first, then get indices from original array we need to
        // clone here, otherwise nothing is going to work anymore
        double[] sortedArray = array.clone();
        Arrays.sort(sortedArray);

        // get in descending order, i.e. reverse
        ArrayUtils.reverse(sortedArray);

        // we get the indices of all max values

        // we count double occurrences of values and take this
        // into account when getting the indices, we hence avoid
        // double indices in the result
        double oldValue = 0d;
        int skip = 0;
        for (int i = 0; i < numberOfIndices; i++) {
            if (sortedArray[i] == oldValue)
                skip++;
            else {
                skip = 0;
                oldValue = sortedArray[i];
            }

            int maxIndex = getIndexOfValue(array, sortedArray[i], skip);
            // we avoid everything being set to -1
            if (maxIndex == -1) {
                return indices;
            }
            indices[i] = maxIndex;
        }
        return indices;
    }

    /**
     * get the max indices of the numberOfIndices largest values in an array
     * 
     * @param array
     * @param numberOfIndices
     * @return
     */
    public static int[] getIndicesOfMaxValues(int[] array, int numberOfIndices) {
        int[] indices = new int[numberOfIndices];

        // sort first, then get indices from original array we need to
        // clone here, otherwise nothing is going to work anymore
        int[] sortedArray = array.clone();
        Arrays.sort(sortedArray);

        // get in descending order, i.e. reverse
        ArrayUtils.reverse(sortedArray);

        // we get the indices of all max values
        for (int i = 0; i < numberOfIndices; i++) {
            int maxIndex = getIndexOfValue(array, sortedArray[i]);
            // we avoid everything being set to -1
            if (maxIndex == -1) {
                return indices;
            }
            indices[i] = maxIndex;
        }
        return indices;
    }

    /**
     * get the index of a given value in an array and skip the given number of indices to avoid duplicate indices in the results, i.e. if the array is
     * [12, 12, 5, 2] we will return 0 if skip = 0 and 1 if skip = 1
     * 
     * @param array
     * @param d
     * @return
     */
    private static int getIndexOfValue(double[] array, double d, int skip) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == d && skip-- == 0)
                return i;
        }
        return -1;
    }

    /**
     * get the index of a given value in an array
     * 
     * @param array
     * @param d
     * @return
     * @deprecated will go away
     */
    private static int getIndexOfValue(double[] array, double d) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == d)
                return i;
        }
        return -1;
    }

    /**
     * get the index of a given value in an array
     * 
     * @param array
     * @param d
     * @return
     */
    private static int getIndexOfValue(int[] array, int d) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == d)
                return i;
        }
        return -1;
    }

    /**
     * Ever needed a certain number of indices with maximum values sorted in descending order? Well, here it comes.
     * 
     * @param array an array of doubles
     * @param numberOfIndices the number of max values you want the indices of
     * @return the indices of these max values in the given array
     * @deprecated FIXME this is not working marked for removal
     */
    public static int[] getIndicesOfMaxValuesOLD(int[] array, int numberOfIndices) {
        int[] indices = new int[numberOfIndices];
        for (int i = 0; i < numberOfIndices; i++) {
            int maxIndex = getIndexOfMaxValue(array);
            indices[i] = maxIndex;
            array = removeIndex(array, maxIndex);
        }
        return indices;
    }

    /**
     * Remove an index from an array, it's as simple as that.
     * 
     * @param array
     * @param indexToRemove
     * @return an array of size array.length - 1 FIXME how about templates for this one?
     */
    public static double[] removeIndex(double[] array, int indexToRemove) {
        double[] shrinkedArray = new double[array.length - 1];
        for (int i = 0, j = 0; i < array.length; i++) {
            if (i != indexToRemove)
                shrinkedArray[j++] = array[i];
        }
        return shrinkedArray;
    }

    /**
     * Remove an index from an array, it's as simple as that.
     * 
     * @param array
     * @param indexToRemove
     * @return an array of size array.length - 1 FIXME how about templates for this one?
     */
    public static int[] removeIndex(int[] array, int indexToRemove) {
        int[] shrinkedArray = new int[array.length - 1];
        for (int i = 0, j = 0; i < array.length; i++) {
            if (i != indexToRemove)
                shrinkedArray[j++] = array[i];
        }
        return shrinkedArray;
    }

    public static int calculateArraySum(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++)
            sum += array[i];
        return sum;
    }

    public static int calculateArrayOverlaps(int[] array1, int[] array2) {
        int overlaps = 0;
        for (int i = 0; i < array1.length; i++)
            if (array1[i] == array2[i])
                overlaps++;
        return overlaps;
    }

    /**
     * merges two direction arrays, i.e. {1, 0, 0} and {0, 0, 1} become {1, 0, 1
     * 
     * @param directions1
     * @param directions2
     * @return
     */
    public static int[] mergeArrays(int[] directions1, int[] directions2) {
        int[] directions = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        for (int i = 0; i < directions.length; i++) {
            if (directions1[i] > 0)
                directions[i] = directions1[i];
            if (directions2[i] > 0)
                directions[i] = directions2[i];
        }
        return directions;
    }

    public static double[][][] multiply(double[][][] data, double factor) {
        double[][][] newData = new double[data.length][data[0].length][data[0][0].length];
        for (int i = 0; i < newData.length; i++) {
            for (int j = 0; j < newData[0].length; j++) {
                for (int k = 0; k < newData[0][0].length; k++) {
                    newData[i][j][k] = data[i][j][k] * factor;
                }
            }
        }
        return newData;
    }

    public static double[][][] divide(double[][][] data, double factor) {
        double[][][] newData = new double[data.length][data[0].length][data[0][0].length];
        for (int i = 0; i < newData.length; i++) {
            for (int j = 0; j < newData[0].length; j++) {
                for (int k = 0; k < newData[0][0].length; k++) {
                    newData[i][j][k] = data[i][j][k] / factor;
                }
            }
        }
        return newData;
    }

    public static boolean isNullVector(double[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] != 0d)
                return false;
        }
        return true;
    }

    public static boolean isNullVector(int[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] != 0)
                return false;
        }
        return true;
    }

    /**
     * Calculates vector normalised to unit length.
     * 
     * @param vector Vector to be normalised
     * @return The normalised vector.
     */
    public static double[] normaliseVectorToUnitLength(double[] vector) {
        int dim = vector.length;
        double len = 0;
        // double y = 10.0; // Ly
        for (int i = 0; i < dim; i++) {
            len += vector[i] * vector[i]; // L2
            // len += vector[i]; // L1
            // if (vector[i] > len) { // Linf
            // len = vector[i]; // Linf
            // } // Linf
            // len += Math.pow(vector[i],y); // Ly

        }
        len = Math.sqrt(len); // L2
        // len = len; // L1 Linf
        /** Ly ** */
        // if ( len < 0 && Math.round(y) == y && Math.round(y) % 2 == 1 ) {
        // len = Math.pow( -len, 1/y );
        // len = -len;
        // } else {
        // len = Math.pow( len, 1/y );
        // }
        /** Ly ** */

        if (len > 0) {
            for (int i = 0; i < dim; i++) {
                vector[i] /= len;
            }
        }
        return vector;
    }

    /**
     * Calculate the cross product of two 3-dimensional direction vectors. This is needed to check whether two lines are parallel or not. At first you
     * gotta get two direction vectors by subtracting x, y, and z values. Then you can calculate the cross product. If the cross product = [0 0 0] you
     * found your parallel lines
     * 
     * @param a first direction vector
     * @param b second direction vector
     * @return cross product of a and b
     */
    public static Point3d crossProduct(Point3d a, Point3d b) {
        Point3d crossProduct = new Point3d();
        crossProduct.x = (a.y * b.z - a.z * b.y);
        crossProduct.y = (a.z * b.x - a.x * b.z);
        crossProduct.z = (a.x * b.y - a.y * b.x);
        return crossProduct;
    }

    public static int[] computeDefaultSize(int numInstances) {
        return computeDefaultSize(numInstances, 10);
    }

    public static int[] computeDefaultSize(int numInstances, int elementsPerUnit) {
        int numUnits = numInstances / elementsPerUnit;
        // area = x * y = x * (x / 4 * 3) = x^2 * 0.75
        // x^2 = area / 0.75
        // x = sqrt(area/0.75)

        int xSize = (int) Math.sqrt(numUnits / 0.75);
        int ySize = (int) (Math.sqrt(numUnits / 0.75) / 4 * 3);
        return new int[] { xSize, ySize };
    }

    public static double[] slice(double[][] array, int i) {
        double[] res = new double[array.length];
        for (int j = 0; j < res.length; j++) {
            res[j] = array[j][i];
        }
        return res;
    }
    
    public static double[] findMiddle(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IndexOutOfBoundsException();
        }
        double[] res = new double[a.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = a[i] + 0.5 * (b[i] - a[i]);
        }
        return res;
    }
}

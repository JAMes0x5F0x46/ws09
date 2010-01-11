package at.tuwien.ifs.somtoolbox.layers.metrics;

import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Implements a static method for metric instantiation and a method for mean vector calculation. Furthermore, implements the convenience methods to
 * call the abstract method having two double arrays as arguments (see {@link #distance(double[], double[])}). This method has to be implemented by
 * classes actually implementing a certain metric.
 * 
 * @author Michael Dittenbach
 * @version $Id: AbstractMetric.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractMetric implements DistanceMetric {
    public static final String PACKAGE_NAME = AbstractMetric.class.getPackage().getName() + ".";

    /**
     * Instantiates a certain distance metric class specified by argument <code>mName</code>.
     * 
     * @param mName the name of the metric.
     * @return a distance metric object of class <code>mName</code>.
     * @throws ClassNotFoundException if class denoted by argument <code>mName</code> is not found.
     * @throws InstantiationException if if this Class represents an abstract class, an interface, an array class, a primitive type, or void; or if
     *             the class has no nullary constructor; or if the instantiation fails for some other reason.
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
     */
    public static DistanceMetric instantiate(String mName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return (DistanceMetric) Class.forName(mName).newInstance();
    }

    /**
     * Same as {@link #instantiate(String)}, but tries to get the metric with the specified name, and then with the package prefix, and throwing only
     * a {@link SOMToolboxException} with the root cause nested.
     */
    public static DistanceMetric instantiateNice(String metricName) throws SOMToolboxException {
        try {
            return AbstractMetric.instantiate(metricName);
        } catch (Exception e) {
            try {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Error instantiating metric: " + e.getMessage() + ", trying for metric class " + PACKAGE_NAME + metricName);
                return AbstractMetric.instantiate(PACKAGE_NAME + metricName);
            } catch (Exception e2) {
                throw new SOMToolboxException("Error instantiating metric: " + e.getMessage(), e2);
            }
        }
    }

    /**
     * Calculates the mean vector of two double array vectors.
     * 
     * @param vector1 first vector.
     * @param vector2 second vector.
     * @return the mean vector.
     * @throws MetricException if the dimensionalities of the two vectors differ.
     */
    public static double[] meanVector(double[] vector1, double[] vector2) throws MetricException {
        if (vector1.length != vector2.length) {
            throw new MetricException("Oops ... tried to calculate the mean vector of two vectors with different dimensionalities.");
        }
        // initialize mean vector
        double[] meanVector = new double[vector1.length];
        for (int ve = 0; ve < vector1.length; ve++) { // calculating mean vector
            meanVector[ve] = (vector1[ve] + vector2[ve]) / (double) 2;
        }
        return meanVector;
    }

    /** Can be used to do some performance testing to compare colt vs. direct distance implementations. */
    protected static void performanceTest(DistanceMetric metric, int dim) {
        try {
            System.out.println("Metric: " + metric);
            DoubleMatrix1D mat1 = VectorTools.generateRandomDoubleMatrix1D(dim);
            DoubleMatrix1D mat2 = VectorTools.generateRandomDoubleMatrix1D(dim);
            double[] vec1 = mat1.toArray();
            double[] vec2 = mat2.toArray();
            long startColt = System.currentTimeMillis();
            double distanceColt = metric.distance(mat1, mat2);
            System.out.println("result colt: " + distanceColt);
            long endColt = System.currentTimeMillis();
            long durationColt = endColt - startColt;
            long startDirect = System.currentTimeMillis();
            double distanceDirect = metric.distance(vec1, vec2);
            System.out.println("result direct: " + distanceDirect);
            System.out.println("equal? " + (distanceColt == distanceDirect) + ", difference: " + Math.abs(distanceColt - distanceDirect));
            long endDirect = System.currentTimeMillis();
            long durationDirect = endDirect - startDirect;
            System.out.println("duration colt: " + durationColt);
            System.out.println("duration direct: " + durationDirect);
        } catch (MetricException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[])
     */
    public abstract double distance(double[] vector1, double[] vector2) throws MetricException;

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], cern.colt.matrix.DoubleMatrix1D)
     */
    public double distance(double[] vector1, DoubleMatrix1D vector2) throws MetricException {
        return distance(vector1, vector2.toArray());
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], at.tuwien.ifs.somtoolbox.data.InputDatum)
     */
    public double distance(double[] vector, InputDatum data) throws MetricException {
        return distance(vector, data.getVector());
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(cern.colt.matrix.DoubleMatrix1D, double[])
     */
    public double distance(DoubleMatrix1D vector1, double[] vector2) throws MetricException {
        return distance(vector1.toArray(), vector2);
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(cern.colt.matrix.DoubleMatrix1D, cern.colt.matrix.DoubleMatrix1D)
     */
    public double distance(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException {
        return distance(vector1.toArray(), vector2.toArray());
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(cern.colt.matrix.DoubleMatrix1D, at.tuwien.ifs.somtoolbox.data.InputDatum)
     */
    public double distance(DoubleMatrix1D vector, InputDatum datum) throws MetricException {
        return distance(vector, datum.getVector());
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(at.tuwien.ifs.somtoolbox.data.InputDatum, double[])
     */
    public double distance(InputDatum data, double[] vector) throws MetricException {
        return distance(data.getVector(), vector);
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(at.tuwien.ifs.somtoolbox.data.InputDatum, cern.colt.matrix.DoubleMatrix1D)
     */
    public double distance(InputDatum datum, DoubleMatrix1D vector) throws MetricException {
        return distance(datum.getVector(), vector);
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(at.tuwien.ifs.somtoolbox.data.InputDatum, at.tuwien.ifs.somtoolbox.data.InputDatum)
     */
    public double distance(InputDatum datum, InputDatum datum2) throws MetricException {
        return distance(datum.getVector(), datum2.getVector());
    }

    /**
     * Performs a check on wether the given vectors have the same dimension.
     * 
     * @throws MetricException If the given vectors have different dimensions.
     */
    protected void checkDimensions(double[] vector1, double[] vector2) throws MetricException {
        if (vector1.length != vector2.length) {
            throw new MetricException("Tried to calculate distance between two vectors with different dimensionalities.");
        }
    }

    /**
     * Performs a check on wether the given vectors have the same dimension.
     * 
     * @throws MetricException If the given vectors have different dimensions.
     */
    protected void checkDimensions(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException {
        if (vector1.cardinality() != vector2.cardinality()) {
            throw new MetricException("Tried to calculate distance between two vectors with different dimensionalities.");
        }
    }

    public double transformValue(double value) {
        return value;
    }

    public double[] transformVector(double[] vector) {
        double[] transformed = new double[vector.length];
        for (int i = 0; i < transformed.length; i++) {
            transformed[i] = transformValue(vector[i]);
        }
        return transformed;
    }

    /** Empty implementation, subclasses needing to set parameters have to override this class. */
    @Override
    public void setMetricParams(String metricParamString) throws SOMToolboxException {
    }

}

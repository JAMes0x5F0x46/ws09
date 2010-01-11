package at.tuwien.ifs.somtoolbox.layers.metrics;

import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Implements the L1 or city block metric. Though this class could us at.tuwien.ifs.somtoolbox.layers.metrics.LNMetric, for performance issues this less
 * complex computation should be used.
 * 
 * @author Michael Dittenbach
 * @version $Id: L1Metric.java 2874 2009-12-11 16:03:27Z frank $
 */
public class L1Metric extends AbstractMetric implements DistanceMetric {

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[])
     */
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);
        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += Math.abs(vector1[i] - vector2[i]);
        }
        return dist;
    }

    public double distance(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException {
        return vector1.aggregate(vector2, Functions.plus, Functions.chain(Functions.abs, Functions.minus));
    }

    public String toString() {
        return "L1";
    }
}

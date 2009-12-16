package at.tuwien.ifs.somtoolbox.layers.metrics;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Generic Ln metric. L1 and L2 metrics are still implemented in seperate classes for performance reasons.
 * 
 * @author Rudolf Mayer
 * @version $Id: LnMetric.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LnMetric extends AbstractMetric implements DistanceMetric {

    private final LnMetricMatrix LN_METRIC_MATRIX = new LnMetricMatrix();

    private double n;

    private double root;

    public LnMetric(double power) {
        this.n = power;
        root = (double) 1 / n;
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[])
     */
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);
        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += Math.pow(Math.abs(vector1[i] - vector2[i]), n);
        }
        return Math.pow(dist, 1 / n);
    }

    public String toString() {
        return "L-" + n;
    }

    public double distance(DoubleMatrix1D vector1, DoubleMatrix1D vector2) throws MetricException {

        return Math.pow(vector1.aggregate(vector2, Functions.plus, LN_METRIC_MATRIX), root);
    }

    class LnMetricMatrix implements DoubleDoubleFunction {
        public double apply(double arg0, double arg1) {
            // return Math.pow(arg0-arg1, n);
            return Math.pow(Math.abs(arg0 - arg1), n);
        }

    }
}

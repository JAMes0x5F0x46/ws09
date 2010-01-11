package at.tuwien.ifs.somtoolbox.layers.metrics;

import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Implements the Mahalanobis distance metric. This metric requires the covariance matrix of the input data to be pre-calculated and set via the
 * {@link #init(double[][])} method prior to calculating distances.
 * 
 * @author Rudolf Mayer
 * @version $Id: MahalanobisMetric.java 2874 2009-12-11 16:03:27Z frank $
 */

public class MahalanobisMetric extends AbstractMetric {

    double[][] covarianceMatrix;

    public void init(double[][] covarianceMatrix) {
        this.covarianceMatrix = covarianceMatrix;
    }

    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        if (covarianceMatrix == null) {
            throw new MetricException("Mahalanobis metric not initialised with covariance matrix, run init(double[][] covarianceMatrix) first!");
        }
        double squareSum = 0.0;
        final double[] diff = VectorTools.subtract(vector1, vector2);
        VectorTools.multiply(VectorTools.multiply(diff, covarianceMatrix), diff);
        return squareSum;
    }
}

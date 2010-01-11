package at.tuwien.ifs.somtoolbox.layers.metrics;

/**
 * @author Rudolf Mayer
 * @version $Id: L2MetricWeighted.java 2874 2009-12-11 16:03:27Z frank $
 */
public class L2MetricWeighted extends AbstractWeightedMetric {

    @Override
    public double distance(double[] vector1, double[] vector2, double[] featureWeights) throws MetricException {
        checkDimensions(vector1, vector2);
        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += featureWeights[i] * ((vector1[i] - vector2[i]) * (vector1[i] - vector2[i]));
        }
        return Math.sqrt(dist);
    }

}

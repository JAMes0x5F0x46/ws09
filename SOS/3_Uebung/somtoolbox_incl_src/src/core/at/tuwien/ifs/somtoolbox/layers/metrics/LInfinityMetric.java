package at.tuwien.ifs.somtoolbox.layers.metrics;

/**
 * Implements the L-Infinity metric,defined for two vectors x and y as <i>max( |xi-yi| ), i = 1,...,|x|</i>.
 * 
 * @author Rudolf Mayer
 * @version $Id: LInfinityMetric.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LInfinityMetric extends AbstractMetric implements DistanceMetric {

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[])
     */
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);
        double maxDist = 0;
        for (int i = 0; i < vector1.length; i++) {
            maxDist = Math.max(Math.abs(vector1[i] - vector2[i]), maxDist);
        }
        return maxDist;
    }

    public String toString() {
        return "L-infinity-metric";
    }

}

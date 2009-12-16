package at.tuwien.ifs.somtoolbox.layers.metrics;

/**
 * Implements a fast version of the L2 or Euclidean metric, by not taking the square root. Thus, this implementation should be used only when the
 * ranking of distances is important, and the total distance value does not matter.
 * 
 * @author Rudolf Mayer
 * @version $Id: L2MetricFast.java 2874 2009-12-11 16:03:27Z frank $
 */
public class L2MetricFast extends L2Metric {

    public double distance(double[] vector1, double[] vector2) throws MetricException {
        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
        }
        return dist;
    }

    public String toString() {
        return "L2-Fast";
    }

}

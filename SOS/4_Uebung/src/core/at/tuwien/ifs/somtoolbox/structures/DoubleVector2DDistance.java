package at.tuwien.ifs.somtoolbox.structures;

import at.tuwien.ifs.somtoolbox.clustering.Cluster;
import at.tuwien.ifs.somtoolbox.clustering.ClusterElementFunctions;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * Implements functions needed for clustering of double arrays.
 * 
 * @author Rudolf Mayer
 * @version $Id: DoubleVector2DDistance.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DoubleVector2DDistance implements ClusterElementFunctions<DoubleVector2D> {
    protected DistanceMetric metric;

    public DoubleVector2DDistance(DistanceMetric metric) {
        this.metric = metric;
    }

    @Override
    /* Computes the distance between two lines, using the given distance function. */
    public double distance(DoubleVector2D element1, DoubleVector2D element2) {
        return distance(element1.getPoints(), element2.getPoints());
    }

    public double distance(double[] vector1, double[] vector2) {
        try {
            return metric.distance(vector1, vector2);
        } catch (MetricException e) {
            return 0; // doesn't happen
        }
    }

    @Override
    public DoubleVector2D meanObject(Cluster<? extends DoubleVector2D> elements) {
        if (elements.size() == 1) {
            return elements.get(0);
        }
        double[] meanVector = new double[elements.get(0).getLength()];
        for (int i = 0; i < meanVector.length; i++) {
            double sum = 0;
            for (int j = 0; j < elements.size(); j++) {
                sum += elements.get(j).get(i);
            }
            meanVector[i] = sum / elements.size();
        }
        return new DoubleVector2D(meanVector);
    }

    public int getIndexOfLineClosestToMean(Cluster<? extends DoubleVector2D> elements) {
        double minDist = Double.POSITIVE_INFINITY;
        int minIndex = 0;
        DoubleVector2D meanObject = meanObject(elements);
        for (int k = 0; k < elements.size(); k++) {
            double distance = distance(meanObject, elements.get(k));
            if (distance <= minDist) {
                minDist = distance;
                minIndex = k;
            }
        }
        return minIndex;
    }

    public String toString(Cluster<? extends DoubleVector2D> elements) {
        StringBuilder sb = new StringBuilder();
        for (double p : meanObject(elements).getPoints()) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(DF.format(p));
        }
        return getClass().getSimpleName() + " # vectors: " + elements.size() + ", mean vector: " + sb;
    }
}

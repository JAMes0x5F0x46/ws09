package at.tuwien.ifs.somtoolbox.layers.metrics;

import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Rudolf Mayer
 * @version $Id: AbstractWeightedMetric.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractWeightedMetric extends AbstractMetric {
    protected double[] featureWeights;

    public static AbstractWeightedMetric instantiate(String mName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return (AbstractWeightedMetric) Class.forName(mName).newInstance();
    }

    public abstract double distance(double[] vector1, double[] vector2, double[] weights) throws MetricException;

    public double distance(double[] vector, Unit unit) throws MetricException {
        return distance(vector, unit.getWeightVector(), unit.getFeatureWeights());
    }

    public double distance(InputDatum inputDatum, Unit unit) throws MetricException {
        return distance(inputDatum.getVector().toArray(), unit.getWeightVector(), unit.getFeatureWeights());
    }

    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        throw new MetricException("Distance measurement without feature weights not supported!");
    }
}

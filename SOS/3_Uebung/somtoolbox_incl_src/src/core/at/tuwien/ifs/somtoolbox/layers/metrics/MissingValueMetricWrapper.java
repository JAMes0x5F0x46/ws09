package at.tuwien.ifs.somtoolbox.layers.metrics;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;

/**
 * A wrapper class around other distance metrics, modifying the distance computation in such a way that only vector attributes that are not missing
 * (indicated by {@link InputData#MISSING_VALUE} are considered.<br/>
 * When instantiating using the empty constructor {@link #MissingValueMetricWrapper()} the default metric {@link #DEFAULT_METRIC} is used.
 * 
 * @author Rudolf Mayer
 * @version $Id: MissingValueMetricWrapper.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MissingValueMetricWrapper extends AbstractMetric {
    private static final L2Metric DEFAULT_METRIC = new L2Metric();

    private DistanceMetric metric;

    public MissingValueMetricWrapper() {
        this(DEFAULT_METRIC);
    }

    public MissingValueMetricWrapper(DistanceMetric metric) {
        this.metric = metric;
    }

    @Override
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        // prepare the input vectors, i.e. remove elements with missing values
        ArrayList<Double> vec1 = new ArrayList<Double>();
        ArrayList<Double> vec2 = new ArrayList<Double>();
        for (int i = 0; i < vector1.length; i++) {
            if (vector1[i] == vector1[i] && vector2[i] == vector2[i]) { // see Double.isNan() for this check
                vec1.add(vector1[i]);
                vec2.add(vector2[i]);
            }
        }
        if (vector1.length != vec1.size()) {
            vector1 = ArrayUtils.toPrimitive(vec1.toArray(new Double[vec1.size()]));
            vector2 = ArrayUtils.toPrimitive(vec2.toArray(new Double[vec2.size()]));
        }
        return metric.distance(vector1, vector2);
    }

    @Override
    public void setMetricParams(String metricParamString) throws SOMToolboxException {
        super.setMetricParams(metricParamString);
        String[] params = metricParamString.split(";");
        for (String string : params) {
            String[] parts = string.split("=");
            if (parts[0].equals("class")) {
                metric = instantiateNice(parts[1]);
            }
        }
    }

    public void setMetric(DistanceMetric metric) {
        this.metric = metric;
    }

}

package at.tuwien.ifs.somtoolbox.layers.metrics;

/**
 * @author Rudolf Mayer
 * @version $Id: LnAlphaMetric.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LnAlphaMetric extends AbstractMetric implements DistanceMetric {

    private double alpha;

    private double n;

    public LnAlphaMetric(double alpha, double n) {
        this.alpha = alpha;
        this.n = n;
    }

    public LnAlphaMetric() {
        this.alpha = 1;
        this.n = 1;
    }

    /** Sets specific parameters for the LnAlpha metric, namely <i>alpha</i> and <i>n</i>. */
    @Override
    public void setMetricParams(String metricParamString) throws MetricException {
        String[] params = metricParamString.split(",");
        if (params.length != 2) {
            throw new MetricException("LnAlphaMetric parameters must be of form <n>,<alpha>; given param string consisted of " + params.length
                    + " parts!");
        }
        try {
            n = Double.parseDouble(params[0]);
            alpha = Double.parseDouble(params[1]);
        } catch (NumberFormatException e) {
            throw new MetricException("Invalid value for LnAlphaMetric param: " + e.getMessage());
        }
    }

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[])
     */
    public double distance(double[] vector1, double[] vector2) throws MetricException {
        checkDimensions(vector1, vector2);

        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            double arg1 = Math.pow(Math.abs(vector1[i]), alpha) * signum(vector1[i]);
            double arg2 = Math.pow(Math.abs(vector2[i]), alpha) * signum(vector2[i]);
            dist += Math.pow(Math.abs(arg1 - arg2), n);
        }
        return Math.pow(dist, 1 / n);
    }

    public double distanceFromPrecalc(double[] vector1, double[] vector2) throws MetricException {

        if (vector1.length != vector2.length) {
            throw new MetricException("Oops ... tried to calculate distance between two vectors with different dimensionalities.");
        }

        double dist = 0;
        for (int i = 0; i < vector1.length; i++) {
            dist += Math.pow(Math.abs(vector1[i] - vector2[i]), n);
        }
        return Math.pow(dist, 1 / n);
    }

    public double transformValue(double value) {
        return Math.pow(Math.abs(value), alpha) * signum(value);
    }

    public String toString() {
        return "L-n-alpha (" + n + "/" + alpha + ") metric";
    }

    public int signum(double number) {
        if (number > 0) {
            return 1;
        } else if (number < 0) {
            return -1;
        } else {
            return 0;
        }
    }

}

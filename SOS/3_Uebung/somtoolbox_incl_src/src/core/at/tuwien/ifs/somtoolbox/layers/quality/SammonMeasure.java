package at.tuwien.ifs.somtoolbox.layers.quality;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * 
 * Sammon Measure for Self Organizing Maps (Sammon 1969)
 * 
 * @author Christoph Hohenwarter
 * @version $Id: SammonMeasure.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SammonMeasure extends AbstractQualityMeasure {

    private double sammon = 0;

    public SammonMeasure(Layer layer, InputData data) {
        super(layer, data);

        mapQualityNames = new String[] { "sammon" };
        mapQualityDescriptions = new String[] { "Sammon Measure" };

        int xs = layer.getXSize();
        int ys = layer.getYSize();

        ArrayList<Unit> neurons = new ArrayList<Unit>();

        double sum = 0;
        double sum2 = 0;
        DistanceMetric metric = layer.getMetric();

        // Construction of an array A of all neurons
        for (int xi = 0; xi < xs; xi++) {
            for (int yi = 0; yi < ys; yi++) {
                try {
                    neurons.add(layer.getUnit(xi, yi));
                } catch (LayerAccessException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        // Construction of AxA out of the array A of the neurons
        // (n1,n2) element of AxA with n1!=n2
        for (int n1 = 0; n1 < neurons.size(); n1++) {
            for (int n2 = 0; n2 < neurons.size(); n2++) {
                if (n1 != n2) {
                    Unit neuro1 = neurons.get(n1);
                    Unit neuro2 = neurons.get(n2);

                    // Distance of the neurons n1,n2 in the Kohonengraph
                    double da = layer.getMapDistance(neuro1, neuro2);

                    double dv = 0;
                    try {
                        // Distance of the weightvectors in the inputspace
                        dv = metric.distance(neuro1.getWeightVector(), neuro2.getWeightVector());
                    } catch (MetricException e) {
                        System.out.println(e.getMessage());
                    }

                    // Intermediate value
                    double zw = (da - dv);
                    double zw2 = Math.pow(zw, 2);

                    sum = sum + da;

                    sum2 = sum2 + (zw2 / da);

                }
            }
        }

        sammon = sum2 / sum;

    }

    public double getMapQuality(String name) throws QualityMeasureNotFoundException {

        return sammon;
    }

    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
    }

}

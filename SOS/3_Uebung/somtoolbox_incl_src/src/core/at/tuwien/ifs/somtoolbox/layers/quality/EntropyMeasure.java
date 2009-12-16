package at.tuwien.ifs.somtoolbox.layers.quality;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * Implementation of SOM Entropy Measure. (Hulle 2000)
 * 
 * @author Christoph Hohenwarter
 * @version $Id: EntropyMeasure.java 2874 2009-12-11 16:03:27Z frank $
 */
public class EntropyMeasure extends AbstractQualityMeasure {

    private double entropie = 0;

    private double[][] unitsE;

    public EntropyMeasure(Layer layer, InputData data) {
        super(layer, data);

        mapQualityNames = new String[] { "entropy" };
        mapQualityDescriptions = new String[] { "Entropy Measure" };

        int xs = layer.getXSize();
        int ys = layer.getYSize();
        double totalHits = 0.0;
        unitsE = new double[xs][ys];

        ArrayList<Unit> units = new ArrayList<Unit>();

        double summe = 0;

        // Construction of an array A of all neurons
        for (int xi = 0; xi < xs; xi++) {
            for (int yi = 0; yi < ys; yi++) {
                try {
                    units.add(layer.getUnit(xi, yi));
                    totalHits = totalHits + layer.getUnit(xi, yi).getNumberOfMappedInputs();
                } catch (LayerAccessException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        for (int i = 0; i < units.size(); i++) {
            double pi = (units.get(i).getNumberOfMappedInputs()) / totalHits;
            // To avoid NaN-calculations of log10
            if (pi > 0) {
                unitsE[units.get(i).getXPos()][units.get(i).getYPos()] = (pi * Math.log10(pi)) * -1;
                summe = summe + pi * Math.log10(pi);
            }
        }

        summe = summe * -1;

        // Endresult
        entropie = summe;

    }

    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        return entropie;
    }

    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        return this.unitsE;
    }

}

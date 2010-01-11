package at.tuwien.ifs.somtoolbox.layers.quality;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * Calculates the <i>Quantisation Error</i>, defined as the average distance between and input data vector and the weight-vector of its
 * best-matching-unit.<br>
 * Calculates the following values:
 * <ul>
 * <li>Unit
 * <ul>
 * <li>Quantisation error (qe). Calculated for each unit as the sum of the distances between the input vectors mapped to this unit to the unit's
 * weight vector.</li>
 * <li>Mean quantisation error (mqe). Calculated for each unit as the (qe/|mapped vectors|), i.e. the qe divided by the number of input data vectors
 * mapped on this unit.</li>
 * </ul>
 * </li>
 * <li>Map
 * <ul>
 * <li>Mean quantisation error (mqe), caluclated as the sum of all unit qe's divided by the number of units with at least one mapped input data
 * vector.</li>
 * <li>Mean mean quantisation error (mmqe), calculated as the sum of all unit mqe's divided by the number of units with at least one mapped input
 * data vector.</li>
 * </ul>
 * </ul>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: QuantizationError.java 2874 2009-12-11 16:03:27Z frank $
 */
public class QuantizationError extends AbstractQualityMeasure {
    private double mmqe;

    private double mqe;

    private double[][] unitMqe;

    private double[][] unitQe;

    public QuantizationError(Layer layer, InputData data) {
        super(layer, data);
        mapQualityNames = new String[] { "mqe", "mmqe" };
        mapQualityDescriptions = new String[] { "Mean Quantization Error", "Mean Mean Quantization Error" };
        unitQualityNames = new String[] { "qe", "mqe" };
        unitQualityDescriptions = new String[] { "Quantization Error", "Mean Quantization Error" };
        int xSize = layer.getXSize();
        int ySize = layer.getYSize();

        mqe = 0;
        mmqe = 0;
        int nonEmpty = 0;

        unitQe = new double[xSize][ySize];
        unitMqe = new double[xSize][ySize];

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                double quantErr = 0;
                Unit u = null;
                try {
                    u = layer.getUnit(x, y);
                } catch (LayerAccessException e) {
                    // TODO: this does not happen
                }
                // added to deal with mnemonic (sparse) SOMs
                if (u != null && u.getNumberOfMappedInputs() > 0) {
                    double[] dists = u.getMappedInputDistances();
                    for (int i = 0; i < u.getNumberOfMappedInputs(); i++) {
                        quantErr += dists[i];
                    }
                    unitQe[x][y] = quantErr;
                    unitMqe[x][y] = quantErr / u.getNumberOfMappedInputs();
                    nonEmpty++;
                    mqe += unitQe[x][y];
                    mmqe += unitMqe[x][y];

                } else {
                    unitQe[x][y] = 0;
                    unitMqe[x][y] = 0;
                }
            }
        }
        mqe = mqe / nonEmpty;
        mmqe = mmqe / nonEmpty;
    }

    public double getMapQuality(String name) throws QualityMeasureNotFoundException {
        if (name.equals("mqe")) {
            return mqe;
        } else if (name.equals("mmqe")) {
            return mmqe;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException {
        if (name.equals("qe")) {
            return unitQe;
        } else if (name.equals("mqe")) {
            return unitMqe;
        } else {
            throw new QualityMeasureNotFoundException("Quality measure with name " + name + " not found.");
        }
    }

}

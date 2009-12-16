package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.image.BufferedImage;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;

/**
 * A basic hit-histogram visualizer.
 * 
 * @author Rudolf Mayer
 * @version $Id: HitHistogrammVisualizer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class HitHistogrammVisualizer extends AbstractMatrixVisualizer implements BackgroundImageVisualizer {
    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        final GrowingLayer layer = gsom.getLayer();
        DoubleMatrix2D matrix = new DenseDoubleMatrix2D(layer.getYSize(), layer.getXSize());
        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                final int numberOfMappedInputs = layer.getUnit(x, y).getNumberOfMappedInputs();
                matrix.setQuick(y, x, numberOfMappedInputs);
                if (numberOfMappedInputs > maximumMatrixValue) {
                    maximumMatrixValue = numberOfMappedInputs;
                }
                if (numberOfMappedInputs < minimumMatrixValue) {
                    minimumMatrixValue = numberOfMappedInputs;
                }
            }
        }
        matrix.assign(Functions.div(maximumMatrixValue));
        return super.createImage(gsom, matrix, width, height, interpolate);
    }

    public HitHistogrammVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Hit Histogram" };
        VISUALIZATION_SHORT_NAMES = new String[] { "HitHistogram" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "A simple Hit Histogram" };
        setInterpolate(false);
    }

}

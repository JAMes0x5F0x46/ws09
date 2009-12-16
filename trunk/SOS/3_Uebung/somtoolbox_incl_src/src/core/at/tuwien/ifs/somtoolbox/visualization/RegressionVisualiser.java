package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMLibRegressInformation;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * This visualizer provides an implementation of the <i>Smoothed Data Histograms</i> in three variants.
 * <ol>
 * <li> Implementation of the Smoothed Data Histograms as described in <i><b>E. Pampalk, A. Rauber, and D. Merkl.</b> Proceedings of the
 * International Conference on Artificial Neural Networks (ICANN'02), pp 871-876, LNCS 2415, Madrid, Spain, August 27-30, 2002, Springer Verlag.</i></li>
 * <li> An extension of the Smoothed Data Histograms. Not the rank is taken into account for histogram calculation, but distances "between input
 * vectors and weight vectors.</li>
 * <li>As 2., but additionally values are normalized per datum.</li>
 * </ol>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: RegressionVisualiser.java 2874 2009-12-11 16:03:27Z frank $
 */
public class RegressionVisualiser extends AbstractMatrixVisualizer implements BackgroundImageVisualizer, ChangeListener {

    /**
     * The minimum value for the smoothing factor (1), resulting in only the best winning units to get a hit counted.
     */
    protected static final int MIN_SMOOTHING_VALUE = 1;

    /**
     * The maximum value for the smoothing factor (300).
     */
    protected static int MAX_SMOOTHING_VALUE = 300;

    /**
     * The default value for the smoothing factor (15).
     */
    protected static int DEFAULT_SMOOTHING_VALUE = 15;

    /**
     * The currently used smooting factor. The smoothing factor decides how many n-best matching units get a hit counted.
     */
    protected int s = DEFAULT_SMOOTHING_VALUE;

    protected SOMLibDataWinnerMapping dataWinnerMapping = null;

    public RegressionVisualiser() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Regression Visualiser" };
        VISUALIZATION_SHORT_NAMES = new String[] { "Regression" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "" };
        neededInputObjects = new String[] { SOMVisualisationData.REGRESS_INFORMATION };

        reversePalette();

        // don't initialise the control panel if we have no graphics environment (e.g. in server applications)
        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new RegressionControlPanel(this);
        }
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        return createVisualization(index, gsom, width, height, 1, 1, false, true);
    }

    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height, int blockWidth, int blockHeight,
            boolean forceSmoothingCacheInitialisation, boolean shallDrawBackground) throws SOMToolboxException {
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        SOMLibRegressInformation regressInfo = gsom.getSharedInputObjects().getSOMLibRegressInformation();
        if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null
                && gsom.getSharedInputObjects().getData(neededInputObjects[1]) == null) {
            throw new SOMToolboxException("You need to specify at least one out of " + neededInputObjects[0] + " or " + neededInputObjects[1]);
        }

        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();
        zSize = gsom.getLayer().getZSize();

        double unitWidth = ((double) width) / xSize;
        double unitHeight = ((double) height) / ySize;
        unitWidth = (int) unitWidth;
        unitHeight = (int) unitHeight;

        DoubleMatrix2D matrix = DoubleFactory2D.dense.make(ySize, xSize);
        double maxValue = Double.MIN_VALUE;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                Unit u = gsom.getLayer().getUnit(x, y);
                System.out.println(u);
                double unitPrediction = 0.0;
                if (u.getMappedInputNames() != null) {
                    for (String string : u.getMappedInputNames()) {
                        System.out.println(string + ": " + regressInfo.getPrediction(string, 0));
                        unitPrediction += regressInfo.getPrediction(string, 0);
                    }
                    unitPrediction = unitPrediction / u.getMappedInputNames().length;
                    System.out.println("unit pred: " + unitPrediction);
                    if (unitPrediction > maxValue) {
                        maxValue = unitPrediction;
                    }
                    System.out.println();
                } else {
                    unitPrediction = -1;
                }
                matrix.set(y, x, unitPrediction);
            }
        }
        System.out.println(matrix);
        new Throwable().printStackTrace();
        return super.createImage(gsom, matrix, width, height, interpolate);

    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource().getClass() == JSpinner.class) {
            JSpinner src = (JSpinner) e.getSource();
            s = ((Integer) src.getValue()).intValue();
        }
        if (visualizationUpdateListener != null) {
            visualizationUpdateListener.updateVisualization();
        }
    }

    /**
     * Return the currently used smooting factor.
     * 
     * @return the smoothing factor
     */
    public int getSmoothingFactor() {
        return s;
    }

    /**
     * A control panel extending the generic {@link AbstractBackgroundImageVisualizer.VisualizationControlPanel}, adding additionally a
     * {@link JSpinner} for controlling the smoothing factor.
     * 
     * @author Rudolf Mayer
     */
    public class RegressionControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        /**
         * The {@link JSpinner} controlling the smooting factor.
         */
        public JSpinner spinnerSmoothingFactor = null;

        private RegressionControlPanel(RegressionVisualiser vis) {
            super("Regression Control");
            spinnerSmoothingFactor = new JSpinner(new SpinnerNumberModel(vis.getSmoothingFactor(), RegressionVisualiser.MIN_SMOOTHING_VALUE,
                    RegressionVisualiser.MAX_SMOOTHING_VALUE, 1));
            spinnerSmoothingFactor.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSpinner src = (JSpinner) e.getSource();
                    s = ((Integer) src.getValue()).intValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            JPanel smoothingFactorPanel = new JPanel();
            smoothingFactorPanel.add(new JLabel("Smoothing factor: "));
            smoothingFactorPanel.add(spinnerSmoothingFactor);
            add(smoothingFactorPanel, c);

            // New, by frank
            JPanel slicePanel = new JPanel();
            slicePanel.add(new JLabel("Show slice"));
            spinnerZSlice = new JSpinner();
            spinnerZSlice.setModel(new SpinnerNumberModel(vis.currentZDimSlice, 0, vis.zSize, 1));
            spinnerZSlice.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSpinner src = (JSpinner) e.getSource();
                    currentZDimSlice = ((Integer) src.getValue()).intValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            slicePanel.add(spinnerZSlice);
            add(slicePanel, c);
        }

        public void updateZDim(int current, int max) {
            spinnerZSlice.setModel(new SpinnerNumberModel(current, 0, max, 1));
            if (max <= 1) {
                spinnerZSlice.setEnabled(false);
            } else {
                spinnerZSlice.setEnabled(true);
            }
            // Force repaint
            revalidate();
            repaint();
        }
    }

    @Override
    public String getHTMLVisualisationControl(Map params) {
        StringBuffer b = new StringBuffer();
        b.append("Smoothing factor:\n");
        b.append("<select name=\"smoothingFactor\">\n");
        for (int i = MIN_SMOOTHING_VALUE; i < MAX_SMOOTHING_VALUE; i++) {
            b.append("<option value=\"" + i + "\"");
            if (params.get("smoothingFactor") != null && params.get("smoothingFactor").equals(String.valueOf(i))) {
                b.append(" selected");
            }
            b.append(">" + i + "</option>\n");
        }
        b.append("</select>\n");
        return b.toString();
    }

}

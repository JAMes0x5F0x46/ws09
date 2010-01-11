package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.comparables.UnitDistance;
import flanagan.interpolation.BiCubicSpline;

/**
 * This visualizer provides an implementation of the <i>Smoothed Data Histograms</i> in three variants.
 * <ol>
 * <li>Implementation of the Smoothed Data Histograms as described in <i><b>E. Pampalk, A. Rauber, and D. Merkl.</b> Proceedings of the International
 * Conference on Artificial Neural Networks (ICANN'02), pp 871-876, LNCS 2415, Madrid, Spain, August 27-30, 2002, Springer Verlag.</i></li>
 * <li>An extension of the Smoothed Data Histograms. Not the rank is taken into account for histogram calculation, but distances "between input
 * vectors and weight vectors.</li>
 * <li>As 2., but additionally values are normalized per datum.</li>
 * </ol>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SmoothedDataHistograms.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SmoothedDataHistograms extends AbstractMatrixVisualizer implements BackgroundImageVisualizer, ChangeListener {

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
     * The currently used smoothing factor. The smoothing factor decides how many n-best matching units get a hit counted.
     */
    protected int s = DEFAULT_SMOOTHING_VALUE;

    /**
     * A cache for the different smoothing factors.
     */
    protected Hashtable<Integer, Histogram>[] smoothingCache = null;

    protected SOMLibDataWinnerMapping dataWinnerMapping = null;

    public SmoothedDataHistograms() {
        NUM_VISUALIZATIONS = 3;
        VISUALIZATION_NAMES = new String[] { "Smoothed Data Histograms", "Weighted SDH", "Weighted SDH (norm.)" };
        VISUALIZATION_SHORT_NAMES = new String[] { "SDH", "WeightedSDH", "WeightedSDHNorm" };
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Implementation of Smoothed Data Histograms as described in \"E. Pampalk, A. Rauber, and D. Merkl.\n"
                        + "Proceedings of the International Conference on Artificial Neural Networks (ICANN'02),\n"
                        + "pp 871-876, LNCS 2415, Madrid, Spain, August 27-30, 2002, Springer Verlag.",

                "Extension of Smoothed Data Histograms. Not rank is taken into account for histogram calculation, but distances\n"
                        + "between input vectors and weight vectors.",

                "Extension of Smoothed Data Histograms. Not rank is taken into account for histogram calculation, but distances\n"
                        + "between input vectors and weight vectors. Values are normalized per datum." };
        neededInputObjects = new String[] { SOMVisualisationData.DATA_WINNER_MAPPING, SOMVisualisationData.INPUT_VECTOR };

        reversePalette();
        smoothingCache = new Hashtable[NUM_VISUALIZATIONS];

        // don't initialise the control panel if we have no graphics environment (e.g. in server applications)
        if (!GraphicsEnvironment.isHeadless()) {
            controlPanel = new SDHControlPanel(this);
        }
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR + "smoothing:" + s;
    }

    /** Visualisation for a specific smoothing factor */
    public BufferedImage getVisualization(int index, int smoothingFactor, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        int oldSmoothingFactor = s;
        s = smoothingFactor;
        controlPanel.updateZDim(gsom.getLayer().getZSize());
        String cacheKey = getCacheKey(gsom, index, width, height);
        logImageCache(cacheKey);
        if (cache.get(cacheKey) == null) {
            cache.put(cacheKey, createVisualization(index, gsom, width, height));
        }
        s = oldSmoothingFactor;
        return cache.get(cacheKey);
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        return createVisualization(index, gsom, width, height, 1, 1, false, true);
    }

    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height, int blockWidth, int blockHeight,
            boolean forceSmoothingCacheInitialisation, boolean shallDrawBackground) throws SOMToolboxException {
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        if (shallDrawBackground) {
            drawBackground(width, height, g);
        }

        return (BufferedImage) generateSDH(index, gsom, width, height, blockWidth, blockHeight, forceSmoothingCacheInitialisation, true, res);
    }

    protected Object generateSDH(int index, GrowingSOM gsom, int width, int height, int blockWidth, int blockHeight,
            boolean forceSmoothingCacheInitialisation, boolean integerPrecision, Object target) throws SOMToolboxException, LayerAccessException {
        // MAX_SMOOTHING_VALUE = (gsom.xSize()*gsom.ySize())/5;

        if (gsom.getSharedInputObjects().getData(neededInputObjects[0]) == null
                && gsom.getSharedInputObjects().getData(neededInputObjects[1]) == null) {
            throw new SOMToolboxException("You need to specify at least one out of " + neededInputObjects[0] + " or " + neededInputObjects[1]);
        }

        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();

        double unitWidth = ((double) width) / xSize;
        double unitHeight = ((double) height) / ySize;
        if (integerPrecision) {
            unitWidth = (int) unitWidth;
            unitHeight = (int) unitHeight;
        }

        if (forceSmoothingCacheInitialisation || (smoothingCache[0] == null) || (smoothingCache[1] == null) || (smoothingCache[2] == null)) {
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            initSmoothingCache(gsom);
        }

        double[][] sdh = (smoothingCache[index].get(new Integer(s))).mh;

        // start bicubic spline stuff

        double[] x1 = new double[xSize + 2];
        x1[0] = 0;
        for (int x = 0; x < xSize; x++) {
            x1[x + 1] = ((x * unitWidth) + (unitWidth / 2));
        }
        x1[xSize + 1] = width;
        double[] x2 = new double[ySize + 2];
        x2[0] = 0;
        for (int y = 0; y < ySize; y++) {
            x2[y + 1] = ((y * unitHeight) + (unitHeight / 2));
        }
        x2[ySize + 1] = height;

        double[][] sdhWithBorders = new double[xSize + 2][ySize + 2];

        sdhWithBorders[0][0] = sdh[0][0] - ((sdh[1][1] - sdh[0][0]) / 2); // top-left corner
        sdhWithBorders[xSize + 1][0] = sdh[xSize - 1][0] - ((sdh[xSize - 2][1] - sdh[xSize - 1][0]) / 2); // top-right corner
        sdhWithBorders[0][ySize + 1] = sdh[0][ySize - 1] - ((sdh[1][ySize - 2] - sdh[0][ySize - 1]) / 2); // bottom-left corner

        // FIXME ? was sdhWithBorders[xSize][ySize] for former method 'createVisualization3'.
        sdhWithBorders[xSize + 1][ySize + 1] = sdh[xSize - 1][ySize - 1] - ((sdh[xSize - 2][ySize - 2] - sdh[xSize - 1][ySize - 1]) / 2); // bottom-right
        // corner
        for (int x = 1; x < xSize + 1; x++) {
            sdhWithBorders[x][0] = sdh[x - 1][0] - ((sdh[x - 1][1] - sdh[x - 1][0]) / 2); // top row
            sdhWithBorders[x][ySize + 1] = sdh[x - 1][ySize - 1] - ((sdh[x - 1][ySize - 2] - sdh[x - 1][ySize - 1]) / 2); // bottom row
        }
        for (int y = 1; y < ySize + 1; y++) {
            sdhWithBorders[0][y] = sdh[0][y - 1] - ((sdh[1][y - 1] - sdh[0][y - 1]) / 2); // left column
            sdhWithBorders[xSize + 1][y] = sdh[xSize - 1][y - 1] - ((sdh[xSize - 2][y - 1] - sdh[xSize - 1][y - 1]) / 2); // right column
        }
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                sdhWithBorders[x + 1][y + 1] = sdh[x][y];
            }
        }
        BiCubicSpline bcs = new BiCubicSpline(x1, x2, sdhWithBorders);
        // bcs.calcDeriv();
        for (int y = 0; y < height; y += blockHeight) {
            for (int x = 0; x < width; x += blockWidth) {
                processSDH(gsom, bcs, y, x, blockWidth, blockHeight, unitWidth, unitHeight, target);
            }
        }
        // end bicubic spline stuff

        // debug only, TODO: move to unit information display
        //
        // g.setColor(Color.BLACK);
        // g.setPaint(Color.BLACK);
        // for (int y = 0; y < gsom.ySize(); y++) {
        // for (int x = 0; x < gsom.xSize(); x++) {
        // g.drawString(StringUtils.format5Digits.format(sdh[x][y]), ((x * unitWidth) + (unitWidth / 2)), ((y * unitHeight) + (unitHeight / 2)));
        // }
        // }

        return target;
    }

    protected void processSDH(GrowingSOM gsom, BiCubicSpline bcs, int y, int x, int blockWidth, int blockHeight, double unitWidth, double unitHeight,
            Object target) throws LayerAccessException {
        Graphics2D g = (Graphics2D) ((BufferedImage) target).getGraphics();
        int elevation = (int) Math.round(bcs.interpolate((double) x + 0.5, (double) y + 0.5) * (double) (palette.length - 1));
        // limit ci value between 0 & max value
        elevation = Math.min(palette.length - 1, Math.max(0, elevation));

        // check for mnemonic SOM: if a unit is empty and has a low value, or the unit has no neighbours
        if ((elevation < 3 && gsom.getLayer().getUnit(x / (int) unitWidth, y / (int) unitHeight, currentZDimSlice) == null)
                || !gsom.getLayer().hasNeighbours(x / (int) unitWidth, y / (int) unitHeight)) {
            g.setPaint(Color.WHITE); // we draw this position white
        } else {
            g.setPaint(palette[elevation]);
        }
        g.fill(new Rectangle(x, y, blockWidth, blockHeight));

        if (elevation < minimumMatrixValue) {
            minimumMatrixValue = elevation;
        }
        if (elevation > maximumMatrixValue) {
            maximumMatrixValue = elevation;
        }
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
     * Return the currently used smoothing factor.
     * 
     * @return the smoothing factor
     */
    public int getSmoothingFactor() {
        return s;
    }

    private void computeDefaultAndMaxSmoothingValues(int xSize, int ySize) {
        if (xSize * ySize < MAX_SMOOTHING_VALUE) {
            MAX_SMOOTHING_VALUE = xSize * ySize;
            DEFAULT_SMOOTHING_VALUE = (MAX_SMOOTHING_VALUE - MIN_SMOOTHING_VALUE) / 2;
            s = DEFAULT_SMOOTHING_VALUE;

            if (controlPanel != null) { // check needed for headless environments
                ((SDHControlPanel) controlPanel).spinnerSmoothingFactor.setModel(new SpinnerNumberModel(s, MIN_SMOOTHING_VALUE, MAX_SMOOTHING_VALUE,
                        1));
            }
        }
    }

    protected void initSmoothingCache(GrowingSOM gsom) throws SOMToolboxException {
        computeDefaultAndMaxSmoothingValues(gsom.getLayer().getXSize(), gsom.getLayer().getYSize());
        
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Initialization of SDH cache (s=" + MIN_SMOOTHING_VALUE + ",...," + MAX_SMOOTHING_VALUE + ") started.");

        for (int i = 0; i < smoothingCache.length; i++) {
            smoothingCache[i] = new Hashtable<Integer, Histogram>();
        }

        // get max number of winners for each datum

        int numVectors = 0;
        Unit[][] winners = null;
        int[][] xPos = null;
        int[][] yPos = null;
        int[][] zPos = null;
        double[][] dists = null;

        if (gsom.getSharedInputObjects().getDataWinnerMapping() != null) { // we have mapping or file
            dataWinnerMapping = gsom.getSharedInputObjects().getDataWinnerMapping();
            numVectors = dataWinnerMapping.getNumVectors();
            winners = new Unit[numVectors][];
            xPos = new int[numVectors][];
            yPos = new int[numVectors][];
            zPos = new int[numVectors][];
            dists = new double[numVectors][];

            for (int d = 0; d < numVectors; d++) {
                xPos[d] = dataWinnerMapping.getXPos(d);
                yPos[d] = dataWinnerMapping.getYPos(d);
                zPos[d] = dataWinnerMapping.getZPos(d);
                dists[d] = dataWinnerMapping.getDists(d);
            }
            MAX_SMOOTHING_VALUE = dataWinnerMapping.getNumBMUs();
            DEFAULT_SMOOTHING_VALUE = (MAX_SMOOTHING_VALUE - MIN_SMOOTHING_VALUE) / 2;
            s = DEFAULT_SMOOTHING_VALUE;

            if (controlPanel != null) { // check needed for headless environments
                ((SDHControlPanel) controlPanel).spinnerSmoothingFactor.setModel(new SpinnerNumberModel(s, MIN_SMOOTHING_VALUE, MAX_SMOOTHING_VALUE,
                        1));
            }
        } else if (gsom.getSharedInputObjects().getInputData() != null) { // we have an input vector file
            InputData data = gsom.getSharedInputObjects().getInputData();
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // FIXME: sparsity!!!!!!!!!!!!!!!!!!!!!!!!!!!
            numVectors = data.numVectors();
            winners = new Unit[numVectors][];
            xPos = new int[numVectors][];
            yPos = new int[numVectors][];
            zPos = new int[numVectors][];
            dists = new double[numVectors][];
            StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Getting winners for datum ", 10);

            for (int d = 0; d < numVectors; d++) {

                UnitDistance[] winnDist = gsom.getLayer().getWinnersAndDistances(data.getInputDatum(d), MAX_SMOOTHING_VALUE);
                winners[d] = new Unit[winnDist.length];
                dists[d] = new double[winnDist.length];
                for (int i = 0; i < winnDist.length; i++) {
                    winners[d][i] = winnDist[i].getUnit();
                    dists[d][i] = winnDist[i].getDistance();
                }

                xPos[d] = new int[MAX_SMOOTHING_VALUE];
                yPos[d] = new int[MAX_SMOOTHING_VALUE];
                zPos[d] = new int[MAX_SMOOTHING_VALUE];
                for (int w = 0; w < MAX_SMOOTHING_VALUE; w++) {
                    xPos[d][w] = winners[d][w].getXPos();
                    yPos[d][w] = winners[d][w].getYPos();
                    zPos[d][w] = winners[d][w].getZPos();
                }
                progressWriter.progress(d + 1);
                // TODO: store the generated data winner mapping for sub-sequent use.
            }
        } else { // throw an exception that will later be handled
            throw new SOMToolboxException("You need to specify at least one out of " + neededInputObjects[0] + " or " + neededInputObjects[1]);
        }

        // create and cache histograms for each smoothing factor
        StdErrProgressWriter progressWriter = new StdErrProgressWriter(MAX_SMOOTHING_VALUE, "Smoothing factor: ");
        for (int svar = MIN_SMOOTHING_VALUE; svar <= MAX_SMOOTHING_VALUE; svar++) {
            double sdhs[][][] = new double[NUM_VISUALIZATIONS][gsom.getLayer().getXSize()][gsom.getLayer().getYSize()];
            for (int k = 0; k < NUM_VISUALIZATIONS; k++) {
                for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                    for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                        sdhs[k][i][j] = 0;
                    }
                }
            }

            int cs = 0;
            for (int i = 0; i < svar; i++) {
                cs += (svar - i);
            }

            for (int d = 0; d < numVectors; d++) {
                // normalization needed for sdh variant 3
                double max = 0;
                double min = Double.MAX_VALUE;
                for (int w = 0; w < svar; w++) {
                    if (dists[d][w] > max) {
                        max = dists[d][w];
                    }
                    if (dists[d][w] < min) {
                        min = dists[d][w];
                    }
                }

                // create sdh matrix entries
                for (int w = 0; w < svar; w++) {
                    sdhs[0][xPos[d][w]][yPos[d][w]] += ((double) svar - (double) w) / cs;
                    sdhs[1][xPos[d][w]][yPos[d][w]] += 1.0d / dists[d][w];
                    sdhs[2][xPos[d][w]][yPos[d][w]] += 1.0d - ((dists[d][w] - min) / (max - min));
                }
            }

            // determine max and min value of matrices
            double[] maxValues = new double[NUM_VISUALIZATIONS];
            double[] minValues = new double[NUM_VISUALIZATIONS];
            for (int i = 0; i < maxValues.length; i++) {
                maxValues[i] = 0;
                minValues[i] = Double.MAX_VALUE;
            }
            for (int k = 0; k < NUM_VISUALIZATIONS; k++) {
                for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                    for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                        if (sdhs[k][i][j] > maxValues[k]) {
                            maxValues[k] = sdhs[k][i][j];
                        }
                        if (sdhs[k][i][j] < minValues[k]) {
                            minValues[k] = sdhs[k][i][j];
                        }
                    }
                }
            }

            // normalize sdh matrix
            for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
                for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
                    for (int k = 0; k < NUM_VISUALIZATIONS; k++) {
                        sdhs[k][i][j] = (sdhs[k][i][j] - minValues[k]) / (maxValues[k] - minValues[k]);
                    }
                }
            }

            // set the generated smoothing caches.
            for (int k = 0; k < NUM_VISUALIZATIONS; k++) {
                smoothingCache[k].put(new Integer(svar), new Histogram(sdhs[k]));
            }

            progressWriter.progress();
        }
        winners = null;
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Initialization of SDH cache finished.");
    }

    private class Histogram {
        private double[][] mh;

        public Histogram(double[][] h) {
            mh = h;
        }
    }

    /**
     * A control panel extending the generic {@link AbstractBackgroundImageVisualizer.VisualizationControlPanel}, adding additionally a
     * {@link JSpinner} for controlling the smoothing factor.
     * 
     * @author Rudolf Mayer
     */
    public class SDHControlPanel extends VisualizationControlPanel {
        private static final long serialVersionUID = 1L;

        /**
         * The {@link JSpinner} controlling the smoothing factor.
         */
        public JSpinner spinnerSmoothingFactor = null;

        private SDHControlPanel(SmoothedDataHistograms vis) {
            super("SDH Control");
            spinnerSmoothingFactor = new JSpinner(new SpinnerNumberModel(vis.getSmoothingFactor(), SmoothedDataHistograms.MIN_SMOOTHING_VALUE,
                    SmoothedDataHistograms.MAX_SMOOTHING_VALUE, 1));
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
        }
    }

    /**
     * Overrides {@link AbstractBackgroundImageVisualizer#needsAdditionalFiles()}, as we need only one of the two possible input files to create this
     * visualisation. If the data winner mapping is present, it will be used directly, otherwise it can be created from the input vectors.
     */
    @Override
    public String[] needsAdditionalFiles() {
        String[] dataFiles = super.needsAdditionalFiles();
        if (dataFiles.length < 2) { // we need only one of the files
            return null;
        } else {
            return dataFiles;
        }
    }

    /**
     * Sets the smoothing factor.
     * 
     * @param smoothingFactor the new smoothing factor
     */
    public void setSmoothingFactor(int smoothingFactor) {
        s = smoothingFactor;
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

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width, int height)
            throws SOMToolboxException {
        return getVisualizationFlavours(variantIndex, gsom, width, height, MAX_SMOOTHING_VALUE);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width, int height, int maxFlavours)
            throws SOMToolboxException {
        HashMap<String, BufferedImage> result = new HashMap<String, BufferedImage>();

        // we need to set the default values, otherwise in the first iteration, we won't be using smoothing factor 1, but the computed"optimal" value
        initSmoothingCache(gsom);
        // FIXME: guess we should do palette handling in a more general way...
        if (isReversed()) {
            reversePalette();
        }

        int currentSF = getSmoothingFactor();
        int count = 0;
        for (int i = 1; i <= MAX_SMOOTHING_VALUE;) {
            count++;
            if (variantIndex == 2 && i == 1) {
                continue; // smoothing factor 1 doesn't work with normalised version, just skip the image
            }
            String key = String.format("_smooth%d", i);

            BufferedImage val = getVisualization(variantIndex, i, gsom, width, height);
            result.put(key, val);
            if (count >= maxFlavours)
                break;
            if (i < 20) {
                i += 1;
            }
            if (20 <= i && i < 50) {
                i += 2;
            } else if (50 <= i) {
                i += 5;
            }
        }
        setSmoothingFactor(currentSF);
        return result;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int variantIndex, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: Implement Method
        return getVisualizationFlavours(variantIndex, gsom, width, height);
    }

}

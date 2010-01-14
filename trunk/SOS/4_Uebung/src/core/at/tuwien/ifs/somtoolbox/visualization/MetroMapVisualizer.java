package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.ArrayUtils;

import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ColourLegendTable;
import at.tuwien.ifs.somtoolbox.clustering.Cluster;
import at.tuwien.ifs.somtoolbox.clustering.ClusterElementFunctions;
import at.tuwien.ifs.somtoolbox.clustering.DistanceFunctionType;
import at.tuwien.ifs.somtoolbox.clustering.WardClustering;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.structures.ComponentLine2D;
import at.tuwien.ifs.somtoolbox.structures.ComponentLine2DDistance;
import at.tuwien.ifs.somtoolbox.structures.ElementWithIndex;
import at.tuwien.ifs.somtoolbox.util.DateUtils;
import at.tuwien.ifs.somtoolbox.util.LeastRecentlyUsedCache;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import at.tuwien.ifs.somtoolbox.util.comparables.ComponentRegionCount;
import at.tuwien.ifs.somtoolbox.visualization.metromap.MetroColorMap;

/**
 * @author Rudolf Mayer
 * @version $Id: MetroMapVisualizer.java 2949 2009-12-15 15:21:15Z frank $
 */
public class MetroMapVisualizer extends AbstractMatrixVisualizer {

    enum Mode {
        NONE("None"), TARGET_NUMBER_OF_COMPONENTS("# comp"), THRESHOLD("similarity");
        private String displayName;

        private Mode(String displayName) {
            this.displayName = displayName;
        }
    }

    private static final Color STATION_FILL_COLOUR = Color.WHITE;

    private static final int MIN_BINS = 2;

    private static final int MAX_BINS = 100;

    /** the attribute legend will have at most this many entries */
    private static final int MAX_NUMBER_OF_LEGEND_ENTRIES = 100;

    private static final String COMP_PREFIX = "Comp. ";

    private int numberOfBins = 6;

    private int[] selectedComponentIndices = null;

    private SOMLibTemplateVector templateVector = null;

    private int radius = 130 / 10;

    private int innerRadius = radius - 2;

    private int lineThickness = radius;

    // these got passed around way too often
    private int unitWidth;

    private int unitHeight;

    // That one's self explanatory now, isn't it? Well, it handles the offSet of parallel lines.
    private double lineOffsetIsThisFractionOfRadius = 100d;

    // shifts between parallel metro lines
    private double lineOffset = lineThickness / lineOffsetIsThisFractionOfRadius;

    // scaling of the ellipses for stops (better don't touch)
    double scale = 1.6d;

    private int dim = 1;

    private int selectionTargetNumberOfComponents = dim / 2;

    private double selectionSimilarity = 0.5;

    private int aggregationTargetNumberOfComponents = dim / 2;

    private double aggregationSimilarity = 0.5;

    private Mode aggregationMode = Mode.NONE;

    private boolean snapping = false;

    private Mode selectionMode = Mode.NONE;

    private DistanceFunctionType lineDistanceFunction = DistanceFunctionType.Euclidean;

    private MetroMapControlPanel metroMapControlPanel;

    private final MetroColorMap colorMap = metroColorMap;

    private static final String[] legendColumnNames = new String[] { "Component", "Color" };

    private JComboBox overlayVisualisationComboBox = new JComboBox();

    private LeastRecentlyUsedCache<String, WardClustering<ComponentLine2D>> clusterCache = new LeastRecentlyUsedCache<String, WardClustering<ComponentLine2D>>(
            20);

    public MetroMapVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Metro Map", "Metro Map Snapped", "Metro Map Aggregated" };
        VISUALIZATION_SHORT_NAMES = new String[] { "MetroMap", "MetroMapSnapped", "MetroMapAggregated" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Robert Neumayer, Rudolf Mayer, Georg Pölzlbauer, and Andreas Rauber."
                + " The  metro visualisation of component planes for self-organising maps. "
                + "In Proceedings of the International Joint Conference on Neural Networks (IJCNN'07), "
                + "Orlando, FL, USA, August 12-17 2007. IEEE Computer Society." };

        neededInputObjects = new String[] { SOMVisualisationData.TEMPLATE_VECTOR };
        if (!(controlPanel instanceof MetroMapControlPanel)) {
            // create control panel if it is a generic panel
            controlPanel = new MetroMapControlPanel(this);
            metroMapControlPanel = ((MetroMapControlPanel) controlPanel);
        }
        // Scale for the MetroMapVisualizer needs to be smaller, as the visualisation is made of lines, which cannot be scaled too much.
        preferredScaleFactor = 1;
    }

    @Override
    public BufferedImage getVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        // TODO Auto-generated method stub
        return createVisualization(index, gsom, width, height);
    }

    private static Color[] COLORS = { Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE,
            Color.PINK, Color.RED, Color.YELLOW };

    private static final MetroColorMap metroColorMap = new MetroColorMap();

    public float[][] dashPatterns = initDashPatterns();

    public static float[] lineThicknessFactors = { 1, 0.75f, 0.5f, 0.3f };

    private UMatrix uMatrixVisualizer;

    private FlowBorderlineVisualizer flowBorderlineVisualizer;

    private ThematicClassMapVisualizer thematicClassMapVisualizer;

    // private DoubleMatrix2D matrix;

    private BufferedImage overlayVisualisation;

    private ColourLegendTable colourLegendTable1;

    private ColourLegendTable colourLegendTable2;

    private double legendColumns = 2;

    private int endIndexTable1;

    // TODO only compute once (after we stop debugging :-))
    private Point2D[][] binCentres;

    private Hashtable<Point2D, Integer> unitsWithStopsOnThemAndHowMany;

    private Hashtable<Point2D, int[]>[] allDirections;

    private GrowingSOM gsom;

    private AffineTransformOp op;

    /** Array of all points on the SOM . */
    private Point2D[] allSOMCoordinates = null;

    /**
     * Lookup-up matrix to check fast if two points are on a diagonal to each other. The table has the sum of the X & Y coordinates of each point. For
     * a given unit, all units that are in the lower-left or upper-right diagonal will have the same value. As an example, for a 6x6 SOM, it would
     * thus have values as follows:
     * 
     * <pre>
     *               0  1  2  3  4  5  6
     *               1  2  3  4  5  6  7
     *               2  3  4  5  6  7  8
     *               3  4  5  6  7  8  9
     *               4  5  6  7  8  9 10
     *               5  6  7  8  9 10 11
     *               6  7  8  9 10 11 12
     * </pre>
     */
    private double[] allSOMCoordinatesSumValues = null;

    /**
     * Similar matrix as {@link #allSOMCoordinatesSumValues}, but containing the difference of the X&Y coordinates, for upper-left and lower-right
     * diagonal units. As an example, for a 6x6 SOM, it would thus have values as follows:
     * 
     * <pre>
     *                0   1   2   3   4   5   6
     *               -1   0   1   2   3   4   5
     *               -2  -1   0   1   2   3   4
     *               -3  -2  -1   0   1   2   3
     *               -4  -3  -2  -1   0   1   2
     *               -5  -4  -3  -2  -1   0   1
     *               -6  -5  -4  -3  -2  -1   0
     * </pre>
     */
    private double[] allSOMCoordinatesDiffValues = null;

    // these guys store the indices for the single component colouring where it is looked up again
    private List<? extends Cluster<ComponentLine2D>> clusters;

    private List<ComponentRegionCount> selectedComponents;

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR
                + buildCacheKey(snapping, aggregationMode, aggregationSimilarity, aggregationTargetNumberOfComponents, numberOfBins);
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        if (templateVector == null || this.gsom != gsom) {
            // do some init
            this.gsom = gsom;
            templateVector = gsom.getSharedInputObjects().getTemplateVector();
            if (templateVector == null) {
                throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
            }
            metroMapControlPanel.initLegendTableNormal();
            final int scrollBarWidth = metroMapControlPanel.colourLegendScrollPane.getVerticalScrollBar().getWidth() + 5;
            final int scrollBarHeight = metroMapControlPanel.colourLegendScrollPane.getHorizontalScrollBar().getHeight() + 5;
            metroMapControlPanel.colourLegendScrollPane.setMinimumSize(new Dimension(CommonSOMViewerStateData.getInstance().controlElementsWidth
                    - scrollBarWidth, Math.max(colourLegendTable1.getPreferredHeight() + scrollBarHeight, 50)));
            metroMapControlPanel.colourLegendScrollPane.setPreferredSize(new Dimension(CommonSOMViewerStateData.getInstance().controlElementsWidth
                    - scrollBarWidth, Math.max(colourLegendTable1.getPreferredHeight() + scrollBarHeight, 50)));
        }

        if (allSOMCoordinates == null) {
            initNeighbourhoodLookup(gsom);
        }
        return createMetromapImage(index, gsom, width, height);
    }

    public void initNeighbourhoodLookup(GrowingSOM gsom) {
        // init the data structures for neighbourhood lookup
        int xSize = gsom.getLayer().getXSize();
        int ySize = gsom.getLayer().getYSize();

        allSOMCoordinates = new Point2D[xSize * ySize];

        // TODO this is highly confusing, what about initialising an index = 0 first and then use this one?
        // or maybe x2 * xSize or something?
        for (int i2 = 0; i2 < xSize; i2++) {
            for (int j = 0; j < ySize; j++) {
                allSOMCoordinates[i2 * ySize + j] = new Point2D.Double(i2, j);
            }
        }

        allSOMCoordinatesSumValues = new double[allSOMCoordinates.length];
        allSOMCoordinatesDiffValues = new double[allSOMCoordinates.length];
        for (int i = 0; i < allSOMCoordinatesDiffValues.length; i++) {
            allSOMCoordinatesSumValues[i] = allSOMCoordinates[i].getX() + allSOMCoordinates[i].getY();
            allSOMCoordinatesDiffValues[i] = allSOMCoordinates[i].getX() - allSOMCoordinates[i].getY();
        }
    }

    /**
     * Will create the metro map image for the given params.
     * 
     * @param index
     * @param gsom
     * @param width
     * @param height
     * @return
     * @throws SOMToolboxException
     */
    private BufferedImage createMetromapImage(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        GrowingLayer layer = gsom.getLayer();
        
        // we do this every time now, check for existing binCentres and correct sizes
        // thereof inside computeFinalComponentLines
        binCentres = computeFinalComponentLines(layer);

        BufferedImage res = createOverlayVisualisation(width + 3000, height);
        Graphics2D g = (Graphics2D) res.getGraphics();
        
        unitWidth = width / layer.getXSize();
        unitHeight = height / layer.getYSize();

        setPalette(0, new Palette("RGB, " + numberOfBins + " colors", "RGB_" + numberOfBins + "_colors", "", ColorGradientFactory.RGBGradient(),
                numberOfBins));
        g.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 5f }, 5f));

        // reset legend table (TODO: do this only if it is not in reset mode
        // FIXME: rudi? I think this is not needed because I don't miss it
        if (selectionMode == Mode.NONE && aggregationMode == Mode.NONE) {
            // metroMapControlPanel.initLegendTableNormal();
        }

        // draw normal layout plus line
        if (!snapping) {
            createLayout(g, layer, true);
        } else { // do parallel layouting in here (for snapped centres only, that's why we check the snapping boolean)
            createLayout(g, layer, false);
            createSnappedMetroLayout(g, layer);
        }

        int ypos = drawCorrelationResult(g, width, height, layer);
        drawExtrema(g,width, height, gsom, ypos);
        
        return res;
    }

    public BufferedImage createMetromapImage(int index, GrowingSOM gsom, int width, int height, int component) {
        // FIXME: this could be a parameter
        int numberOfBins = 6;

        GrowingLayer layer = gsom.getLayer();
        int[][] binAssignment = layer.getBinAssignment(component, numberOfBins);

        unitWidth = width / gsom.getLayer().getXSize();
        unitHeight = height / gsom.getLayer().getYSize();
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        colourUnits(g, binAssignment, new Palette("RGB, " + numberOfBins + " colors", "RGB_" + numberOfBins + "_colors", "",
                ColorGradientFactory.RGBGradient(), numberOfBins).getColors());

        // special stroke for export mode, size pretty hard-coded
        g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawLine(g, component, layer.getBinCentres(numberOfBins));

        drawUnitGrid((Graphics2D) res.getGraphics(), gsom, width, height);
        return res;
    }

    /**
     * Creates the selected overlay visualisation.
     * 
     * @param width width of the overlay
     * @param height of the overlay
     * @return overlay image
     * @throws SOMToolboxException
     */
    private BufferedImage createOverlayVisualisation(int width, int height) throws SOMToolboxException {
        BufferedImage res;
        Graphics2D g;

        String visName = overlayVisualisationComboBox.getSelectedItem().toString();
        String visOption = null;
        int indexOf = visName.indexOf("/");
        if (indexOf != -1) {
            visOption = visName.substring(indexOf + 1);
            visName = visName.substring(0, indexOf);
        }

        if (StringUtils.equalsAny(visName, UMatrix.UMATRIX_SHORT_NAMES)) {
            if (uMatrixVisualizer == null) {
                uMatrixVisualizer = new UMatrix();
            }
            Palette riverPalette = Palettes.getPaletteByName("MetroMap");
            uMatrixVisualizer.setPalette(Palettes.getPaletteIndex(riverPalette), riverPalette);
            overlayVisualisation = uMatrixVisualizer.createVisualization(visName, gsom, width / uMatrixVisualizer.getPreferredScaleFactor(), height
                    / uMatrixVisualizer.getPreferredScaleFactor());
            double scaleX = (double) width / (double) overlayVisualisation.getWidth();
            double scaleY = (double) height / (double) overlayVisualisation.getHeight();
            op = new AffineTransformOp(AffineTransform.getScaleInstance(scaleX, scaleY), new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            res = op.filter(overlayVisualisation, null);
            g = (Graphics2D) res.getGraphics();

        } else if (StringUtils.equalsAny(visName, FlowBorderlineVisualizer.FLOWBORDER_SHORT_NAMES)) {
            if (flowBorderlineVisualizer == null) {
                flowBorderlineVisualizer = new FlowBorderlineVisualizer();
            }
            overlayVisualisation = flowBorderlineVisualizer.createVisualization(visName, gsom, width
                    / flowBorderlineVisualizer.getPreferredScaleFactor(), height / flowBorderlineVisualizer.getPreferredScaleFactor());
            double scaleX = (double) width / (double) overlayVisualisation.getWidth();
            double scaleY = (double) height / (double) overlayVisualisation.getHeight();
            op = new AffineTransformOp(AffineTransform.getScaleInstance(scaleX, scaleY), new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            res = op.filter(overlayVisualisation, null);
            g = (Graphics2D) res.getGraphics();

        } else if (visName.equals(ThematicClassMapVisualizer.CLASSMAP_SHORT_NAME)) {
            if (thematicClassMapVisualizer == null) {
                thematicClassMapVisualizer = new ThematicClassMapVisualizer();
            }
            thematicClassMapVisualizer.setInputObjects(inputObjects);
            if (org.apache.commons.lang.StringUtils.equals(visOption, "Chess")) {
                thematicClassMapVisualizer.setInitialParams(true, true, 0);
            }
            try {
                overlayVisualisation = thematicClassMapVisualizer.createVisualization(0, gsom, width
                        / thematicClassMapVisualizer.getPreferredScaleFactor(), height / thematicClassMapVisualizer.getPreferredScaleFactor());
            } catch (SOMToolboxException e) {
                e.printStackTrace();
            }

            double scaleX = (double) width / (double) overlayVisualisation.getWidth();
            double scaleY = (double) height / (double) overlayVisualisation.getHeight();
            op = new AffineTransformOp(AffineTransform.getScaleInstance(scaleX, scaleY), new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            res = op.filter(overlayVisualisation, null);
            g = (Graphics2D) res.getGraphics();
        }
        // otherwise create an empty image
        else {
            res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            g = (Graphics2D) res.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
        }
        return res;
    }

    /**
     * Colours SOM units according to the given bin assignment.
     * 
     * @param g thingy to draw on
     * @param binAssignment assignment of single bins
     * @param binPalette the palette to draw the bins with
     */
    private void colourUnits(Graphics2D g, int[][] binAssignment, Color[] binPalette) {
        for (int j = 0; j < binAssignment.length; j++) {
            for (int k = 0; k < binAssignment[j].length; k++) {
                if (binAssignment[j][k] != -1) {
                    g.setColor(binPalette[binAssignment[j][k]]);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(j * unitWidth, k * unitWidth, unitWidth, unitHeight);
            }
        }
    }

    /**
     * Draw the line for the current line or component index. Note that this one's not snapped.
     * 
     * @param g thingy to draw on
     * @param lineIndex the line to draw
     */
    private void drawLineAndStation(Graphics2D g, int lineIndex) {
        for (int stopIndex = 0; stopIndex < binCentres[lineIndex].length; stopIndex++) {
            if (stopIndex < binCentres[lineIndex].length - 1) {
                drawComponentLineSegment(binCentres[lineIndex][stopIndex], binCentres[lineIndex][stopIndex + 1], g, lineIndex, false);
            }

            // TODO make drawing stations a function this is used twice (at least)
            g.setColor(Color.BLACK);
            int x = (int) (binCentres[lineIndex][stopIndex].getX() * new Double(unitWidth).doubleValue() + new Double(unitWidth).doubleValue() / 2);
            int y = (int) (binCentres[lineIndex][stopIndex].getY() * new Double(unitHeight).doubleValue() + new Double(unitHeight).doubleValue() / 2);
            g.fillOval(new Double(x).intValue() - radius, new Double(y).intValue() - radius, radius * 2, radius * 2);
            g.setColor(colorMap.getColor(lineIndex));
            // make this one a station if the last one had the same coordinates (i.e. multiple consecutive stops on the same coordinates)
            if (stopIndex > 1 && binCentres[lineIndex][stopIndex].getX() == binCentres[lineIndex][stopIndex - 1].getX()
                    && binCentres[lineIndex][stopIndex].getY() == binCentres[lineIndex][stopIndex - 1].getY()) {
                g.setColor(Color.WHITE);
            }
            g.fillOval(new Double(x).intValue() - innerRadius, new Double(y).intValue() - innerRadius, innerRadius * 2, innerRadius * 2);
        }
    }

    private void drawLine(Graphics2D g, int lineIndex, Point2D[][] binCentres) {
        for (int stopIndex = 0; stopIndex < binCentres[lineIndex].length - 1; stopIndex++) {
            drawComponentLineSegment(binCentres[lineIndex][stopIndex], binCentres[lineIndex][stopIndex + 1], g, lineIndex, true);
        }
    }

    /**
     * Create the layout for the metro lines (not snapped). This method possibly colours the units of the SOM according to their bin assignments, that
     * is if only one component or component group is selected.
     * 
     * @param g piece of paper
     * @param layer layer of the SOM
     * @param drawLine colouring only or drawing the actual lines too (you always have a choice after all)
     */
    private void createLayout(Graphics2D g, GrowingLayer layer, boolean drawLine) {
        ComponentLine2DDistance dist = new ComponentLine2DDistance(lineDistanceFunction);
        for (int i = 0; i < binCentres.length; i++) {
            // if selection -> draw only selected lines
            // draw component plane if we have only one component selected, and no selection / aggregation was done
            // FIXME: we should also draw the component plane in case of aggregation, but then we need to derive the "real" selected component
            // index
            // FIXME: still -- we now assign the bin assignment of the closest line to the cluster centre, probably not the
            // best choice but it will do for now
            if (ArrayUtils.isEmpty(selectedComponentIndices) || ArrayUtils.contains(selectedComponentIndices, i)) {
                int binAssignmentIndex = -1;
                if (clusters != null) {
                    Cluster<ComponentLine2D> cluster = clusters.get(i);
                    binAssignmentIndex = cluster.get(dist.getIndexOfLineClosestToMean(cluster)).getIndex();
                }
                if (selectedComponents != null) {
                    binAssignmentIndex = (selectedComponents.get(i)).getIndex().intValue();
                }
                // colour units in here (only if a single component is selected)
                if (!ArrayUtils.isEmpty(selectedComponentIndices) && selectedComponentIndices.length == 1) { // && selectionMode == Mode.NONE){
                    binAssignmentIndex = (binAssignmentIndex == -1) ? i : binAssignmentIndex;
                    int[][] binAssignment = layer.getBinAssignment(binAssignmentIndex, numberOfBins);
                    colourUnits(g, binAssignment, getPalette());
                }
                if (drawLine) {
                    drawLineAndStation(g, i);
                }
            }
        }
    }

    /**
     * Get a Hastable[] containing the per-dimension directions to draw on each unit of the som. This includes units not having a stop, i.e. this also
     * handles long links across empty units (and if you think it's a mess now, you should've seen it earlier on).
     * 
     * @return Hashtable[] containing the outgoing lines in each unit for all dimensions
     */
    // FIXME other way around this? Rudi?
    @SuppressWarnings("unchecked")
    private Hashtable<Point2D, int[]>[] getAllDirections() {

        // with crap like this you never know
        boolean debug = false;
        long start = System.currentTimeMillis();

        Hashtable<Point2D, int[]>[] allDirections = new Hashtable[binCentres.length];

        for (int l = 0; l < binCentres.length; l++) { // for each component
            Hashtable<Point2D, int[]> outgoingLineTable = new Hashtable<Point2D, int[]>(); // find counts for parallel lines
            Point2D[] snappedLine = binCentres[l];
            // ok, heres the deal:
            // for each line stop on the line we store the in and outgoing directions (that's two for each stop)
            // then we go look for units that have a line on them but no stop (this can happen in both directions
            // on each stop and include stops that lie on the same coordinates which is annoying like hell)
            // we also add units found this way
            for (int m = 0; m < snappedLine.length; m++) {
                Point2D currentPoint = snappedLine[m];
                Point2D backwardPoint = null;
                Point2D backwardCoords = null;
                Point2D forwardPoint = null;
                Point2D forwardCoords = null;
                int[] forwardDirections = null;
                int[] backwardDirections = null;

                // now we initialise some of these guys depending on
                // whether we're on the first or last stop or somewhere
                // along the line

                if (m > 0) {
                    backwardPoint = snappedLine[m - 1];
                    backwardCoords = new Point2D.Double();
                    backwardDirections = getDirectionArray(currentPoint, backwardPoint);
                    backwardCoords = getNextCoords(currentPoint, backwardDirections);

                    if (!(m < snappedLine.length - 1)) {
                        if (outgoingLineTable.get(currentPoint) != null) {
                            outgoingLineTable.put(currentPoint, VectorTools.mergeArrays(backwardDirections, outgoingLineTable.get(currentPoint)));
                        } else {
                            outgoingLineTable.put(currentPoint, backwardDirections);
                        }
                    }
                }

                if (m < snappedLine.length - 1) {
                    forwardPoint = snappedLine[m + 1];
                    forwardCoords = new Point2D.Double();
                    forwardDirections = getDirectionArray(currentPoint, forwardPoint);
                    forwardCoords = getNextCoords(currentPoint, forwardDirections);
                    if (!(m > 0)) {
                        if (outgoingLineTable.get(currentPoint) != null) {
                            outgoingLineTable.put(currentPoint, VectorTools.mergeArrays(forwardDirections, outgoingLineTable.get(currentPoint)));
                        } else {
                            outgoingLineTable.put(currentPoint, forwardDirections);
                        }
                    }
                }

                // for this component we need no processing other than that
                // it can only go in two directions when lying on a line (needed to say that)
                // of it's not an endpoint of a line, add the line itself
                if (backwardPoint != null && forwardPoint != null) {
                    int[] mergedDirections = VectorTools.mergeArrays(forwardDirections, backwardDirections);
                    if (outgoingLineTable.get(currentPoint) != null) {
                        mergedDirections = VectorTools.mergeArrays(mergedDirections, outgoingLineTable.get(currentPoint));
                    }
                    if (debug) {
                        System.out.println(l + " " + currentPoint + " " + ArrayUtils.toString(mergedDirections) + "(direct add - no border stop)");
                    }
                    outgoingLineTable.put(currentPoint, mergedDirections);
                }

                // then go for the forward stops and units in between stops
                if (forwardCoords != null) {
                    while (!(forwardPoint.getX() == forwardCoords.getX() && forwardPoint.getY() == forwardCoords.getY())) {
                        if (outgoingLineTable.get(forwardCoords) != null) {
                            if (debug) {
                                System.out.println(l + " adding: " + forwardCoords + " "
                                        + ArrayUtils.toString(VectorTools.mergeArrays(forwardDirections, outgoingLineTable.get(forwardCoords)))
                                        + "(up found)");
                            }
                            outgoingLineTable.put(forwardCoords, VectorTools.mergeArrays(forwardDirections, outgoingLineTable.get(forwardCoords)));
                        } else {
                            outgoingLineTable.put(forwardCoords, forwardDirections);
                            if (debug) {
                                System.out.println(l + " adding: " + forwardCoords + " " + ArrayUtils.toString(forwardDirections) + "(up new)");
                                System.out.println(currentPoint);
                                System.out.println(forwardPoint);
                                System.out.println(forwardCoords);
                            }
                        }
                        forwardCoords = getNextCoords(forwardCoords, forwardDirections);
                    }
                }
                // then go for the backward stops and units in between stops
                if (backwardCoords != null) {
                    backwardCoords = getNextCoords(currentPoint, backwardDirections);
                    while (!(backwardPoint.getX() == backwardCoords.getX() && backwardPoint.getY() == backwardCoords.getY())) {
                        if (outgoingLineTable.get(backwardCoords) != null) {
                            if (debug) {
                                System.out.println(l + " adding: " + backwardCoords + " "
                                        + ArrayUtils.toString(VectorTools.mergeArrays(backwardDirections, outgoingLineTable.get(backwardCoords)))
                                        + "(down found)");
                            }
                            outgoingLineTable.put(backwardCoords, VectorTools.mergeArrays(backwardDirections, outgoingLineTable.get(backwardCoords)));
                        } else {
                            outgoingLineTable.put(backwardCoords, backwardDirections);
                            if (debug) {
                                System.out.println(l + " adding: " + backwardCoords + " " + ArrayUtils.toString(backwardDirections) + "(down new)");
                            }
                        }
                        backwardCoords = getNextCoords(backwardCoords, backwardDirections);
                    }
                }
            }
            allDirections[l] = outgoingLineTable;
        }
        if (debug) {
            System.out.println(DateUtils.formatDuration(System.currentTimeMillis() - start));
        }
        return allDirections;
    }

    /**
     * Get a Hashtable of unit coordinates and the number of centres which lie on it.
     * 
     * @return
     */
    private Hashtable<Point2D, Integer> getUnitsWithStopsOnThemAndHowMany() {
        Hashtable<Point2D, Integer> unitsWithStopsOnThemAndHowMany = new Hashtable<Point2D, Integer>();
        for (int i_bin = 0; i_bin < numberOfBins; i_bin++) {
            for (int i_components = 0; i_components < allDirections.length; i_components++) {
                // check for selected components
                if (!ArrayUtils.isEmpty(selectedComponentIndices) && !ArrayUtils.contains(selectedComponentIndices, i_components)) {
                    continue;
                }
                Point2D currentCentre = binCentres[i_components][i_bin];
                if (unitsWithStopsOnThemAndHowMany.get(currentCentre) != null) {
                    unitsWithStopsOnThemAndHowMany.put(currentCentre, unitsWithStopsOnThemAndHowMany.get(currentCentre) + 1);
                } else {
                    unitsWithStopsOnThemAndHowMany.put(currentCentre, new Integer(1));
                }
            }
        }
        return unitsWithStopsOnThemAndHowMany;
    }

    /**
     * Gets components which have links in the given direction.
     * 
     * @param currentSOMUnit
     * @param i_direction
     * @return
     */
    private int[] getComponentsInDirection(Point2D currentSOMUnit, int i_direction) {
        int[] componentsInDirection = new int[allDirections.length];
        // get all four on this unit
        // then get the number of links in directions per component
        // mcgyver together the number of links per component
        // only draw for selected components
        for (int i_components = 0; i_components < allDirections.length; i_components++) {
            Hashtable<Point2D, int[]> outgoingLineTable = allDirections[i_components];
            int[] directions = outgoingLineTable.get(currentSOMUnit);
            // check for selected indices and if so only take into account the selected ones
            if (directions == null || (!ArrayUtils.isEmpty(selectedComponentIndices) && !ArrayUtils.contains(selectedComponentIndices, i_components))) {
                componentsInDirection[i_components] = 0;
                continue;
            }
            componentsInDirection[i_components] = directions[i_direction];
        }
        return componentsInDirection;
    }

    /**
     * Organises all metro drawing for snapped lines, i.e. the layouting for parallel lines.
     * 
     * @param g thingy to draw on
     */
    private void createSnappedMetroLayout(Graphics2D g, GrowingLayer layer) {
        // first initialise our caches
        allDirections = getAllDirections();
        unitsWithStopsOnThemAndHowMany = getUnitsWithStopsOnThemAndHowMany();
        // int[][] directionsCache = new int[8][allDirections.length];

        for (Point2D currentSOMUnit : allSOMCoordinates) { // we do this for each unit of the som
            // we now draw for all directions so that stops can be handled in this (one) loop
            for (int i_direction = 0; i_direction < 8; i_direction++) {
                // adasfdafs
                int compCounter = 0; // counts the number of components to be drawn on this unit
                int[] componentsInDirection = getComponentsInDirection(currentSOMUnit, i_direction);

                double numberOfLines = VectorTools.calculateArraySum(componentsInDirection);
                double multiLineOffset = ((numberOfLines - 1) * lineOffset) / 2;
                double xDir = 0;
                double yDir = 0;
                double xOffset = 0;
                double yOffset = 0;

                // TODO this is flipped, doesn't hurt, but is kind of strange and makes it differ from matlab
                // for (int i_components = componentsInDirection.length - 1; i_components >= 0; i_components--) {
                for (int i_components = 0; i_components < componentsInDirection.length; i_components++) {
                    if (componentsInDirection[i_components] == 0) {
                        continue;
                    }
                    if (i_direction == 0) {
                        xDir = 0;
                        yDir = -1;
                        xOffset = lineOffset * compCounter - multiLineOffset;
                        yOffset = 0;
                    }
                    if (i_direction == 1) {
                        xDir = 1;
                        yDir = -1;
                        xOffset = (lineOffset * compCounter - multiLineOffset) / Math.sqrt(2);
                        yOffset = (lineOffset * compCounter - multiLineOffset) / Math.sqrt(2);
                    }
                    if (i_direction == 2) {
                        xDir = 1;
                        yDir = 0;
                        xOffset = 0;
                        yOffset = lineOffset * compCounter - multiLineOffset;
                    }
                    if (i_direction == 3) {
                        xDir = 1;
                        yDir = 1;
                        xOffset = +(lineOffset * compCounter - multiLineOffset) / Math.sqrt(2);
                        yOffset = -(lineOffset * compCounter - multiLineOffset) / Math.sqrt(2);
                    }
                    // Finally, draw the line

                    Point2D startPoint = new Point2D.Double(currentSOMUnit.getX() + xOffset, currentSOMUnit.getY() + yOffset);
                    Point2D endPoint = new Point2D.Double(currentSOMUnit.getX() + xDir + xOffset, currentSOMUnit.getY() + yDir + yOffset);
                    drawComponentLineSegment(startPoint, endPoint, g, i_components, false);
                    compCounter++;

                    // handle stops (i.e. region centres)
                    for (int i_bin = 0; i_bin < numberOfBins; i_bin++) {
                        Point2D currentCentre = binCentres[i_components][i_bin];
                        // draw no stops if this is gonna be a station anyways (stops with more than two centres on them are stations)
                        if (!(currentCentre.getX() == currentSOMUnit.getX() && currentCentre.getY() == currentSOMUnit.getY())
                                || (unitsWithStopsOnThemAndHowMany.get(currentSOMUnit) != null && unitsWithStopsOnThemAndHowMany.get(currentSOMUnit) > 1)
                                || (yOffset == 0 && xOffset == 0) && numberOfLines > 1) {
                            continue;
                        }
                        int x = (int) ((currentCentre.getX()) * unitWidth + unitWidth / 2);
                        int y = (int) ((currentCentre.getY() + yOffset) * unitHeight + unitHeight / 2);

                        // filter out the units that have several lines but only one centre of them
                        g.setColor(Color.BLACK);
                        g.fillOval(new Double(x).intValue() - radius, new Double(y).intValue() - radius, radius * 2, radius * 2);
                        g.setColor(colorMap.getColor(i_components));
                        g.fillOval(x - innerRadius, y - innerRadius, innerRadius * 2, innerRadius * 2);
                    }
                }
            }
        }
        // now go for the stations, i.e. direction changes for more than two lines
        // or more than two stops on one coordinate
        createStations(g);
    }

    /**
     * Draw metro stations on top of existing maps. A metro station is drawn as a large ellipse with dark border. In contrast to normal stops which
     * are drawn on the bin centres, stations are drawn whenever: 1) several bin centres are located on the same unit 2) lines that were parallel are
     * not so anymore (merging of lines) 3) several lines have stops on the same unit
     * 
     * @param g thingy to draw on
     */
    private void createStations(Graphics2D g) {
        for (Point2D currentSOMUnit : allSOMCoordinates) { // we do this for each unit of the som
            int[][] directionsCache = new int[8][allDirections.length];

            boolean isStation = false;
            for (int i_direction = 0; i_direction < 8; i_direction++) {
                int[] componentInDirections = getComponentsInDirection(currentSOMUnit, i_direction);
                directionsCache[i_direction] = componentInDirections;

                double numberOfLines = VectorTools.calculateArraySum(componentInDirections);
                if (numberOfLines > 0) {
                    // do we have more than one bincentre on this unit? if so it's gonna be a stop anyways
                    if (unitsWithStopsOnThemAndHowMany.get(currentSOMUnit) != null && unitsWithStopsOnThemAndHowMany.get(currentSOMUnit) > 1) {
                        isStation = true;
                        continue;
                    }
                    int matches = 0;
                    for (int i_direction_comparison = i_direction + 1; i_direction_comparison < 8; i_direction_comparison++) {
                        int[] componentsInDirectionComparison = getComponentsInDirection(currentSOMUnit, i_direction_comparison);
                        // double numberOfLinesComparison = calculateArraySum(directionsPerComponentComparison);
                        if (allDirections.length == VectorTools.calculateArrayOverlaps(componentInDirections, componentsInDirectionComparison)) {
                            matches++;
                        } else if (VectorTools.calculateArraySum(componentsInDirectionComparison) > 0) {
                            isStation = true;
                            continue;
                        }
                    }
                    if (matches > 1) {
                        isStation = true;
                    }
                }
            }
            if (isStation) {
                g.setColor(Color.WHITE);
                int x = (int) (currentSOMUnit.getX() * unitWidth + unitWidth / 2);
                int y = (int) (currentSOMUnit.getY() * unitHeight + unitHeight / 2);

                g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                // g.setColor(colorMap.getColor(i_component));
                g.fillOval(x - innerRadius, y - innerRadius, innerRadius * 2, innerRadius * 2);

                int[] stationLayout = new int[] { 0, 0, 0, 0 };

                stationLayout[0] = Math.max(VectorTools.calculateArraySum(directionsCache[2]), VectorTools.calculateArraySum(directionsCache[6]));
                stationLayout[1] = Math.max(VectorTools.calculateArraySum(directionsCache[3]), VectorTools.calculateArraySum(directionsCache[7]));

                stationLayout[2] = Math.max(VectorTools.calculateArraySum(directionsCache[0]), VectorTools.calculateArraySum(directionsCache[4]));
                stationLayout[3] = Math.max(VectorTools.calculateArraySum(directionsCache[1]), VectorTools.calculateArraySum(directionsCache[5]));

                int i_length = VectorTools.getIndexOfMaxValue(stationLayout);
                int i_width = 0;
                if (i_length == 0) {
                    i_width = 2;
                } else if (i_length == 2) {
                    i_width = 0;
                } else if (i_length == 1) {
                    i_width = 3;
                } else if (i_length == 3) {
                    i_width = 1;
                    // }
                }

                double len = stationLayout[i_length];
                double wid = Math.max(1, stationLayout[i_width]);
                double x_offset = 0;
                double y_offset = 0;
                double len_draw = len * .6;

                // % the rotation of the ellipse
                double rotation = 0;
                double tmp;

                if (i_length == 0) { // 0 -- up
                    x_offset = 0;
                    y_offset = lineOffset * len_draw;
                } else if (i_length == 1) { // 1 -- right upper
                    x_offset = lineOffset * len_draw / Math.sqrt(2);
                    y_offset = lineOffset * len_draw / Math.sqrt(2);
                    rotation = 3;
                    tmp = wid;
                    wid = len;
                    len = tmp;
                } else if (i_length == 2) { // 2 -- right
                    x_offset = lineOffset * len / 2;
                    y_offset = 0;
                    tmp = wid;
                    wid = len;
                    len = tmp;
                } else if (i_length == 3) { // 3 -- right lower
                    x_offset = lineOffset * len_draw / Math.sqrt(2);
                    y_offset = -(lineOffset * len_draw / Math.sqrt(2));
                    rotation = 1;
                    tmp = wid;
                    wid = len;
                    len = tmp;
                }

                double hhh = ((x_offset + wid) / (lineThickness * scale));
                double www = ((y_offset + len) / (lineThickness * scale));

                // make ellipses bigger than underlying stops
                scale *= 1.2d;
                // % this is the outer black ellipse
                g.setColor(Color.BLACK);

                Ellipse2D ellipse = new Ellipse2D.Double(x - hhh * radius * radius * scale / 2, y - www * radius * radius * scale / 2, hhh * radius
                        * radius * scale, www * radius * radius * scale);

                // ok, here's how rotation works:
                // 1. rotate the whole world
                // 2. draw the ellipse (not rotated)
                // 3. backrotate the whole world

                AffineTransform tx = new AffineTransform();
                tx.rotate(Math.toRadians(45 * rotation), x, y);
                g.draw(tx.createTransformedShape(ellipse));
                g.setColor(STATION_FILL_COLOUR);
                g.fill(tx.createTransformedShape(ellipse));
                tx.rotate(Math.toRadians(-45 * rotation), x, y);
            }
        }
    }

    /**
     * We do the line selection in here.
     * 
     * @param layer
     * @return new bin centres for selected lines only
     * @throws SOMToolboxException
     */
    private Point2D[][] doSelection(Point2D[][] binCentres, GrowingLayer layer) throws SOMToolboxException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting component selection, mode " + selectionMode.displayName);
        ArrayList<ComponentRegionCount> numberOfRegions = layer.getNumberOfRegions(numberOfBins);
        Collections.sort(numberOfRegions);
        if (selectionMode == Mode.TARGET_NUMBER_OF_COMPONENTS) {
            selectedComponents = numberOfRegions.subList(0, selectionTargetNumberOfComponents);
        } else if (selectionMode == Mode.THRESHOLD) {
            selectedComponents = new ArrayList<ComponentRegionCount>();
            for (int i = 0; i < numberOfRegions.size(); i++) {
                ComponentRegionCount region = numberOfRegions.get(i);
                if (region.getFactor(numberOfBins) < selectionSimilarity) {
                    selectedComponents.add(region);
                }
            }
        } else {
            throw new SOMToolboxException("Illegal selection method");
        }
        Point2D[][] selectedBinCentres = new Point2D[selectedComponents.size()][];
        for (int i = 0; i < selectedComponents.size(); i++) {
            int regionIndex = (selectedComponents.get(i)).getIndex().intValue();
            selectedBinCentres[i] = binCentres[regionIndex];
        }
        if (this.binCentres.length != selectedBinCentres.length) {
            metroMapControlPanel.initLegendTableAfterSelection(selectedComponents);
        }
        return selectedBinCentres;
    }

    /**
     * Clustering of metro lines is done in here (i.e. aggregation step).
     * 
     * @param layer
     * @return new, aggregated bin centres
     * @throws SOMToolboxException
     */
    private Point2D[][] doAggregation(ArrayList<ComponentLine2D> binCentresAsList, GrowingLayer layer) throws SOMToolboxException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting component aggregation, mode " + aggregationMode.displayName);
        ClusterElementFunctions<ComponentLine2D> dist = new ComponentLine2DDistance(lineDistanceFunction);

        // check if we have the clustering in the cache
        String key = getClusteringKey(dist.getClass().getName());
        // System.out.println("Cluster cache contains: " + clusterCache.keySet());
        if (clusterCache.containsKey(key)) {
            clusters = clusterCache.get(key).getClusters();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Retrieving clustering for key '" + key + "' from cache.");
        } else { // make a new clustering
            WardClustering<ComponentLine2D> wardClustering = null;
            if (aggregationMode == Mode.TARGET_NUMBER_OF_COMPONENTS) {
                wardClustering = new WardClustering<ComponentLine2D>(dist, aggregationTargetNumberOfComponents);
                // clusters = WardClustering.wardClustering(binCentresAsList, aggregationTargetNumberOfComponents, lineDistanceFunction);
            } else if (aggregationMode == Mode.THRESHOLD) {
                wardClustering = new WardClustering<ComponentLine2D>(dist, aggregationSimilarity);
                // clusters = WardClustering.wardClustering(binCentresAsList, aggregationSimilarity, lineDistanceFunction);
            } else {
                throw new SOMToolboxException("Illegal aggregion method");
            }

            // do multi-threading
            int cpus = Runtime.getRuntime().availableProcessors();
            if (cpus > 1) { // use 2 on dual core, 3 on quad core, and n-2 on higher
                wardClustering.setNumberOfCPUs(cpus == 2 ? cpus : cpus == 4 ? 3 : cpus - 2);
            }

            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting clustering for key '" + key + "'.");
            clusters = wardClustering.doCluster(binCentresAsList);
            clusterCache.put(key, wardClustering);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Adding clustering for key '" + key + "' to cache.");
        }

        Point2D[][] aggregatedBinCentres = new Point2D[clusters.size()][numberOfBins];
        for (int i = 0; i < aggregatedBinCentres.length; i++) {
            Cluster<ComponentLine2D> cluster = clusters.get(i);
            aggregatedBinCentres[i] = dist.meanObject(cluster).getPoints();
        }
        if (this.binCentres.length != aggregatedBinCentres.length || ArrayUtils.isEmpty(selectedComponentIndices)) {
            metroMapControlPanel.initLegendTableAfterAggregation(clusters);
        }
        return aggregatedBinCentres;
    }

    private String getClusteringKey(String dist) throws SOMToolboxException {
        if (aggregationMode == Mode.TARGET_NUMBER_OF_COMPONENTS) {
            return aggregationMode + "_" + dist + "_" + aggregationTargetNumberOfComponents;
        } else if (aggregationMode == Mode.THRESHOLD) {
            return aggregationMode + "_" + dist + "_" + aggregationSimilarity;
        } else {
            throw new SOMToolboxException("Illegal aggregion method");
        }
    }

    /**
     * We do snap as well.
     * 
     * @param binCentres
     * @param layer
     * @return new bin centres (containing int coordinates only)
     * @throws SOMToolboxException
     */
    private Point2D[][] doSnapping(Point2D[][] binCentres, GrowingLayer layer) throws SOMToolboxException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Starting snapping process");
        Point2D[][] snappedBinCentres = new Point2D[binCentres.length][numberOfBins];
        for (int i_components = 0; i_components < binCentres.length; i_components++) {
            snappedBinCentres[i_components] = snap(binCentres[i_components], layer.getXSize(), layer.getYSize());
        }
        Point2D[][] snappedCentres = snappedBinCentres;
        Hashtable<Point2D, int[]> outgoingLineTable = new Hashtable<Point2D, int[]>(); // find counts for parallel lines
        for (Point2D[] snappedLine : snappedCentres) { // for each component
            outgoingLineTable = this.countOutgoingLines(snappedLine, outgoingLineTable);
        }
        return snappedBinCentres;
    }

    /**
     * Performs the computation of the new bin centres, therefore component aggregation, selection as well as snapping are handled
     * 
     * @param layer
     * @return new Point[][] of bin centres
     * @throws SOMToolboxException
     */
    private Point2D[][] computeFinalComponentLines(GrowingLayer layer) throws SOMToolboxException {
        // that's the original bin centres (no snapping, no selection, no aggregation)
        Point2D[][] binCentres = layer.getBinCentres(numberOfBins);
        ArrayList<ComponentLine2D> binCentresAsList = layer.getBinCentresAsList(numberOfBins);

        // reset colour legend if there is no aggregation & selection, but we
        // obviously had one before (and thus different array lengths)
        if (aggregationMode == Mode.NONE && selectionMode == Mode.NONE && this.binCentres != null && binCentres.length != this.binCentres.length) {
            metroMapControlPanel.initLegendTableNormal();
        }

        // do component selection
        if (selectionMode != Mode.NONE) {
            binCentres = doSelection(binCentres, layer);
        } else {
            selectedComponents = null;
        }

        // do component aggregation
        if (aggregationMode != Mode.NONE) {
            binCentres = doAggregation(binCentresAsList, layer);
        } else {
            clusters = null;
        }

        // do snapping
        if (snapping) {
            binCentres = doSnapping(binCentres, layer);
        }
        return binCentres;
    }

    /**
     * Draws a line segment for the given component.
     * 
     * @param begin begin coordinates
     * @param end end coordinates
     * @param component does it for a given component
     * @param keepCurrentStroke whether or not to keep the current line stroke (and not modify it to use e.g. dashes, ...; used when for exporting
     *            small images)
     */
    private void drawComponentLineSegment(Point2D begin, Point2D end, Graphics2D g, int component, boolean keepCurrentStroke) {
        Path2D path = new Path2D.Double();
        g.setColor(colorMap.getColor(component));
        Point2D p = begin;
        int x = (int) (p.getX() * unitWidth + unitWidth / 2);
        int y = (int) (p.getY() * unitHeight + unitHeight / 2);
        Point2D lastP = end;
        int xLast = (int) (lastP.getX() * unitWidth + unitWidth / 2);
        int yLast = (int) (lastP.getY() * unitHeight + unitHeight / 2);
        path.append(new Line2D.Double(new Point2D.Double(x, y), new Point2D.Double(xLast, yLast)), false);

        // handle dashed lines
        float lineThicknessFactor = 1;
        if (component % colorMap.getColourCount() == 0) {
            int j = component / colorMap.getColourCount();
            if (j + 1 > dashPatterns.length) {
                j = dashPatterns.length - 1;
                lineThicknessFactor = lineThicknessFactors[j % lineThicknessFactors.length];
            }
            float[] ds = dashPatterns[j];
            if (!keepCurrentStroke) {
                g.setStroke(new BasicStroke(lineThickness * lineThicknessFactor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, ds, ds[0] / 2));
            }
        }
        g.drawLine(x, y, xLast, yLast);
        if (!keepCurrentStroke) {
            // set stroke back to normal (believe me it's necessary)
            g.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 5f }, 5f));
        }
    }

    /**
     * return the coordinates on the som grid for the next unit in the given direction
     * 
     * @param point start point
     * @param directions directions to next point
     * @return next point as directed to in directions
     */
    private Point2D getNextCoords(Point2D currentPoint, int[] directions) {
        Point2D nextPoint = new Point2D.Double(currentPoint.getX(), currentPoint.getY());
        if (directions[0] == 1) {
            nextPoint.setLocation(currentPoint.getX() + 0, currentPoint.getY() - 1);
        }
        if (directions[1] == 1) {
            nextPoint.setLocation(currentPoint.getX() + 1, currentPoint.getY() - 1);
        }
        if (directions[2] == 1) {
            nextPoint.setLocation(currentPoint.getX() + 1, currentPoint.getY() + 0);
        }
        if (directions[3] == 1) {
            nextPoint.setLocation(currentPoint.getX() + 1, currentPoint.getY() + 1);
        }
        if (directions[4] == 1) {
            nextPoint.setLocation(currentPoint.getX() + 0, currentPoint.getY() + 1);
        }
        if (directions[5] == 1) {
            nextPoint.setLocation(currentPoint.getX() - 1, currentPoint.getY() + 1);
        }
        if (directions[6] == 1) {
            nextPoint.setLocation(currentPoint.getX() - 1, currentPoint.getY() + 0);
        }
        if (directions[7] == 1) {
            nextPoint.setLocation(currentPoint.getX() - 1, currentPoint.getY() - 1);
        }
        return nextPoint;
    }

    /**
     * returns the direction between two nodes based on the following scheme: 0 7 left up up right up 1 \ | / 6 left - * - right 6 / | \ 5 left down
     * down right down 3 4
     * 
     * @param current current node
     * @param next next node to go to
     * @return dir from current to next
     */
    private int getDirection(Point2D current, Point2D next) {
        // this handles the case when this annoying function is called with identical parameters
        int dir = -1;
        // up
        if (current.getX() == next.getX() && current.getY() > next.getY()) {
            dir = 0;
        }
        // right up
        if (current.getX() < next.getX() && current.getY() > next.getY()) {
            dir = 1;
        }
        // right
        if (current.getX() < next.getX() && current.getY() == next.getY()) {
            dir = 2;
        }
        // right down
        if (current.getX() < next.getX() && current.getY() < next.getY()) {
            dir = 3;
        }
        // down
        if (current.getX() == next.getX() && current.getY() < next.getY()) {
            dir = 4;
        }
        // left down
        if (current.getX() > next.getX() && current.getY() < next.getY()) {
            dir = 5;
        }
        // left
        if (current.getX() > next.getX() && current.getY() == next.getY()) {
            dir = 6;
        }
        // left up
        if (current.getX() > next.getX() && current.getY() > next.getY()) {
            dir = 7;
        }
        return dir;
    }

    /**
     * get a direction array for the given points its indices are set to on for outgoing directions and it takes the form given in
     * 
     * @see #getDirection(Point2D, Point2D)
     * @param current current node
     * @param next next node
     * @return array for outgoing lines (boolean in this case)
     */
    private int[] getDirectionArray(Point2D current, Point2D next) {
        int[] directions = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        // handle identical arguments
        if (getDirection(current, next) == -1) {
            return directions;
        }
        directions[getDirection(current, next)] = 1;
        return directions;

    }

    /**
     * update a given direction array for the given points its indices are set to on for outgoing directions and it takes the form given in
     * 
     * @see #getDirection(Point2D, Point2D)
     * @param current current node
     * @param next next node
     * @param directions -
     * @return array for outgoing lines (counts for outgoing directions this time)
     */
    private int[] getDirectionArray(Point2D current, Point2D next, int[] directions) {
        int dir = this.getDirection(current, next);
        // again, if this is called with identical arguments
        if (dir == -1) {
            return directions;
        }
        if (directions[dir] == 0) {
            directions[dir] = 1;
        } else {
            directions[dir] = directions[dir]++;
        }
        return directions;
    }

    private Hashtable<Point2D, int[]> countOutgoingLines(Point2D[] line, Hashtable<Point2D, int[]> outgoingLineTable) {
        int[] directions = { 0, 0, 0, 0, 0, 0, 0, 0 };
        for (int i = 0; i < line.length - 1; i++) {
            int[] storedDirections = outgoingLineTable.get(line[i]);
            if (storedDirections != null) {
                directions = storedDirections;
            }

            directions = getDirectionArray(line[i], line[i + 1], directions);
            outgoingLineTable.put(line[i], directions);
        }
        // TODO loop line in opposite direction to cover all outgoing thingys -- check whether this is realised by checking all 8 directions now, I'd
        // guess it it

        return outgoingLineTable;
    }

    /**
     * Compute matrix for {@link UMatrix} visualisation. // TODO adjust to new overlay visualisation function private DoubleMatrix2D
     * computeUMatrix(GrowingSOM gsom) { uMatrixVisualizer = new UMatrix(); Palette riverPalette = Palettes.getPaletteByName("MetroMap");
     * uMatrixVisualizer.initCache(Palettes.getAvailablePalettes().length); uMatrixVisualizer.setPalette(Palettes.getPaletteIndex(riverPalette),
     * riverPalette); DoubleMatrix2D umatrix = uMatrixVisualizer.createUMatrix(gsom); VectorTools.normalise(umatrix); return umatrix; }
     */

    /*
     * private void drawComponentLine(Point2D[] binCentres, Graphics2D g, int component) { Path2D path = new Path2D.Double(); // draw lines
     * g.setColor(colorMap.getColor(component)); for (int i = 1; i < binCentres.length; i++) { Point2D p = binCentres[i]; int x = (int) (p.getX()
     * unitWidth + unitWidth / 2); int y = (int) (p.getY() unitHeight + unitHeight / 2); Point2D lastP = binCentres[i - 1]; int xLast = (int)
     * (lastP.getX() unitWidth + unitWidth / 2); int yLast = (int) (lastP.getY() unitHeight + unitHeight / 2); path.append(new Line2D.Double(new
     * Point2D.Double(x, y), new Point2D.Double(xLast, yLast)), false); // g.drawLine(x, y, xLast, yLast); } g.draw(path); // draw stops (on top of
     * line) for (int i = 0; i < binCentres.length; i++) { Point2D p = binCentres[i]; int x = (int) (p.getX() unitWidth + unitWidth / 2); int y =
     * (int) (p.getY() unitHeight + unitHeight / 2); g.setColor(Color.BLACK); g.fillOval(x - radius, y - radius, radius 2, radius 2);
     * g.setColor(colorMap.getColor(component)); g.fillOval(x - innerRadius, y - innerRadius, innerRadius 2, innerRadius 2); } }
     */

    /**
     * Returns a snapped line of the given line. Snapping the metro lines means to find a line as similar as possible to the given line, which has all
     * bin centres in the unit centres, and line segments are connected in multiples of 45° degree angles to each other.<br>
     * TODO: Consider disallowing 135° / 315° as too sharp turns.
     */
    private Point2D[] snap(Point2D[] line, int xSize, int ySize) {
        // Snapping process
        // 1. For each bin centre, find the 4 neighbouring Unit locations, thus resulting in bins * 4 points
        // 2. For each point: consider the point as fixed, and find a line that is correctly snapped (only 45°) angles and as near as possible to the
        // original line
        // 3. From the resulting bins * 4 lines, chose the one closest to the original line
        ArrayList<Point2D[]> allSnappedLines = new ArrayList<Point2D[]>();

        for (int i = 0; i < line.length; i++) {
            Point2D[] neighbouringPoints = getNeighbouringUnits(line[i]);
            for (Point2D neighbouringPoint : neighbouringPoints) {
                // find the snapped points forward and backwards from the current bin point
                // this means we will have lines e.g. as follows (for 6 bins, and i == 3)
                // lineSegmentForward = (0/0 0/0 0/0 0/0 x5/y5 x6/y6)
                // lineSegmentBackward = (x1/y1 x2/y2, x3/y3 0/0 0/0 0/0)
                Point2D[] lineSegmentForward = snapPoint(neighbouringPoint, line, i + 1, +1, xSize, ySize, numberOfBins);
                Point2D[] lineSegmentBackward = snapPoint(neighbouringPoint, line, i - 1, -1, xSize, ySize, numberOfBins);

                // then merge them to one line, and set the fixed point
                Point2D[] mergedLine = new Point2D[lineSegmentForward.length];
                for (int k = 0; k < lineSegmentBackward.length; k++) {
                    mergedLine[k] = new Point2D.Double(lineSegmentForward[k].getX() + lineSegmentBackward[k].getX(), lineSegmentForward[k].getY()
                            + lineSegmentBackward[k].getY());
                }
                mergedLine[i] = neighbouringPoint;

                // now if that point is the same as the last one, don't add that one
                // for some strange reason this is required additionally to the condition in the recursion
                if ((i > 1 && (!(line[i].getX() == line[i - 1].getX() && line[i].getY() == line[i - 1].getY())))) {
                    allSnappedLines.add(mergedLine);

                }
            }

        }
        // find the closest snapped line
        double minDist = Double.MAX_VALUE;
        Point2D[] minDistLine = null;
        for (int i = 0; i < allSnappedLines.size(); i++) {
            Point2D[] currentLine = allSnappedLines.get(i);
            double dist = new ComponentLine2DDistance(lineDistanceFunction).distance(line, currentLine);
            if (dist < minDist) {
                minDist = dist;
                minDistLine = currentLine;
            }
        }
        return minDistLine;
    }

    /**
     * Snaps the next point on the line.
     * 
     * @param startPoint the point to start from
     * @param line the line to snap
     * @param currentPosition the current position on the line
     * @param direction forward (1) or backwards (-1)
     * @param xSize x-size of the map
     * @param ySize y-size of the map
     * @param bins number of bins
     * @return a snapped line
     */
    private Point2D[] snapPoint(Point2D startPoint, Point2D[] line, int currentPosition, int direction, int xSize, int ySize, int bins) {
        Point2D[] result = new Point2D[bins];
        if ((currentPosition == -1 && direction == -1) || (currentPosition == bins && direction == 1)) {
            for (int i = 0; i < result.length; i++) {
                result[i] = new Point2D.Double(0, 0);
            }
            return result;
        }

        int startPointCoordinatesSum = (int) (startPoint.getX() + startPoint.getY());
        int startPointCoordinatesDifference = (int) (startPoint.getX() - startPoint.getY());
        double minDistance = Double.MAX_VALUE;
        Point2D closestPoint = null;
        for (int i = 0; i < allSOMCoordinates.length; i++) {
            // find units that are either in the same row (x equal), same column (y equal) or are in a diagonal (sum or diff values equal)
            if (allSOMCoordinates[i].getX() == startPoint.getX() || allSOMCoordinates[i].getY() == startPoint.getY()
                    || allSOMCoordinatesSumValues[i] == startPointCoordinatesSum || allSOMCoordinatesDiffValues[i] == startPointCoordinatesDifference) {
                double currentDistance = allSOMCoordinates[i].distance(line[currentPosition]);
                if (currentDistance < minDistance) {
                    closestPoint = allSOMCoordinates[i];
                    minDistance = currentDistance;
                }
            }
        }
        // compare this startpoint to the last one and check for idendity and don't consider the closest but the point itself for further processing
        // if so
        if (currentPosition > 1 && line[currentPosition].getX() == line[currentPosition - direction].getX()
                && line[currentPosition].getY() == line[currentPosition - direction].getY()) {
            result = snapPoint(startPoint, line, currentPosition + direction, direction, xSize, ySize, bins);
            result[currentPosition] = startPoint;
        } else {
            result = snapPoint(closestPoint, line, currentPosition + direction, direction, xSize, ySize, bins);
            result[currentPosition] = closestPoint;
        }
        return result;
    }

    /** ***************** VISUALISATION IMPROVEMENTS *********************** */
    /*
     * //TODO hm? private Point2D[][] pullOverlapping(Point2D[][] binCentres) { for (int i = 0; i < binCentres.length; i++) { for (int j = 0; j <
     * binCentres[i].length; j++) { Point2D point2D = binCentres[i][j]; for (int k = i; k < binCentres.length; k++) { for (int l = j; l <
     * binCentres[k].length; l++) { Point2D point2D2 = binCentres[k][l]; if (point2D.equals(point2D2) && point2D != point2D2) {
     * System.out.println("found overlapping points: " + point2D + ", " + point2D2); Point2D other2; if (l == 0) { other2 = binCentres[k][l + 1]; }
     * else { other2 = binCentres[k][l - 1]; } double changeX = other2.getX() - point2D2.getX(); double changeY = other2.getY() - point2D2.getY();
     * System.out.println("changes: " + changeX + ", " + changeY); double d = (lineThickness 1.5 / 130d); if (changeX > changeY) { // we are moving
     * vertical --> dislocate point rightwards point2D2.setLocation(point2D2.getX() + d, point2D2.getY()); } else { // otherwise downwards
     * point2D2.setLocation(point2D2.getX(), point2D2.getY() + d); } System.out.println("moved: " + point2D2); } } } } } return binCentres; }
     */
    private String getComponentName(int i) {
        if (inputObjects != null && inputObjects.getTemplateVector() != null) {
            return inputObjects.getTemplateVector().getLabel(i);
        } else {
            return COMP_PREFIX + (i + 1);
        }
    }

    private String[] getComponentNames() {
        String[] items = new String[dim];
        for (int i = 0; i < dim; i++) {
            items[i] = getComponentName(i);
        }
        return items;
    }

    /*
     * private int getComponentIndex(String label) { if (inputObjects != null && inputObjects.getTemplateVector() != null) { return
     * inputObjects.getTemplateVector().getIndex(label); } else { // assume generated component name String s =
     * label.substring(label.indexOf(COMP_PREFIX) + COMP_PREFIX.length()).trim(); return Integer.parseInt(s); } }
     */

    /**
     * A control panel extending the generic {@link AbstractBackgroundImageVisualizer.VisualizationControlPanel}, adding additionally a {@link JList}
     * and a {@link JTextField} for selecting a component from the {@link TemplateVector}.
     * 
     * @author Rudolf Mayer
     */
    private class MetroMapControlPanel extends VisualizationControlPanel implements ActionListener, ChangeListener, ListSelectionListener {
        private static final long serialVersionUID = 1L;

        private JSpinner binSpinner;

        private JSpinner thickNessSpinner;

        // private JComboBox singleComponentSelector;

        private JCheckBox boxSnapping;

        private JRadioButton buttonAggregationSimilarity;

        private JRadioButton buttonAggregationTargetNumberComponents;

        private JSpinner spinnerAggregationTargetNumberComponents;

        private JSpinner spinnerAggregationSimilarity;

        private JRadioButton buttonSelectionTargetNumberComponents;

        private JSpinner spinnerSelectionTargetNumberComponents;

        private JRadioButton buttonSelectionSimilarity;

        private JSpinner spinnerSelectionSimilarity;

        private JRadioButton buttonAggregationNone;

        private JRadioButton buttonSelectionNone;

        private JScrollPane colourLegendScrollPane;

        JComboBox distanceFunctionComboBox;

        /**
         * Constructs a new component-plane control panel
         * 
         * @param vis The ComponentPlanesVisualizer listening to updates from the list box.
         */
        private MetroMapControlPanel(MetroMapVisualizer vis) {
            super("Metro Map Control");

            JPanel metroPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.anchor = GridBagConstraints.NORTHWEST;
            constr.insets = new Insets(2, 2, 2, 2);
            constr.gridy = 0;

            JPanel distanceFunctionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            distanceFunctionPanel.add(new JLabel("Dist.Funct."));
            distanceFunctionComboBox = new JComboBox(DistanceFunctionType.values());
            distanceFunctionComboBox.setFont(reallySmallerFont);
            distanceFunctionComboBox.setSelectedItem(lineDistanceFunction);
            distanceFunctionComboBox.addActionListener(this);
            distanceFunctionPanel.add(distanceFunctionComboBox);

            metroPanel.add(distanceFunctionPanel, constr);
            constr.gridy += 1;

            JPanel spinners = new JPanel(new FlowLayout(FlowLayout.LEFT));
            binSpinner = new JSpinner(new SpinnerNumberModel(numberOfBins, MIN_BINS, MAX_BINS, 1));
            binSpinner.setFont(smallerFont);
            binSpinner.addChangeListener(this);
            spinners.add(new JLabel("# bins "));
            spinners.add(binSpinner);

            thickNessSpinner = new JSpinner(new SpinnerNumberModel(lineThickness, 1, (int) (radius * 1.5), 1));
            thickNessSpinner.addChangeListener(this);
            thickNessSpinner.setFont(smallerFont);
            spinners.add(new JLabel("Line thickn. "));
            spinners.add(thickNessSpinner);

            metroPanel.add(spinners, constr);
            constr.gridy += 1;

            // snapping checkbox
            boxSnapping = new JCheckBox("Snapping");
            boxSnapping.setSelected(snapping);
            boxSnapping.addActionListener(this);
            metroPanel.add(boxSnapping, constr);
            constr.gridy += 1;

            // overlay vis combobox
            ArrayList<String> overlayVisShortnames = new ArrayList<String>();
            overlayVisShortnames.add("Empty");
            overlayVisShortnames.addAll(Arrays.asList(UMatrix.UMATRIX_SHORT_NAMES));
            overlayVisShortnames.addAll(Arrays.asList(FlowBorderlineVisualizer.FLOWBORDER_SHORT_NAMES));
            overlayVisShortnames.add(ThematicClassMapVisualizer.CLASSMAP_SHORT_NAME);
            overlayVisShortnames.add(ThematicClassMapVisualizer.CLASSMAP_SHORT_NAME + "/Chess");
            overlayVisualisationComboBox.setModel(new DefaultComboBoxModel(overlayVisShortnames.toArray()));
            overlayVisualisationComboBox.addActionListener(this);
            overlayVisualisationComboBox.setFont(smallerFont);

            JPanel overlayVisualisationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            overlayVisualisationPanel.add(new JLabel("Overlay Vis"));
            overlayVisualisationPanel.add(overlayVisualisationComboBox);
            metroPanel.add(overlayVisualisationPanel, constr);
            constr.gridy += 1;

            /* component aggregation panel */
            // no aggregation
            buttonAggregationNone = new JRadioButton(Mode.NONE.displayName);
            buttonAggregationNone.setFont(smallerFont);
            buttonAggregationNone.addActionListener(this);
            buttonAggregationNone.setSelected(true);

            // target number
            buttonAggregationTargetNumberComponents = new JRadioButton(Mode.TARGET_NUMBER_OF_COMPONENTS.displayName);
            buttonAggregationTargetNumberComponents.setFont(smallerFont);
            buttonAggregationTargetNumberComponents.addActionListener(this);
            buttonAggregationTargetNumberComponents.setEnabled(false);

            spinnerAggregationTargetNumberComponents = new JSpinner();
            spinnerAggregationTargetNumberComponents.setFont(smallerFont);
            spinnerAggregationTargetNumberComponents.setEnabled(false);
            spinnerAggregationTargetNumberComponents.addChangeListener(this);

            // similarity threshold
            buttonAggregationSimilarity = new JRadioButton(Mode.THRESHOLD.displayName);
            buttonAggregationSimilarity.setFont(smallerFont);
            buttonAggregationSimilarity.addActionListener(this);

            spinnerAggregationSimilarity = new JSpinner(new SpinnerNumberModel(0.5, 0, 1, 0.01));
            spinnerAggregationSimilarity.setFont(smallerFont);
            spinnerAggregationSimilarity.setEnabled(false);
            spinnerAggregationSimilarity.addChangeListener(this);

            ButtonGroup aggregationMethod = new ButtonGroup();
            aggregationMethod.add(buttonAggregationNone);
            aggregationMethod.add(buttonAggregationTargetNumberComponents);
            aggregationMethod.add(buttonAggregationSimilarity);

            GridBagConstraints c2 = new GridBagConstraints();
            c2.gridx = 0;
            c2.gridy = 0;
            c2.fill = GridBagConstraints.HORIZONTAL;
            c2.ipadx = 2;
            JPanel aggregationPanel = new JPanel(new GridBagLayout());
            aggregationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Component Aggregation"),
                    BorderFactory.createEmptyBorder(0, 2, 0, 2)));
            aggregationPanel.add(buttonAggregationNone, c2);
            c2.gridx = 1;
            aggregationPanel.add(buttonAggregationTargetNumberComponents, c2);
            c2.gridx = 2;
            aggregationPanel.add(buttonAggregationSimilarity, c2);
            c2.gridy += 1;
            c2.gridx = 0;
            aggregationPanel.add(new JPanel(), c2);
            c2.gridx = 1;
            aggregationPanel.add(spinnerAggregationTargetNumberComponents, c2);
            c2.gridx = 2;
            aggregationPanel.add(spinnerAggregationSimilarity, c2);

            metroPanel.add(aggregationPanel, constr);
            constr.gridy += 1;

            /* component selection panel */
            c2.gridx = 0;

            // no selection
            buttonSelectionNone = new JRadioButton(Mode.NONE.displayName);
            buttonSelectionNone.setFont(smallerFont);
            buttonSelectionNone.addActionListener(this);
            buttonSelectionNone.setSelected(true);

            // target number
            buttonSelectionTargetNumberComponents = new JRadioButton(Mode.TARGET_NUMBER_OF_COMPONENTS.displayName);
            buttonSelectionTargetNumberComponents.setFont(smallerFont);
            buttonSelectionTargetNumberComponents.addActionListener(this);
            buttonSelectionTargetNumberComponents.setEnabled(false);

            spinnerSelectionTargetNumberComponents = new JSpinner();
            spinnerSelectionTargetNumberComponents.setFont(smallerFont);
            spinnerSelectionTargetNumberComponents.setEnabled(false);
            spinnerSelectionTargetNumberComponents.addChangeListener(this);

            // similarity threshold
            buttonSelectionSimilarity = new JRadioButton(Mode.THRESHOLD.displayName);
            buttonSelectionSimilarity.setFont(smallerFont);
            buttonSelectionSimilarity.addActionListener(this);

            spinnerSelectionSimilarity = new JSpinner(new SpinnerNumberModel(0.5, 0, 1, 0.01));
            spinnerSelectionSimilarity.setFont(smallerFont);
            spinnerSelectionSimilarity.setEnabled(false);
            spinnerSelectionSimilarity.addChangeListener(this);

            ButtonGroup selectionMethod = new ButtonGroup();
            selectionMethod.add(buttonSelectionNone);
            selectionMethod.add(buttonSelectionTargetNumberComponents);
            selectionMethod.add(buttonSelectionSimilarity);

            JPanel selectionPanel = new JPanel(new GridBagLayout());
            selectionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Component Selection"),
                    BorderFactory.createEmptyBorder(0, 2, 0, 2)));
            c2.gridy = 0;
            selectionPanel.add(buttonSelectionNone, c2);
            c2.gridx = 1;
            selectionPanel.add(buttonSelectionTargetNumberComponents, c2);
            c2.gridx = 2;
            selectionPanel.add(buttonSelectionSimilarity, c2);
            c2.gridx = 0;
            c2.gridy += 1;
            selectionPanel.add(new JPanel(), c2);
            c2.gridx = 1;
            selectionPanel.add(spinnerSelectionTargetNumberComponents, c2);
            c2.gridx = 2;
            selectionPanel.add(spinnerSelectionSimilarity, c2);
            metroPanel.add(selectionPanel, constr);
            constr.gridy += 1;

            colourLegendTable1 = new ColourLegendTable(legendColumnNames, this);
            colourLegendTable2 = new ColourLegendTable(legendColumnNames, this);
            JPanel colourLegendPanel = new JPanel(new GridLayout(1, 2));
            colourLegendPanel.add(colourLegendTable1);
            colourLegendPanel.add(colourLegendTable2);
            colourLegendScrollPane = new JScrollPane(colourLegendPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            metroPanel.add(colourLegendScrollPane, constr);
            constr.gridy += 1;

            add(metroPanel, c);
        }

        private void setLegendTableData(final String[] names) {
            endIndexTable1 = (int) Math.round(names.length / legendColumns);
            if (endIndexTable1 > MAX_NUMBER_OF_LEGEND_ENTRIES) {
                return;
            }
            colourLegendTable1.setData(names, colorMap.getColors(names.length), 0, endIndexTable1);
            colourLegendTable2.setData(names, colorMap.getColors(names.length), endIndexTable1, (int) (names.length / legendColumns));
        }

        private void initLegendTableNormal() {
            setLegendTableData(getComponentNames());
        }

        private void initLegendTableAfterAggregation(List<? extends Cluster<ComponentLine2D>> clusters) {
            String[] items = new String[clusters.size()];
            for (int i = 0; i < clusters.size(); i++) {
                Cluster<ComponentLine2D> cluster = clusters.get(i);
                String mergedName = "";
                for (int j = 0; j < cluster.size(); j++) {
                    ElementWithIndex line = cluster.get(j);
                    mergedName += getComponentName(line.getIndex());
                    if (j + 1 < cluster.size()) {
                        mergedName += " / ";
                    }
                }
                items[i] = mergedName;
            }
            setLegendTableData(items);
        }

        private void initLegendTableAfterSelection(List<ComponentRegionCount> selectedComponents) {
            String[] items = new String[selectedComponents.size()];
            for (int i = 0; i < selectedComponents.size(); i++) {
                ComponentRegionCount region = selectedComponents.get(i);
                String componentName = getComponentName(region.getIndex().intValue());
                items[i] = componentName;
            }
            setLegendTableData(items);
        }

        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            // aggregation radio buttons
            try {
                if (source == buttonAggregationNone && aggregationMode != Mode.NONE) {
                    spinnerAggregationSimilarity.setEnabled(false);
                    spinnerAggregationTargetNumberComponents.setEnabled(false);
                    aggregationMode = Mode.NONE;
                    updateVis();
                } else if (source == buttonAggregationTargetNumberComponents && aggregationMode != Mode.TARGET_NUMBER_OF_COMPONENTS) {
                    spinnerAggregationSimilarity.setEnabled(false);
                    spinnerAggregationTargetNumberComponents.setEnabled(true);
                    aggregationMode = Mode.TARGET_NUMBER_OF_COMPONENTS;
                    updateVis();
                } else if (source == buttonAggregationSimilarity && aggregationMode != Mode.THRESHOLD) {
                    spinnerAggregationSimilarity.setEnabled(true);
                    spinnerAggregationTargetNumberComponents.setEnabled(false);
                    aggregationMode = Mode.THRESHOLD;
                    updateVis();
                }
                // selection radio buttons
                else if (source == overlayVisualisationComboBox) {
                    updateVis();
                } else if (source == buttonSelectionNone && selectionMode != Mode.NONE) {
                    spinnerSelectionSimilarity.setEnabled(false);
                    spinnerSelectionTargetNumberComponents.setEnabled(false);
                    selectionMode = Mode.NONE;
                    updateVis();
                } else if (source == buttonSelectionTargetNumberComponents && selectionMode != Mode.TARGET_NUMBER_OF_COMPONENTS) {
                    spinnerSelectionSimilarity.setEnabled(false);
                    spinnerSelectionTargetNumberComponents.setEnabled(true);
                    selectionMode = Mode.TARGET_NUMBER_OF_COMPONENTS;
                    updateVis();
                } else if (source == buttonSelectionSimilarity && selectionMode != Mode.THRESHOLD) {
                    spinnerSelectionSimilarity.setEnabled(true);
                    spinnerSelectionTargetNumberComponents.setEnabled(false);
                    selectionMode = Mode.THRESHOLD;
                    updateVis();
                } else if (source == boxSnapping && snapping != boxSnapping.isSelected()) {
                    snapping = boxSnapping.isSelected();
                    updateVis();
                } /*
                   * else if (source == boxUMatrix && uMatrix != boxUMatrix.isSelected()) { uMatrix = boxUMatrix.isSelected(); updateVis(); }
                   */
                // distance function box
                else if (source == distanceFunctionComboBox) {
                    lineDistanceFunction = (DistanceFunctionType) distanceFunctionComboBox.getSelectedItem();
                    if (snapping || selectionMode != Mode.NONE || aggregationMode != Mode.NONE) {
                        updateVis();
                    }
                }
            } catch (SOMToolboxException ex) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error creating metro map visualisation: " + ex.getMessage());
            }
        }

        private void updateVis() throws SOMToolboxException {
            binCentres = computeFinalComponentLines(gsom.getLayer());
            // TODO put rest in here
            reInitVis();
        }

        public void stateChanged(ChangeEvent e) {
            JSpinner spinner = (JSpinner) e.getSource();

            SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
            Number number = model.getNumber();
            try {
                if (spinner == binSpinner) {
                    if (number.intValue() != numberOfBins) {
                        numberOfBins = number.intValue();
                        updateVis();
                    }
                } else if (spinner == thickNessSpinner) {
                    if (number.intValue() != lineThickness) {
                        lineThickness = number.intValue();
                        dashPatterns = initDashPatterns();
                        updateVis();
                    }
                } else if (spinner == spinnerAggregationTargetNumberComponents) {
                    if (number.intValue() != aggregationTargetNumberOfComponents) {
                        aggregationTargetNumberOfComponents = number.intValue();
                        updateVis();
                    }
                } else if (spinner == spinnerSelectionTargetNumberComponents) {
                    if (number.intValue() != selectionTargetNumberOfComponents) {
                        selectionTargetNumberOfComponents = number.intValue();
                        updateVis();
                    }
                } else if (spinner == spinnerAggregationSimilarity) {
                    if (number.doubleValue() != aggregationSimilarity) {
                        aggregationSimilarity = number.doubleValue();
                        updateVis();
                    }
                } else if (spinner == spinnerSelectionSimilarity) {
                    if (number.doubleValue() != selectionSimilarity) {
                        selectionSimilarity = number.doubleValue();
                        updateVis();
                    }
                }
            } catch (SOMToolboxException ex) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error creating metro map visualisation: " + ex.getMessage());
            }
        }

        @Override
        public int getPreferredHeight() {
            int height = 275 + colourLegendTable1.getPreferredHeight() + 70;
            if (height < 320) { // min height of 320
                height = 320;
            } else if (height > 550) { // max height of 550
                height = 550;
            }
            return height;
        }

        public void valueChanged(ListSelectionEvent e) {
            final int[] selectedRows = colourLegendTable1.getSelectedRows();
            final int[] selectedRows2 = colourLegendTable2.getSelectedRows();
            for (int i = 0; i < selectedRows2.length; i++) {
                selectedRows2[i] = selectedRows2[i] + endIndexTable1;
            }
            MetroMapVisualizer.this.selectedComponentIndices = ArrayUtils.addAll(selectedRows, selectedRows2);
            reInitVis();
        }
    }

    private void reInitVis() {
        if (visualizationUpdateListener != null) {
            visualizationUpdateListener.updateVisualization();
        }
    }

    private float[][] initDashPatterns() {
        return new float[][] { { 1 }, { 25, lineThickness + 2 }, { 21, lineThickness + 2, 3, lineThickness + 2 }, { 50, (lineThickness + 2) * 2 },
                { 42, lineThickness + 2, 6, lineThickness + 2 } };
    }

    public Color[] getColours(int components) {
        Color[] colors = new Color[components];
        System.arraycopy(COLORS, 0, colors, 0, components);
        return colors;
    }

    public Color getColour(int component) {
        return COLORS[component % COLORS.length];
    }

    /** Finds the four units around the given point. */
    public Point2D[] getNeighbouringUnits(Point2D p) {

        // FIXME what about setting all x values to x if x % 1 == 0?

        Point2D leftUpper = new Point2D.Double((int) p.getX(), (int) p.getY());
        Point2D rightUpper = new Point2D.Double(leftUpper.getX() + 1, leftUpper.getY());
        Point2D leftLower = new Point2D.Double(leftUpper.getX(), leftUpper.getY() + 1);
        Point2D rightLower = new Point2D.Double(leftUpper.getX() + 1, leftUpper.getY() + 1);

        if (p.getX() % 1 == 0) {
            rightUpper.setLocation(leftUpper.getX(), leftUpper.getY());
            rightLower.setLocation(leftUpper.getX(), leftUpper.getY() + 1);
        }
        if (p.getY() % 1 == 0) {
            leftLower.setLocation(leftUpper.getX(), leftUpper.getY());
            rightLower.setLocation(leftUpper.getX() + 1, leftUpper.getY());
        }
        if (p.getX() % 1 == 0 && p.getY() % 1 == 0) {
            rightLower.setLocation(leftUpper.getX(), leftUpper.getY());
        }

        // the order of the points returned here is the same as in the matlab version
        return new Point2D[] { leftUpper, leftLower, rightUpper, rightLower };
    }

    @Override
    public void setInputObjects(SharedSOMVisualisationData inputObjects) {
        super.setInputObjects(inputObjects);
        metroMapControlPanel.initLegendTableNormal();
    }

    @Override
    public void setSOMData(SOMInputReader reader) {
        super.setSOMData(reader);
        dim = reader.getDim();
        int initialValueSpinner = (int) Math.min(10, Math.round(dim / 2d));
        selectionTargetNumberOfComponents = initialValueSpinner;
        aggregationTargetNumberOfComponents = initialValueSpinner;
        int minValue = Math.min(2, dim);
        if (dim > 2) {
            metroMapControlPanel.spinnerAggregationTargetNumberComponents.setModel(new SpinnerNumberModel(aggregationTargetNumberOfComponents,
                    minValue, dim, 1));
            metroMapControlPanel.spinnerSelectionTargetNumberComponents.setModel(new SpinnerNumberModel(selectionTargetNumberOfComponents, minValue,
                    dim, 1));
        }
        metroMapControlPanel.spinnerAggregationTargetNumberComponents.setEnabled(dim > 2);
        metroMapControlPanel.spinnerSelectionTargetNumberComponents.setEnabled(dim > 2);
        metroMapControlPanel.initLegendTableNormal();
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        // FIXME: implement this
        return getVisualizationFlavours(index, gsom, width, height, -1);
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height, int maxFlavours)
            throws SOMToolboxException {
        HashMap<String, BufferedImage> res = new HashMap<String, BufferedImage>();
        // iterate over bins
        int currentNumberOfBins = numberOfBins;
        boolean currentSnapping = snapping;
        for (numberOfBins = 3; numberOfBins < 10; numberOfBins++) {
            String key = "_bins" + numberOfBins;
            snapping = false;
            res.put(key, getVisualization(index, gsom, width, height));
            snapping = true;
            res.put(key + "_snapped", getVisualization(index, gsom, width, height));
        }
        numberOfBins = currentNumberOfBins;
        snapping = currentSnapping;
        return res;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: implement this
        return getVisualizationFlavours(index, gsom, width, height, -1);
    }
    
    private void drawExtrema (Graphics2D g, int width, int height, GrowingSOM gsom, int ypos) {
    	
    	ypos += 60;
    	
    	for (int i = 0; i < binCentres.length; i++) {
    		int xmax=0,ymax=0,xmin=0,ymin=0;
    		DoubleMatrix2D plane = this.createNormalizedComponentPlane(gsom, i);
    		for (int j=0; j < plane.columns(); j++) {
    			for (int k=0; k < plane.rows(); k++) {
    				if (plane.get(j, k)==1) {
    					xmax = j;
    					ymax = k;
    				}
    				if (plane.get(j, k)==0) {
    					xmin = j;
    					ymin = k;
    				}
    			}
    		}
    		g.setColor(colorMap.getColor(i));
			g.drawString("comp" + String.valueOf(i) + ": min auf Position (" + String.valueOf(ymin) + ", " + String.valueOf(xmin) + 
					"), max auf Position (" + String.valueOf(ymax) + ", " + String.valueOf(xmax) + ")", width + 20, ypos);
			ypos+=35;
			
			String posMinOutput ="";
			if (ymin <= plane.rows()/3) {
				posMinOutput += "oben";
			}else if (ymin <= plane.rows()*2/3){
				posMinOutput += "mitte";
			}else {
				posMinOutput += "unten";
			}
			posMinOutput += "-";
			if (xmin <= plane.rows()/3) {
				posMinOutput += "links";
			}else if (xmin <= plane.rows()*2/3){
				posMinOutput += "mitte";
			}else {
				posMinOutput += "rechts";
			}
			
			String posMaxOutput ="";
			if (ymax <= plane.columns()/3) {
				posMaxOutput += "oben";
			}else if (ymax <= plane.columns()*2/3){
				posMaxOutput += "mitte";
			}else {
				posMaxOutput += "unten";
			}
			posMaxOutput += "-";
			if (xmax <= plane.columns()/3) {
				posMaxOutput += "links";
			}else if (xmax <= plane.columns()*2/3){
				posMaxOutput += "mitte";
			}else {
				posMaxOutput += "rechts";
			}
			
			g.drawString("comp" + String.valueOf(i) + " verläuft von " + posMinOutput + " zu " + posMaxOutput, width + 20, ypos);
			ypos+=35;
    	}
    		
    }
    
    private DoubleMatrix2D createNormalizedComponentPlane(GrowingSOM gsom, int component) {
        DoubleMatrix2D plane = new DenseDoubleMatrix2D(gsom.getLayer().getComponentPlane(component, 0));
        plane = plane.viewDice();

        // normalize component plane
        final double minValue = plane.aggregate(Functions.min, Functions.identity);
        final double maxValue = plane.aggregate(Functions.max, Functions.identity);
        plane.assign(new DoubleFunction() {
            public double apply(double argument) {
                return (argument - minValue) / (maxValue - minValue);
            }
        });

        return plane;

    }
    
    private int drawCorrelationResult(Graphics2D g, int width, int height, GrowingLayer layer){
   	
    	double[][] dist = getCorrelation();
    	
    	double strongLimit = (layer.getXSize()+layer.getYSize())/2*binCentres[0].length*0.33;
    	double weakLimit = (layer.getXSize()+layer.getYSize())/2*binCentres[0].length*0.5;
    	
    	g.setFont(new Font("Serif", Font.PLAIN, 30));
    	g.setColor(Color.BLACK);
    	
    	int ypos = 100;
    	
    	for (int i=0; i < dist.length; i++){
    		
    		String corStrong = "";
    		String corWeak = "";
    		for (int j=0; j < dist.length; j++){
    			if (i==j) continue;
    			if (dist[i][j]<=strongLimit){
    				if (!corStrong.isEmpty()) corStrong += ", ";
    				
    				corStrong += "comp" + String.valueOf(j);
    			}else if (dist[i][j]<=weakLimit ){
    				if (!corWeak.isEmpty()) corWeak += ", ";
    				
    				corWeak += "comp" + String.valueOf(j);
    			}
    		}
    		 g.setColor(colorMap.getColor(i));
    		if (!corStrong.isEmpty()){
				g.drawString("comp" + String.valueOf(i) + " korreliert stark mit " + corStrong, width + 20, ypos);
				ypos+=35;
			}
			if (!corWeak.isEmpty()){
				g.drawString("comp" + String.valueOf(i) + " korreliert schwach mit " + corWeak, width + 20, ypos);
				ypos+=35;
			}
    	}
    	return ypos;
    }
    
    private double[][] getCorrelation(){
    	
    	ClusterElementFunctions<ComponentLine2D> distanceFunc = new ComponentLine2DDistance(lineDistanceFunction);
    	
    	double[][] dist = new double[binCentres.length][binCentres.length];
    	
    	for (int i=0; i < binCentres.length - 1; i++) {
    		for (int j=i+1; j < binCentres.length; j++) {
    			double d = distanceFunc.distance(new ComponentLine2D(binCentres[i]), 
    											 new ComponentLine2D(binCentres[j]));
    			dist[i][j]= d;
    			dist[j][i]= d;
    		}
    	}
    	
    	return dist;
    }
}

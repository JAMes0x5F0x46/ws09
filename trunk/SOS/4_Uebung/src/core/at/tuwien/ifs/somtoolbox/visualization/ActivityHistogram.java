package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * This is the implementation of an Activity Histogram.<br>
 * FIXME: there are a lot of common parts with {@link ComponentPlanesVisualizer} ==> make a superclass for both of them.
 * 
 * @author Roman Gerger
 * @author Florian Mistelbauer
 * @author Rudolf Mayer
 * @version $Id: ActivityHistogram.java 2874 2009-12-11 16:03:27Z frank $
 */
class ActivityHistogram extends AbstractItemVisualizer {

    private String dataPoint;

    private InputData inputData;

    public ActivityHistogram() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Activity Histogram" };
        VISUALIZATION_SHORT_NAMES = new String[] { "ActivityHistogram" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Displays a colour-coding of the activity of a certain input vector over the whole map, i.e. in principle the vector's distances to all weight vectors.\nImplemented by Roman Gerger and Florian Mistelbauer" };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR + buildCacheKey("input:" + dataPoint);
    }

    /**
     * Draws the activity histogram. Given one input point it calculates the euclidian distance to each weight vector.
     * 
     * @throws SOMToolboxException
     */
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        if (inputData == null) {
            inputData = gsom.getSharedInputObjects().getInputData();
            if (inputData == null) {
                throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
            } else {
                dataPoint = inputData.getInputDatum(0).getLabel();
            }
        }

        if (!(controlPanel instanceof ActivityHistrogramControlPanel)) {
            // create control panel once we have the input data vector, and if it is a generic panel
            controlPanel = new ActivityHistrogramControlPanel(this, inputData);
        }

        BufferedImage result = null;
        // reading the vector file to fetch the necessary data
        InputDatum item = inputData.getInputDatum(dataPoint);
        L2Metric metric = new L2Metric();
        result = createImage(width, height, 0x00ffffff);

        // calculating the size of a matrix field (width & height in pixels)
        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();
        try {
            // calculating the min and max of the distances
            for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
                for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                    Unit u = gsom.getLayer().getUnit(x, y);
                    double dist = metric.distance(u.getWeightVector(), item);
                    if (dist > maximumMatrixValue)
                        maximumMatrixValue = dist;
                    if (dist < minimumMatrixValue)
                        minimumMatrixValue = dist;
                }
            }

            // retrieving the canvas
            Graphics2D g = (Graphics2D) result.getGraphics();

            // now coloring and calculating the appropriate color based on the palette given
            for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
                for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
                    Unit u = gsom.getLayer().getUnit(x, y);

                    // calculating the distance
                    double dist = metric.distance(u.getWeightVector(), item);

                    // calculating the color according to the palette given
                    int color = (int) Math.round(dist / maximumMatrixValue * palette.length - 1);

                    g.setPaint(palette[color]);
                    g.setColor(null);
                    g.fill(new Rectangle(x * unitWidth, y * unitHeight, unitWidth, unitHeight));
                }
            }

        } catch (Exception e) {
        }

        return result;
    }

    /** Creates an image with one colour as background */
    private BufferedImage createImage(int width, int height, int bkcolor) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, bkcolor);
            }
        }

        return image;
    }

    /** Implements the control UI for this visualisation. */
    private class ActivityHistrogramControlPanel extends AbstractSelectedItemVisualizerControlPanel implements ListSelectionListener {
        private static final long serialVersionUID = 1L;

        private ActivityHistrogramControlPanel(ActivityHistogram hist, InputData inputData) {
            super("Activity Histogram Control");
            JPanel histPanel = new JPanel(new GridBagLayout());

            GridBagConstraints constr = new GridBagConstraints();
            constr.gridwidth = GridBagConstraints.REMAINDER;
            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;

            initialiseList(inputData.getLabels());
            JScrollPane listScroller = new JScrollPane(list);
            histPanel.add(listScroller, constr);

            text.setToolTipText("Enter a (part) of a component plane name, and start the search with the <enter> key");
            text.setText(dataPoint);
            histPanel.add(text, constr);

            add(histPanel, c);
            setVisible(true);
        }

        @Override
        public void componentResized(ComponentEvent e) {
            updateListSize(list, getHeight(), inputData.numVectors());
        }

        public void valueChanged(ListSelectionEvent e) {
            dataPoint = (String) list.getSelectedValue();
            if (visualizationUpdateListener != null) {
                visualizationUpdateListener.updateVisualization();
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(map.getState().controlElementsWidth, 300);
        }

        public Dimension getMinimumSize() {
            return this.getPreferredSize();
        }

    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        return getVisualizationFlavours(index, gsom, width, height, gsom.getSharedInputObjects().getInputData().numVectors());
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height, int maxFlavours)
            throws SOMToolboxException {
        String currentDataPoint = dataPoint;
        InputData currentInputData = inputData;

        HashMap<String, BufferedImage> result = new HashMap<String, BufferedImage>();

        String[] labels = gsom.getSharedInputObjects().getInputData().getLabels();

        inputData = gsom.getSharedInputObjects().getInputData();
        for (int i = 0; i < labels.length && i < maxFlavours; i++) {
            dataPoint = inputData.getInputDatum(i).getLabel();
            result.put("_" + labels[i], getVisualization(index, gsom, width, height));
        }
        dataPoint = currentDataPoint;
        inputData = currentInputData;
        return result;
    }

    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        // FIXME: Implement this
        return super.getVisualizationFlavours(index, gsom, width, height, flavourParameters);
    }

}
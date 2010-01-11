package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * Implementation of the Cluster Connections Visualisation as described in <i><b>D. Merkl and A. Rauber.</b> Proceedings of the Workshop on
 * Self-Organizing Maps (WSOM97), Helsinki, Finland, June 4-6 1997.
 * 
 * @author Robert Thurnher
 * @author Michael Groh
 * @version $Id: ClusterConnectionsVisualizer.java 2949 2009-12-15 15:21:15Z frank $
 */
public class ClusterConnectionsVisualizer extends AbstractBackgroundImageVisualizer implements BackgroundImageVisualizer {

    private static Logger log = Logger.getLogger("at.tuwien.ifs.somtoolbox");

    private double t1 = 0.8;

    private double t2 = 1.1;

    private double t3 = 1.6;

    public ClusterConnectionsVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Cluster Connections" };
        VISUALIZATION_SHORT_NAMES = new String[] { "ClusterConn" };
        VISUALIZATION_DESCRIPTIONS = new String[] { " Implementation of the Cluster Connections Visualisation as described in \"D. Merkl and A. Rauber.\"\n"
                + "Proceedings of the Workshop on Self-Organizing Maps (WSOM97),\n" + "Helsinki, Finland, June 4-6 1997." };
    }

    public void setT1(double t1) {
        this.t1 = t1;
    }

    public void setT2(double t2) {
        this.t2 = t2;
    }

    public void setT3(double t3) {
        this.t3 = t3;
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int currentVariant, int width, int height) {
        return super.getCacheKey(gsom, currentVariant, width, height) + CACHE_KEY_SECTION_SEPARATOR + buildCacheKey("t1:" + t1, "t2:" + t2, "t3:" + t3);
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        controlPanel = new ClusterConnectionsControlPanel(this);
        DoubleMatrix2D matrix = createMatrix(gsom);

        if (matrix != null) {
            return createImage(matrix, width, height);
        }
        return null;
    }

    /**
     * Creates a distance matrix to be used for vis.<br>
     * FIXME: this should be in {@link GrowingLayer}, similar to the method in UMatrix#calculateDissimilarityMatrix
     * 
     * @param som for vis
     * @return distanceMatrix
     */
    public DoubleMatrix2D createMatrix(GrowingSOM som) {
        DistanceMetric metric = som.getLayer().getMetric();
        int width = som.getLayer().getXSize();
        int height = som.getLayer().getYSize();
        DoubleMatrix2D distanceMatrix = DoubleFactory2D.dense.make(height * 2 - 1, width);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                try {
                    double[] vector1 = som.getLayer().getUnit(i, j).getWeightVector();

                    if (j < width - 1) {
                        double[] vector2 = som.getLayer().getUnit(i, j + 1).getWeightVector();
                        int newI = (i + 1) * 2 - 2;
                        distanceMatrix.set(newI, j, metric.distance(vector1, vector2));
                    }

                    if (i < height - 1) {
                        double[] vector2 = som.getLayer().getUnit(i + 1, j).getWeightVector();
                        int newI = (i + 1) * 2 - 1;
                        distanceMatrix.set(newI, j, metric.distance(vector1, vector2));
                    }
                } catch (SOMToolboxException e) {
                    log.severe("An error occured in createMatrix: " + e.getMessage());
                    return null;
                }
            }
        }

        return distanceMatrix;
    }

    public BufferedImage createImage(DoubleMatrix2D matrix, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, width, height);
        width /= 10;
        height /= 10;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (j < width - 1) {
                    double distance = matrix.get((i + 1) * 2 - 2, j);
                    g.setPaint(getColor(distance));

                    // draw horizontal rectangle
                    g.fillRect(i * 10 + 4, j * 10 + 7, 2, 6);
                }

                if (i < height - 1) {
                    double distance = matrix.get((i + 1) * 2 - 1, j);
                    g.setPaint(getColor(distance));

                    // draw vertical rectangle
                    g.fillRect(i * 10 + 7, j * 10 + 4, 6, 2);
                }
            }
        }

        return image;
    }

    /**
     * Gets the color for a certain distance value accordingly.
     * 
     * @param distance to get color for
     * @return color
     */
    private Color getColor(double distance) {
        if (distance <= t1) {
            return Color.BLACK;
        } else if (distance > t1 && distance <= t2) {
            return Color.GRAY;
        } else if (distance > t2 && distance <= t3) {
            return Color.LIGHT_GRAY;
        } else if (distance > t3) {
            return Color.WHITE;
        } else {
            throw new IllegalStateException("This can't happen.");
        }
    }

    /**
     * For slider controls.
     */
    public class ClusterConnectionsControlPanel extends VisualizationControlPanel implements ChangeListener, ItemListener {
        private static final long serialVersionUID = 1L;

        private ClusterConnectionsVisualizer clusterConnectionsVisualizer;

        private boolean instantAdjustment = true;

        private JCheckBox instantAdjustmentCheckBox;

        private JLabel t1label, t2label, t3label;

        private JSlider t1slider, t2slider, t3slider;

        public ClusterConnectionsControlPanel(ClusterConnectionsVisualizer clusterConnectionsVisualizer) {
            super("Cluster Connections Control Panel");
            this.clusterConnectionsVisualizer = clusterConnectionsVisualizer;

            instantAdjustmentCheckBox = new JCheckBox("instant adjustment?");
            instantAdjustmentCheckBox.setSelected(true);
            instantAdjustmentCheckBox.addItemListener(this);

            t1slider = new JSlider(0, 200, (int) (t1 * 100));
            t1slider.addChangeListener(this);
            t2slider = new JSlider(0, 200, (int) (t2 * 100));
            t2slider.addChangeListener(this);
            t3slider = new JSlider(0, 200, (int) (t3 * 100));
            t3slider.addChangeListener(this);

            t1label = new JLabel("t1: " + t1);
            t2label = new JLabel("t2: " + t2);
            t3label = new JLabel("t3: " + t3);

            add(instantAdjustmentCheckBox, c);
            c.gridy += 1;
            add(t1label, c);
            c.gridy += 1;
            add(t1slider, c);
            c.gridy += 1;
            add(t2label, c);
            c.gridy += 1;
            add(t2slider, c);
            c.gridy += 1;
            add(t3label, c);
            c.gridy += 1;
            add(t3slider, c);
        }

        /**
         * Overridden to specify custom default height of control.
         * 
         * @return height
         */
        @Override
        public int getPreferredHeight() {
            return 200;
        }

        /**
         * Handles state changes of instant adjustment checkbox.
         * 
         * @param event of checkbox item state change
         */
        public void itemStateChanged(ItemEvent event) {
            JCheckBox source = (JCheckBox) event.getSource();

            if (source.equals(instantAdjustmentCheckBox)) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    instantAdjustment = false;
                } else if (event.getStateChange() == ItemEvent.SELECTED) {
                    instantAdjustment = true;
                }
            }
        }

        /**
         * Handles state changes of sliders control.
         * 
         * @param event of sliders control state change
         */
        public void stateChanged(ChangeEvent event) {
            JSlider source = (JSlider) event.getSource();

            if (source.equals(t1slider)) {
                if (t2slider.getValue() < t1slider.getValue()) {
                    t2slider.setValue(source.getValue());
                }
                if (instantAdjustment || !source.getValueIsAdjusting()) {
                    clusterConnectionsVisualizer.setT1((double) t1slider.getValue() / 100d);
                    t1label.setText("t1: " + t1slider.getValue() / 100d);
                }
            }

            if (source.equals(t2slider)) {
                if (t3slider.getValue() < t2slider.getValue()) {
                    t3slider.setValue(t2slider.getValue());
                }
                if (t1slider.getValue() > t2slider.getValue()) {
                    t1slider.setValue(t2slider.getValue());
                }
                if (instantAdjustment || !source.getValueIsAdjusting()) {
                    clusterConnectionsVisualizer.setT2((double) t2slider.getValue() / 100d);
                    t2label.setText("t2: " + t2slider.getValue() / 100d);
                }
            }

            if (source.equals(t3slider)) {
                if (t2slider.getValue() > t3slider.getValue()) {
                    t2slider.setValue(t3slider.getValue());
                }
                if (instantAdjustment || !source.getValueIsAdjusting()) {
                    clusterConnectionsVisualizer.setT3((double) t3slider.getValue() / 100d);
                    t3label.setText("t3: " + t3slider.getValue() / 100d);
                }
            }

            if (instantAdjustment || !source.getValueIsAdjusting()) {
                // invalidateCache();
                visualizationUpdateListener.updateVisualization();
            }
        }
    }

}

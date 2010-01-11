package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.VisualizationControl;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.LeastRecentelyUsedImageCache;
import at.tuwien.ifs.somtoolbox.util.MathUtils;

/**
 * This class provides basic support for classes implementing {@link BackgroundImageVisualizer}.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: AbstractBackgroundImageVisualizer.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractBackgroundImageVisualizer implements BackgroundImageVisualizer {

    public static final String CACHE_KEY_SEPARATOR = ", ";

    // 250 MB cache for visualisation images, max half memory size
    public static final long MAX_CACHE_SIZE_MB = MathUtils.cap(250 * 1024 * 1024, Runtime.getRuntime().maxMemory() / 2);

    protected static final String CACHE_KEY_SECTION_SEPARATOR = " >> ";

    /**
     * The number of visualisation variants this visualizer provides
     */
    protected int NUM_VISUALIZATIONS = 0;

    /**
     * The names of the visualisation variants.
     */
    protected String[] VISUALIZATION_NAMES = null;

    protected String[] VISUALIZATION_SHORT_NAMES = null;

    /**
     * Longer description for the visualiation variants. Can e.g. be references to the algorithm, etc.
     */
    protected String[] VISUALIZATION_DESCRIPTIONS = null;

    /**
     * The panel to control the behaviour of the visualisation.
     */
    protected VisualizationControlPanel controlPanel;

    /**
     * The cache of generated images, to allow faster switching between different visualisations and palettes. Note that the cache is static, i.e.
     * only one cache for all visualisation subclasses is used.
     */
    protected static final LeastRecentelyUsedImageCache cache = new LeastRecentelyUsedImageCache(MAX_CACHE_SIZE_MB);

    /**
     * The listener registered to act on changing visualisation variants or other properties.
     */
    protected VisualizationUpdateListener visualizationUpdateListener = null;

    protected SharedSOMVisualisationData inputObjects;

    protected String[] neededInputObjects;

    protected MapPNode map;

    /**
     * The opacity (transparency) value for this visualisation. 100 means no transparency, while 0 means total transparency.
     */
    protected int opacity = 100;

    protected boolean interpolate = true;

    // FIXME: the selection of the x-dim should be moved to the mapPane, as it doesn't only affect the visualisation, but the whole viewer appearance,
    // i.e. also the labels displayed, the pie-charts, etc..
    public int zSize = 1;

    public int currentZDimSlice = 0;

    /**
     * Visualisations that need a specific scale different to {@link BackgroundImageVisualizer#DEFAULT_BACKGROUND_VISUALIZATION_SCALE} shall set this
     * value different in their constructor.
     */
    protected int preferredScaleFactor = DEFAULT_BACKGROUND_VISUALIZATION_SCALE;

    /**
     * Initialises the control panel, if {@link GraphicsEnvironment#isHeadless()} reports to be in a non-headless environment.
     */
    public AbstractBackgroundImageVisualizer() {
        super();
        // don't initialise the control panel if we have no graphics environment (e.g. in server applications)
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                controlPanel = new VisualizationControlPanel("Visualisation Control");
            } catch (Throwable e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Caught runtime exception/error during graphics init: " + e.getMessage() + "\n Headless environment? "
                                + GraphicsEnvironment.isHeadless());
            }
        }
    }

    /**
     * The key of a cache is created as follows: VisualisationShortName + Hashcode of the SOM + Width + Height + Opacity.<br/>
     * Sub-classes might add more information to the cache, if needed.
     */
    protected String getCacheKey(GrowingSOM gsom, int currentVariant, int width, int height) {
        return buildCacheKey(getVisualizationShortName(currentVariant), "SOM:" + gsom.hashCode(), width + "x" + height, "opac:" + opacity);
    }

    /**
     * Returns the requested visualization image, either by retrieving it from the image cache, or by invoking
     * {@link #createVisualization(int, GrowingSOM, int, int)} to create the image new. Subclasses should not overwrite this method, unless they
     * implement their own caching mechanism, or do not want any caching.
     * 
     * @see at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer#getVisualization(int, at.tuwien.ifs.somtoolbox.models.GrowingSOM, int, int)
     */
    public BufferedImage getVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        controlPanel.updateZDim(gsom.getLayer().getZSize());
        String cacheKey = getCacheKey(gsom, index, width, height);
        logImageCache(cacheKey);
        if (cache.get(cacheKey) == null) {
            cache.put(cacheKey, createVisualization(index, gsom, width, height));
        }
        return cache.get(cacheKey);
    }

    protected void logImageCache(String cacheKey) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").fine(
                (cache.get(cacheKey) == null ? "Creating" : "Loading ") + " image, cache size: " + cache.size() + ", key: " + cacheKey);
    }

    public static String buildCacheKey(Object... parts) {
        StringBuilder sb = new StringBuilder();
        for (Object part : parts) {
            if (sb.length() > 0) {
                sb.append(CACHE_KEY_SEPARATOR);
            }
            sb.append(part);
        }
        return sb.toString();
    }

    /** Deletes all cached elements from the given visualisation. */
    protected void invalidateCache(final String visualizationName) {
        for (String key : new ArrayList<String>(cache.keySet())) { // use a copy to avoid concurrency issues when removing elements below
            if (key.startsWith(visualizationName)) {
                cache.remove(key);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Removed cache for: " + key);
            }
        }
    }

    /**
     * Creates a visualisation image. Subclasses must implement this method.
     * 
     * @param variantIndex the index of the variant to use
     * @param gsom the GrowingSOM to take build the visualisation for
     * @param width the desired width of the image, in pixels
     * @param height the desired height of the image, in pixels.
     * @return an image for this visualisation.
     * @throws SOMToolboxException
     */
    public abstract BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height) throws SOMToolboxException;

    /**
     * Creates a visualisation for the given variant name. Basically just a call to {@link #createVisualization(int, GrowingSOM, int, int)}, but
     * throws a {@link SOMToolboxException} if the given variant name is not known in either the {@link #VISUALIZATION_NAMES} nor
     * {@link #VISUALIZATION_SHORT_NAMES}.
     */
    public BufferedImage createVisualization(String variantName, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        int i = ArrayUtils.indexOf(VISUALIZATION_NAMES, variantName);
        if (i == ArrayUtils.INDEX_NOT_FOUND) {
            i = ArrayUtils.indexOf(VISUALIZATION_SHORT_NAMES, variantName);
        }
        if (i == ArrayUtils.INDEX_NOT_FOUND) {
            throw new SOMToolboxException("Unkown visualisation variant '" + variantName + "' for " + getClass().getName());
        } else {
            return createVisualization(i, gsom, width, height);
        }
    }

    public int getNumberOfVisualizations() {
        return NUM_VISUALIZATIONS;
    }

    protected void checkVariantIndex(int index, Class<?> klass) throws SOMToolboxException {
        if (index < 0 || index > 3) {
            throw new SOMToolboxException("Illegal variant index " + index + " for " + klass.getSimpleName() + ", only " + NUM_VISUALIZATIONS
                    + " are available.");
        }
    }

    public String getVisualizationDescription(int index) {
        if (NUM_VISUALIZATIONS > 0) {
            return VISUALIZATION_DESCRIPTIONS[index];
        } else {
            return null;
        }
    }

    public String[] getVisualizationDescriptions() {
        return VISUALIZATION_DESCRIPTIONS;
    }

    public String getVisualizationName(int index) {
        if (NUM_VISUALIZATIONS > 0) {
            return VISUALIZATION_NAMES[index];
        } else {
            return null;
        }
    }

    public String[] getVisualizationNames() {
        return VISUALIZATION_NAMES;
    }

    public String getVisualizationShortName(int index) {
        if (NUM_VISUALIZATIONS > 0) {
            return VISUALIZATION_SHORT_NAMES[index];
        } else {
            return null;
        }
    }

    public String[] getVisualizationShortNames() {
        return VISUALIZATION_SHORT_NAMES;
    }

    public String[] needsAdditionalFiles() {
        if (neededInputObjects == null) {
            return null;
        }
        Vector<String> neededFiles = new Vector<String>();
        for (int i = 0; i < neededInputObjects.length; i++) {
            if (!inputObjects.getObject(neededInputObjects[i]).hasData()) {
                neededFiles.add(neededInputObjects[i]);
            }
        }
        return neededFiles.toArray(new String[neededFiles.size()]);
    }

    protected void checkNeededObjectsAvailable(GrowingSOM gsom) throws SOMToolboxException {
        String[] needsAdditionalFiles = needsAdditionalFiles();
        if (needsAdditionalFiles != null && needsAdditionalFiles.length > 0) {
            throw new SOMToolboxException(Arrays.toString(needsAdditionalFiles) + " required.");
        }
    }

    public VisualizationControlPanel getControlPanel() {
        return controlPanel;
    }

    public void setVisualizationUpdateListener(VisualizationUpdateListener listener) {
        visualizationUpdateListener = listener;
    }

    /** Implementing sub-classes shall override this method if they need to set some specific input object related information. */
    public void setInputObjects(SharedSOMVisualisationData inputObjects) {
        this.inputObjects = inputObjects;
    }

    /** Implementing sub-classes shall override this method if they need to set some specific input-data related information. */
    public void setSOMData(SOMInputReader reader) {
    }

    /**
     * A basic visualisation control panel, providing a {@linkplain JSpinner} to control the opacity. Visualisations that require more control
     * elements should extend this class, and add their own elements.
     * 
     * @author Rudolf Mayer
     */
    public class VisualizationControlPanel extends JPanel implements ComponentListener {
        private static final long serialVersionUID = 1L;

        protected final JSpinner opacitySpinner = new JSpinner(new SpinnerNumberModel(opacity, 0, 100, 1)); // values in % opacity

        protected final JCheckBox interpolateCheckbox = new JCheckBox("Interpolate");

        public final Font smallerFont = new Font("Tahoma_small", Font.PLAIN, 9);

        public final Font reallySmallerFont = new Font("Tahoma_small", Font.PLAIN, 8);

        /**
         * The {@link GridBagConstraints} to be used to add components.
         */
        protected GridBagConstraints c = new GridBagConstraints();

        public JSpinner spinnerZSlice = null;

        /** Constructs a new VisualizationControlPanel with a specific name, using a {@link GridBagLayout},. */
        public VisualizationControlPanel(String name) {
            super(new GridBagLayout());
            setName(name);
            c.gridy = 0;
            c.gridx = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;

            opacitySpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    opacity = ((Integer) ((JSpinner) e.getSource()).getValue()).intValue();
                    if (visualizationUpdateListener != null) {
                        visualizationUpdateListener.updateVisualization();
                    }
                }
            });
            JPanel basicPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            basicPanel.add(new JLabel("Opacity: "));
            basicPanel.add(opacitySpinner);
            opacitySpinner.setFont(smallerFont);

            // interpolate spinner, only for matrix visualizers!
            if (AbstractBackgroundImageVisualizer.this instanceof MatrixVisualizer) {
                basicPanel.add(interpolateCheckbox);
                interpolateCheckbox.setSelected(interpolate);
                interpolateCheckbox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        interpolate = interpolateCheckbox.isSelected();
                        if (visualizationUpdateListener != null) {
                            visualizationUpdateListener.updateVisualization();
                        }
                    }
                });
            }

            add(basicPanel, c);

            // New, by frank
            JPanel slicePanel = new JPanel();
            slicePanel.add(new JLabel("Show slice"));
            spinnerZSlice = new JSpinner();
            spinnerZSlice.setModel(new SpinnerNumberModel(currentZDimSlice, 0, zSize, 1));
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

            c.gridy += 1;
        }

        public void updateSwitchControls() {
            if (canSwitch()) {
                enableSwitchControls(true);
            } else {
                enableSwitchControls(false);
            }
        }

        private void enableSwitchControls(boolean enabled) {
            opacitySpinner.setEnabled(enabled);
        }

        private boolean canSwitch() {
            if (map != null) {
                return map.getCurrentVisualization() != null && map.isBackgroundImageVisible();
            } else {
                return false;
            }
        }

        /**
         * Indicates how high this control panel would like to be, used in {@link VisualizationControl} to set the preferred size. Subclasses which
         * need a different size should override this method.
         */
        public int getPreferredHeight() {
            return 150;
        }

        protected void updateListSize(JList list, int componentHeight, int dataSize) {
            if (componentHeight > 0) {
                int visibleRowCount = Math.min(dataSize, (componentHeight - 50) / 16);
                if (visibleRowCount != list.getVisibleRowCount()) {
                    list.setVisibleRowCount(visibleRowCount);
                    list.invalidate();
                }
            }
        }

        @Override
        public void componentHidden(ComponentEvent e) { // no specific action needed in this class, sub-classes shall override
        }

        @Override
        public void componentMoved(ComponentEvent e) { // no specific action needed in this class, sub-classes shall override
        }

        @Override
        public void componentResized(ComponentEvent e) {// no specific action needed in this class, sub-classes shall override
        }

        @Override
        public void componentShown(ComponentEvent e) { // no specific action needed in this class, sub-classes shall override
        }

        protected void updateZDim(int zDim) {
            if (AbstractBackgroundImageVisualizer.this.zSize != zDim) {
                AbstractBackgroundImageVisualizer.this.zSize = zDim;
                spinnerZSlice.setModel(new SpinnerNumberModel(currentZDimSlice, 0, zDim, 1));
                spinnerZSlice.setEnabled(zDim > 1);
                spinnerZSlice.setVisible(zDim > 1);
                // Force repaint
                revalidate();
                repaint();
            }
        }

    }

    public void setMap(MapPNode map) {
        this.map = map;
    }

    /**
     * Draws a background image on the given graphics object, and sets the Composite according to the currentely set opacity value.
     * 
     * @param width the desired width of the background image, in pixels
     * @param height the desired height of the background image, in pixels
     * @param g the graphics to draw on.
     */
    protected void drawBackground(int width, int height, Graphics2D g) {
        // if background image is available, draw it on this graphics object
        if (map != null && map.getBackgroundImage() != null) {
            g.drawImage(map.getBackgroundImage(), 0, 0, width, height, null);
        }
        // set opacity factor for alpha composition
        float alpha = (float) opacity / 100f;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }

    /** Draws a black grid of units on the {@link BufferedImage} */
    public static void drawUnitGrid(BufferedImage bufferedImage, GrowingSOM gsom, int width, int height) {
        drawUnitGrid((Graphics2D) bufferedImage.getGraphics(), gsom, width, height);
    }

    /** Draws a grid of units on the {@link Graphics2D} object in the given colour */
    public static void drawUnitGrid(Graphics2D g, GrowingSOM gsom, int width, int height, Color colour) {
        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        g.setColor(colour);
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke());

        for (int x = 0; x < gsom.getLayer().getXSize(); x++) {
            for (int y = 0; y < gsom.getLayer().getYSize(); y++) {
                try {
                    if (gsom.getLayer().getUnit(x, y) != null) { // check needed for mnemonic SOMs
                        g.drawRect(x * unitWidth, y * unitHeight, unitWidth, unitHeight);
                    }
                } catch (LayerAccessException e) {
                    // should never happen
                    e.printStackTrace();
                }
            }
        }
        g.setStroke(stroke);
    }

    /** Draws a black grid of units on the {@link Graphics2D} object */
    public static void drawUnitGrid(Graphics2D g, GrowingSOM gsom, int width, int height) {
        drawUnitGrid(g, gsom, width, height, Color.BLACK);
    }

    /** draws a black border around the image */
    public static void drawBorder(BufferedImage buffImage) {
        Graphics2D g2D = buffImage.createGraphics();
        int width = buffImage.getWidth();
        int height = buffImage.getHeight();
        g2D.setColor(Color.BLACK);
        g2D.drawLine(0, height - 1, width - 1, height - 1);
        g2D.drawLine(width - 1, 0, width - 1, height - 1);
        g2D.drawLine(0, 0, width - 1, 0);
        g2D.drawLine(0, 0, 0, height - 1);
    }

    public String getHTMLVisualisationControl(Map params) {
        return "";
    }

    public int getPreferredScaleFactor() {
        return preferredScaleFactor;
    }

    @Override
    // Sorting visualisations after two criterea:
    // - first, QualityMeasureVisualizer are always last
    // - secondly, by the name of the first visualisation
    public int compareTo(BackgroundImageVisualizer o) {
        if (o instanceof QualityMeasureVisualizer) {
            if (this instanceof QualityMeasureVisualizer) {
                return getVisualizationName(0).compareTo(o.getVisualizationName(0));
            } else {
                return -1;
            }
        } else {
            if (this instanceof QualityMeasureVisualizer) {
                return 1;
            } else {
                return getVisualizationName(0).compareTo(o.getVisualizationName(0));
            }
        }
    }

    /**
     * Default implementation which returns a map of size 1 with the standard, unparameterised visualisation of the given variant. Subclasses that
     * want to return more flavours should override this method.
     */
    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        HashMap<String, BufferedImage> hashMap = new HashMap<String, BufferedImage>(1, 1); // size 1, load factor 1
        hashMap.put("", getVisualization(index, gsom, width, height));
        return hashMap;
    }

    /** Default implementation equal to {@link #getVisualizationFlavours(int, GrowingSOM, int, int)}. */
    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height, int maxFlavours)
            throws SOMToolboxException {
        return getVisualizationFlavours(index, gsom, width, height);
    }

    /** Default implementation equal to {@link #getVisualizationFlavours(int, GrowingSOM, int, int)}. */
    @Override
    public HashMap<String, BufferedImage> getVisualizationFlavours(int index, GrowingSOM gsom, int width, int height,
            Map<String, String> flavourParameters) throws SOMToolboxException {
        return getVisualizationFlavours(index, gsom, width, height);
    }

}

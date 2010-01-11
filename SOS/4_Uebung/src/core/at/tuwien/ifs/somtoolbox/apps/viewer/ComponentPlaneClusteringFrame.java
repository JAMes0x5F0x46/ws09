package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMProperties;
import at.tuwien.ifs.somtoolbox.visualization.ComponentPlanesVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * This class implements ordered display and clustering of SOM Component Planes. The components planes are transformed to vectors, and are
 * subsequently either displayed in their order, or clustered on a new SOM.
 * 
 * @author Arnaud Moreau
 * @author Peter Vorlaufer
 * @author Rudolf Mayer
 * @version $Id: ComponentPlaneClusteringFrame.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ComponentPlaneClusteringFrame extends JInternalFrame implements ActionListener, ChangeListener {
    private static final String CLUSTER = "Clustering";

    private static final String DISPLAY = "Display ordered";

    private static final long serialVersionUID = 1L;

    private AbstractSOMLibSparseInputData input;

    private SOMProperties props;

    private String[] labels;

    private GrowingSOM orginalSom;

    private GenericPNodeScrollPane pane;

    private JSpinner spinnerXSize;

    private JSpinner spinnerYSize;

    private SpinnerNumberModel spinnerNumberModelXSize;

    private SpinnerNumberModel spinnerNumberModelYSize;

    /** A cache for already trained SOMs. */
    private Hashtable<String, ComponentPlaneClustering> clusteredMapCache = new Hashtable<String, ComponentPlaneClustering>();

    private PNode unclusteredComponentPNodeWithNames;

    private PNode unclusteredComponentPNodeWithOutNames;

    final int uHeight = MapPNode.DEFAULT_UNIT_HEIGHT;

    final int uWidth = MapPNode.DEFAULT_UNIT_WIDTH;

    private int dim;

    private SOMLibTemplateVector tv;

    private SOMViewer somViewer;

    private CommonSOMViewerStateData state;

    private GrowingSOM currentSOM;

    private ButtonGroup buttons;

    private int padding = 12;

    private JCheckBox checkboxShowComponentNames;

    public ComponentPlaneClusteringFrame(SOMViewer somViewer, GrowingSOM orginalSom, SOMLibTemplateVector tv) throws SOMToolboxException {
        super("Component Plane Clustering", true, true, true, true);
        this.orginalSom = orginalSom;
        currentSOM = orginalSom;
        this.somViewer = somViewer;
        this.tv = tv;
        GrowingLayer layer = orginalSom.getLayer();
        dim = tv.dim();
        // create covariance matrix from CPs
        DoubleMatrix2D cov = this.getCov(layer);
        labels = new String[dim];
        InputDatum[] newData = new InputDatum[dim];
        // extract feature names and save new training vectors
        for (int i = 0; i < dim; i++) {
            labels[i] = tv.getLabel(i);
            newData[i] = new InputDatum(labels[i], cov.viewColumn(i), cov.viewColumn(i).cardinality());
        }

        // compute new x=y SOM Size
        int newSOMAxisSize = (int) Math.ceil(Math.sqrt(dim)) + 1;

        // create new Input Data for the SOM
        input = SOMLibSparseInputData.create(newData, null);
        tv = new SOMLibTemplateVector(input.numVectors(), input.dim());

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JRadioButton radioButtonDisplay = new JRadioButton(DISPLAY);
        radioButtonDisplay.setActionCommand(DISPLAY);
        radioButtonDisplay.addActionListener(this);
        topPanel.add(radioButtonDisplay);

        JRadioButton radioButtonCluster = new JRadioButton(CLUSTER);
        radioButtonCluster.setActionCommand(CLUSTER);
        radioButtonCluster.addActionListener(this);
        topPanel.add(radioButtonCluster);

        topPanel.add(new JLabel("xSize"));
        spinnerNumberModelXSize = new SpinnerNumberModel(newSOMAxisSize, 1, 50, 1);
        spinnerXSize = new JSpinner(spinnerNumberModelXSize);
        spinnerXSize.setEnabled(false);
        spinnerXSize.addChangeListener(this);
        topPanel.add(spinnerXSize);

        topPanel.add(new JLabel("ySize"));
        spinnerNumberModelYSize = new SpinnerNumberModel(newSOMAxisSize, 1, 50, 1);
        spinnerYSize = new JSpinner(spinnerNumberModelYSize);
        spinnerYSize.setEnabled(false);
        spinnerYSize.addChangeListener(this);
        topPanel.add(spinnerYSize);

        buttons = new ButtonGroup();
        buttons.add(radioButtonDisplay);
        buttons.add(radioButtonCluster);
        radioButtonDisplay.setSelected(true);

        JButton buttonSave = new JButton("Save");
        buttonSave.setToolTipText("Save the component plane pane to an image file");
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File filePath = ExportUtils.getFilePath(ComponentPlaneClusteringFrame.this,
                        ComponentPlaneClusteringFrame.this.state.getFileChooser(), "Save Component Plane pane as PNG");
                if (filePath != null) {
                    try {
                        ExportUtils.saveMapPaneAsImage(pane, filePath.getAbsolutePath());
                        JOptionPane.showMessageDialog(ComponentPlaneClusteringFrame.this, "Export to file finished!");
                    } catch (SOMToolboxException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Error saving", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        topPanel.add(buttonSave);

        checkboxShowComponentNames = new JCheckBox("Show component names", true);
        checkboxShowComponentNames.addActionListener(this);
        topPanel.add(checkboxShowComponentNames);

        contentPane.add(topPanel, BorderLayout.NORTH);

        state = new CommonSOMViewerStateData(somViewer.getSOMViewerState());
        state.inputDataObjects = new SharedSOMVisualisationData();
        state.inputDataObjects.setData(SOMVisualisationData.TEMPLATE_VECTOR, tv);

        unclusteredComponentPNodeWithOutNames = createUnclusteredPane(somViewer, tv, layer, false);
        unclusteredComponentPNodeWithNames = createUnclusteredPane(somViewer, tv, layer, true);
        pane = new GenericPNodeScrollPane(state, unclusteredComponentPNodeWithNames);
        contentPane.add(pane, BorderLayout.CENTER);
    }

    private ComponentPlaneClustering createClusteredPane(SOMViewer parent, SOMLibTemplateVector tv, GrowingLayer layer) throws SOMToolboxException {
        // specify Properties of new SOM
        try {
            props = new SOMProperties(spinnerNumberModelXSize.getNumber().intValue(), spinnerNumberModelYSize.getNumber().intValue(), 7, 0, 1000,
                    0.7, 0, 1, "", true);
        } catch (PropertiesException pe) {
            pe.printStackTrace();
        }

        // create Layer and train it
        GrowingSOM cpsom = new GrowingSOM(layer.getDim(), false, props, input);
        cpsom.train(input, props);

        // check if there are multiple items on one Unit
        reStructureMap(cpsom);

        CommonSOMViewerStateData state = new CommonSOMViewerStateData(parent.getSOMViewerState());
        state.inputDataObjects = new SharedSOMVisualisationData();
        state.inputDataObjects.setData(SOMVisualisationData.TEMPLATE_VECTOR, tv);
        return new ComponentPlaneClustering(cpsom, makeComponentPNode(createComponentPlanesVisualizer(state), cpsom));
    }

    public PNode makeComponentPNode(ComponentPlanesVisualizer visualizer, GrowingSOM cpsom) throws SOMToolboxException {
        final GrowingLayer layer = cpsom.getLayer();
        PNode componentImages = createPNode(layer.getXSize(), layer.getYSize());

        // make a map grid
        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                PPath rect = PPath.createRectangle((float) x * (uWidth + padding), (float) y * (uHeight + padding), (float) (uWidth + padding),
                        (float) (uHeight + padding));
                componentImages.addChild(rect);
            }
        }

        // draw all component images
        for (int i = 0; i < labels.length; i++) {
            Unit u = layer.getUnitForDatum(labels[i]);
            createComponentImage(visualizer, componentImages, i, u.getXPos(), u.getYPos(), false);
        }
        return componentImages;
    }

    private PNode createUnclusteredPane(SOMViewer parent, SOMLibTemplateVector tv, GrowingLayer layer, boolean showComponentNames)
            throws SOMToolboxException {
        int neededXSize = (int) Math.ceil(Math.sqrt(dim));
        int neededYSize = (int) Math.floor(Math.sqrt(dim));
        PNode componentImages = createPNode(neededXSize, neededYSize);

        // draw all component images
        for (int i = 0; i < labels.length; i++) {
            int xPos = i % neededXSize;
            int yPos = i / neededXSize;
            createComponentImage(createComponentPlanesVisualizer(state), componentImages, i, xPos, yPos, showComponentNames);
        }
        return componentImages;
    }

    private ComponentPlanesVisualizer createComponentPlanesVisualizer(CommonSOMViewerStateData state) {
        ComponentPlanesVisualizer vis = new ComponentPlanesVisualizer();
        vis.setInputObjects(state.inputDataObjects);
        vis.setPalette(0, Palettes.getPaletteByName("RGB256"));
        return vis;
    }

    private void createComponentImage(ComponentPlanesVisualizer visualizer, PNode componentImages, int componentIndex, int xPos, int yPos,
            boolean showComponentNames) throws SOMToolboxException {
        BufferedImage bimg = visualizer.createVisualization(0, componentIndex, orginalSom, orginalSom.getLayer().getXSize() * 10,
                orginalSom.getLayer().getYSize() * 10);
        int textHeight = 15;
        if (showComponentNames) { // also display component names?
            PText componentName = new PText(labels[componentIndex]);
            double width2 = componentName.getWidth();
            componentName.setHeight(textHeight);
            componentImages.addChild(componentName);
            componentName.moveToFront();
            componentName.translate(((uWidth - width2) / 2) + (uWidth + padding) * xPos + padding / 2, (uHeight + padding + (textHeight * 0.75))
                    * yPos + padding / 2 + (yPos == 0 ? textHeight * -0.25 : 0));
        }
        PImage img = new PImage(bimg);
        img.addAttribute("tooltip", "Component #" + componentIndex + ", '" + labels[componentIndex] + "'");
        img.setWidth(uWidth);
        img.setHeight(uHeight);
        componentImages.addChild(img);
        img.moveToFront();
        img.translate((uWidth + padding) * xPos + padding / 2, (uHeight + padding) * yPos + (showComponentNames ? textHeight * (yPos + 1) : 0)
                + padding / 2);
    }

    private PNode createPNode(int xSize, int ySize) {
        PNode componentImages = new PNode();
        componentImages.setWidth(xSize * (uWidth + padding) + 2);
        componentImages.setHeight(ySize * (uHeight + padding) + 2);
        return componentImages;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        update();
    }

    private void update() {
        if (buttons.getSelection().getActionCommand() == DISPLAY) {
            if (checkboxShowComponentNames.isSelected()) {
                pane.setPNode(unclusteredComponentPNodeWithNames);
            } else {
                pane.setPNode(unclusteredComponentPNodeWithOutNames);
            }
            currentSOM = orginalSom;
        } else {
            String key = spinnerNumberModelXSize.getNumber() + "x" + spinnerNumberModelYSize.getNumber();
            if (clusteredMapCache.get(key) == null) {
                try {
                    clusteredMapCache.put(key, createClusteredPane(somViewer, tv, orginalSom.getLayer()));
                } catch (SOMToolboxException e1) {
                    e1.printStackTrace();
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error creating component plane clustering: '" + e1.getMessage() + "'.");
                }
            }
            pane.setPNode(clusteredMapCache.get(key).vis);
            currentSOM = clusteredMapCache.get(key).som;
        }
        boolean enableSpinner = buttons.getSelection().getActionCommand() != DISPLAY;
        spinnerXSize.setEnabled(enableSpinner);
        spinnerYSize.setEnabled(enableSpinner);
    }

    private DoubleMatrix2D getCov(GrowingLayer layer) {
        // save CPs in new matrix
        DenseDoubleMatrix2D matrix = new DenseDoubleMatrix2D(layer.getXSize() * layer.getYSize(), layer.getDim());
        for (int i = 0; i < layer.getDim(); i++) {
            double[][] cp = layer.getComponentPlane(i);
            for (int n = 0; n < layer.getXSize(); n++) {
                for (int m = 0; m < layer.getYSize(); m++) {
                    matrix.setQuick(n * layer.getYSize() + m, i, cp[n][m]);
                }
            }
        }
        // return covariance matrix
        return Statistic.covariance(matrix);
    }

    private void reStructureMap(GrowingSOM cpsom) {
        int doubleUnits = 0;
        int iter = 0;
        InputData d = cpsom.getLayer().getData();
        do {
            Unit[] units = cpsom.getLayer().getAllUnits();
            doubleUnits = 0;
            for (int i = 0; i < units.length; i++) {
                String[] lab = units[i].getMappedInputNames();
                if (lab != null) {
                    if (lab.length > 1) {
                        doubleUnits++;
                        for (int j = 1; j < lab.length; j++) {
                            InputDatum da = d.getInputDatum(lab[j]);
                            Unit[] winners = cpsom.getLayer().getWinners(da, 2 + iter);
                            winners[1 + iter].addMappedInput(da, true);
                            units[i].removeMappedInput(lab[j]);
                            System.out.println("Moving Label " + lab[j] + " from " + units[i] + " to " + winners[1 + iter]);
                        }
                    }
                }
            }
            System.out.println(doubleUnits);
            iter++;
        } while (doubleUnits > 0);
    }

    @Override
    public Dimension getPreferredSize() {
        int maxWidth = (int) pane.getPreferredSize().getWidth();
        int maxHeight = (int) pane.getPreferredSize().getHeight();
        int spacer = 50; // TODO: replace with real values for size of title bar, etc.

        int preferredWidth = currentSOM.getLayer().getXSize() * MapPNode.DEFAULT_UNIT_WIDTH + spacer;
        int preferredHeight = currentSOM.getLayer().getXSize() * MapPNode.DEFAULT_UNIT_WIDTH + spacer;
        if (preferredWidth > maxWidth || preferredHeight > maxHeight) {
            return new Dimension(maxWidth, maxHeight);
        } else {
            return new Dimension(preferredWidth, preferredHeight);
        }
    }

    private class ComponentPlaneClustering {
        private GrowingSOM som;

        private PNode vis;

        public ComponentPlaneClustering(GrowingSOM som, PNode vis) {
            this.som = som;
            this.vis = vis;
        }
    }
}

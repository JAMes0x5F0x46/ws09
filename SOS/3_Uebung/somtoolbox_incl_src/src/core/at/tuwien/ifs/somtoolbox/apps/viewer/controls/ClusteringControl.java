package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.SortedMap;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.LabelXmlUtils;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.quality.EntropyAndPurityCalculator;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterElementsStorage;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterNode;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringTree;
import at.tuwien.ifs.somtoolbox.visualization.clustering.KMeans;
import at.tuwien.ifs.somtoolbox.visualization.clustering.KMeansTreeBuilder;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PObjectOutputStream;

/**
 * The control panel for the clustering functionality.
 * 
 * @author Angela Roiger
 * @author Rudolf Mayer
 * @version $Id: ClusteringControl.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClusteringControl extends AbstractViewerControl {
    private static final long serialVersionUID = 1L;

    private static final FileNameExtensionFilter clusteringFilter = new FileNameExtensionFilter("Clustering and Labels (*.clustering)", "clustering");

    private static final FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("Labels as xml (*.xml)", "xml");

    private JSpinner spinnerNoCluster;

    private JSpinner labelSpinner;

    private GridBagConstraints c = new GridBagConstraints();

    private JPanel clusterPanel;

    private int numClusters = 1;

    private SOMPane mapPane;

    private JCheckBox colorCluster;

    private JCheckBox showValues;

    private JSlider valueQe;

    private JCheckBox sticky = new JCheckBox("fix", false);;

    private boolean st = false;

    private int numLabels = 0;

    private int maxCluster;

    private JButton buttonColour;

    private JButton qualityMeasureButton;

    private JLabel entropyLabel;

    private JLabel purityLabel;

    public ClusteringControl(String title, CommonSOMViewerStateData state, SOMPane mappane) {
        super(title, state, new GridBagLayout());
        this.mapPane = mappane;
        init();
    }

    public void init() {
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.NORTHWEST;

        maxCluster = state.growingLayer.getXSize() * state.growingLayer.getYSize() - 1;

        spinnerNoCluster = new JSpinner(new SpinnerNumberModel(1, 1, maxCluster, 1));
        spinnerNoCluster.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                numClusters = (Integer) ((JSpinner) e.getSource()).getValue();
                SortedMap<Integer, ClusterElementsStorage> m = ClusteringControl.this.mapPane.getMap().getCurrentClusteringTree().getAllClusteringElements();
                if (m.containsKey(numClusters)) {
                    st = (m.get(numClusters)).sticky;
                } else {
                    st = false;
                }
                sticky.setSelected(st);
                redrawClustering();
            }
        });

        final JComboBox initialisationBox = new JComboBox(KMeans.InitType.values());
        initialisationBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object o = ClusteringControl.this.mapPane.getMap().getClusteringTreeBuilder();
                if (o instanceof KMeansTreeBuilder) {
                    ((KMeansTreeBuilder) o).reInit((KMeans.InitType) (initialisationBox.getSelectedItem()));
                    // FIXME: is this call needed?
                    ClusteringControl.this.mapPane.getMap().getCurrentClusteringTree().getAllClusteringElements();
                    redrawClustering();
                }
            }
        });
        clusterPanel = new JPanel(new GridLayout(2, 3));
        clusterPanel.add(new JLabel("# Cluster"));
        clusterPanel.add(spinnerNoCluster);
        clusterPanel.add(sticky);
        clusterPanel.add(new JLabel("Initialisation"));
        clusterPanel.add(initialisationBox);
        getContentPane().add(clusterPanel, c);

        this.sticky.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ClusteringControl.this.st = sticky.isSelected();
                SortedMap<Integer, ClusterElementsStorage> m = ClusteringControl.this.mapPane.getMap().getCurrentClusteringTree().getAllClusteringElements();
                if (m.containsKey(numClusters)) {
                    ClusterElementsStorage c = m.get(numClusters);
                    c.sticky = st;
                    // System.out.println("test");
                    // ((ClusterElementsStorageNode)m.get(numClusters)).sticky = st;
                    redrawClustering();
                }
            }
        });

        this.colorCluster = new JCheckBox("colour", state.colorClusters);
        this.colorCluster.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ClusteringControl.this.state.colorClusters = colorCluster.isSelected();
                // TODO: Palette anzeigen (?)
                redrawClustering();
            }
        });
        clusterPanel.add(colorCluster);
        c.gridy += 1;

        JPanel numLabelPanel = new JPanel(new GridLayout(1, 3));

        labelSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        labelSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                numLabels = (Integer) ((JSpinner) e.getSource()).getValue();
                ClusteringControl.this.state.clusterWithLabels = numLabels;
                redrawClustering();
            }
        });

        numLabelPanel.add(new JLabel("# Labels"));
        numLabelPanel.add(labelSpinner);

        this.showValues = new JCheckBox("values", state.labelsWithValues);
        this.showValues.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ClusteringControl.this.state.labelsWithValues = showValues.isSelected();
                redrawClustering();
            }
        });
        numLabelPanel.add(showValues);
        getContentPane().add(numLabelPanel, c);
        c.gridy += 1;

        int start = new Double((1 - ClusteringControl.this.state.clusterByValue) * 100).intValue();
        valueQe = new JSlider(0, 100, start);
        c.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(valueQe, c);
        c.fill = GridBagConstraints.NONE;
        c.gridy += 1;

        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(0, new JLabel("by Value"));
        labelTable.put(100, new JLabel("by Qe"));
        valueQe.setLabelTable(labelTable);
        valueQe.setPaintLabels(true);

        JPanel borderPanel = new JPanel(new GridLayout(1, 4));

        JSpinner borderSpinner = new JSpinner(new SpinnerNumberModel(ClusteringTree.INITIAL_BORDER_WIDTH_MAGNIFICATION_FACTOR, 0.1, 5, 0.1));
        borderSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                state.clusterBorderWidthMagnificationFactor = ((Double) ((JSpinner) e.getSource()).getValue()).floatValue();
                redrawClustering();
            }
        });

        borderPanel.add(new JLabel("Border"));
        borderPanel.add(new JLabel("width"));
        borderPanel.add(borderSpinner);

        borderPanel.add(new JLabel("colour"));
        buttonColour = new JButton("");
        buttonColour.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ClusterBoderColorChooser(state.parentFrame, state.clusterBorderColour, ClusteringControl.this);
            }
        });
        buttonColour.setBackground(state.clusterBorderColour);
        borderPanel.add(buttonColour);

        getContentPane().add(borderPanel, c);
        c.gridy += 1;

        JPanel evaluationPanel = new JPanel(new GridLayout(3, 2));

        evaluationPanel.add(new JLabel("quality measure"));
        qualityMeasureButton = new JButton();
        qualityMeasureButton.setText("ent/pur calc");
        qualityMeasureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("doing entropy here");
                ClusteringTree clusteringTree = state.mapPNode.getClusteringTree();
                if (clusteringTree == null) {
                    // we have not clustered yet
                    return;
                }
                // System.out.println(clusteringTree.getChildrenCount());

                // FIXME put this in a method and call it on numcluster.change()
                SOMLibClassInformation classInfo = state.inputDataObjects.getClassInfo();
                ArrayList<ClusterNode> clusters = clusteringTree.getNodesAtLevel(numClusters);
                System.out.println(clusters.size());
                // EntropyMeasure.computeEntropy(clusters, classInfo);
                EntropyAndPurityCalculator eapc = new EntropyAndPurityCalculator(clusters, classInfo);
                // FIXME round first
                entropyLabel.setText(String.valueOf(eapc.getEntropy()));
                purityLabel.setText(String.valueOf(eapc.getPurity()));

            }
        });
        evaluationPanel.add(qualityMeasureButton);

        c.gridy += 1;

        evaluationPanel.add(new JLabel("entropy"), c);
        evaluationPanel.add(new JLabel("purity"), c);

        c.gridy += 1;
        entropyLabel = new JLabel("n/a");
        purityLabel = new JLabel("n/a");

        evaluationPanel.add(entropyLabel, c);
        evaluationPanel.add(purityLabel, c);

        getContentPane().add(evaluationPanel, c);
        c.gridy += 1;

        JPanel panelButtons = new JPanel(new GridLayout(1, 4));

        JButton saveButton = new JButton("Save");
        saveButton.setFont(smallFont);
        saveButton.setMargin(SMALL_INSETS);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser;
                if (ClusteringControl.this.state.fileChooser.getSelectedFile() != null) {
                    fileChooser = new JFileChooser(ClusteringControl.this.state.fileChooser.getSelectedFile().getPath());
                } else {
                    fileChooser = new JFileChooser();
                }
                fileChooser.addChoosableFileFilter(clusteringFilter);
                fileChooser.addChoosableFileFilter(xmlFilter);

                File filePath = ExportUtils.getFilePath(ClusteringControl.this, fileChooser, "Save Clustering and Labels");

                if (filePath != null) {
                    if (xmlFilter.accept(filePath)) {
                        LabelXmlUtils.saveLabelsToFile(ClusteringControl.this.state.mapPNode, filePath);
                    } else {
                        try {
                            FileOutputStream fos = new FileOutputStream(filePath);
                            GZIPOutputStream gzipOs = new GZIPOutputStream(fos);
                            PObjectOutputStream oos = new PObjectOutputStream(gzipOs);

                            oos.writeObjectTree(ClusteringControl.this.mapPane.getMap().getCurrentClusteringTree());
                            oos.writeObjectTree(ClusteringControl.this.mapPane.getMap().getManualLabels());
                            oos.writeInt(ClusteringControl.this.state.clusterWithLabels);
                            oos.writeBoolean(ClusteringControl.this.state.labelsWithValues);
                            oos.writeDouble(ClusteringControl.this.state.clusterByValue);
                            oos.writeObject(spinnerNoCluster.getValue());

                            oos.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    // keep the selected path for future references
                    ClusteringControl.this.state.fileChooser.setSelectedFile(filePath.getParentFile());
                }
            }
        });
        panelButtons.add(saveButton);

        JButton loadButton = new JButton("Load");
        loadButton.setFont(smallFont);
        loadButton.setMargin(SMALL_INSETS);
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = getFileChooser();
                fileChooser.setName("Open Clustering");
                fileChooser.addChoosableFileFilter(xmlFilter);
                fileChooser.addChoosableFileFilter(clusteringFilter);

                int returnVal = fileChooser.showDialog(ClusteringControl.this, "Open");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File filePath = fileChooser.getSelectedFile();
                    if (xmlFilter.accept(filePath)) {
                        PNode restoredLabels = null;
                        try {
                            restoredLabels = LabelXmlUtils.restoreLabelsFromFile(fileChooser.getSelectedFile());
                            PNode manual = ClusteringControl.this.state.mapPNode.getManualLabels();

                            ArrayList<PNode> tmp = new ArrayList<PNode>();
                            for (ListIterator<?> iter = restoredLabels.getChildrenIterator(); iter.hasNext();) {
                                PNode element = (PNode) iter.next();
                                tmp.add(element);
                            }
                            manual.addChildren(tmp);
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Successfully loaded cluster labels.");
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Error loading cluster labels: " + e1.getMessage());
                        }

                    } else {

                        try {
                            FileInputStream fis = new FileInputStream(filePath);
                            GZIPInputStream gzipIs = new GZIPInputStream(fis);
                            ObjectInputStream ois = new ObjectInputStream(gzipIs);
                            ClusteringTree tree = (ClusteringTree) ois.readObject();

                            PNode manual = (PNode) ois.readObject();
                            PNode all = ClusteringControl.this.mapPane.getMap().getManualLabels();
                            ArrayList<PNode> tmp = new ArrayList<PNode>();
                            for (ListIterator<?> iter = manual.getChildrenIterator(); iter.hasNext();) {
                                PNode element = (PNode) iter.next();
                                tmp.add(element);
                            }
                            all.addChildren(tmp);

                            ClusteringControl.this.state.clusterWithLabels = ois.readInt();
                            labelSpinner.setValue(ClusteringControl.this.state.clusterWithLabels);
                            ClusteringControl.this.state.labelsWithValues = ois.readBoolean();
                            showValues.setSelected(ClusteringControl.this.state.labelsWithValues);
                            ClusteringControl.this.state.clusterByValue = ois.readDouble();
                            valueQe.setValue(new Double((1 - ClusteringControl.this.state.clusterByValue) * 100).intValue());

                            ClusteringControl.this.mapPane.getMap().buildTree(tree);
                            spinnerNoCluster.setValue(ois.readObject());

                            ois.close();
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Successfully loaded clustering.");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Error loading clustering: " + ex.getMessage());
                        }

                    }
                    // keep the selected path for future references
                    ClusteringControl.this.state.fileChooser.setSelectedFile(filePath.getParentFile());
                }
            }

        });
        panelButtons.add(loadButton);

        JButton exportImages = new JButton("Export");
        exportImages.setFont(smallFont);
        exportImages.setMargin(SMALL_INSETS);
        exportImages.setToolTipText("Export labels as images (not yet working)");
        exportImages.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (CommonSOMViewerStateData.fileNamePrefix != null && !CommonSOMViewerStateData.fileNamePrefix.equals("")) {
                    fileChooser.setCurrentDirectory(new File(CommonSOMViewerStateData.fileNamePrefix));
                } else {
                    fileChooser.setCurrentDirectory(ClusteringControl.this.state.getFileChooser().getCurrentDirectory());
                }
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                    fileChooser.setSelectedFile(null);
                }
                fileChooser.setName("Choose path");
                int returnVal = fileChooser.showDialog(ClusteringControl.this, "Choose path");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // save images
                    String path = fileChooser.getSelectedFile().getAbsolutePath();
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing label images to " + path);
                }
            }
        });
        panelButtons.add(exportImages);

        JButton deleteManual = new JButton("Delete");
        deleteManual.setFont(smallFont);
        deleteManual.setMargin(SMALL_INSETS);
        deleteManual.setToolTipText("Delete manually added labels.");
        deleteManual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ClusteringControl.this.state.mapPNode.getManualLabels().removeAllChildren();
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Manual Labels deleted.");
            }
        });
        panelButtons.add(deleteManual);
        c.anchor = GridBagConstraints.NORTH;
        this.getContentPane().add(panelButtons, c);
        c.gridy += 1;

        valueQe.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int byValue = ((JSlider) e.getSource()).getValue();
                ClusteringControl.this.state.clusterByValue = 1 - byValue / 100;
                redrawClustering();
            }
        });

        this.setVisible(true);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(state.controlElementsWidth, 215);
    }

    // update allClusteringElements of the current clustering tree and show them
    private void redrawClustering() {
        // BasicStroke bs = new BasicStroke(12.0f);
        this.mapPane.getState().numClusters = numClusters;
        this.mapPane.getMap().showClusters(this.numClusters, sticky.isSelected());
    }

    private JFileChooser getFileChooser() {
        JFileChooser fileChooser;
        if (ClusteringControl.this.state.fileChooser.getSelectedFile() != null) {
            fileChooser = new JFileChooser(ClusteringControl.this.state.fileChooser.getSelectedFile().getPath());
        } else {
            fileChooser = new JFileChooser();
        }
        return fileChooser;
    }

    public void updateClusterColourSelection(Color colour) {
        buttonColour.setBackground(colour);
        state.clusterBorderColour = colour;
        redrawClustering();
    }

}

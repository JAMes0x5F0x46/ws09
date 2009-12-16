package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ProgressMonitor;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.SOMToolboxMetaConstants;
import at.tuwien.ifs.somtoolbox.apps.DataSetViewer;
import at.tuwien.ifs.somtoolbox.apps.PaletteEditor;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractSelectionPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractViewerControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ClassLegendPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ClusteringControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ComparisonPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.DocSOMPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.GHSOMNavigationPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MapDetailPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MapOverviewPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MultichannelPlaybackPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PalettePanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PlaySOMPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.PlaygroundPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.QuerySOMPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ShiftsControlPanel;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.VisualizationControl;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.PlaySOMPlayer;
import at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.PocketSOMConnector;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.ExportUtils;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.PocketSOMFormatUtils;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.LoggingHandler;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.InputCorrections;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.CreationType;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.InputCorrection;
import at.tuwien.ifs.somtoolbox.layers.Unit.FeatureWeightMode;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;
import at.tuwien.ifs.somtoolbox.properties.SOMViewerProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.ReportGenerator;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.JMultiLineRadioButtonMenuItem;
import at.tuwien.ifs.somtoolbox.util.SwingWorker;
import at.tuwien.ifs.somtoolbox.util.UiUtils;
import at.tuwien.ifs.somtoolbox.visualization.AbstractMatrixVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.ComparisonVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedDataHistograms;
import at.tuwien.ifs.somtoolbox.visualization.ThematicClassMapVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedDataHistograms.SDHControlPanel;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringAbortedException;
import at.tuwien.ifs.somtoolbox.visualization.clustering.CompleteLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.KMeansTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.SingleLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.TreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.WardsLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.WardsLinkageTreeBuilderAll;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import edu.umd.cs.piccolo.PLayer;

/**
 * The class providing the main window of the SOMViewer application. Initialises all the control element windows (see
 * {@link at.tuwien.ifs.somtoolbox.apps.viewer.controls} package), toolbars, and the {@link SOMFrame} holding the map representation ({@link MapPNode}
 * ).
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @author Thomas Lidy
 * @version $Id: SOMViewer.java 2881 2009-12-11 17:56:16Z mayer $
 */
public class SOMViewer extends JFrame implements ActionListener, Observer, SOMToolboxApp {

    /*
     * Requirements for SOMToolboxApp
     */
    public static final String VERSION = "0.7.4";

    public static final String DESCRIPTION = "SOMViewer, part of SOMToolbox";

    public static final String LONG_DESCRIPTION = "The SOMViewer is to show SOMs!";

    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptUnitDescriptionFile(true),
            OptionFactory.getOptWeightVectorFile(true), OptionFactory.getOptHighlightedDataNamesFile(false),
            OptionFactory.getOptClassInformationFile(false), OptionFactory.getOptRegressionInformationFile(false),
            OptionFactory.getOptMapDescriptionFile(false), OptionFactory.getOptDataInformationFileFile(false),
            OptionFactory.getOptFileNamePrefix(false), OptionFactory.getOptFileNameSuffix(false), OptionFactory.getOptDataWinnerMappingFile(false),
            OptionFactory.getOptInputVectorFile(false), OptionFactory.getOptTemplateVectorFile(false), OptionFactory.getOptLinkageFile(false),
            OptionFactory.getOptApplicationDirectory(false), OptionFactory.getOptViewerWorkingDir(false),
            OptionFactory.getOptInitialVisualisation(false), OptionFactory.getOptInitialVisParams(false), OptionFactory.getOptInitialPalette(false),
            OptionFactory.getSwitchDocumentMode(), OptionFactory.getOptDecodeProbability(false), OptionFactory.getOptDecodedOutputDir(false),
            OptionFactory.getOptClassColoursFile(false), OptionFactory.getOptInputCorrections(false), OptionFactory.getSwitchNoPlayer(),
            OptionFactory.getOptSetSecondSOM(false) };

    private static final long serialVersionUID = 1L;

    public static final String PREFS_FILE = "somviewer.prop";

    // messages
    private static final String CENTER_AND_FIT_MAP = "Center and fit map to screen";

    private static final String SELECT_LINE = "Select Line Selection Handler";

    private static final String SELECT_RECTANGLE = "Select Rectangle or Unit Selection Handler";

    private static final String SELECT_CLUSTER = "Select Cluster Selection Handler";

    private static final String RESET_DESKTOP_LAYOUT = "Reset desktop windows layout";

    private static final String SOMVIEWER_3D = "Start 3D SOM Viewer";

    private static final String MOVE_INPUT = "Move Input"; // rudi for semi-supervised

    private static final String MOVE_LABEL = "Move Labels"; // Angela

    private static final String CREATE_LABEL = "Create new Label"; // Angela

    private static final String TOGGLE_PIE_CHARTS = "Show pie charts";

    private static final String TOGGLE_LABELS = "Show labels";

    private static final String TOGGLE_DATA = "Show data";

    private static final String TOGGLE_EXACT_PLACEMENT = "Exact placement of input vectors";

    private static final String TOGGLE_RELOCATE = "Relocate overlapping input vectors";

    private static final String TOGGLE_LINKAGE = "Display input linkages";

    private static final String MSG_EXACTPLACEMENT_DISABLED = "Data winner mapping file needs to be loaded for this feature!";

    // resources
    public static final String RESOURCE_PATH_ICONS = "rsc/icons/";

    // settings
    // wether to create a fullscreen gui or not
    // private static final boolean fullScreen = true;

    private String unitDescriptionFileName = null;

    private String weightVectorFileName = null;

    // private String highlightedDataNamesFileName = null;

    private String mapDescriptionFileName = null;

    private String classInformationFileName = null;

    private String regressionInformationFileName = null;

    private String dataInformationFileName = null;

    private String inputVectorFileName = null;

    private String templateVectorFileName;

    private String dataWinnerMappingFileName = null;

    private String linkageMapFileName = null;

    private JFrame docViewerFrame = null;

    private boolean documentMode = false;

    private String viewerWorkingDirectoryName = ".";

    private String applicationDirectory = ".";

    private SOMViewerProperties prefs;

    private LoggingHandler loggingHandler = null;

    private BackgroundImageVisualizer initialVisualisation;

    private int initialVisualisationVariant;

    private String classColoursFile = null;

    // menu
    private JMenuBar menuBar = null;

    private JMenu visualizationMenu = null;

    private JMenu paletteMenu = null;

    private ButtonGroup visualizationMenuItemGroup = null;

    private ButtonModel oldSelectedVisualizationMenuItem = null;

    private JMultiLineRadioButtonMenuItem thematicClassRadioButton = null;

    private JCheckBoxMenuItem reversePaletteMenuItem = null;

    private ButtonGroup linkages;

    private int clusteringLevel = 1;

    private ButtonModel oldLinkage; // previously selected item

    private JMenu windowMenu;

    // ToolBar
    private JToolBar toolBar = null;

    private AbstractButton pieChartToggleButton;

    private AbstractButton shiftOverlappingToggleButton;

    private AbstractButton exactPlacementToggleButton;

    private AbstractButton linkageToggleButton;

    // StatusBar
    private StatusBar statusBar = null;

    // panes and other stuff
    private JDesktopPane desktop = new JDesktopPane();

    private ClassLegendPane classLegendPane = null;

    private VisualizationControl visControlPanel = null;

    private SOMPane mapPane = null;

    private PalettePanel palettePanel = null;

    private QuerySOMPanel queryPane = null;

    private CommonSOMViewerStateData state = new CommonSOMViewerStateData(this, 220);

    private SOMFrame somFrame = new SOMFrame(state);

    private Dimension screenSize;

    // SOM Comparison stuff:
    private JCheckBoxMenuItem showShiftsMenuItem = null;

    private ShiftsControlPanel shiftsControlPanel = null;

    private JMenu switchMapSubmenu = null;

    private JMultiLineRadioButtonMenuItem useMainMap = null;

    private JMultiLineRadioButtonMenuItem useSecondMap = null;

    private boolean noInternalPlayer;

    private Vector<VisualizationChangeListener> visChangeListeners = new Vector<VisualizationChangeListener>();

    private JMenuItem paletteEditorMenuItem = null;

    /**
     * Starts a new SOM Viewer frame.
     * 
     * @param args Needed program arguments:
     *            <ul>
     *            <li>-u unitDescriptionFileName, mandatory</li>
     *            <li>-w weightVectorFileName, mandatory</li>
     *            <li>-l drawLines, switch</li>
     *            <li>-m mapDescriptionFileName, optional</li>
     *            <li>-c classInformationFileName, optional</li>
     *            <li>-r regressionInformationFileNameInformationFileName, optional</li>
     *            <li>-d dataNamesFilename, optional</li>
     *            <li>-i dataInfoFileName, optional</li>
     *            <li>-v inputVectorFile, optional</li>
     *            <li>-t templateVectorFile, optional</li>
     *            <li>--dw dataWinnerMappingFile, optional</li>
     *            <li>-t templateVectorFile, optional</li>
     *            <li>-p fileNamePrefix, optional</li>
     *            <li>-s fileNameSuffix, optional</li>
     *            <li>--dir viewerWorkingDirectory, optional</li>
     *            <li>-o documentMode, switch, default = false</li>
     *            <li>imageName</li>
     *            </ul>
     * @throws HeadlessException
     */
    public SOMViewer(JSAPResult config) throws HeadlessException {
        super();

        // just trigger to do initialisation work as finding visualisations and palettes in the beginning
        // with a current bug happening sometimes on loading the visualisation classes, this also prevents spending time on loading the input data,
        // and then the application to hang
        Visualizations.getAvailableVisualizations();
        Palettes.getAvailablePalettes();

        /* set handler for all logging events, which decides where to output it */
        try {
            loggingHandler = new LoggingHandler();
            loggingHandler.setLevel(Level.FINEST);
            loggingHandler.setParentComponent(this);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").setLevel(Level.FINEST);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").addHandler(loggingHandler);
        } catch (SecurityException e1) {
            e1.printStackTrace();
        }

        // Jakob Frank: init state moved here - avoids NullPointerException
        state = new CommonSOMViewerStateData(this, 220);

        // highlightedDataNamesFileName = config.getString("highlightedDataNamesFile");
        unitDescriptionFileName = config.getString("unitDescriptionFile");
        weightVectorFileName = config.getString("weightVectorFile");
        mapDescriptionFileName = config.getString("mapDescriptionFile");
        classInformationFileName = config.getString("classInformationFile");
        regressionInformationFileName = config.getString("regressionInformationFile");
        dataInformationFileName = config.getString("dataInformationFile");
        inputVectorFileName = config.getString("inputVectorFile");
        templateVectorFileName = config.getString("templateVectorFile");
        dataWinnerMappingFileName = config.getString("dataWinnerMappingFile");
        linkageMapFileName = config.getString("linkageMapFile");
        classColoursFile = config.getString("classColours");
        noInternalPlayer = config.getBoolean("noplayer");

        final String s = config.getString("secondSOMPrefix");
        if (StringUtils.isNotBlank(s)) {
            final File file = new File(s).getAbsoluteFile();
            if (file.getParentFile() != null && file.getParentFile().exists()) {
                state.secondSOMName = file.getAbsolutePath();
                // add SOM to comparison visualisation
                ((ComparisonVisualizer) Visualizations.getVisualizationByName("ComparisonMean").getVis()).addSOM(s);
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Couldn't find path for second some, given: '" + s + ", resolves to: '" + file.getAbsolutePath() + "'.");
            }
        }

        String inputCorrectionsFileName = config.getString("inputCorrections");

        if (config.getString("applicationDirectory") != null) {
            applicationDirectory = config.getString("applicationDirectory");
        }
        if (config.getString("viewerWorkingDirectory") != null) {
            viewerWorkingDirectoryName = config.getString("viewerWorkingDirectory");
        } else { // default the directory to the unit file location as the most likely path
            viewerWorkingDirectoryName = FileUtils.getPathFrom(unitDescriptionFileName);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Defaulting viewer working directory to " + viewerWorkingDirectoryName);
        }

        if (config.getString("fileNamePrefix") != null) {
            CommonSOMViewerStateData.fileNamePrefix = config.getString("fileNamePrefix");
        }
        if (config.getString("fileNameSuffix") != null) {
            CommonSOMViewerStateData.fileNameSuffix = config.getString("fileNameSuffix");
        }

        documentMode = config.getBoolean("documentMode", false);

        /* load preferences from file */
        String prefs_file = applicationDirectory + System.getProperty("file.separator") + PREFS_FILE;
        try {
            // first try properties file for specific operating system
            String osname = System.getProperty("os.name").split(" ", 2)[0];
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading preferences from " + prefs_file + "." + osname);
            prefs = new SOMViewerProperties(prefs_file + "." + osname);
        } catch (PropertiesException e) {
            try {
                // then try to read generic properties file
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("FAILED. Reading preferences from " + prefs_file);
                prefs = new SOMViewerProperties(prefs_file);
            } catch (PropertiesException pe) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(pe.getMessage());
                // TODO show message
                prefs = new SOMViewerProperties(); // create empty prefs to avoid NullPointerExceptions
            }
        }
        // Load the users personal prefs-file (if it exists)
        File userPrefs = SOMToolboxMetaConstants.USER_SOMVIEWER_PREFS;
        if (userPrefs.exists() && userPrefs.canRead()) {
            try {
                prefs.load(new FileReader(userPrefs));
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Could not load users preferences file: " + userPrefs.getPath());
            }
        }

        CommonSOMViewerStateData.somViewerProperties = prefs;
        state.loadPalettes();

        if (prefs.getAudioPlayer() != null) {
            CommonSOMViewerStateData.MimeTypes.setAudioPlayer(prefs.getAudioPlayer());
        }

        /** ** input objects *** */
        state.inputDataObjects = new SharedSOMVisualisationData(classInformationFileName, regressionInformationFileName, dataInformationFileName,
                dataWinnerMappingFileName, inputVectorFileName, templateVectorFileName, linkageMapFileName);
        // reading input objects, if we have filenames set
        state.inputDataObjects.readAvailableData();

        if (StringUtils.isNotBlank(inputCorrectionsFileName)) {
            state.inputDataObjects.setFileName(SOMVisualisationData.INPUT_CORRECTIONS, inputCorrectionsFileName);
        }

        createAndShowGUI();

        // if passed as parameter - set initial palette
        String initialPalette = config.getString("initialPalette");
        if (initialPalette != null) {
            Palette palette = Palettes.getPaletteByName(initialPalette);
            if (palette != null) {
                state.setDefaultPalette(palette);
                // activate palette in menu
                for (int i = 0; i < paletteMenu.getMenuComponentCount(); i++) {
                    if (paletteMenu.getMenuComponent(i) instanceof JRadioButtonMenuItem) {
                        JRadioButtonMenuItem rb = (JRadioButtonMenuItem) paletteMenu.getMenuComponent(i);
                        if (at.tuwien.ifs.somtoolbox.util.StringUtils.equalsAny(rb.getText(), palette.getName(), palette.getShortName())) {
                            rb.setSelected(true);
                            break;
                        }
                    }
                }

            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Unknown initial palette '" + initialPalette + "'.");
            }
        }

        // if passed as parameter - set initial visualisation
        String initialVis = config.getString("initialVisualisation");
        if (initialVis != null) {
            BackgroundImageVisualizerInstance vis = Visualizations.getVisualizationByName(initialVis);
            if (vis != null) {
                initialVisualisation = vis.getVis();
                initialVisualisationVariant = vis.getVariant();
                try {
                    if (initialVisualisation instanceof AbstractMatrixVisualizer) {
                        ((AbstractMatrixVisualizer) initialVisualisation).reversePalette();
                    }

                    // if passed as parameter - set initial visualisation params
                    String initialVisParams = config.getString("initialVisParams");
                    if (initialVisParams != null) {
                        // set params for SDH
                        if (initialVisualisation instanceof SmoothedDataHistograms) {
                            try {
                                int smoothingFactor = Integer.parseInt(initialVisParams);
                                SmoothedDataHistograms sdh = (SmoothedDataHistograms) initialVisualisation;
                                sdh.setSmoothingFactor(smoothingFactor);
                                SDHControlPanel controlPanel = (SDHControlPanel) sdh.getControlPanel();
                                controlPanel.spinnerSmoothingFactor.setValue(new Integer(smoothingFactor));
                            } catch (NumberFormatException e) {
                                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                        "Visualisation Param 'Smoothing factor' is not a number: '" + initialVisParams + "'.");
                            }
                        } else if (initialVisualisation instanceof ThematicClassMapVisualizer) {
                            ThematicClassMapVisualizer them = (ThematicClassMapVisualizer) initialVisualisation;
                            boolean voronoi = false;
                            boolean chessBoard = false;
                            double minVisibleClass = 0;
                            String[] params = initialVisParams.split(",");
                            for (String param : params) {
                                String[] property = param.split("=");
                                if (property == null || property.length != 2) {
                                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                                            "Visualisation property '" + property + "' is not correct in : '" + initialVisParams + "'.");
                                } else {
                                    if (property[0].equalsIgnoreCase("voronoi")) {
                                        voronoi = Boolean.valueOf(property[1]).booleanValue();
                                    } else if (property[0].equalsIgnoreCase("chessboard")) {
                                        chessBoard = Boolean.valueOf(property[1]).booleanValue();
                                    } else if (property[0].equalsIgnoreCase("minVisibleClass")) {
                                        minVisibleClass = Double.valueOf(property[1]).doubleValue();
                                    }

                                }
                            }
                            System.out.println("params - chessBoard:" + chessBoard + ", voronoi:" + voronoi + ", minVisibleClass:" + minVisibleClass);
                            them.setInitialParams(chessBoard, voronoi, minVisibleClass);
                        } else if (initialVisualisation instanceof ComparisonVisualizer) {
                            ComparisonVisualizer compVis = (ComparisonVisualizer) initialVisualisation;
                            String[] soms = initialVisParams.split(",");
                            for (String string : soms) {
                                compVis.addSOM(string);
                            }
                        }
                    }

                    mapPane.setInitialVisualization(initialVisualisation, initialVisualisationVariant);

                    // select correct visualisation from menu
                    for (int i = 0; i < visualizationMenu.getMenuComponentCount(); i++) {
                        Component comp = visualizationMenu.getMenuComponent(i);
                        if (comp instanceof JRadioButtonMenuItem) {
                            JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) comp;
                            if (at.tuwien.ifs.somtoolbox.util.StringUtils.equalsAny(radioButton.getText(), vis.getShortName(), vis.getName())) {
                                radioButton.setSelected(true);
                            }
                        }
                    }

                    updatePalettePanel();
                    visControlPanel.updateVisualisationControl();

                } catch (SOMToolboxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Unknown initial visualisation '" + initialVis + "'.");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Valid options are: " + Arrays.toString(Visualizations.getAvailableVisualizationNames()));
            }
        }

        // if passed as parameter - set second SOM
        if (StringUtils.isNotBlank(getMap().getState().secondSOMName)) {
            updateSOMComparison(true);
            mapPane.setShiftArrowsVisibility(true);
            mapPane.centerAndFitMapToScreen(0);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (screenSize == null) {
            screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            screenSize.height -= 48;
        }
        return (screenSize);
    }

    private void createAndShowGUI() {
        /** ** files given on command line are read in here, map is created *** */
        mapPane = new SOMPane(this, weightVectorFileName, unitDescriptionFileName, mapDescriptionFileName, state);

        // basics
        UiUtils.setSOMToolboxLookAndFeel();
        setDefaultLookAndFeelDecorated(true);

        // setTitle("PlaySOM");
        setTitle("SOM Viewer - " + weightVectorFileName);
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(RESOURCE_PATH_ICONS + "somviewer_logo-24.png")));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Could not find application logo image file. Continuing.");
        }

        initWindowClosing();

        state.fileChooser = new JFileChooser(new File(viewerWorkingDirectoryName + "/."));

        /** ** MENU *** */
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // begin file menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem dataFilesMenuItem = new JMenuItem("Data Files");
        dataFilesMenuItem.setMnemonic(KeyEvent.VK_F);
        dataFilesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SharedSOMVisualisationDataDialog(SOMViewer.this, state).setVisible(true);
            }
        });
        fileMenu.add(dataFilesMenuItem);

        // add the plot data menu entry
        JMenuItem plotDataMenuItem;
        if (state.inputDataObjects == null || state.inputDataObjects.getInputData() == null) {
            plotDataMenuItem = new JMenuItem("Plot data (data needs to be loaded!)");
            plotDataMenuItem.setEnabled(false);
        } else if (state.inputDataObjects.getInputData().dim() > 3) {
            plotDataMenuItem = new JMenuItem("Plot data (only for dim <= 3)");
            plotDataMenuItem.setEnabled(false);
        } else {
            plotDataMenuItem = new JMenuItem("Plot data");
        }
        plotDataMenuItem.setMnemonic(KeyEvent.VK_P);
        plotDataMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DataSetViewer viewer;
                SOMLibClassInformation classInfo = state.inputDataObjects.getClassInfo();
                InputData inputData = state.inputDataObjects.getInputData();
                if (inputData == null) {
                    SOMVisualisationData inputObject = state.inputDataObjects.getObject(SOMVisualisationData.INPUT_VECTOR);
                    try {
                        inputObject.loadFromFile(state.fileChooser, SOMViewer.this);
                        inputData = state.inputDataObjects.getInputData();
                    } catch (SOMToolboxException e1) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Input data file needed!");
                    }
                    if (inputData == null) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Input data file needed!");
                        return;
                    }
                }
                if (classInfo == null) {
                    viewer = new DataSetViewer(SOMViewer.this, inputData.getData());
                } else {
                    inputData.setClassInfo(classInfo);
                    String[] classNames = classInfo.classNames();
                    double[][][] data = new double[classNames.length][][];
                    for (int i = 0; i < classNames.length; i++) {
                        try {
                            data[i] = inputData.getData(classNames[i]);
                        } catch (SOMToolboxException ex) {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error retrieving class info: " + ex.getMessage());
                        }
                    }
                    viewer = new DataSetViewer(SOMViewer.this, classNames, classLegendPane.getColors(), data);
                }
                viewer.setVisible(true);
            }

        });
        fileMenu.add(plotDataMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);
        // end file menu

        getContentPane().setLayout(new BorderLayout());

        /** ** TOOLBar *** */
        toolBar = new JToolBar("SOMViewer Toolbar");

        toolBar.add(makeToolbarButton("center3d-24.png", CENTER_AND_FIT_MAP, "Center Map"));
        toolBar.add(makeToolbarButton("reset-desktop.png", RESET_DESKTOP_LAYOUT, "Reset Layout"));
        toolBar.addSeparator();

        // Selection handler buttons
        ArrayList<AbstractButton> selectionHandlerButtons = new ArrayList<AbstractButton>();
        selectionHandlerButtons.add(makeToolbarToggleButton("rectangle.png", SELECT_RECTANGLE, "Rectangle Selection", true));
        selectionHandlerButtons.add(makeToolbarToggleButton("line.png", SELECT_LINE, "Line Selection", false));
        selectionHandlerButtons.add(makeToolbarToggleButton("clusterSelection.png", SELECT_CLUSTER, "Cluster Selection", false));
        // rudi: moving inputs for semi-supervised learning
        selectionHandlerButtons.add(makeToolbarToggleButton("moveinput.png", MOVE_INPUT, "Move Inputs", false));
        // Angela: move Labels
        selectionHandlerButtons.add(makeToolbarToggleButton("movelabel.png", MOVE_LABEL, "Move Labels", false));

        ButtonGroup selectionHandlerButtonGroup = new ButtonGroup();
        for (AbstractButton button : selectionHandlerButtons) {
            toolBar.add(button);
            selectionHandlerButtonGroup.add(button);
        }
        toolBar.addSeparator();
        // end selection handler buttons

        // toggles for map details (pie-charts, labels, data info
        boolean classInfoLoaded = getMap().getInputObjects().getClassInfo() != null;
        pieChartToggleButton = makeToolbarToggleButton("piechart.png", TOGGLE_PIE_CHARTS, "Pie charts", classInfoLoaded);
        pieChartToggleButton.setEnabled(classInfoLoaded);
        toolBar.add(pieChartToggleButton);

        toolBar.add(makeToolbarToggleButton("labels.png", TOGGLE_LABELS, TOGGLE_LABELS, state.labelVisibilityMode));

        toolBar.add(makeToolbarToggleButton("data.png", TOGGLE_DATA, TOGGLE_DATA, state.dataVisibilityMode));

        boolean linkageInfoLoaded = getMap().getInputObjects().getLinkageMap() != null;
        linkageToggleButton = makeToolbarToggleButton("linkage.png", TOGGLE_LINKAGE, "Linkage", state.displayInputLinkage);
        linkageToggleButton.setEnabled(linkageInfoLoaded);
        toolBar.add(linkageToggleButton);

        toolBar.addSeparator();

        // toggles specific for input location (exact placement)
        exactPlacementToggleButton = makeToolbarToggleButton("exactPlacement.png", TOGGLE_EXACT_PLACEMENT, "Exact", state.exactUnitPlacement);
        toolBar.add(exactPlacementToggleButton);

        shiftOverlappingToggleButton = makeToolbarToggleButton("shiftOverlapping.png", TOGGLE_RELOCATE, "Relocate", state.shiftOverlappingInputs);
        toolBar.add(shiftOverlappingToggleButton);

        if (!state.exactUnitPlacementEnabled) {
            exactPlacementToggleButton.setEnabled(false);
            exactPlacementToggleButton.setToolTipText(TOGGLE_EXACT_PLACEMENT + ": " + MSG_EXACTPLACEMENT_DISABLED);
            shiftOverlappingToggleButton.setEnabled(false);
            shiftOverlappingToggleButton.setToolTipText(TOGGLE_RELOCATE + ": " + MSG_EXACTPLACEMENT_DISABLED);
        }

        toolBar.addSeparator();
        // end toggles for map details

        toolBar.add(makeToolbarButton("createlabel.png", CREATE_LABEL, "New label"));
        toolBar.addSeparator();

        AbstractButton btn3dViewer = makeToolbarButton("somviewer3D_logo-24.png", SOMVIEWER_3D, "3D");
        // Check if 3D is available
        try {
            Class.forName("at.tuwien.ifs.somtoolbox.apps.viewer3d.SOMViewer3D");
        } catch (ClassNotFoundException e1) {
            btn3dViewer.setEnabled(false);
        }
        toolBar.add(btn3dViewer);
        toolBar.addSeparator();

        getContentPane().add(toolBar, BorderLayout.NORTH);

        /** ** StatusBar *** */
        statusBar = new StatusBar();
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        if (loggingHandler != null) {
            loggingHandler.setStatusBar(statusBar);
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Initializing GUI ...");

        /** ** Layout of the left control pane *** */
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;

        /** ** Map Overview *** */
        MapOverviewPane mapOverviewPane = new MapOverviewPane("Map overview", state);
        mapOverviewPane.connect(mapPane.getCanvas(), new PLayer[] { mapPane.getCanvas().getLayer() });
        desktop.add(mapOverviewPane, c);

        /** ** Palette panel *** */
        palettePanel = new PalettePanel("Palette", state);
        desktop.add(palettePanel, c);

        /** ** PlaySOMPanel *** */
        AbstractSelectionPanel selectionPanel = makeSelectionPanel();
        mapPane.connectSelectionHandlerTo(selectionPanel);
        c.weighty = 0.5; // IMPORTANT note: these values remain active for ComparisonPanel
        c.weightx = 0.1; // and are reset afterwards
        desktop.add(selectionPanel, c);
        state.selectionPanel = selectionPanel;

        // added by Philip Langer
        /** ** HierarchicalDiverPanel *** */
        AbstractSelectionPanel diverPanel = new GHSOMNavigationPanel(state, mapPane);
        mapPane.connectSelectionHandlerTo(diverPanel);
        diverPanel.setVisible(false);
        desktop.add(diverPanel, c);
        // /added by Philip Langer

        /** ** Comparison *** */
        ComparisonPanel compPanel = new ComparisonPanel(state);
        mapPane.connectSelectionHandlerTo(compPanel);
        desktop.add(compPanel, c);
        compPanel.setVisible(false);
        c.weightx = 0.0;
        c.weighty = 0.0;

        MultichannelPlaybackPanel multichannelPlaybackPanel = new MultichannelPlaybackPanel(state, mapPane);
        mapPane.connectSelectionHandlerTo(compPanel);
        desktop.add(mapPane, c);
        // mapPane.setVisible(false);
        c.weightx = 0.0;
        c.weighty = 0.0;

        /** ** Class legend panel *** */
        classLegendPane = new ClassLegendPane(mapPane, "Class legend", state);
        desktop.add(classLegendPane, c);

        /** ** Query legend panel *** */
        queryPane = new QuerySOMPanel("Query", state);
        desktop.add(queryPane, c);
        if (!documentMode) {
            queryPane.setVisible(false);
        }

        /** ** Map detail panel *** */
        MapDetailPanel mapDetailPanel = new MapDetailPanel("Map details", state);
        mapDetailPanel.setVisible(false);
        desktop.add(mapDetailPanel, c);

        /** ** Shifts Control Panel *** */
        shiftsControlPanel = new ShiftsControlPanel(mapPane, state, "Shifts Control Panel");
        desktop.add(shiftsControlPanel, c);

        /** ** Visualization Control panel (e.g. SDH) *** */
        visControlPanel = new VisualizationControl("Visualisation control", state, mapPane); // just a placeholder for the actual control panel
        desktop.add(visControlPanel, c);

        /** ** Clustering Control panel (Angela) *** */
        ClusteringControl clusteringControl = new ClusteringControl("Clustering Control", state, mapPane);
        clusteringControl.setVisible(false);
        desktop.add(clusteringControl, c);

        /* PocketSOM Conncetor (Jakob) */
        PocketSOMConnector psCon = new PocketSOMConnector("PocketSOM Connector", state);
        psCon.setVisible(false);
        desktop.add(psCon, c);

        /* */
        PlaygroundPanel pgp = new PlaygroundPanel("Playground", state);
        pgp.setVisible(false);
        desktop.add(pgp, c);

        somFrame.getContentPane().add(mapPane);
        somFrame.toBack();
        desktop.add(somFrame);

        getContentPane().add(desktop, BorderLayout.CENTER);

        createVisualizationMenu();
        menuBar.add(visualizationMenu);

        /** ** palette menu *** */
        menuBar.add(createPaletteMenu());

        // Angela: add the cluster menu
        createClusterMenu();

        // add the export menu
        createExportMenu();

        // add the input correction menu
        JMenu inputCorrectionMenu = new JMenu("Input correction");

        JMenuItem loadInputCorrectionsFileMenuItem = new JMenuItem("Load corrections file");
        loadInputCorrectionsFileMenuItem.setMnemonic(KeyEvent.VK_L);
        loadInputCorrectionsFileMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File inputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser, "Load input corrections file",
                        new FileNameExtensionFilter("Input corrections file (*.cor)", "corr"));
                if (inputFile != null) {
                    try {
                        state.inputDataObjects.getInputCorrections().readFromFile(inputFile.getAbsolutePath(), state.growingLayer,
                                state.inputDataObjects.getInputData());
                        getMap().createInputCorrectionArrows();
                        getMap().updateDetailsAfterMoving();
                        JOptionPane.showMessageDialog(SOMViewer.this, "Loading input corrections from file '" + inputFile.getAbsolutePath()
                                + "' finished!");
                    } catch (SOMToolboxException ex) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
                    }
                }
            }
        });
        inputCorrectionMenu.add(loadInputCorrectionsFileMenuItem);

        JMenuItem saveInputCorrectionsFileMenuItem = new JMenuItem("Save corrections file");
        saveInputCorrectionsFileMenuItem.setMnemonic(KeyEvent.VK_S);
        saveInputCorrectionsFileMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File outputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser, "Save input corrections file",
                        new FileNameExtensionFilter("Input corrections file (*.corr)", "corr"));
                if (outputFile != null) {
                    try {
                        String outputFileName = outputFile.getAbsolutePath();
                        if (!outputFileName.endsWith(".corr")) {
                            outputFileName += ".corr";
                            outputFile = new File(outputFileName);
                        }
                        state.inputDataObjects.getInputCorrections().writeToFile(outputFile);
                        JOptionPane.showMessageDialog(SOMViewer.this, "Saving input corrections to file '" + outputFile.getAbsolutePath()
                                + "' finished!");
                    } catch (SOMToolboxException ex) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
                    }
                }
            }
        });
        inputCorrectionMenu.add(saveInputCorrectionsFileMenuItem);

        JMenuItem saveUnitFileMenuItem = new JMenuItem("Save moved unit file");
        saveUnitFileMenuItem.setMnemonic(KeyEvent.VK_U);
        saveUnitFileMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File outputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser, "Save unit file", new FileNameExtensionFilter(
                        "Unit description files (*.unit)", "unit"));
                if (outputFile != null) {
                    try {
                        SOMLibMapOutputter.writeUnitDescriptionFile(state.growingSOM, outputFile.getParentFile().getAbsolutePath(),
                                outputFile.getName(), true);
                        JOptionPane.showMessageDialog(SOMViewer.this, "Saving to unit file '" + outputFile.getAbsolutePath() + "' finished!");
                    } catch (IOException ex) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error writing unit file: " + ex.getMessage());
                    }
                }
            }
        });
        inputCorrectionMenu.add(saveUnitFileMenuItem);

        JMenuItem clearInputCorrectionsMenuItem = new JMenuItem("Clear corrections");
        clearInputCorrectionsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (InputCorrection correction : state.inputDataObjects.getInputCorrections().getInputCorrections()) {
                    correction.getSourceUnit().addMappedInput(correction.getLabel(), correction.getOriginalDistance(), true);
                    correction.getTargetUnit().removeMappedInput(correction.getLabel());
                }
                getMap().updateDetailsAfterMoving();
                state.inputDataObjects.getInputCorrections().getInputCorrections().clear();
                getMap().clearInputCorrections();
            }
        });
        inputCorrectionMenu.add(clearInputCorrectionsMenuItem);

        final JCheckBoxMenuItem showInputCorrectionsMenuItem = new JCheckBoxMenuItem("Show corrections");
        showInputCorrectionsMenuItem.setSelected(true);
        showInputCorrectionsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getMap().setInputCorrectionsVisible(showInputCorrectionsMenuItem.isSelected());
            }
        });
        inputCorrectionMenu.add(showInputCorrectionsMenuItem);

        JMenu calcFeatureWeightsMenu = new JMenu("Calculate feature weights");

        JMenuItem calcFeatureWeightsGlobalMenuItem = new JMenuItem("Global");
        calcFeatureWeightsGlobalMenuItem.addActionListener(new CalculateFeatureWeightsActionListener(FeatureWeightMode.GLOBAL));
        calcFeatureWeightsMenu.add(calcFeatureWeightsGlobalMenuItem);

        JMenuItem calcFeatureWeightsLocalMenuItem = new JMenuItem("Local");
        calcFeatureWeightsLocalMenuItem.addActionListener(new CalculateFeatureWeightsActionListener(FeatureWeightMode.LOCAL));
        calcFeatureWeightsMenu.add(calcFeatureWeightsLocalMenuItem);

        JMenuItem calcFeatureWeightsGeneralMenuItem = new JMenuItem("General");
        calcFeatureWeightsGeneralMenuItem.addActionListener(new CalculateFeatureWeightsActionListener(FeatureWeightMode.GENERAL));
        calcFeatureWeightsMenu.add(calcFeatureWeightsGeneralMenuItem);

        inputCorrectionMenu.add(calcFeatureWeightsMenu);

        menuBar.add(inputCorrectionMenu);

        // end mapCorrectionMenu

        // add the labelling menu
        JMenu labelMenu = new JMenu("Labelling");
        JMenuItem runLabelMenuItem = new JMenuItem("Rerun labelling");
        runLabelMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LabellingDialog(state).setVisible(true);
            }
        });
        labelMenu.add(runLabelMenuItem);
        menuBar.add(labelMenu);

        // window menu
        state.registerComponentWindow(toolBar, "Toolbar");
        state.registerComponentWindow(somFrame, "SOM Map");
        // controls, ordered in their appearance
        state.registerComponentWindow(mapOverviewPane, "Map Overview");
        state.registerComponentWindow(selectionPanel, "Play Selection");
        // added by Philip Langer
        state.registerComponentWindow(diverPanel, "Hierarchical Diver Control");
        // /added by Philip Langer
        state.registerComponentWindow(classLegendPane, "Class Legend");
        state.registerComponentWindow(shiftsControlPanel, "Shifts Control");
        state.registerComponentWindow(palettePanel, "Palette");
        state.registerComponentWindow(compPanel, "Label Comparison");
        state.registerComponentWindow(queryPane, "Map Query");
        state.registerComponentWindow(visControlPanel, "Visualization Control");
        state.registerComponentWindow(clusteringControl, "Clustering Control");
        state.registerComponentWindow(mapDetailPanel, "Map Detail Control");
        state.registerComponentWindow(psCon, "PocketSOM Connector");
        state.registerComponentWindow(pgp, "Playground");
        state.registerComponentWindow(multichannelPlaybackPanel, "Multichannel playback panel");

        // register observers to the class information
        getMap().getInputObjects().getObject(SOMVisualisationData.CLASS_INFO).addObserver(this);

        pack();
        somFrame.pack();
        somFrame.setLocation(state.controlElementsWidth, 0);
        somFrame.setVisible(true);

        createWindowMenu(mapOverviewPane);

        alignControlElements(false);

        mapPane.centerAndFitMapToScreen(0);

        if (documentMode) {
            initDocViewer(selectionPanel);
        }

        if (classColoursFile != null && state.inputDataObjects.getClassInfo() != null) {
            state.inputDataObjects.getClassInfo().loadClassColours(new File(classColoursFile));
            classLegendPane.updateClassColours();
        }

        setVisible(true);
    }

    /**
     * 
     */
    private JMenu createPaletteMenu() {
        if (paletteMenu == null) {
            paletteMenu = new JMenu("Palette");
            paletteMenu.setMnemonic(KeyEvent.VK_P);

            rebuildPaletteMenu();
        }
        return paletteMenu;
    }

    /**
     * 
     */
    public void rebuildPaletteMenu() {
        paletteMenu.removeAll();
        ButtonGroup paletteMenuItemGroup = new ButtonGroup();

        for (int i = 0; i < state.palettes.length; i++) {
            Palette palette = state.palettes[i];
            JRadioButtonMenuItem paletteMenuItem = new JRadioButtonMenuItem(palette.getName());
            paletteMenuItem.setMnemonic(palette.getName().charAt(0));
            paletteMenuItemGroup.add(paletteMenuItem);
            paletteMenuItem.addActionListener(new PaletteCheckboxMenuItemListener(i));
            paletteMenu.add(paletteMenuItem);
            paletteMenuItem.setToolTipText(palette.getDescription());

            if (palette == state.defaultPalette) {
                paletteMenuItem.setSelected(true);
            }
        }

        paletteMenu.addSeparator();
        if (reversePaletteMenuItem == null) {
            reversePaletteMenuItem = new JCheckBoxMenuItem("Reverse");
            reversePaletteMenuItem.setMnemonic(KeyEvent.VK_R);
            reversePaletteMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        boolean success = getMap().reversePalette();
                        if (success) {
                            SOMViewer.this.visualizationChangeSuccess();
                        } else {
                            SOMViewer.this.visualizationChangeFailure();
                        }
                        updatePalettePanel();
                        mapPane.repaint();
                    } catch (SOMToolboxException ex) {
                        JOptionPane.showMessageDialog(SOMViewer.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        paletteMenu.add(reversePaletteMenuItem);
        paletteMenu.addSeparator();

        if (paletteEditorMenuItem == null) {
            paletteEditorMenuItem = new JMenuItem("Palette Editor");
            paletteEditorMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new PaletteEditor(SOMViewer.this, state).setVisible(true);
                }
            });
        }
        paletteMenu.add(paletteEditorMenuItem);
    }

    private void alignControlElements(boolean maximizeElements) {
        int totalMinimumHeight = 0;
        int totalPreferredHeight = 0;
        for (int i = 0; i < state.registeredViewerControls.size(); i++) {
            AbstractViewerControl comp = (AbstractViewerControl) state.registeredViewerControls.get(i);
            if (comp.isVisible()) {
                totalMinimumHeight += comp.getMinimumSize().getHeight();
                totalPreferredHeight += comp.getPreferredSize().getHeight();
            }
        }
        int maxHeight = desktop.getHeight();
        int surplus = maxHeight - totalMinimumHeight;
        int possibleSurplus = totalPreferredHeight - totalMinimumHeight;
        double surplusPercentage = 1;
        if (possibleSurplus > 0) {
            surplusPercentage = surplus / possibleSurplus;
        }

        int offsetY = 0;
        for (int i = 0; i < state.registeredViewerControls.size(); i++) {
            AbstractViewerControl comp = (AbstractViewerControl) state.registeredViewerControls.get(i);
            if (comp.isVisible()) {
                double compMinimumHeight = comp.getMinimumSize().getHeight();
                double compPreferredHeight = comp.getPreferredSize().getHeight();
                double realHeight = compMinimumHeight + ((compPreferredHeight - compMinimumHeight) * surplusPercentage);
                comp.setSize((int) comp.getPreferredSize().getWidth(), (int) realHeight);
                comp.setLocation(0, offsetY);
                offsetY += realHeight;
                if (maximizeElements) {
                    try {
                        comp.setIcon(false);
                    } catch (PropertyVetoException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void resetDesktopLayout() {
        alignControlElements(true);
        somFrame.pack();
        somFrame.setLocation(state.controlElementsWidth, 0);
    }

    private AbstractSelectionPanel makeSelectionPanel() {
        if (documentMode) {
            return new DocSOMPanel(state);
        } else {
            if (noInternalPlayer) {
                return new PlaySOMPanel(state);
            } else {
                return new PlaySOMPlayer(state);
            }
        }
    }

    private void createWindowMenu(MapOverviewPane mapOverviewPane) {
        windowMenu = new JMenu("Window");
        windowMenu.setMnemonic(KeyEvent.VK_W);

        ArrayList<String> componentNames = new ArrayList<String>(state.registeredComponentWindows.keySet());
        Collections.sort(componentNames);
        for (String name : componentNames) {
            Component component = state.registeredComponentWindows.get(name);
            MyJCheckBoxMenuItem checkBoxMenuItem = new MyJCheckBoxMenuItem(name, component);
            windowMenu.add(checkBoxMenuItem);
        }

        menuBar.add(windowMenu);
    }

    public void uncheckComponentInMenu(Component comp) {
        Component[] components = windowMenu.getMenuComponents();
        for (Component component2 : components) {
            if (component2 instanceof MyJCheckBoxMenuItem) {
                MyJCheckBoxMenuItem item = (MyJCheckBoxMenuItem) component2;
                if (item.getComponent() == comp) {
                    item.setSelected(false);
                }
            }
        }
    }

    private void createVisualizationMenu() {
        visualizationMenu = new JMenu("Visualization");
        visualizationMenu.setMnemonic(KeyEvent.VK_V);
        visualizationMenuItemGroup = new ButtonGroup();
        JRadioButtonMenuItem nonVis = new JRadioButtonMenuItem("none");
        nonVis.setToolTipText("no cluster visualization");
        nonVis.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapPane.setNoVisualization();
                palettePanel.setPalette(null);
            }
        });
        visualizationMenuItemGroup.add(nonVis);
        visualizationMenu.add(nonVis);
        for (int v = 0; v < mapPane.getVisualizations().length; v++) {
            visualizationMenu.add(new JSeparator());
            for (int w = 0; w < mapPane.getVisualizations()[v].getNumberOfVisualizations(); w++) {
                JMultiLineRadioButtonMenuItem mi = new JMultiLineRadioButtonMenuItem(mapPane.getVisualizations()[v].getVisualizationName(w));
                mi.addActionListener(new VisualizationActionListener(v, w));
                mi.setToolTipText(mapPane.getVisualizations()[v].getVisualizationDescription(w));
                visualizationMenuItemGroup.add(mi);
                visualizationMenu.add(mi);
                if (mapPane.getVisualizations()[v] instanceof ThematicClassMapVisualizer) {
                    thematicClassRadioButton = mi;
                    if (!((ThematicClassMapVisualizer) mapPane.getVisualizations()[v]).hasClassInfo()) {
                        mi.setEnabled(false);
                    }
                }
            }
        }
        nonVis.setSelected(true);
        oldSelectedVisualizationMenuItem = nonVis.getModel();

        visualizationMenu.add(new JSeparator());

        final JCheckBoxMenuItem showBackgroundImageMenuItem = new JCheckBoxMenuItem("Show background image");
        showBackgroundImageMenuItem.setMnemonic(KeyEvent.VK_B);
        showBackgroundImageMenuItem.setEnabled(false);
        showBackgroundImageMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getMap().setBackgroundImageVisibility(showBackgroundImageMenuItem.isSelected());
                mapPane.getCurrentVisualization().getControlPanel().updateSwitchControls();
            }
        });
        visualizationMenu.add(showBackgroundImageMenuItem);

        JMenuItem loadBackgroundImageMenuItem = new JMenuItem("Load background image");
        loadBackgroundImageMenuItem.setMnemonic(KeyEvent.VK_L);
        loadBackgroundImageMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = state.fileChooser;
                if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                    state.fileChooser = new JFileChooser(fileChooser.getSelectedFile().getPath());
                }
                fileChooser.setName("Open Background Image");
                int returnVal = fileChooser.showDialog(SOMViewer.this, "Open Image");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage image = ImageIO.read(new File(fileChooser.getSelectedFile().getAbsolutePath()));
                        getMap().setBackgroundImage(image);
                        showBackgroundImageMenuItem.setEnabled(true);
                        showBackgroundImageMenuItem.setSelected(true);
                        if (mapPane.getCurrentVisualization() != null && mapPane.getCurrentVisualization().getControlPanel() != null) {
                            mapPane.getCurrentVisualization().getControlPanel().updateSwitchControls();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        visualizationMenu.add(loadBackgroundImageMenuItem);

        showShiftsMenuItem = new JCheckBoxMenuItem("Show data shifts");
        showShiftsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapPane.setShiftArrowsVisibility(showShiftsMenuItem.isSelected());
                shiftsControlPanel.setEnabled(showShiftsMenuItem.isSelected());
            }
        });
        if (getMap().getState().secondSOMName.equals("")) {
            showShiftsMenuItem.setEnabled(false);
        } else {
            showShiftsMenuItem.setSelected(true);
            shiftsControlPanel.setVisible(true);
        }
        visualizationMenu.add(showShiftsMenuItem);

        // switch map submenu
        switchMapSubmenu = new JMenu("Switch map");
        switchMapSubmenu.setToolTipText("Switch visualisations between main and second map.");

        useMainMap = new JMultiLineRadioButtonMenuItem("Use main map");
        useMainMap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // add magic here.
                // mapPane.setShiftsVisibility(showShiftsMenuItem.isSelected());
                // shiftsControlPanel.setEnabled(showShiftsMenuItem.isSelected());
                // mapPane.updateShifts();
            }
        });
        useSecondMap = new JMultiLineRadioButtonMenuItem("Use second map");
        useSecondMap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // add magic here.
                // mapPane.setShiftsVisibility(showShiftsMenuItem.isSelected());
                // shiftsControlPanel.setEnabled(showShiftsMenuItem.isSelected());
                // mapPane.updateShifts();
            }
        });

        ButtonGroup switchMapMenuGroup = new ButtonGroup();
        switchMapMenuGroup.add(useMainMap);
        switchMapMenuGroup.add(useSecondMap);

        switchMapSubmenu.add(useMainMap);
        switchMapSubmenu.add(useSecondMap);

        if (getMap().getState().secondSOMName.equals("")) {
            switchMapSubmenu.setEnabled(false);
        } else {
            switchMapSubmenu.setSelected(true);
        }

        visualizationMenu.add(switchMapSubmenu);

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("SOMViewer ready.");
    }

    /**
     * creates a menu entry for exporting the current visualization
     */
    private void createExportMenu() {
        JMenu exportMenu = new JMenu("Export");
        exportMenu.setMnemonic(KeyEvent.VK_X);

        JMenuItem exportMapPane = new JMenuItem("Export current MapPane ...");
        exportMapPane.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File filePath = ExportUtils.getFilePath(SOMViewer.this, SOMViewer.this.state.getFileChooser(), "Save MapPane as PNG");
                if (filePath != null) {
                    try {
                        ExportUtils.saveMapPaneAsImage(mapPane, filePath.getAbsolutePath());
                        JOptionPane.showMessageDialog(SOMViewer.this, "Export to file finished!");
                    } catch (SOMToolboxException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Error saving", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JMenuItem exportVisualization = new JMenuItem("Export current Visualization ...");
        exportVisualization.setMnemonic(KeyEvent.VK_X);
        exportVisualization.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File filePath = ExportUtils.getFilePath(SOMViewer.this, SOMViewer.this.state.getFileChooser(), "Save Visualization as PNG");
                if (filePath != null) {
                    try {
                        ExportUtils.saveVisualizationAsImage(SOMViewer.this.state, -1, filePath.getAbsolutePath());
                        JOptionPane.showMessageDialog(SOMViewer.this, "Export to file finished!");
                    } catch (SOMToolboxException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Error saving", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JMenuItem exportToPocketSOMFormat = new JMenuItem("Export to PocketSOMFormat ...");
        exportToPocketSOMFormat.setMnemonic(KeyEvent.VK_P);
        exportToPocketSOMFormat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File outputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser, "Save in PocketSOMFormat");
                if (outputFile != null) {
                    PocketSOMFormatUtils.convertMapFormat(state.growingLayer, outputFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(SOMViewer.this, "Export to PocketSOMFormat finished!");
                }
            }
        });

        JMenuItem exportRhythmPatterns = new JMenuItem("Export Rhythm Patterns Images ...");
        exportRhythmPatterns.setMnemonic(KeyEvent.VK_R);
        exportRhythmPatterns.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File outputFile = ExportUtils.getFilePath(SOMViewer.this, state.fileChooser, "Export Rhythm Patterns Images");
                if (outputFile != null) {
                    try {
                        ExportUtils.saveRhythmPatternsOfWeightVectors(outputFile.getAbsolutePath(), state.growingLayer);
                        JOptionPane.showMessageDialog(SOMViewer.this, "Export of Rhythm Patterns Images finished!");
                    } catch (SOMToolboxException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Error exporting Rhythm Patterns Images!",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JMenuItem exportVisualizationAdvanced = new JMenuItem("Export as HTML ...");
        exportVisualizationAdvanced.setMnemonic(KeyEvent.VK_X);
        exportVisualizationAdvanced.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExportDialog(SOMViewer.this, state).setVisible(true);
            }
        });

        JMenuItem exportTuxRacer = new JMenuItem("Export TuxRacer Map...");
        exportTuxRacer.setMnemonic(KeyEvent.VK_T);
        exportTuxRacer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TuxRacerExportDialog(SOMViewer.this, state).setVisible(true);
            }
        });

        // ADDED: SEBASTIAN SKRITEK -
        JMenuItem exportReport = new JMenuItem("Create Report ... ");
        exportReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ReportGenerator(false, state);
            }
        });

        exportMenu.add(exportMapPane);
        exportMenu.add(exportVisualization);
        exportMenu.add(exportVisualizationAdvanced);
        exportMenu.add(exportRhythmPatterns);
        exportMenu.add(exportToPocketSOMFormat);
        exportMenu.add(exportTuxRacer);
        exportMenu.add(exportReport);
        menuBar.add(exportMenu);
    }

    private class CalculateFeatureWeightsActionListener implements ActionListener {
        private FeatureWeightMode mode;

        public CalculateFeatureWeightsActionListener(FeatureWeightMode mode) {
            this.mode = mode;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                InputCorrections calculatedCorrections = getMap().getGsom().getLayer().computeUnitFeatureWeights(
                        state.inputDataObjects.getInputCorrections(), state.inputDataObjects.getInputData(), mode);
                // remove potentially existing nodes
                getMap().clearInputCorrections(CreationType.COMPUTED);
                for (InputCorrection correction : calculatedCorrections.getInputCorrections()) {
                    ArrowPNode arrow = ArrowPNode.createInputCorrectionArrow(correction, InputCorrections.CreationType.COMPUTED, getMap().getUnit(
                            correction.getSourceUnit()), getMap().getUnit(correction.getTargetUnit()));
                    getMap().getInputCorrectionsPNode().addChild(arrow);
                    arrow.moveToBack();
                }
            } catch (SOMToolboxException ex) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
            }
        }
    }

    private class LinkageMenuItemActionListener implements ActionListener {
        private TreeBuilder builder;

        public LinkageMenuItemActionListener(TreeBuilder builder) {
            this.builder = builder;
        }

        public void actionPerformed(ActionEvent e) {
            SwingWorker worker = new TreeBuildWorker();
            worker.start();
        }

        // builds the tree in a separate thread and shows progress bar
        private class TreeBuildWorker extends SwingWorker {
            private ProgressMonitor monitor;

            public TreeBuildWorker() {
                monitor = new ProgressMonitor(SOMViewer.this, "Building cluster tree...", "", 0, 100); // Maximum will be set later;
            }

            @Override
            public Object construct() {
                if (builder != null) {
                    builder.setMonitor(monitor);
                }
                try {
                    getMap().buildTree(builder);
                    showPalettePanel();
                    redrawClustering();
                } catch (ClusteringAbortedException ex) {
                    // reset to old menu item
                    SOMViewer.this.linkages.setSelected(oldLinkage, true);
                }
                oldLinkage = SOMViewer.this.linkages.getSelection();
                monitor.close();
                return null;
            }
        }
    }

    // Angela
    private void redrawClustering() {
        // BasicStroke bs = new BasicStroke(12.0f);
        getMap().showClusters(this.clusteringLevel, false);
    }

    private MapPNode getMap() {
        return mapPane.getMap();
    }

    // Angela: creates a menu entry for showing clusters
    private void createClusterMenu() {
        JMenu clusterMenu = new JMenu("Clustering");
        JMenuItem menuItemCPCLustering = new JMenuItem("Component Plane Clustering");
        menuItemCPCLustering.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ComponentPlaneClusteringFrame componentPlaneFrame = new ComponentPlaneClusteringFrame(SOMViewer.this, getMap().getGsom(),
                            state.inputDataObjects.getTemplateVector());
                    desktop.add(componentPlaneFrame);
                    componentPlaneFrame.pack();
                    componentPlaneFrame.setLocation(state.controlElementsWidth, 0);
                    componentPlaneFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    componentPlaneFrame.setVisible(true);
                } catch (SOMToolboxException ex) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error during Component Plane Clustering: " + ex.getMessage());
                }
            }
        });
        clusterMenu.add(menuItemCPCLustering);

        JMenu menuMapClustering = new JMenu("Map Clustering");
        clusterMenu.add(menuMapClustering);
        linkages = new ButtonGroup();

        JRadioButtonMenuItem menuItem;
        menuItem = new JRadioButtonMenuItem("none", true);
        oldLinkage = menuItem.getModel();
        menuItem.addActionListener(new LinkageMenuItemActionListener(null));
        linkages.add(menuItem);
        menuMapClustering.add(menuItem);
        menuItem = new JRadioButtonMenuItem("single linkage");
        menuItem.addActionListener(new LinkageMenuItemActionListener(new SingleLinkageTreeBuilder()));
        linkages.add(menuItem);
        menuMapClustering.add(menuItem);
        menuItem = new JRadioButtonMenuItem("complete linkage");
        menuItem.addActionListener(new LinkageMenuItemActionListener(new CompleteLinkageTreeBuilder()));
        linkages.add(menuItem);
        menuMapClustering.add(menuItem);
        menuItem = new JRadioButtonMenuItem("Ward's linkage (fast)");
        menuItem.addActionListener(new LinkageMenuItemActionListener(new WardsLinkageTreeBuilder()));
        linkages.add(menuItem);
        menuMapClustering.add(menuItem);
        menuItem = new JRadioButtonMenuItem("Ward's linkage (all)");
        menuItem.addActionListener(new LinkageMenuItemActionListener(new WardsLinkageTreeBuilderAll()));
        linkages.add(menuItem);
        menuMapClustering.add(menuItem);
        menuItem = new JRadioButtonMenuItem("Ward's linkage (all, test)");
        menuItem.addActionListener(new LinkageMenuItemActionListener(new WardsLinkageTreeBuilderAll(true)));
        linkages.add(menuItem);
        menuMapClustering.add(menuItem);

        menuItem = new JRadioButtonMenuItem("k-means");
        menuItem.addActionListener(new LinkageMenuItemActionListener(new KMeansTreeBuilder()));
        linkages.add(menuItem);
        menuMapClustering.add(menuItem);

        menuBar.add(clusterMenu);
    }

    private AbstractButton makeToolbarButton(String imageName, String toolTipText, String altText) {
        return setToolbarButtonDetails(new JButton(), imageName, toolTipText, altText, false);
    }

    private AbstractButton makeToolbarToggleButton(String imageName, String toolTipText, String altText, boolean isSelected) {
        return setToolbarButtonDetails(new JToggleButton(), imageName, toolTipText, altText, isSelected);
    }

    private AbstractButton setToolbarButtonDetails(AbstractButton button, String imageName, String toolTipText, String altText, boolean isSelected) {
        URL imageURL = ClassLoader.getSystemResource(RESOURCE_PATH_ICONS + imageName);
        button.setActionCommand(toolTipText);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        button.setSelected(isSelected);

        if (imageURL != null) {
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {
            button.setText(altText);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Image resource for button not found. This should not happen, continuing anyway.");
        }

        return button;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals(CENTER_AND_FIT_MAP)) {
            mapPane.centerAndFitMapToScreen();
        } else if (cmd.equals(RESET_DESKTOP_LAYOUT)) {
            resetDesktopLayout();
        } else if (cmd.equals(SELECT_LINE)) {
            mapPane.setLine();
        } else if (cmd.equals(SELECT_RECTANGLE)) {
            mapPane.setRectangle();
        } else if (cmd.equals(SELECT_CLUSTER)) {
            mapPane.setCluster();
        } else if (cmd.equals(MOVE_INPUT)) {
            // rudi for moving inputs
            mapPane.setInput();
        } else if (cmd.equals(MOVE_LABEL)) {
            // Angela
            mapPane.setLabel();
        } else if (cmd.equals(CREATE_LABEL)) {
            // Angela
            getMap().createLabel();
        } else if (cmd.equals(TOGGLE_DATA)) {
            state.dataVisibilityMode = ((AbstractButton) e.getSource()).isSelected();
            getMap().reInitUnitDetails();
        } else if (cmd.equals(TOGGLE_LABELS)) {
            state.labelVisibilityMode = ((AbstractButton) e.getSource()).isSelected();
            getMap().reInitUnitDetails();
        } else if (cmd.equals(TOGGLE_PIE_CHARTS)) {
            state.classPiechartsVisible = pieChartToggleButton.isSelected();
            classLegendPane.setEnabled(pieChartToggleButton.isSelected());
            mapPane.updateClassSelection(null);
        } else if (cmd.equals(TOGGLE_EXACT_PLACEMENT)) {
            state.exactUnitPlacement = ((AbstractButton) e.getSource()).isSelected();
            getMap().reInitUnitDetails();
        } else if (cmd.equals(TOGGLE_RELOCATE)) {
            state.shiftOverlappingInputs = ((AbstractButton) e.getSource()).isSelected();
            getMap().updatePointLocations();
        } else if (cmd.equals(TOGGLE_LINKAGE)) {
            state.displayInputLinkage = ((AbstractButton) e.getSource()).isSelected();
            state.mapPNode.setLinkageVisibilityMode(state.displayInputLinkage);
        } else if (cmd.equals(SOMVIEWER_3D)) {
            try {
                // load & instantiate state object of 3D
                Class<?> classViewerState3D = Class.forName("at.tuwien.ifs.somtoolbox.apps.viewer3d.CommonSOMViewerStateData3D");
                Constructor<?> constrViewerState3D = classViewerState3D.getConstructor(CommonSOMViewerStateData.class);
                Object viewerState3D = constrViewerState3D.newInstance(state);

                // load and instantiate 3D Viewer itself
                @SuppressWarnings("unchecked")
                Class<JInternalFrame> c = (Class<JInternalFrame>) Class.forName("at.tuwien.ifs.somtoolbox.apps.viewer3d.SOMViewer3D");
                Constructor<JInternalFrame> constr = c.getConstructor(classViewerState3D);
                JInternalFrame somviewer3D = constr.newInstance(new Object[] { viewerState3D });

                desktop.add(somviewer3D);
                somviewer3D.pack();
                somviewer3D.setLocation(state.controlElementsWidth, 0);
                somviewer3D.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } catch (Exception e1) {
                if (e1 instanceof SOMToolboxException) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e1.getMessage());
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                            "SOMViever3D not available. To enable add the required classes to your classpath");
                }
            }

        }
    }

    public static void main(String[] args) {
        // register and parse all options
        JSAPResult config = AbstractOptionFactory.parseResults(args, OPTIONS);
        new SOMViewer(config);
    }

    public void addVisualizationChangeListener(VisualizationChangeListener l) {
        visChangeListeners.add(l);
    }

    public void removeVisualizationChangeListener(VisualizationChangeListener l) {
        visChangeListeners.remove(l);
    }

    private void visualizationChangeFailure() {
        visualizationMenuItemGroup.setSelected(oldSelectedVisualizationMenuItem, true);
    }

    private void visualizationChangeSuccess() {
        oldSelectedVisualizationMenuItem = visualizationMenuItemGroup.getSelection();
        for (VisualizationChangeListener listener : visChangeListeners) {
            listener.visualisationChanged();
        }
    }

    private class VisualizationActionListener implements ActionListener {
        private int visualization;

        private int variant;

        public VisualizationActionListener(int vi, int va) {
            super();
            visualization = vi;
            variant = va;
        }

        public void actionPerformed(ActionEvent e) {
            final SwingWorker worker = new SwingWorker() {
                @Override
                public Object construct() {
                    try {
                        state.currentVariant = variant;
                        boolean success = mapPane.setVisualization(visualization, variant);
                        state.currentVariant = variant;
                        if (success) {
                            visControlPanel.updateVisualisationControl();
                            SOMViewer.this.visualizationChangeSuccess();
                            updatePalettePanel();
                            mapPane.repaint();
                        } else {
                            SOMViewer.this.visualizationChangeFailure();
                        }
                    } catch (SOMToolboxException e) {
                        JOptionPane.showMessageDialog(SOMViewer.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
            };
            worker.start(); // required for SwingWorker 3
        }
    }

    public void update(Observable o, Object arg) {
        pieChartToggleButton.setEnabled(false);
        if (arg instanceof SOMLibClassInformation && arg != null) { // we loaded a new class info
            getMap().updateClassInfo((SOMLibClassInformation) arg);
            classLegendPane.initClassTable();
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridwidth = GridBagConstraints.REMAINDER;
            pieChartToggleButton.setEnabled(true);
            ThematicClassMapVisualizer tcmVis = getMap().getThematicClassMapVisualizer();
            if (tcmVis != null) {
                thematicClassRadioButton.setEnabled(true);
            }

            if (mapPane.getSecondMap() != null) {
                mapPane.getSecondMap().updateClassInfo((SOMLibClassInformation) arg);
                tcmVis = mapPane.getSecondMap().getThematicClassMapVisualizer();
                if (tcmVis != null) {
                    thematicClassRadioButton.setEnabled(true);
                }
            }
        } else {
            state.classPiechartsVisible = false;
            getMap().updateClassInfo((SOMLibClassInformation) arg);
            if (mapPane.getSecondMap() != null) {
                mapPane.getSecondMap().updateClassInfo((SOMLibClassInformation) arg);
            }
            mapPane.updateClassSelection(null);
            pieChartToggleButton.setSelected(false);
            classLegendPane.initNoClassInfo();
            thematicClassRadioButton.setEnabled(false);
        }
    }

    public void updateSOMComparison(boolean haveData) {

        boolean error = false;

        try {
            mapPane.updateSOMComparison();
        } catch (SOMToolboxException e1) {
            error = true;
        }

        if (haveData && !error) {
            shiftsControlPanel.initGUIElements();
            showShiftsMenuItem.setEnabled(true);
            switchMapSubmenu.setEnabled(true);
        } else {
            shiftsControlPanel.initNoShiftsInfo();
            showShiftsMenuItem.setEnabled(false);
            switchMapSubmenu.setEnabled(false);
        }
    }

    public void updatePaletteAfterEditing() {
        try {
            boolean success = getMap().reloadPaletteAfterEditing(state.currentPalette);
            if (success) {
                SOMViewer.this.visualizationChangeSuccess();
            } else {
                SOMViewer.this.visualizationChangeFailure();
            }
            showPalettePanel();
        } catch (SOMToolboxException e) {
            JOptionPane.showMessageDialog(SOMViewer.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class MyJCheckBoxMenuItem extends JCheckBoxMenuItem implements ActionListener {
        private static final long serialVersionUID = 1L;

        private Component component;

        public MyJCheckBoxMenuItem(String name, Component component) {
            super(name);
            this.component = component;
            setSelected(component.isVisible());
            setMnemonic(name.charAt(0));
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            component.setVisible(isSelected());
        }

        @Override
        public Component getComponent() {
            return component;
        }
    }

    private class PaletteCheckboxMenuItemListener implements ActionListener {
        private int palette;

        public PaletteCheckboxMenuItemListener(int palette) {
            this.palette = palette;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                boolean success = getMap().changePalette(palette);
                if (success) {
                    SOMViewer.this.visualizationChangeSuccess();
                } else {
                    SOMViewer.this.visualizationChangeFailure();
                }
                // Angela: moved some code to showPalettePanel (clustering needs panel too)
                showPalettePanel();

            } catch (SOMToolboxException ex) {
                JOptionPane.showMessageDialog(SOMViewer.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initDocViewer(AbstractSelectionPanel sp) {
        /** ** DocViewPanel *** */
        DocViewPanel docviewer = new DocViewPanel();
        docviewer.setDocumentPath(CommonSOMViewerStateData.fileNamePrefix);
        docviewer.setDocumentSuffix(CommonSOMViewerStateData.fileNameSuffix);
        sp.setItemListener(docviewer);

        docViewerFrame = new JFrame("DocViewer") {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(450, 750);
            }
        };
        // This does not work in Java 1.4
        // FIXME: find another solution in 1.4
        // docViewerFrame.setAlwaysOnTop(true);

        docViewerFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        docViewerFrame.getContentPane().add(docviewer, BorderLayout.CENTER);
        docViewerFrame.pack();
        docViewerFrame.setVisible(true);
    }

    /**
     * handles the window closing to dispose of a docviewer frame, if present, and not to do EXIT on close, but dispose.<br>
     * If running standalone, the JVM will exit automatically after disposing the last frame, but if called from another application, this will only
     * dispose this window, not exit the JVM.
     */
    private void initWindowClosing() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // DISPOSE_ON_CLOSE); //EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            /**
             * maybe there are some other windows we want to close too?
             */
            @Override
            public void windowClosing(WindowEvent e) {
                if (!documentMode) {
                    System.exit(0);
                }

                System.out.println("closing");
                if (docViewerFrame != null) {
                    docViewerFrame.setVisible(false);
                    docViewerFrame.dispose();
                }
                setVisible(false);
                dispose();
            }
        });
    }

    public Color[] getClassLegendColors() {
        return classLegendPane.getColors();
    }

    // Angela: shows or hides the palette panel
    private void showPalettePanel() {
        if (mapPane.getCurrentVisualization() != null || state.colorClusters) {
            if (mapPane.getCurrentVisualization() instanceof AbstractMatrixVisualizer) {
                AbstractMatrixVisualizer vis = (AbstractMatrixVisualizer) mapPane.getCurrentVisualization();
                palettePanel.setPalette(state.palettes[state.currentPalette].getColors(), vis.getMinimumMatrixValue(), vis.getMaximumMatrixValue());
            } else {
                palettePanel.setPalette(state.palettes[state.currentPalette].getColors());
            }
        } else {
            palettePanel.setPalette(null);
        }
        mapPane.repaint();
    }

    private void updatePalettePanel() {
        BackgroundImageVisualizer curVis = mapPane.getCurrentVisualization();
        if (curVis != null) {
            if (curVis instanceof AbstractMatrixVisualizer) {
                AbstractMatrixVisualizer matrixVis = (AbstractMatrixVisualizer) curVis;
                if (matrixVis.getPalette() != null) {
                    reversePaletteMenuItem.setSelected(matrixVis.isReversed());
                    palettePanel.setPalette(matrixVis.getPalette(), matrixVis.getMinimumMatrixValue(), matrixVis.getMaximumMatrixValue());
                }
            }
        } else {
            palettePanel.setPalette(null);
        }
    }

    public String getUnitDescriptionFileName() {
        return unitDescriptionFileName;
    }

    public String getWeightVectorFileName() {
        return weightVectorFileName;
    }

    public String getMapDescriptionFileName() {
        return mapDescriptionFileName;
    }

    public CommonSOMViewerStateData getSOMViewerState() {
        return state;
    }

}

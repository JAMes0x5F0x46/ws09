package at.tuwien.ifs.somtoolbox.util.mnemonic;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;

/**
 * @author Rudolf Mayer
 * @version $Id: MnemonicSOMGenerator.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MnemonicSOMGenerator extends JFrame implements ChangeListener {
    private static final short DEFAULT_ROWS = 8;

    private static final short DEFAULT_COLS = (short) 15;

    private static final long serialVersionUID = 1L;

    private static final String APP_ICON = "resources/icons/somviewer_logo-24.png";

    private BorderLayout mainBorderLayout = new BorderLayout();

    private BorderLayout controlsPanelBorderLayout = new BorderLayout();

    private GridBagLayout labelpanelGridBagLayout = new GridBagLayout();

    private JButton buttonExit;

    private JButton buttonLoad;

    private JButton buttonSave;

    private JFileChooser fileChooser = new JFileChooser();

    private JLabel labelActiveNodesTitle;

    private JLabel labelActiveNodesValue;

    private JLabel labelStatus;

    private JPanel controlsPanel = new JPanel();

    private JPanel buttonPanel = new JPanel();

    private JPanel labelPanel = new JPanel();

    private JSpinner nodeSlider = new JSpinner();

    private MapPanel mapPanel;

    private JButton buttonSaveImage = new JButton();

    private int enabledNodes = 0;

    public MnemonicSOMGenerator(short cols, short rows) {
        this(null, cols, rows);
    }

    public MnemonicSOMGenerator(String image, short cols, short rows) {
        boolean[][] toDraw = new boolean[cols][rows];
        for (int i = 0; i < toDraw.length; i++) {
            for (int j = 0; j < toDraw[0].length; j++) {
                toDraw[i][j] = true;
            }
        }
        mapPanel = new MapPanel(toDraw, image);

        initFrame();
    }

    private void initFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        setIconImage(Toolkit.getDefaultToolkit().getImage(SOMViewer.class.getResource(APP_ICON)));

        setResizable(false);
        jbInit();
        registerListeners();
        updateNodeCount();
        pack();
    }

    public MnemonicSOMGenerator(String image, int totalNodes) {
        if (image != null) {
            mapPanel = new MapPanel(totalNodes, image);
        }

        initFrame();
    }

    public static void main(String[] args) {
        if (args.length > 2) {
            new MnemonicSOMGenerator(args[0], Short.parseShort(args[1]), Short.parseShort(args[2])).setVisible(true);
        } else if (args.length > 1) {
            new MnemonicSOMGenerator(args[0], Integer.parseInt(args[1])).setVisible(true);
        } else if (args.length > 0) {
            new MnemonicSOMGenerator(args[0], DEFAULT_COLS, DEFAULT_ROWS).setVisible(true);
        } else {
            new MnemonicSOMGenerator(DEFAULT_COLS, DEFAULT_ROWS).setVisible(true);
        }
    }

    private void jbInit() {
        this.getContentPane().setLayout(mainBorderLayout);
        this.setTitle("Mnemonic Map Creator");

        controlsPanel.setLayout(controlsPanelBorderLayout);
        labelPanel.setLayout(labelpanelGridBagLayout);

        buttonSaveImage = new JButton("saveImage");
        buttonSaveImage.setMnemonic('i');
        buttonSaveImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage(e);
            }
        });

        labelActiveNodesTitle = new JLabel("active Nodes: ");
        labelActiveNodesValue = new JLabel("...");

        labelStatus = new JLabel("<<status>>");
        labelStatus.setAlignmentX((float) 0.5);
        labelStatus.setHorizontalAlignment(SwingConstants.CENTER);

        buttonExit = new JButton("exit");
        buttonExit.setMnemonic('E');

        buttonLoad = new JButton("load Map");
        buttonLoad.setMnemonic('L');

        buttonSave = new JButton("save Map");
        buttonSave.setMnemonic('S');

        nodeSlider.setModel(new SpinnerNumberModel(mapPanel.getNodeCount(), 1, 10000, 1));
        nodeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int newNodeCount = ((Integer) nodeSlider.getValue()).intValue();
                mapPanel.createNodes(newNodeCount);
                mapPanel.repaint();
            }
        });

        this.getContentPane().add(mapPanel, BorderLayout.CENTER);

        buttonPanel.add(buttonSave, null);
        buttonPanel.add(buttonSaveImage, null);
        buttonPanel.add(buttonLoad, null);
        buttonPanel.add(buttonExit, null);
        buttonPanel.add(nodeSlider);

        controlsPanel.add(buttonPanel, BorderLayout.SOUTH);

        labelPanel.add(labelActiveNodesTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        labelPanel.add(labelActiveNodesValue, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        labelPanel.add(labelStatus, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0,
                0, 0), 0, 0));
        controlsPanel.add(labelPanel, BorderLayout.CENTER);
        this.getContentPane().add(controlsPanel, BorderLayout.SOUTH);
    }

    private void registerListeners() {
        buttonSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveMap();
            }
        });
        buttonLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadSOM();
            }
        });
        buttonExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitApplication();
            }
        });
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
        mapPanel.addChangeListener(this);
    }

    public void exitApplication() {
        System.exit(0);
    }

    /**
     * 
     *
     */
    public void saveMap() {
        int vectorDimension = 10;
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                labelStatus.setText("saving map....");

                // write the unit description file
                BufferedWriter fileOutUnit = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile().getPath()
                        + SOMLibFormatInputReader.unitFileNameSuffix));
                fileOutUnit.write("$TYPE rect\n");
                fileOutUnit.write("$XDIM " + mapPanel.getToDraw().length + "\n");
                fileOutUnit.write("$YDIM " + mapPanel.getToDraw()[0].length + "\n");

                for (int col = 0; col < mapPanel.getToDraw().length; col++) {
                    for (int row = 0; row < mapPanel.getToDraw()[0].length; row++) {
                        if (mapPanel.getToDraw()[col][row]) {
                            fileOutUnit.write("$POS_X " + col + "\n");
                            fileOutUnit.write("$POS_Y " + row + "\n");
                            fileOutUnit.write("$UNIT_ID " + fileChooser.getSelectedFile().getPath() + "_(" + col + "/" + row + ")\n");
                        }
                    }
                }
                fileOutUnit.flush();
                fileOutUnit.close();

                // write the map description file
                BufferedWriter fileOutMap = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile().getPath() + ".map"));
                fileOutMap.write("$TYPE som\n");
                fileOutMap.write("$XDIM " + mapPanel.getToDraw().length + "\n");
                fileOutMap.write("$YDIM " + mapPanel.getToDraw()[0].length + "\n");
                fileOutMap.write("$VEC_DIM " + vectorDimension + "\n");

                fileOutMap.write("$METRIC at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric\n");
                fileOutMap.write("$LAYER_REVISION $Revision: 2874 $");

                fileOutMap.flush();
                fileOutMap.close();

                labelStatus.setText("Map saved successfully to '" + fileChooser.getSelectedFile().getPath() + "'!");

            } catch (FileNotFoundException e) {
                System.out.println(e);
                e.getStackTrace();
            } catch (IOException e) {
                System.out.println(e);
                e.getStackTrace();
            }
        }
    }

    public void loadSOM() {
        // if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        // labelStatus.setText("loading map....");
        // TODO: implement
    }

    public void stateChanged(ChangeEvent e) {
        updateNodeCount();
    }

    private void updateNodeCount() {
        System.out.println("updating node count");
        enabledNodes = 0;
        for (int i = 0; i < mapPanel.getToDraw().length; i++) {
            for (int j = 0; j < mapPanel.getToDraw()[0].length; j++) {
                if (mapPanel.getToDraw()[i][j]) {
                    enabledNodes++;
                }
            }
        }
        labelActiveNodesValue.setText(String.valueOf(enabledNodes));
    }

    public void saveImage(ActionEvent e) {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            labelStatus.setText("saved image to: " + mapPanel.saveScreenToImage(fileChooser.getSelectedFile()));
        }
    }
}
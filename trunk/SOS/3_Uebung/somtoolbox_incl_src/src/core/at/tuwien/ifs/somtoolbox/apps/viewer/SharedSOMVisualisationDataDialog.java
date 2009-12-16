package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.MySOMVisualisationDataFileFilter;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.util.CentredDialog;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * A dialog to load and unload shared input data used by various visualisations, such as the {@link TemplateVector}, {@link InputData}, etc.
 * 
 * @author Rudolf Mayer
 * @version $Id: SharedSOMVisualisationDataDialog.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SharedSOMVisualisationDataDialog extends CentredDialog {
    private static final long serialVersionUID = 1L;

    private CommonSOMViewerStateData state;

    public SharedSOMVisualisationDataDialog(JFrame owner, CommonSOMViewerStateData state) throws HeadlessException {
        super(owner, "Loaded Input data", true);
        this.state = state;
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 5;
        c.ipadx = 2;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridy = 0;
        c.gridx = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.CENTER;

        Font headerFont = new Font("", Font.BOLD, 12);

        JLabel headerDataType = new JLabel("Data type");
        headerDataType.setFont(headerFont);
        getContentPane().add(headerDataType, c);

        JLabel headerFileName = new JLabel("File name");
        headerFileName.setFont(headerFont);
        getContentPane().add(headerFileName, c);

        JLabel headerActions = new JLabel("Actions");
        headerActions.setFont(headerFont);
        c.gridwidth = 2;
        getContentPane().add(headerActions, c);
        c.gridwidth = 1;

        c.anchor = GridBagConstraints.WEST;
        SOMVisualisationData[] files = state.inputDataObjects.getObjects();
        for (int i = 0; i < files.length; i++) {
            c.gridy = c.gridy + 1;
            
            getContentPane().add(new JLabel(files[i].getType()), c);

            JLabel labelFileName;
            if (files[i].getFileName() != null) {
                labelFileName = new JLabel(files[i].getFileName());
            } else {
                labelFileName = new JLabel("-none-");
            }
            getContentPane().add(labelFileName, c);

            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(new ClearButtonActionListener(files[i], labelFileName));
            getContentPane().add(clearButton, c);

            JButton loadButton = new JButton("Load");
            loadButton.addActionListener(new LoadButtonActionListener(files[i], labelFileName));
            getContentPane().add(loadButton, c);
        }

        // show & edit prefix value
        c.gridy = c.gridy + 1;
        getContentPane().add(new JLabel("Data items path"), c);
        JLabel labelPrefix = new JLabel(CommonSOMViewerStateData.fileNamePrefix);
        getContentPane().add(labelPrefix, c);

        JButton clearPrefixButton = new JButton("Clear");
        clearPrefixButton.addActionListener(new PrefixClearActionListener(labelPrefix));
        getContentPane().add(clearPrefixButton, c);

        JButton browsePrefixButton = new JButton("Browse");
        getContentPane().add(browsePrefixButton, c);
        browsePrefixButton.addActionListener(new PrefixBrowseActionListener(labelPrefix));

        // show and edit path to second SOM
        c.gridy = c.gridy + 1;
        getContentPane().add(new JLabel("Second SOM"), c);
        JLabel labelSecSOM = new JLabel(state.secondSOMName);
        getContentPane().add(labelSecSOM, c);

        JButton clearSecSOMButton = new JButton("Clear");
        clearSecSOMButton.addActionListener(new SecondSOMClearActionListener(labelSecSOM));
        getContentPane().add(clearSecSOMButton, c);

        JButton browseSecSOMButton = new JButton("Load");
        getContentPane().add(browseSecSOMButton, c);
        browseSecSOMButton.addActionListener(new SecSOMBrowseActionListener(labelSecSOM));

        center();
    }

    private void center() {
        pack();
    }

    private class ClearButtonActionListener implements ActionListener {
        private SOMVisualisationData inputObject;

        private JLabel labelFileName;

        public ClearButtonActionListener(SOMVisualisationData inputObject, JLabel labelFileName) {
            super();
            this.inputObject = inputObject;
            this.labelFileName = labelFileName;
        }

        public void actionPerformed(ActionEvent e) {
            this.inputObject.setData(null);
            this.inputObject.setFileName(null);
            labelFileName.setText("-none-");
            pack();
        }

    }

    private class LoadButtonActionListener implements ActionListener {
        private SOMVisualisationData inputObject;

        private JLabel labelFileName;

        public LoadButtonActionListener(SOMVisualisationData inputObject, JLabel labelFileName) {
            super();
            this.inputObject = inputObject;
            this.labelFileName = labelFileName;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = state.getFileChooser();
            if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                fileChooser = new JFileChooser(fileChooser.getSelectedFile().getPath());
            }
            fileChooser.setFileFilter(new MySOMVisualisationDataFileFilter(inputObject));
            fileChooser.setName(inputObject.getType());
            int returnVal = fileChooser.showDialog(SharedSOMVisualisationDataDialog.this, "Open " + inputObject.getType());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    inputObject.readFromFile(fileChooser.getSelectedFile().getAbsolutePath());
                    labelFileName.setText(inputObject.getFileName());
                    center();
                } catch (SOMToolboxException ex) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ex.getMessage());
                }
            }
        }

    }

    private class PrefixBrowseActionListener implements ActionListener {
        private JLabel label;

        public PrefixBrowseActionListener(JLabel label) {
            this.label = label;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            if (CommonSOMViewerStateData.fileNamePrefix != null && !CommonSOMViewerStateData.fileNamePrefix.equals("")) {
                fileChooser.setCurrentDirectory(new File(CommonSOMViewerStateData.fileNamePrefix));
            } else {
                fileChooser.setCurrentDirectory(state.getFileChooser().getCurrentDirectory());
            }
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                fileChooser.setSelectedFile(null);
            }
            fileChooser.setName("Choose path");
            int returnVal = fileChooser.showDialog(SharedSOMVisualisationDataDialog.this, "Choose path");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                CommonSOMViewerStateData.fileNamePrefix = fileChooser.getSelectedFile().getAbsolutePath() + java.io.File.separator;
                label.setText(CommonSOMViewerStateData.fileNamePrefix);
                center();
            }
        }
    }

    private class PrefixClearActionListener implements ActionListener {
        private JLabel label;

        public PrefixClearActionListener(JLabel label) {
            this.label = label;
        }

        public void actionPerformed(ActionEvent e) {
            CommonSOMViewerStateData.fileNamePrefix = "";
            label.setText(CommonSOMViewerStateData.fileNamePrefix);
            center();
        }
    }

    private class SecSOMBrowseActionListener implements ActionListener {
        private JLabel label;

        public SecSOMBrowseActionListener(JLabel label) {
            this.label = label;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            if (state.secondSOMName != null && !state.secondSOMName.equals("")) {
                fileChooser.setCurrentDirectory(new File(state.secondSOMName));
            } else {
                fileChooser.setCurrentDirectory(state.getFileChooser().getCurrentDirectory());
            }
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new FileUtils.SOMDescriptionFileFilter());

            if (fileChooser.getSelectedFile() != null) { // reusing the dialog
                fileChooser = new JFileChooser(fileChooser.getSelectedFile().getPath());
            }
            fileChooser.setName("Choose second SOM file");
            int returnVal = fileChooser.showDialog(SharedSOMVisualisationDataDialog.this, "Choose second SOM description file");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                state.secondSOMName = fileChooser.getSelectedFile().getAbsolutePath(); // + java.io.File.separator;
                label.setText(state.secondSOMName);
                center();

                // initialises shift arrows and that stuff
                ((SOMViewer) state.parentFrame).updateSOMComparison(true);
            }
        }
    }

    private class SecondSOMClearActionListener implements ActionListener {
        private JLabel label;

        public SecondSOMClearActionListener(JLabel label) {
            this.label = label;
        }

        public void actionPerformed(ActionEvent e) {
            state.secondSOMName = "";
            label.setText(state.secondSOMName);
            center();

            // deletes arrows and stuff
            // state.mapPNode.updateCompareSOMs();
            ((SOMViewer) state.parentFrame).updateSOMComparison(false);
        }
    }
}

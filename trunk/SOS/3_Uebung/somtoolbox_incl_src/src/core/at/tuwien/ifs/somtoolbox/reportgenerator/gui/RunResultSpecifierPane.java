package at.tuwien.ifs.somtoolbox.reportgenerator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: RunResultSpecifierPane.java 2874 2009-12-11 16:03:27Z frank $
 */

public class RunResultSpecifierPane extends JPanel implements ActionListener {

    public static final long serialVersionUID = 1l;

    private int id = -1;

    private ReportGenWindow controller;

    private JLabel headerLabel = null;

    private JLabel weightLabel = null;

    private JLabel mapLabel = null;

    private JLabel unitLabel = null;

    private JLabel propertyLabel = null;

    private JLabel dwLabel = null;

    private JTextField weightText = null;

    private JTextField mapText = null;

    private JTextField unitText = null;

    private JTextField propertyText = null;

    private JTextField dwText = null;

    private JButton weightBrowseButton = null;

    private JButton mapBrowseButton = null;

    private JButton unitBrowseButton = null;

    private JButton propertyBrowseButton = null;

    private JButton dwBrowseButton = null;

    private JPanel headerPane = null;

    private JPanel weightPane = null;

    private JPanel mapPane = null;

    private JPanel unitPane = null;

    private JPanel dwPane = null;

    private JPanel propertyPane = null;

    private JButton removeButton = null; // @jve:decl-index=0:visual-constraint="510,49"

    private JFileChooser fileChooser;

    public RunResultSpecifierPane(int id, ReportGenWindow controller) {
        this.id = id;
        this.controller = controller;

        // initialize the UI component
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        this.add(getHeaderPane());
        this.add(getMapPane());
        this.add(getPropertyPane());
        this.add(getWeightPane());
        this.add(getUnitPane());
        this.add(getDWPane());
    }

    public String getMapFilePath() {
        return this.mapText.getText();
    }

    public String getUnitFilePath() {
        return this.unitText.getText();
    }

    public String getWeightFilePath() {
        return this.weightText.getText();
    }

    public String getPropertyFilePath() {
        return this.propertyText.getText();
    }

    public String getDwFilePath() {
        return this.dwText.getText();
    }

    /**
     * updates the id of the object to the given value. this may be necessary if another RunResultSpecifier has been removed from a list.
     * 
     * @param newId the new id of this object
     */
    public void updateId(int newId) {
        this.id = newId;
        this.headerLabel.setText("Results of run " + (this.id + 1) + ":");
    }

    /*  PRIVATE UI PART */

    /**
     * creates and returns the header Panel (the one with the header text and the remove Button
     * 
     * @return
     */
    private JPanel getHeaderPane() {
        if (this.headerPane == null) {
            headerPane = new JPanel();
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            headerPane.setLayout(flowLayout);

            headerLabel = new JLabel();
            headerLabel.setText("Results of run " + (this.id + 1) + ":");
            headerPane.add(headerLabel);
            headerPane.add(getRemoveButton());
        }
        return this.headerPane;
    }

    /**
     * creates and returns the Panel that contains the Values for specifying the location of the .wgt file
     * 
     * @return
     */
    private JPanel getWeightPane() {

        if (weightPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            weightPane = new JPanel();
            weightPane.setPreferredSize(new Dimension(397, 25));
            weightPane.setLayout(flowLayout);

            weightLabel = new JLabel();
            weightLabel.setText("weight vector file (*.wgt[.gz]):");
            weightPane.add(weightLabel, null);

            weightText = new JTextField();
            weightText.setColumns(10);
            weightText.setText(this.controller.getWeightPath());
            weightPane.add(weightText);

            weightBrowseButton = new JButton();
            weightBrowseButton.setText("Browse");
            weightBrowseButton.setPreferredSize(new Dimension(80, 18));
            weightBrowseButton.addActionListener(this);
            weightPane.add(weightBrowseButton);
        }
        return weightPane;
    }

    /**
     * creates and returns the Panel that contains the Values for specifying the location of the .map file
     * 
     * @return
     */
    private JPanel getMapPane() {

        if (mapPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            mapPane = new JPanel();
            mapPane.setSize(new Dimension(416, 18));
            mapPane.setPreferredSize(new Dimension(416, 25));
            mapPane.setLayout(flowLayout);

            mapLabel = new JLabel();
            mapLabel.setText("map description file (*.map[.gz]):");
            mapPane.add(mapLabel, null);

            mapText = new JTextField();
            mapText.setColumns(10);
            mapText.setText(this.controller.getMapPath());
            mapPane.add(mapText);

            mapBrowseButton = new JButton();
            mapBrowseButton.setText("Browse");
            mapBrowseButton.setPreferredSize(new Dimension(80, 18));
            mapBrowseButton.addActionListener(this);
            mapPane.add(mapBrowseButton);
        }
        return mapPane;
    }

    /**
     * creates and returns the Panel that contains the Values for specifying the location of the .unit file
     * 
     * @return
     */
    private JPanel getUnitPane() {

        if (unitPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            unitPane = new JPanel();
            unitPane.setPreferredSize(new Dimension(380, 25));
            unitPane.setLayout(flowLayout);

            unitLabel = new JLabel();
            unitLabel.setText("unit vector file (*.unit[.gz]):");
            unitPane.add(unitLabel, null);

            unitText = new JTextField();
            unitText.setColumns(10);
            unitText.setText(this.controller.getUnitPath());
            unitPane.add(unitText);

            unitBrowseButton = new JButton();
            unitBrowseButton.setText("Browse");
            unitBrowseButton.setPreferredSize(new Dimension(80, 18));
            unitBrowseButton.addActionListener(this);
            unitPane.add(unitBrowseButton);
        }
        return unitPane;
    }

    /**
     * creates and returns the Panel that contains the Values for specifying the location of the .props file
     * 
     * @return
     */
    private JPanel getPropertyPane() {

        if (propertyPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            propertyPane = new JPanel();
            propertyPane.setPreferredSize(new Dimension(380, 25));
            propertyPane.setLayout(flowLayout);

            propertyLabel = new JLabel();
            propertyLabel.setText("property file (*.prop):");
            propertyPane.add(propertyLabel);

            propertyText = new JTextField();
            propertyText.setColumns(10);
            propertyText.setText(this.controller.getPropertiesPath());
            propertyPane.add(propertyText);

            propertyBrowseButton = new JButton();
            propertyBrowseButton.setText("Browse");
            propertyBrowseButton.setPreferredSize(new Dimension(80, 18));
            propertyBrowseButton.addActionListener(this);
            propertyPane.add(propertyBrowseButton);
        }
        return propertyPane;
    }

    /**
     * creates and returns the Panel that contains the Values for specifying the location of the .dw file
     * 
     * @return
     */
    private JPanel getDWPane() {

        if (dwPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            dwPane = new JPanel();
            dwPane.setPreferredSize(new Dimension(380, 25));
            dwPane.setLayout(flowLayout);

            dwLabel = new JLabel();
            dwLabel.setText("data winner file (*.dwm[.gz]):");
            dwPane.add(dwLabel);

            dwText = new JTextField();
            dwText.setColumns(10);
            dwText.setText(this.controller.getDataWinnerMappingPath());
            dwPane.add(dwText);

            dwBrowseButton = new JButton();
            dwBrowseButton.setText("Browse");
            dwBrowseButton.setPreferredSize(new Dimension(80, 18));
            dwBrowseButton.addActionListener(this);
            dwPane.add(dwBrowseButton);
        }
        return dwPane;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getRemoveButton() {
        if (removeButton == null) {
            removeButton = new JButton();
            removeButton.setText("remove");
            removeButton.setPreferredSize(new Dimension(80, 18));
            removeButton.addActionListener(this);
        }
        return removeButton;
    }

    private JFileChooser getFileChooser(String path) {
        fileChooser = new JFileChooser(path);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return fileChooser;
    }

    /**
     * reacts to ACtions, especially if clicked onto the remove button, the controller element is informed that this testrun shall be removed.
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == this.removeButton) {
            this.controller.removeTestrun(this.id);
        } else if (e.getSource() == this.mapBrowseButton) {
            // show the dialog to select the output file
            if (getFileChooser(this.mapText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.mapText.setText(this.fileChooser.getSelectedFile().getPath());
            }
        } else if (e.getSource() == this.weightBrowseButton) {
            // show the dialog to select the output file
            if (getFileChooser(this.weightText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.weightText.setText(this.fileChooser.getSelectedFile().getPath());
            }
        } else if (e.getSource() == this.unitBrowseButton) {
            // show the dialog to select the output file
            if (getFileChooser(this.unitText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.unitText.setText(this.fileChooser.getSelectedFile().getPath());
            }
        } else if (e.getSource() == this.propertyBrowseButton) {
            // show the dialog to select the output file
            if (getFileChooser(this.propertyText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.propertyText.setText(this.fileChooser.getSelectedFile().getPath());
            }
        } else if (e.getSource() == this.dwBrowseButton) {
            // show the dialog to select the output file
            if (getFileChooser(this.dwText.getText()).showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.dwText.setText(this.fileChooser.getSelectedFile().getPath());
            }
        }
    }
}

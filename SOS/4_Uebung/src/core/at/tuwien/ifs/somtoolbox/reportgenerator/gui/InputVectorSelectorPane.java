package at.tuwien.ifs.somtoolbox.reportgenerator.gui;

import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import at.tuwien.ifs.somtoolbox.data.InputDatum;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: InputVectorSelectorPane.java 2874 2009-12-11 16:03:27Z frank $
 */

public class InputVectorSelectorPane extends JPanel {

    static final long serialVersionUID = 1701;

    private int id = -1;

    private String name = "";

    private JCheckBox jCheckBox = null;

    private JLabel jLabel = null;

    public InputVectorSelectorPane(int id, String name, InputDatum inputVector) {

        super();

        this.name = name;
        this.id = id;
        // this.inputVector = inputVector;

        if (this.name.length() == 0) {
            this.name = "" + this.id;
        }

        // initialize the UI component
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        jLabel = new JLabel();
        jLabel.setText(this.name + " (" + getInputLabelDetails(inputVector) + ")");
        this.setLayout(flowLayout);
        this.add(getJCheckBox());
        this.add(jLabel);

    }

    private String getInputLabelDetails(InputDatum inputVector) {
        String values = "";
        for (int i = 0; i < inputVector.getDim(); i++) {
            if (i > 0)
                values += "; ";
            values += String.format("%.3f", inputVector.getVector().get(i));
        }
        return values;
    }

    /**
     * selects this Entry
     */
    public void select() {
        this.getJCheckBox().setSelected(true);
        this.updateUI();
    }

    /**
     * unselects this Entry
     */
    public void unselect() {
        this.getJCheckBox().setSelected(false);
        this.updateUI();
    }

    /**
     * This method initializes jCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBox() {
        if (jCheckBox == null) {
            jCheckBox = new JCheckBox();
        }
        return jCheckBox;
    }

    /**
     * returns whether this input vector is selected or not
     * 
     * @return true if selected, false if not
     */
    public boolean isSelected() {
        return this.jCheckBox.isSelected();
    }

    /**
     * returns the id of this vector (that is its index in all lists and the input file
     * 
     * @return the index of the vector
     */
    public int getId() {
        return this.id;
    }
}

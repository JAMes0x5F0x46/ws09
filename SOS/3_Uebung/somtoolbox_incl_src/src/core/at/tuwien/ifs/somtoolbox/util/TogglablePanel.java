package at.tuwien.ifs.somtoolbox.util;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A Panel that can toggle the visibility of its components.
 * 
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class TogglablePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String TEXT_CLOSE = "<<<<";

    public static final String TEXT_OPEN = ">>>>";

    protected boolean expandedStatus = true;

    public TogglablePanel(LayoutManager layout) {
        super(layout);
    }

    protected void toggleState(JLabel trigger) {
        expandedStatus = !expandedStatus;
        if (expandedStatus) {
            trigger.setText(trigger.getText().replace(TEXT_OPEN, TEXT_CLOSE));
        } else {
            trigger.setText(trigger.getText().replace(TEXT_CLOSE, TEXT_OPEN));
        }
        for (Component c : getComponents()) {
            if (c != trigger) {
                c.setVisible(expandedStatus);
            }
        }
    }

}
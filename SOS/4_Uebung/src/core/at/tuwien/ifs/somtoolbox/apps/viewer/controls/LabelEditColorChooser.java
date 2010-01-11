package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Color;
import java.awt.Window;

import javax.swing.event.ChangeEvent;

import edu.umd.cs.piccolo.nodes.PText;

/**
 * The color chooser panel used to edit colors of labels.
 * 
 * @author Angela Roiger
 * @version $Id: LabelEditColorChooser.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LabelEditColorChooser extends ColorChooser {
    private static final long serialVersionUID = 1L;

    PText text = null;

    /**
     * Creates and shows a new color chooser window.
     * 
     * @param t the PText of which the color will be changed.
     */
    public LabelEditColorChooser(Window parent, PText t) {
        super(parent, (Color) t.getTextPaint(), "Changing color for label '" + t.getText() + "'");
        this.text = t;
        setVisible(true);
    }

    public void stateChanged(ChangeEvent e) {
        text.setTextPaint(cc.getColor());
    }

}

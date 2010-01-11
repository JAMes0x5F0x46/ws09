package at.tuwien.ifs.somtoolbox.util;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.plaf.ToolTipUI;

/**
 * This class implements tooltips that can span over multiple lines. <br>
 * <br>
 * As for the super class {@link JToolTip}, the rendering is done in via a sub-class of {@link ToolTipUI}, also an own subclass has to be created
 * for this tooltip - {@link MultiLineToolTipUI}.<br>
 * Also, any class derived from {@link JComponent} automatically supports {@link JToolTip}. However, since we want a component to support our
 * multi-line tooltip, we have to also subclass all components that should support the new tooltip. In the SOMtoolbox,
 * {@link JMultiLineRadioButtonMenuItem} is currently the only class doing this.<br>
 * <br>
 * The original code was found at code found at <a href="http://www.codeguru.com/java/articles/122.shtml" target="_blank">Java CodeGuru</a> and is
 * written by Zafir Anjum.
 * 
 * @author Michael Dittenbach
 * @version $Id: JMultiLineToolTip.java 2874 2009-12-11 16:03:27Z frank $
 */
public class JMultiLineToolTip extends JToolTip {
    private static final long serialVersionUID = 1L;

    private static final String uiClassID = "ToolTipUI";

    String tipText;

    JComponent component;

    public JMultiLineToolTip() {
        updateUI();
    }

    public void updateUI() {
        setUI(MultiLineToolTipUI.createUI(this));
    }

    public void setColumns(int columns) {
        this.columns = columns;
        this.fixedwidth = 0;
    }

    public int getColumns() {
        return columns;
    }

    public void setFixedWidth(int width) {
        this.fixedwidth = width;
        this.columns = 0;
    }

    public int getFixedWidth() {
        return fixedwidth;
    }

    protected int columns = 0;

    protected int fixedwidth = 0;

}

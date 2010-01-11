package at.tuwien.ifs.somtoolbox.util;

import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolTip;

/**
 * This class implements a radio button menu item that will use {@link JMultiLineToolTip} for displaying multi-line tooltips. <br>
 * <br>
 * The original code was found at code found at <a href="http://www.codeguru.com/java/articles/122.shtml" target="_blank">Java CodeGuru</a> and is
 * written by Zafir Anjum.
 * 
 * @author Michael Dittenbach
 * @version $Id: JMultiLineRadioButtonMenuItem.java 2874 2009-12-11 16:03:27Z frank $
 */
public class JMultiLineRadioButtonMenuItem extends JRadioButtonMenuItem {

    private static final long serialVersionUID = 1L;

    public JMultiLineRadioButtonMenuItem(String text) {
        super(text);
    }

    public JToolTip createToolTip() {
        return new JMultiLineToolTip();
    }
}

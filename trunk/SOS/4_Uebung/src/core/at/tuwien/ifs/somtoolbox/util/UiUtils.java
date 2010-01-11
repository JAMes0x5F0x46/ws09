package at.tuwien.ifs.somtoolbox.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * This class provides utility methods for User-Interfaces.
 * 
 * @author Rudolf Mayer
 * @version $Id: UiUtils.java 2874 2009-12-11 16:03:27Z frank $
 */
public class UiUtils {

    /**
     * Places the given component in the middle of the screen.<br>
     * Actually intended for {@link JFrame} and {@link JDialog}, but {@link java.awt.Component} is superclass of both.
     */
    public static void centerWindow(Component window) {
        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = window.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        window.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    public static void setSOMToolboxLookAndFeel() {
        try {
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticLookAndFeel");
        } catch (Exception e) {
            try {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Could not find JGoodies Look & Feel - defaulting to cross-platform Look & Feel.");
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e2) {
            }
        }
    }

}

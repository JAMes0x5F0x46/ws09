package at.tuwien.ifs.somtoolbox.util;

import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Window;
import java.util.logging.Logger;

import javax.swing.JDialog;

/**
 * This class represents a JDialog that will always be centred on top of the owning JFrame.
 * 
 * @author Rudolf Mayer
 * @version $Id: CentredDialog.java 2874 2009-12-11 16:03:27Z frank $
 */
public class CentredDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public CentredDialog(Window owner, String title, boolean modal) {
        super(owner, title);
        setModal(modal);
    }

    private void centre() {
        try {
            // Get the owning window size
            Dimension ownerSize = getOwner().getSize();

            // Calculate the frame location
            int x = (ownerSize.width - getWidth()) / 2 + getOwner().getLocationOnScreen().x;
            int y = (ownerSize.height - getHeight()) / 2 + getOwner().getLocationOnScreen().y;

            // Set the new frame location
            setLocation(x, y);
        } catch (IllegalComponentStateException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Coudl not centre dialog: " + e.getMessage());
        }
    }

    public void setSize(Dimension size) {
        setSize(size.width, size.height);
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
        centre();
    }

    public void pack() {
        super.pack();
        centre();
    }

}

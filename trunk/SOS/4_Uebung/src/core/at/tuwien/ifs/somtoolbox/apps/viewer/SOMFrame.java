package at.tuwien.ifs.somtoolbox.apps.viewer;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * The frame holding the {@link SOMPane}.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMFrame.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMFrame extends JInternalFrame implements InternalFrameListener {
    private static final long serialVersionUID = 1L;

    private CommonSOMViewerStateData state;

    public SOMFrame(CommonSOMViewerStateData state) {
        super("SOM", true, true, true, true);
        setName("SOM Frame");
        this.state = state;
        addInternalFrameListener(this);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        ((SOMViewer) state.parentFrame).uncheckComponentInMenu(this);
    }

    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameActivated(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

}

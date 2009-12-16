package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;

/**
 * This class implements basic functionality for a control window in the SOMViewer application. All control elements should extend this class.
 * 
 * @author Rudolf Mayer
 * @version $Id: AbstractViewerControl.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractViewerControl extends JInternalFrame implements InternalFrameListener {

    private static final long serialVersionUID = 1L;

    protected final CommonSOMViewerStateData state;

    protected static final Font smallFont = new Font("Tahoma_small", Font.PLAIN, 9);

    protected static final Font smallerFont = new Font("Tahoma_small", Font.PLAIN, 8);

    protected static final Insets SMALL_INSETS = new Insets(2, 5, 1, 5);

    /**
     * Creates a new instance with the given title.
     * 
     * @param title The title of the control element.
     * @param state The som viewer state object.
     */
    protected AbstractViewerControl(String title, CommonSOMViewerStateData state) {
        super(title, true, true, true, true);
        this.state = state;
        addInternalFrameListener(this);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
    }

    /**
     * Creates a new instance with the given title and layout.
     * 
     * @param title The title of the control element.
     * @param state The som viewer state object.
     * @param layout The layout to be used for this control element.
     */
    protected AbstractViewerControl(String title, CommonSOMViewerStateData state, LayoutManager layout) {
        this(title, state);
        getContentPane().setLayout(layout);
    }

    /**
     * Sets the visibility of this control, and invokes <code>pack()</code> before.
     * 
     * @see java.awt.Component#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        if (visible) {
            pack();
        }
        super.setVisible(visible);
    }

    public void internalFrameActivated(InternalFrameEvent e) {
    }

    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        ((SOMViewer) state.parentFrame).uncheckComponentInMenu(this);
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
        // TODO: arrange internal frame on left side
    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

}

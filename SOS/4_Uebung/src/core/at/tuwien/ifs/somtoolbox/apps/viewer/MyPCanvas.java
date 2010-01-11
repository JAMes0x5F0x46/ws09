package at.tuwien.ifs.somtoolbox.apps.viewer;

import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.ClusterSelectionEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.LineSelectionEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyInputDragSequenceEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyLabelDragSequenceEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyRectangleSelectionEventHandler;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

/**
 * adds selection handler set and get methods to the piccolo pcanvas class
 * 
 * @author Robert Neumayer
 * @version $Id: MyPCanvas.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MyPCanvas extends PCanvas {

    private static final long serialVersionUID = 1L;

    private PDragSequenceEventHandler selectionEventHandler;

    /**
     * Get the selection event handler associated with this canvas.
     */
    public PDragSequenceEventHandler getSelectionEventHandler() {
        return this.selectionEventHandler;
    }

    public MyRectangleSelectionEventHandler getMyRectangleSelectionEventHandler() {
        return (MyRectangleSelectionEventHandler) this.selectionEventHandler;
    }

    public LineSelectionEventHandler getLineSelectionEventHandler() {
        return (LineSelectionEventHandler) this.selectionEventHandler;
    }

    /**
     * Set the selection event handler associated with this canvas.
     * 
     * @param handler the new selection event handler
     */
    public void setSelectionEventHandler(MyRectangleSelectionEventHandler handler) {
        if (this.selectionEventHandler instanceof MyRectangleSelectionEventHandler)
            return;
        clearOldHandlers();
        this.selectionEventHandler = handler;
        if (this.selectionEventHandler != null) {
            addInputEventListener(this.selectionEventHandler);
        }
    }

    private void clearOldHandlers() {
        if (this.selectionEventHandler != null) {
            if (this.selectionEventHandler instanceof LineSelectionEventHandler) {
                ((LineSelectionEventHandler) this.selectionEventHandler).deleteOldLine();
            } else if (this.selectionEventHandler instanceof ClusterSelectionEventHandler) {
                PSelectionEventHandler p = (PSelectionEventHandler) this.selectionEventHandler;
                p.unselectAll();
            } else if (this.selectionEventHandler instanceof PSelectionEventHandler) {
                PSelectionEventHandler p = (PSelectionEventHandler) this.selectionEventHandler;
                p.unselectAll();
            }
            removeInputEventListener(this.selectionEventHandler);
        }
    }

    /**
     * Set the corridor selection event handler associated with this canvas.
     * 
     * @param handler the new selection event handler
     */
    public void setSelectionEventHandler(LineSelectionEventHandler handler) {
        if (this.selectionEventHandler instanceof LineSelectionEventHandler)
            return;
        clearOldHandlers();
        this.selectionEventHandler = handler;
        if (this.selectionEventHandler != null) {
            addInputEventListener(this.selectionEventHandler);
        }
    }

    /**
     * Set the cluster selection event handler associated with this canvas.
     * 
     * @param handler the new selection event handler
     */
    public void setSelectionEventHandler(ClusterSelectionEventHandler handler) {
        if (this.selectionEventHandler instanceof ClusterSelectionEventHandler)
            return;
        clearOldHandlers();
        this.selectionEventHandler = handler;
        if (this.selectionEventHandler != null) {
            addInputEventListener(this.selectionEventHandler);
        }
    }

    /**
     * Removes the current selectionEventHandler associated with this canvas and adds event handler for moving inputs.
     * 
     * @param handler the event handler for input moving
     */
    public void setSelectionEventHandler(MyInputDragSequenceEventHandler handler) {
        if (this.selectionEventHandler instanceof MyInputDragSequenceEventHandler)
            return;
        if (this.selectionEventHandler != null) {
            PDragSequenceEventHandler p = this.selectionEventHandler;
            if (p instanceof LineSelectionEventHandler) {
                LineSelectionEventHandler lse = (LineSelectionEventHandler) p;
                lse.deleteOldLine();
            }
            if (p instanceof MyRectangleSelectionEventHandler) {
                MyRectangleSelectionEventHandler rse = (MyRectangleSelectionEventHandler) p;
                rse.unselectAll();
            }
            removeInputEventListener(this.selectionEventHandler);
            this.selectionEventHandler = handler;
            if (this.selectionEventHandler != null) {
                addInputEventListener(this.selectionEventHandler);
            }
        }
    }

    // Angela: TODO Maybe merge the three setSelectionEventHandler in 1 function

    /**
     * Removes the current selectionEventHandler associated with this canvas and adds event handler for moving Labels.
     * 
     * @param handler the event handler for label moving
     */
    public void setSelectionEventHandler(MyLabelDragSequenceEventHandler handler) {
        if (this.selectionEventHandler instanceof MyLabelDragSequenceEventHandler)
            return;
        if (this.selectionEventHandler != null) {
            PDragSequenceEventHandler p = this.selectionEventHandler;
            if (p instanceof LineSelectionEventHandler) {
                LineSelectionEventHandler lse = (LineSelectionEventHandler) p;
                lse.deleteOldLine();
            }
            if (p instanceof MyRectangleSelectionEventHandler) {
                MyRectangleSelectionEventHandler rse = (MyRectangleSelectionEventHandler) p;
                rse.unselectAll();
            }
            removeInputEventListener(this.selectionEventHandler);
            this.selectionEventHandler = handler;
            if (this.selectionEventHandler != null) {
                addInputEventListener(this.selectionEventHandler);
            }
        }
    }

    // edit by epei
    public void removeSelection() {
        PDragSequenceEventHandler handler = this.getSelectionEventHandler();
        if (handler instanceof MyRectangleSelectionEventHandler) {
            ((MyRectangleSelectionEventHandler) handler).unselectAll();
        }
        if (handler instanceof LineSelectionEventHandler) {
            ((LineSelectionEventHandler) handler).deleteOldLine();
        }
    }

}

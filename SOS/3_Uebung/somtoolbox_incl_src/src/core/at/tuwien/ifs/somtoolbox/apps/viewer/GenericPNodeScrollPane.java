package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;

import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.ClusterSelectionEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.LineSelectionEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyMapInputEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyPanEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyRectangleSelectionEventHandler;
import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.MyWheelZoomEventHandler;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.event.PNotification;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * A generic scrollable pane that holds a {@link PNode} and handles line and rectangle selection events via registered listeners. This class makes use
 * of the <a href="http://www.cs.umd.edu/hcil/jazz/" target="_blank">Piccolo framework</a>.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: GenericPNodeScrollPane.java 2874 2009-12-11 16:03:27Z frank $
 */
public class GenericPNodeScrollPane extends PScrollPane {

    private static final long serialVersionUID = 1L;

    protected PCanvas canvas = new MyPCanvas();

    protected MyRectangleSelectionEventHandler rectangleSelectionEventHandler;

    protected ClusterSelectionEventHandler clusterSelectionEventHandler;

    protected LineSelectionEventHandler lineSelectionEventHandler;

    protected PNode node = null;

    protected UnitSelectionListener[] connectedSelectionHandlers = null;

    protected HashSet<UnitSelectionListener> connectedSelectionHandlersSet = new HashSet<UnitSelectionListener>();

    protected Point2D lastSelectedPoint;

    protected CommonSOMViewerStateData state;

    protected final ToolTipPNode tooltipNode = new ToolTipPNode();

    public GenericPNodeScrollPane(CommonSOMViewerStateData state, PNode node) {
        super();
        this.state = state;
        this.node = node;
        init();
    }

    protected GenericPNodeScrollPane() {
        super();
        setViewportView(canvas);
    }

    protected void init() {
        setViewportView(canvas);
        setAutoscrolls(false);

        initPNodeSpecific();

        // create MouseLstener for cursor changes, etc.
        canvas.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                }
                if (e.getButton() == MouseEvent.BUTTON2) {
                    centerAndFitMapToScreen();
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        // create event handler for panning
        canvas.removeInputEventListener(getCanvas().getPanEventHandler());
        canvas.setPanEventHandler(new MyPanEventHandler());

        // create event handler for zooming
        canvas.removeInputEventListener(getCanvas().getZoomEventHandler());
        canvas.addInputEventListener(new MyWheelZoomEventHandler());

        // create event handler for selection
        this.initSelectionHandlers();

        // redraw interface if necessary
        validate();
    }

    private void initPNodeSpecific() {
        canvas.getLayer().addChild(node);
        // create tooltip object and add event listener
        node.addChild(tooltipNode);
        getCanvas().addInputEventListener(new MyMapInputEventHandler(tooltipNode, node));
    }

    public void selectionChanged(PNotification notification) {
        if (connectedSelectionHandlers != null) {
            PSelectionEventHandler handler = (PSelectionEventHandler) notification.getObject();
            Collection<?> coll = handler.getSelection();
            boolean newSelection = false;
            if (!handler.getMousePressedCanvasPoint().equals(lastSelectedPoint)) {
                lastSelectedPoint = (Point2D) handler.getMousePressedCanvasPoint().clone();
                newSelection = true;
            }
            for (int i = 0; i < connectedSelectionHandlers.length; i++) {
                connectedSelectionHandlers[i].unitSelectionChanged(coll.toArray(), newSelection);
            }
        }
    }

    /** set the line selection handler. */
    public void setLine() {
        ((MyPCanvas) canvas).setSelectionEventHandler(this.lineSelectionEventHandler);
        PNotificationCenter.defaultCenter().addListener(this, "selectionChanged", PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                this.lineSelectionEventHandler);
    }

    /** set the rectangle selection handler. */
    public void setRectangle() {
        ((MyPCanvas) canvas).setSelectionEventHandler(this.rectangleSelectionEventHandler);
        PNotificationCenter.defaultCenter().addListener(this, "selectionChanged", PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                this.rectangleSelectionEventHandler);
    }

    /** set the cluster selection handler. */
    public void setCluster() {
        ((MyPCanvas) canvas).setSelectionEventHandler(this.clusterSelectionEventHandler);
        PNotificationCenter.defaultCenter().addListener(this, "selectionChanged", PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                this.clusterSelectionEventHandler);
    }

    public PCanvas getCanvas() {
        return canvas;
    }

    /**
     * initialize both selection handlers and set rectangle selection as default. also initializes handler for selecting and moving a cluster label.
     */
    public void initSelectionHandlers() {
        PLayer layer = this.getCanvas().getLayer();
        PNode child = layer.getChild(0);
        this.lineSelectionEventHandler = new LineSelectionEventHandler(layer, child);
        this.rectangleSelectionEventHandler = new MyRectangleSelectionEventHandler(layer, child);
        if (child instanceof MapPNode) { // e.g. in the ComponentPlaneClusteringFrame, we do not have a MapPNode, just a PNode
            this.clusterSelectionEventHandler = new ClusterSelectionEventHandler(layer, child);
        }
        this.setRectangle();
    }

    public void centerAndFitMapToScreen(int animationDuration) {
        canvas.getCamera().animateViewToCenterBounds(new Rectangle2D.Double(0, 0, node.getWidth(), node.getHeight()), true, animationDuration);
    }

    public void connectSelectionHandlerTo(UnitSelectionListener panel) {
        connectedSelectionHandlersSet.add(panel);
        connectedSelectionHandlers = (UnitSelectionListener[]) connectedSelectionHandlersSet.toArray(new UnitSelectionListener[connectedSelectionHandlersSet.size()]);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        Container parentContainer = getParent().getParent().getParent().getParent().getParent();
        return new Dimension(parentContainer.getWidth() - state.controlElementsWidth, parentContainer.getHeight());
    }

    public void centerAndFitMapToScreen() {
        centerAndFitMapToScreen(2000);
    }

    public void setPNode(PNode newPNode) {
        canvas.getLayer().removeAllChildren();
        this.node = newPNode;
        initPNodeSpecific();
    }

}
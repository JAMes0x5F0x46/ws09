package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Handles zooming by the mouse wheel.
 * 
 * @author Robert Neumayer
 * @version $Id: MyWheelZoomEventHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MyWheelZoomEventHandler extends PBasicInputEventHandler {
    // private Point2D wheelEventCanvasPoint = null;

    private double minScale = 0;

    private double maxScale = Double.MAX_VALUE;

    public MyWheelZoomEventHandler() {
        super();
    }

    public void mouseWheelRotated(PInputEvent e) {
        double scaleDelta;
        if (e.getWheelRotation() == 1) {
            scaleDelta = (1.0 - (0.08));
        } else {
            scaleDelta = (1.0 + (0.08));
        }

        PCamera camera = e.getCamera();

        double currentScale = camera.getViewScale();
        double newScale = currentScale * scaleDelta;

        if (newScale < minScale) {
            scaleDelta = minScale / currentScale;
        }
        if ((maxScale > 0) && (newScale > maxScale)) {
            scaleDelta = maxScale / currentScale;
        }
        Point2D mousePosition = e.getCanvasPosition();
        Point2D localMousePosition = e.getCamera().localToView(mousePosition);
        camera.scaleViewAboutPoint(scaleDelta, localMousePosition.getX(), localMousePosition.getY());
    }
}

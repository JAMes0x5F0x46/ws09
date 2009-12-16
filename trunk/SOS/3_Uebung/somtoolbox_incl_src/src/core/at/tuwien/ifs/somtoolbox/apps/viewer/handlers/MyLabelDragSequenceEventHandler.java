package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Used to move Labels of clusters.
 * 
 * @author Angela Roiger
 * @version $Id: MyLabelDragSequenceEventHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MyLabelDragSequenceEventHandler extends AbstractDragSequenceEventHandler {
    public MyLabelDragSequenceEventHandler() {
        allowedNodeTypes = new String[] { "clusterLabel", "manualLabel", "smallClusterLabel" };
    }

    protected void endDrag(PInputEvent e) {
        super.endDrag(e);
        Point2D endPos = e.getPosition();
        e.getPickedNode().getParent().setOffset(endPos.getX() - clickX, endPos.getY() - clickY);
    }
}

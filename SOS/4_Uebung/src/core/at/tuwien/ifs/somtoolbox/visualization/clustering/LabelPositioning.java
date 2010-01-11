package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PNode;

/**
 * Methods for positioning the label inside a cluster.
 * 
 * @author Angela Roiger
 * @version $Id: LabelPositioning.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LabelPositioning {

    /** 
     * Place the label in the center of the surrounding rectangle of the cluster.
     */
    public static void center(ClusterNode cluster, PNode label) {
        label.setWidth(label.getChild(0).getWidth());
        label.setHeight(label.getChild(0).getHeight());
        label.centerFullBoundsOnPoint(cluster.getX() + (cluster.getWidth() / 2), cluster.getY() + (cluster.getHeight() / 2));

    }
    
    /**
     * Place the label in the centroid of the cluster. 
     */
    public static void centroid(ClusterNode cluster, PNode label) {
        Point2D.Double centroid = cluster.getCentroid();
        label.setOffset(centroid.x, centroid.y);
    }

}

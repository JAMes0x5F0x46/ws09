package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Class used to paint the clusters on the map. Each ColoredClusterPNode is associated with one ClusterNode.
 * 
 * @author Angela Roiger
 * @version $Id: ColoredClusterPNode.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ColoredClusterPNode extends PNode {
    private static final long serialVersionUID = 1L;

    private ClusterNode correspondingCluster;

    public ColoredClusterPNode(ClusterNode cluster) {
        super();
        correspondingCluster = cluster;
    }


    /**
     * Fills the all Units ({@link GeneralUnitPNode}) inside the Cluster with Color.
     * Replaces the paint method from {@link PNode}.
     * 
     */
    protected void paint(PPaintContext paintContext) {

        GeneralUnitPNode[] unitNodes = correspondingCluster.getNodes();

        if (this.getPaint() != null) {
            Graphics2D g2d = paintContext.getGraphics();
            g2d.setPaint(this.getPaint());
            for (int i = 0; i < unitNodes.length; i++) {
                GeneralUnitPNode u = unitNodes[i];
                g2d.fill(new Rectangle2D.Double(u.getX(), u.getY(), u.getWidth(), u.getHeight()));
            }
        }
    }
}

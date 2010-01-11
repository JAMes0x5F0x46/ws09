package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.awt.BasicStroke;
import java.util.ListIterator;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * Stores all the border lines for one cluster. 
 * 
 * @author Angela Roiger
 * @version $Id: BorderPNode.java 2874 2009-12-11 16:03:27Z frank $
 */
public class BorderPNode extends PNode {
    private static final long serialVersionUID = 1L;

    /**
     * Changes the stroke of the border to the specified BasicStroke
     * @param bs the new stroke for the border
     */
    public void changeBorderStroke(BasicStroke bs) {
        for (ListIterator li = this.getChildrenIterator(); li.hasNext();) {
            ((PPath) li.next()).setStroke(bs);
        }
    }

}

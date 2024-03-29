package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.awt.BasicStroke;
import java.awt.Font;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * A container to store borders and labels of one level of a clustering
 * 
 * @author Angela Roiger
 * @version $Id: ClusterElementsStorage.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClusterElementsStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    public ArrayList<PNode> clusterBorders; // contains the borders of all clusters of this level

    public ArrayList<PNode> clusterLabels;

    public ArrayList<ColoredClusterPNode> clusterColors;

    public boolean sticky = false;

    public static Font defaultFont = ClusteringTree.defaultFont;

    /**
     * Changes the stroke of the clusters' border lines.
     * 
     * @param bs the new BasicStroke for the borders
     */
    public void changeBorderStroke(BasicStroke bs) {
        for (ListIterator<PNode> li = clusterBorders.listIterator(); li.hasNext();) {
            BorderPNode c = (BorderPNode) li.next();
            c.changeBorderStroke(bs);
            c.repaint();
        }
    }

    /**
     * Changes the Font of all labels texts. If there is more than one text inside a label the first one will have the specified Font, and the others
     * will have the same Font but only half fontsize. Changes the yOffset of the texts so they are not overlapping/too far apart.
     * 
     * @param f the Font
     */
    public void changeFont(Font f) {
        Font font;
        if (f != null) {
            font = f;
        } else {
            font = ClusteringTree.defaultFont;
        }
        if (this.clusterLabels != null) {
            for (ListIterator<PNode> li = this.clusterLabels.listIterator(); li.hasNext();) {
                PNode labels = (PNode) li.next();
                PText bigLabel = new PText(); // remember size :)
                int i = 0;
                for (ListIterator l = labels.getChildrenIterator(); l.hasNext();) {
                    try {
                        PText currentLabel = (PText) l.next();

                        if (new String("clusterLabel").equals((String) currentLabel.getAttribute("type"))) {
                            double x = currentLabel.getXOffset() + currentLabel.getWidth() / 2;
                            double y = currentLabel.getYOffset() + currentLabel.getHeight() / 2;
                            currentLabel.setFont(font);
                            currentLabel.centerFullBoundsOnPoint(x, y);
                            bigLabel = currentLabel;
                        } else if (new String("smallClusterLabel").equals((String) currentLabel.getAttribute("type"))) {
                            currentLabel.setFont(font.deriveFont(font.getSize2D() / 2));
                            currentLabel.setOffset(bigLabel.getXOffset(), bigLabel.getYOffset() + bigLabel.getHeight()
                                    + (i * currentLabel.getHeight()));
                            i++;
                        }

                        currentLabel.repaint();
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

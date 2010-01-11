package at.tuwien.ifs.somtoolbox.apps.viewer;

import edu.umd.cs.piccolo.PNode;

/**
 * Representation of an input object on the map
 * 
 * @author Khalid Latif (May 14, 2007)
 * @version $Id: InputPNode.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class InputPNode extends PNode {

    private static final long serialVersionUID = 1l;

    /** Quarter of the height of the node */
    public static final int HEIGHT_4 = 3;

    /** Half of the height of the node */
    public static final int HEIGHT_2 = HEIGHT_4 * 2;

    /** Height of the node */
    public static final int HEIGHT = HEIGHT_2 * 2;

    /** Quarter of the width of the node */
    public static final int WIDTH_4 = 3;

    /** Half of the width of the node */
    public static final int WIDTH_2 = WIDTH_4 * 2;

    /** Width of the node */
    public static final int WIDTH = WIDTH_2 * 2;

    /** Minimum distance (square) to be maintained between the nodes */
    public static final int MIN_DISTANCE_SQ = WIDTH_4 * HEIGHT_4;

    /**
     * Default constructor
     */
    public InputPNode() {
        setWidth(WIDTH);
        setHeight(HEIGHT);
    }

    /**
     * Initializes this node with the given x, y position
     * 
     * @see PNode#setBounds(double, double, double, double)
     */
    public InputPNode(double x, double y) {
        super.setBounds(x, y, WIDTH, HEIGHT);
    }

}

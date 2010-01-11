package at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree;

import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @version $Id: Node.java 2874 2009-12-11 16:03:27Z frank $
 */
public class Node implements Comparable<Node> {
    private String label;

    private Unit unit;

    private double x;

    private double y;

    public Node(String label, double x, double y, Unit unit) {
        this.label = label;
        this.x = x;
        this.unit = unit;
        this.y = y;
    }

    public int compareTo(Node o) {
        return label.compareTo(o.getLabel());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Node && label.equals(((Node) o).getLabel());

    }

    public String getLabel() {
        return label;
    }

    public Unit getUnit() {
        return unit;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
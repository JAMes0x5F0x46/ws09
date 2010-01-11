package at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @version $Id: Edge.java 2874 2009-12-11 16:03:27Z frank $
 */
public class Edge {

    private Node start;

    private Node end;

    private double weight;

    public Edge(Node start, Node end, double weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public Node getEnd() {
        return end;
    }

    public Node getStart() {
        return start;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "(" + start.getLabel() + "," + end.getLabel() + ")";
    }
}

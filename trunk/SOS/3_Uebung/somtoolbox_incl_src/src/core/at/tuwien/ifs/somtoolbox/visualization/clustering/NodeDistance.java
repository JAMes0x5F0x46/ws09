package at.tuwien.ifs.somtoolbox.visualization.clustering;


/**
 * Used to store a distance between two clusters. The order of the Objects is by ascending distances. If two distances are equal, the Object with the
 * lower hash code comes first.
 * 
 * @author Angela Roiger
 * @version $Id: NodeDistance.java 2874 2009-12-11 16:03:27Z frank $
 */
class NodeDistance implements Comparable<NodeDistance> {

    ClusterNode n1;

    ClusterNode n2;

    // double -> float .. to save space
    double dist;

    public NodeDistance(ClusterNode no1, ClusterNode no2, double d) {
        this.n1 = no1;
        this.n2 = no2;
        this.dist = (float) d;
    }

    /**
     * Compare this Node distance to another Object. Two distances are equal if all their components (nodes & distance value) are equal.
     */
    public boolean equals(Object o) {
 if (o instanceof NodeDistance) {
            NodeDistance tmp = (NodeDistance) o;            
            return (this.n1 == tmp.n1) && (this.n2 == tmp.n2) && (this.dist == tmp.dist);
        }
        return false;
    }


    /**
     * Must be equal if the objects are equal according to nodeDistance.equal. Should not be equal otherwise.
     */
    public int hashCode() {
        return this.n1.hashCode() + (2 * this.n2.hashCode());
    }

    public int compareTo(NodeDistance o) throws ClassCastException {
        if (this.equals(o)) {
            return 0;
        }
        if (this.dist == o.dist) {
            return new Integer(this.hashCode()).compareTo(o.hashCode());
        } else {
            return Double.compare(this.dist, o.dist);
        }
    }
}

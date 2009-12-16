package at.tuwien.ifs.somtoolbox.visualization.clustering;

/**
 * @author Doris Baum
 * @version $Id: ClusterEquivalence.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClusterEquivalence implements Comparable<ClusterEquivalence> {
    public double percentage = -1;

    public int cluster1 = -1;

    public int cluster2 = -1;

    public int compareTo(ClusterEquivalence other) {
        return Double.compare(this.percentage, other.percentage);
    }
}

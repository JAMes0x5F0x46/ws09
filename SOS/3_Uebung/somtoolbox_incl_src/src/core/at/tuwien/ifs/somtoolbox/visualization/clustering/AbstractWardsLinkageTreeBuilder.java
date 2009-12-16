package at.tuwien.ifs.somtoolbox.visualization.clustering;

import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * @author Rudolf Mayer
 * @version $Id: $
 */
public abstract class AbstractWardsLinkageTreeBuilder extends TreeBuilder {

    public AbstractWardsLinkageTreeBuilder() {
        super();
    }

    /**
     * Calculates the increase of the Error Sum of Squares if the two Clusters are united. To get the "real" Error Sum of Squares increase one must
     * take the square root of the returned value. This step is left out of here to save computation time, as it changes nothing in the order of the
     * distances if this calculation is left out for all distances.
     * 
     * @return the 'distance' value
     */
    protected double calcESSincrease(ClusterNode n1, ClusterNode n2) {
        int length1 = n1.getNodes().length;
        int length2 = n2.getNodes().length;

        L2Metric l2 = new L2Metric();

        double dist = l2.norm(VectorTools.subtract(n1.getMeanVector(), n2.getMeanVector()));
        double result = ((dist * dist) / (length1 + length2)) * length1 * length2;

        return result;
    }

}
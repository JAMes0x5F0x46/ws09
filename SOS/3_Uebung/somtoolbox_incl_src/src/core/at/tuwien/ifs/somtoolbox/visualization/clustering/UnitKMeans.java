package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.util.Hashtable;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;

public class UnitKMeans extends KMeans {

    private static Hashtable<double[], GeneralUnitPNode> unitLookupTable;

    public UnitKMeans(int k, GeneralUnitPNode[][] units) {
        super(k, convert(units));
    }

    public UnitKMeans(int k, GeneralUnitPNode[][] units, InitType initialisation) {
        super(k, convert(units), initialisation);
    }

    /**
     * Convert a GeneralUnitPNode[][] to a simple doule[][].
     * @param units units to convert to.
     * @return plain double[][] data matrix.
     */
    public static double[][] convert(GeneralUnitPNode[][] units) {
        double[][] data = new double[units.length * units[0].length][units[0].length];
        unitLookupTable = new Hashtable<double[], GeneralUnitPNode>();
        int i = 0;
        for (int y = 0; y < units[0].length; y++) {
            for (int x = 0; x < units.length; x++) {
                data[i] = units[x][y].getUnit().getWeightVector();
                unitLookupTable.put(data[i], units[x][y]);
                i++;
            }
        }
        return data;
    }

    /**
     * Returns the ClusterNodes for the given level. Thanks a million 
     * Angela for this prime example of programming art :-)
     * @param level
     * @return
     */
    public ClusterNode[] getClusterNodes(int level) {
        ClusterNode newNode = null;
        ClusterNode[] clusterNodes = new ClusterNode[clusters.length];
        for (int clusterIndex = 0; clusterIndex < clusters.length; clusterIndex++) {
            level--;
            double[][] instances = clusters[clusterIndex].getInstances(data);
            for (int instanceIndex = 0; instanceIndex < instances.length; instanceIndex++) {
                // create new node

                if (instanceIndex == 0) {
                    newNode = new ClusterNode(unitLookupTable.get(instances[instanceIndex]), level);
                } else {
                    newNode = new ClusterNode(newNode, new ClusterNode(unitLookupTable.get(instances[instanceIndex]), 1), level);
                }
            }
            clusterNodes[clusterIndex] = newNode;
        }
        return clusterNodes;
    }
}

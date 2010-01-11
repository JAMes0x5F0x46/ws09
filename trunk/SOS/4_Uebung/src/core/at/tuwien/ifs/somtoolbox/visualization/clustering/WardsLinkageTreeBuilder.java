package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;

/**
 * Adaption of the Ward's Linkage Clustering Algorithm. This Version only calculates the distances to directly neighboring clusters. This class is not
 * compatible with mnemonic SOMs (and probably also not compatible with hierarchical SOMs) Use WardsLinkageTreeBuilderAll for the "real" Ward's
 * clustering.
 * 
 * @author Angela Roiger
 * @version $Id: WardsLinkageTreeBuilder.java 2874 2009-12-11 16:03:27Z frank $
 */
public class WardsLinkageTreeBuilder extends AbstractWardsLinkageTreeBuilder {

    /**
     * Calculation of the Clustering. This code is only compatible with rectangular, non hierarchical SOMs!
     * 
     * @param units the GeneralUnitPNode Array containing all the units of the SOM
     * @return the ClusteringTree (i.e. the top node of the tree)
     */
    public ClusteringTree createTree(GeneralUnitPNode[][] units) throws ClusteringAbortedException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start Clustering ");
        this.level = units.length * units[0].length;

        // initialize monitor
        resetMonitor(2 * level);

        TreeSet<NodeDistance> dists = calculateInitialDistances(units);
        NodeDistance tmpDist;
        ClusterNode newNode = null;

        while (dists.size() > 0) {
            tmpDist = (NodeDistance) dists.first();

            dists.remove(tmpDist);
            level--;
            incrementMonitor();
            allowAborting();
            newNode = new ClusterNode(tmpDist.n1, tmpDist.n2, level);

            // remove not needed connections and change distances from n1,n2 to newNode
            TreeSet<NodeDistance> storeChanges = new TreeSet<NodeDistance>();
            for (Iterator<NodeDistance> i = dists.iterator(); i.hasNext();) {
                NodeDistance x = (NodeDistance) i.next();
                if ((x.n1 == tmpDist.n1) || (x.n1 == tmpDist.n2)) {
                    x.n1 = newNode;
                }
                if ((x.n2 == tmpDist.n1) || (x.n2 == tmpDist.n2)) {
                    x.n2 = newNode;
                }
                if (x.n1 == x.n2) {
                    i.remove();
                }

                // mehrfache verbindungen entfernen...
                // TODO

                // recalculate distances for connections containing the newNode
                // remove them fron the set, and add again later...
                if (((x.n1 == newNode) || (x.n2 == newNode)) && (x.n1 != x.n2)) {
                    storeChanges.add(new NodeDistance(x.n1, x.n2, calcESSincrease(x.n1, x.n2)));
                    i.remove();
                }
            }
            dists.addAll(storeChanges);
        }
        finishMonitor();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished Clustering - Wards Linkage");

        return new ClusteringTree(newNode, units.length);
    }

    /**
     * Calculates the initial distances from each unit to its neighbours to the right, bottom, bottom-right and bottom-left.
     * 
     * @param units A GeneralUnitPNode[][] containing the Units of the som
     * @return a TreeSet of NodeDistances containing the distances between the units starting with the smallest.
     * @throws ClusteringAbortedException
     */
    private TreeSet<NodeDistance> calculateInitialDistances(GeneralUnitPNode[][] units) throws ClusteringAbortedException {

        /*
         * To make this code compatible with mnemonic soms: Take care only distances between existing units are calculated only calculate (store) the
         * distance between 2 clusters once
         */

        int xdim = units.length;
        int ydim = units[0].length;

        ClusterNode[][] tmp = new ClusterNode[xdim][ydim];
        TreeSet<NodeDistance> dists = new TreeSet<NodeDistance>();

        // create all basic Nodes
        for (int i = 0; i < xdim; i++) {
            for (int j = 0; j < ydim; j++) {
                tmp[i][j] = new ClusterNode(units[i][j], level);
            }
        }

        // calculate initial distances:
        for (int i = 0; i < xdim; i++) {
            for (int j = 0; j < ydim; j++) {
                incrementMonitor();
                allowAborting();
                try {
                    if (i < (xdim - 1)) {
                        dists.add(new NodeDistance(tmp[i][j], tmp[i + 1][j], calcESSincrease(tmp[i][j], tmp[i + 1][j])));
                    }
                    if (j < (ydim - 1)) {
                        dists.add(new NodeDistance(tmp[i][j], tmp[i][j + 1], calcESSincrease(tmp[i][j], tmp[i][j + 1])));
                        if (i < (xdim - 1)) {
                            dists.add(new NodeDistance(tmp[i][j], tmp[i + 1][j + 1], calcESSincrease(tmp[i][j], tmp[i + 1][j + 1])));
                        }
                    }
                    if ((j > 1) && (i < (xdim - 1))) {
                        dists.add(new NodeDistance(tmp[i][j], tmp[i + 1][j - 1], calcESSincrease(tmp[i][j], tmp[i + 1][j - 1])));
                    }
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Cannot create clustering: " + e.getMessage());
                }
            }
        }
        return dists;
    }

    @Override
    public String getClusteringAlgName() {
        return "Ward's Linkage (onlyNeighbourDistances)";
    }

}

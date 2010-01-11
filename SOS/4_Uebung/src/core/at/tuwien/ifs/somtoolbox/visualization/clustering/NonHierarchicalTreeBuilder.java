package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.util.Hashtable;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;

/**
 * This is an abstract class for non-hierarchical tree builders, which need to build a new clustering tree for each level. This class provides caching
 * of already computed cluster levels.
 * 
 * @author Rudolf Mayer
 * @version $Id: NonHierarchicalTreeBuilder.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class NonHierarchicalTreeBuilder extends TreeBuilder {
    protected Hashtable<Integer, ClusteringTree> cache = new Hashtable<Integer, ClusteringTree>();

    public abstract ClusteringTree createTree(GeneralUnitPNode[][] units, int k) throws ClusteringAbortedException;

    public ClusteringTree getTree(GeneralUnitPNode[][] units, int k) throws ClusteringAbortedException {
        if (cache.get(k) == null) {
            cache.put(k, createTree(units, k));
        }
        return cache.get(k);
    }

    

}
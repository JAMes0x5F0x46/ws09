package at.tuwien.ifs.somtoolbox.clustering;

import prefuse.data.Tree;

/**
 * @author Rudolf Mayer
 * @version $Id: HierarchicalClusteringAlgorithm.java 2874 2009-12-11 16:03:27Z frank $
 * @param <E>
 */
public interface HierarchicalClusteringAlgorithm<E> extends ClusteringAlgorithm<E> {

    public Tree getPrefuseTree();
}

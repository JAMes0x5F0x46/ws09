package at.tuwien.ifs.somtoolbox.clustering;

import java.util.List;

/**
 * @author mayer
 * @version $Id: ClusteringAlgorithm.java 2874 2009-12-11 16:03:27Z frank $
 * @param <E>
 */
public interface ClusteringAlgorithm<E> {

    public List<? extends Cluster<E>> doCluster(List<E> data);

    // public List<Cluster<E>> doCluster(Cluster<E> data);
    // public List<List<E>> doCluster(List<E> data);
    // public List<List<E>> doCluster(List<E> data);

}

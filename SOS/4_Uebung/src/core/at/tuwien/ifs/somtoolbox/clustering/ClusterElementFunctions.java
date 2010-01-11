package at.tuwien.ifs.somtoolbox.clustering;

import java.text.DecimalFormat;

public interface ClusterElementFunctions<E> {

    static final DecimalFormat DF = new DecimalFormat("#0.0000");

    public double distance(E element1, E element2);

    public E meanObject(Cluster<? extends E> elements);

    public String toString(Cluster<? extends E> elements);
}

package at.tuwien.ifs.somtoolbox.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.NotImplementedException;

import prefuse.data.Node;
import prefuse.data.Tree;
import at.tuwien.ifs.somtoolbox.structures.ElementWithIndex;
import at.tuwien.ifs.somtoolbox.util.Indices2D;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * @author Rudolf Mayer
 * @version $Id: WardClustering.java 2874 2009-12-11 16:03:27Z frank $
 */
public class WardClustering<E> implements HierarchicalClusteringAlgorithm<E> {

    private int numberOfCPUs = 1;

    private CountDownLatch doneSignal;

    private ThreadPoolExecutor e;

    protected ArrayList<Cluster<E>>[] clusterLevels;

    protected ClusterElementFunctions<? super E> elementDistance;

    protected double threshold = Double.MIN_VALUE;

    protected int targetSize = 1;

    protected List<HierarchicalCluster<E>> clusters;

    /** Turn on some debugging */
    protected boolean debug = false;;

    /** Do full clustering tree. */
    public WardClustering(ClusterElementFunctions<? super E> elementDistance) {
        this.elementDistance = elementDistance;
    }

    /** Do clustering with a specified threshold to stop building the tree. */
    public WardClustering(ClusterElementFunctions<? super E> elementDistance, double threshold) {
        this(elementDistance);
        this.threshold = threshold;
    }

    /** Do clustering with a specified target number of clusters to reach. */
    public WardClustering(ClusterElementFunctions<? super E> elementDistance, int targetSize) {
        this(elementDistance);
        this.targetSize = targetSize;
    }

    @Override
    public Tree getPrefuseTree() {
        Tree tree = new Tree();
        tree.addColumn(HierarchicalCluster.COLUMN_NAME_LEVEL, String.class);
        tree.addColumn(HierarchicalCluster.COLUMN_NAME_CONTENT, String.class);
        tree.addColumn(HierarchicalCluster.COLUMN_NAME_CONTENT_LONG, String.class);
        final Node root = tree.addRoot();
        if (clusters.size() == 1) {
            clusters.get(0).buildPrefuseTree(tree, root);
        } else {
            for (HierarchicalCluster<E> cluster : clusters) {
                cluster.buildPrefuseTree(tree, tree.addChild(root));
            }
        }
        return tree;
    }

    @Override
    public List<HierarchicalCluster<E>> doCluster(List<E> data) {
        init(data);

        if (threshold != Double.MIN_VALUE) {
            double minESSIncrease = getInitialMinESS(clusters);
            // System.out.println("minESS: " + minESS);
            // System.out.println("threshold: " + threshold);
            // System.out.println(ArrayUtils.toString(clusters.toArray()));
            while (minESSIncrease < threshold && clusters.size() > 1) { // merge clusters
                // System.out.println("minESS: " + minESS);
                // System.out.println("threshold: " + threshold);
                minESSIncrease = clusterStep(clusters).getMergeCostIncrease();
            }
        } else {
            StdErrProgressWriter progress = new StdErrProgressWriter(data.size() - targetSize, "Merging clusters ");
            while (clusters.size() > targetSize) { // merge clusters
                clusterStep(clusters);
                progress.progress();
            }
            return clusters;
        }
        return clusters;
    }

    protected void init(List<E> data) {
        clusterLevels = new ArrayList[data.size()];
        if (data.get(0) instanceof Cluster) {
            clusters = (List<HierarchicalCluster<E>>) data;
        } else {
            clusters = new ArrayList<HierarchicalCluster<E>>();
            for (int i = 0; i < data.size(); i++) {
                HierarchicalCluster<E> e2 = new HierarchicalCluster<E>(data.get(i), "Cluster " + i);
                if (data.get(i) instanceof ElementWithIndex) {
                    e2.setLabel(((ElementWithIndex) data.get(i)).getLabel());
                }
                clusters.add(e2);
            }
        }
        clusterLevels[clusters.size() - 1] = new ArrayList<Cluster<E>>(clusters);
    }

    public double getInitialMinESS(List<HierarchicalCluster<E>> clusters) {
        double[][] ess = new double[clusters.size()][clusters.size()];

        double minESS = Double.MAX_VALUE;
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                HierarchicalCluster<E> cluster1 = clusters.get(i);
                HierarchicalCluster<E> cluster2 = clusters.get(j);
                HierarchicalCluster<E> merged = new HierarchicalCluster<E>(cluster1, cluster2);
                merged.setMergeCost(ess(merged));
                cluster1.setMergeCost(ess(cluster1));
                cluster2.setMergeCost(ess(cluster2));

                double increase = merged.getMergeCostIncrease();
                ess[i][j] = ess[j][i] = increase;
                if (increase < minESS) {
                    minESS = increase;
                    // short-cut
                    if (minESS == 0) {
                        return minESS;
                    }
                }
            }
        }
        return minESS;
    }

    public double ess(Cluster<E> cluster) {
        if (cluster.size() == 1) {
            return 0;
        }
        double e = 0;
        E meanLine = (E) elementDistance.meanObject(cluster);
        for (E e2 : cluster) {
            e += elementDistance.distance(e2, meanLine);
        }
        return e;
    }

    public HierarchicalCluster<E> clusterStep(List<HierarchicalCluster<E>> clusters) {
        HierarchicalCluster<E> cMerged = null;
        // we need to build the cross-product of all elements, i.e. clusters.size()*clusters.size() combinations, forming a symmetric matrix
        // we can cut off a bit by just taking one half of the matrix, and cutting the diagonal

        if (numberOfCPUs > 1 && clusters.size() > numberOfCPUs) {
            // we are taking the hard-way for multi-core clustering. a technically much easier solution would be:
            // - build a list of all needed combinations of clusters i and j (i.e. one half of the symmetric matrix)
            // - cut the list in numberOfCPUs pieces, and process each of them, then merge
            // this is easy to do, but needs a list of ((clusters.size()*clusters.size()/2 - clusters.size()) entries, potentially a lot of memory
            //
            // thus, we calculate indices for starting and ending offsets in the matrix
            final Indices2D[] indices = getIndices(clusters);

            doneSignal = new CountDownLatch(indices.length); // important: we need to reset this in each iteration!
            // System.out.println("distributed clustering, count: " + doneSignal.getCount());
            ArrayList<ClusterThread> threads = new ArrayList<ClusterThread>((int) doneSignal.getCount());

            // create & execute all the threads
            for (int i = 0; i < indices.length; i++) {
                final ClusterThread thread = new ClusterThread(indices[i]);
                threads.add(thread);
                e.execute(thread);
            }
            try {
                doneSignal.await(); // wait for all processes to finish
            } catch (InterruptedException ie) {
            }
            // merge the results
            double minESSIncrease = Double.MAX_VALUE;
            for (int i = 0; i < threads.size(); i++) {
                final HierarchicalCluster<E> mergedCluster = threads.get(i).mergedCluster;
                // System.out.println(mergedCluster);
                final double increase = mergedCluster.getMergeCostIncrease();
                if (increase < minESSIncrease) {
                    cMerged = mergedCluster;
                    minESSIncrease = increase;
                }
            }
        } else {
            // System.out.println("normal clustering");
            cMerged = findOptiomalClusterMerger(clusters);
        }
        if (debug) {
            System.out.println("\nMerging clusters with size " + cMerged.getLeftNode().size() + " & " + cMerged.getRightNode().size() + ", ESS: "
                    + cMerged.getMergeCostIncrease());
        }
        clusters.remove(cMerged.getLeftNode());
        clusters.remove(cMerged.getRightNode());
        clusters.add(cMerged);
        clusterLevels[clusters.size() - 1] = new ArrayList<Cluster<E>>(clusters);
        return cMerged;
    }

    private HierarchicalCluster<E> findOptiomalClusterMerger(List<HierarchicalCluster<E>> clusters) {
        return findOptiomalClusterMerger(clusters, 0, 0 + 1, clusters.size() - 1, clusters.size() - 1);
    }

    private HierarchicalCluster<E> findOptiomalClusterMerger(List<HierarchicalCluster<E>> clusters, int startX, int startY, int endX, int endY) {
        double minESSIncrease = Double.MAX_VALUE;
        HierarchicalCluster<E> cMerged = null;
        // System.out.println("find best cluster in " + startX + ", " + startY + ", " + endX + ", " + endY);
        for (int i = startX; i <= endX; i++) {
            for (int j = (i == startX ? startY : i + 1); j <= (i == endX ? endY : clusters.size() - 1); j++) {
                // System.out.println(i + ", " + j);
                HierarchicalCluster<E> c1 = clusters.get(i);
                HierarchicalCluster<E> c2 = clusters.get(j);
                HierarchicalCluster<E> cnew = new HierarchicalCluster<E>(c1, c2);
                cnew.setMergeCost(ess(cnew));
                c1.setMergeCost(ess(c1));
                c2.setMergeCost(ess(c2));

                double increase = cnew.getMergeCostIncrease();
                if (increase < minESSIncrease) {
                    minESSIncrease = increase;
                    cMerged = cnew;
                    // shortcut, stop if we have already a minimal ESS
                    // FIXME: maybe if there are more elements with the same ESS, they should all be merged in the same step?
                    if (minESSIncrease == startX) {
                        System.out.println("\nBreaking calculation, minESSIncrease is 0");
                        i = endX;
                        break;
                    }
                }
            }
        }
        return cMerged;
    }

    public ArrayList<Cluster<E>> getClustersByThreshold(double threshold) throws NotImplementedException {
        // FIXME: implement this!!!!
        // TODO for it: store the cluster merge costs, and if needed, continue merging an under-developed clustering tree.
        throw new NotImplementedException("Method not yet implemented!");
    }

    public ArrayList<Cluster<E>> getClustersAtLevel(int num) {
        if (num > clusterLevels.length - 1) {
            return clusterLevels[clusterLevels.length - 1];
        }
        return clusterLevels[num];
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        System.out.println("Ward clustering, set debug to: " + debug);
    }

    public void setNumberOfCPUs(int numberOfCPUs) {
        System.out.println("Ward clustering, working with " + numberOfCPUs + " CPUs.");
        this.numberOfCPUs = numberOfCPUs;
        e = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfCPUs);
        doneSignal = new CountDownLatch(numberOfCPUs);
        // System.out.println("Set donesignal: " + doneSignal.getCount() + ", executor thread: " + e.getCorePoolSize());
    }

    /** find matrix indices for starting & ending the symetrical matrix indices */
    private Indices2D[] getIndices(List<HierarchicalCluster<E>> clusters) {
        // the number of elements in the matrix is 1+2+3...n
        final int size = clusters.size();
        int elementCount = (int) ((size - 1) * (size / 2d));
        int splitSize = (int) Math.ceil(elementCount / (double) numberOfCPUs);

        // there might be cases in which we can't use the last CPU
        // e.g. we have 36 combinations, and 7 CPUs
        // ==> the splitsize would be 5.14, ceiling up to 6 (as 5*7 == 35 and thus not covering all combinations)
        // but then, already 6 splits (6*6) is covering all combinations, thus we don't need the last split!
        int splits = (numberOfCPUs - 1) * splitSize == elementCount ? numberOfCPUs - 1 : numberOfCPUs;

        Indices2D[] indices = new Indices2D[splits];
        // System.out.println("\nclusters:" + clusters.size() + ", elementCount: " + elementCount + ", splitsize:" + splitSize);
        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                final int indicesIndex = index / splitSize;
                // System.out.println("size: " + size + ", (index + 1) / splitSize: " + indicesIndex);
                // System.out.println(index + " => " + i + "/" + j);
                if (index % splitSize == 0) { // start index
                    indices[indicesIndex] = new Indices2D(i, j, 0, 0);
                    // System.out.println("set start for split " + indicesIndex);
                }
                if ((index + 1) % splitSize == 0 || index + 1 == elementCount) { // end index
                    indices[indicesIndex].setEnd(i, j);
                    // System.out.println("set end for split " + indicesIndex);
                }
                index++;
            }
        }
        // System.out.println("\nclusters:" + clusters.size() + ", elementCount: " + elementCount + ", splitsize:" + splitSize + " => "
        // + Arrays.toString(indices));

        return indices;
    }

    public List<HierarchicalCluster<E>> getClusters() {
        return clusters;
    }

    class ClusterThread implements Runnable {

        private HierarchicalCluster<E> mergedCluster;

        private Indices2D coords;

        public ClusterThread(Indices2D coords) {
            this.coords = coords;
        }

        @Override
        public void run() {
            mergedCluster = findOptiomalClusterMerger(clusters, coords.startX, coords.startY, coords.endX, coords.endY);
            // System.out.println("found cluster for " + coords + " : " + mergedCluster);
            doneSignal.countDown();
        }
    }
}

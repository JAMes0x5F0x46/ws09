package at.tuwien.ifs.somtoolbox.visualization.clustering;

import javax.swing.ProgressMonitor;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.util.AdaptiveStdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * Superclass providing aborting and status monitoring functionality for the clustering algorithms.
 * @author Angela Roiger
 * @version $Id: TreeBuilder.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class TreeBuilder {
    protected int level;

    protected ProgressMonitor monitor; // use this to show the status

    protected StdErrProgressWriter progressWriter;
    
    protected int progress = 0; // how far are we?

    public abstract ClusteringTree createTree(GeneralUnitPNode[][] units) throws ClusteringAbortedException;

    public void setMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
    }

    protected void resetMonitor(int maximum) {
        progress = 0;
        if (monitor != null) {
            monitor.setMaximum(maximum);
        }
        progressWriter = new AdaptiveStdErrProgressWriter(maximum, "Calculating clusters: ");
    }

    protected void incrementMonitor() {
        if (monitor != null) {
            monitor.setProgress(++progress);
        }
        progressWriter.progress();
    }
    
    protected void finishMonitor() {
        if (monitor != null) {
            monitor.close();
        }
        progressWriter.progress(progressWriter.getSteps());
    }

    protected void allowAborting() throws ClusteringAbortedException {
        if (monitor != null && monitor.isCanceled()) {
            throw new ClusteringAbortedException();
        }
    }

    public abstract String getClusteringAlgName();
        
}

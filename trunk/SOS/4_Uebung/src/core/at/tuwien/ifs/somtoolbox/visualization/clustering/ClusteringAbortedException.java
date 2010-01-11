package at.tuwien.ifs.somtoolbox.visualization.clustering;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Is thrown when the user has aborted the running clustering algorithm.
 * 
 * @author Angela Roiger
 * @version $Id: ClusteringAbortedException.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClusteringAbortedException extends SOMToolboxException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     */
    public ClusteringAbortedException() {
        super("Clustering has been aborted by the user.");
    }
}

package at.tuwien.ifs.somtoolbox.layers.quality;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Is thrown if a quality measure can not be found.
 * 
 * @author Michael Dittenbach
 * @version $Id: QualityMeasureNotFoundException.java 2874 2009-12-11 16:03:27Z frank $
 */
public class QualityMeasureNotFoundException extends SOMToolboxException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     */
    public QualityMeasureNotFoundException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */

    public QualityMeasureNotFoundException(String message) {
        super(message);
    }

}

package at.tuwien.ifs.somtoolbox.data;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Is thrown if data dimensions are erroneous. E.g. if the dimensions given in $DATA_DIM header line does not agree with $VEC_DIM or if a Rhythm
 * Pattern is instanciated, and the dimensions do not match the weight vector.
 * 
 * @author Thomas Lidy
 * @version $Id: DataDimensionException.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DataDimensionException extends SOMToolboxException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     */
    public DataDimensionException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */

    public DataDimensionException(String message) {
        super(message);
    }

}
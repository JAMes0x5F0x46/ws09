package at.tuwien.ifs.somtoolbox;

/**
 * Basic exception type in the SOMToolBox framework. This class should be sub-typed to fit more specific purposes.
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMToolboxException.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMToolboxException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     */
    public SOMToolboxException() {
        super();
    }

    public SOMToolboxException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public SOMToolboxException(String message) {
        super(message);
    }

    public SOMToolboxException(String message, Throwable cause) {
        super(message, cause);
    }

}

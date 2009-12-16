package at.tuwien.ifs.somtoolbox.data;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Is thrown if an error occurs when reading properties from file.
 * 
 * @author Thomas Lidy
 * @version $Id: ClassInfoHeaderNotFoundException.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClassInfoHeaderNotFoundException extends SOMToolboxException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     */
    public ClassInfoHeaderNotFoundException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */

    public ClassInfoHeaderNotFoundException(String message) {
        super(message);
    }

}
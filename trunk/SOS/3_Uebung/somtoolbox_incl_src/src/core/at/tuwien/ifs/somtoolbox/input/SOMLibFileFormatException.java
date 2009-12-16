package at.tuwien.ifs.somtoolbox.input;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * Is thrown if the file format of a SOMLib file is corrupt.
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMLibFileFormatException.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMLibFileFormatException extends SOMToolboxException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     */
    public SOMLibFileFormatException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */

    public SOMLibFileFormatException(String message) {
        super(message);
    }

}

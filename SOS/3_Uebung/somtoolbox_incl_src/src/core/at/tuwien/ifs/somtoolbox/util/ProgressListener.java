package at.tuwien.ifs.somtoolbox.util;

/**
 * An interface defining a listener for progress messages. Currently {@link StdErrProgressWriter} is the only class implementing this interface.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: ProgressListener.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface ProgressListener {

    /** Progress by the given steps, and change the message */
    public void progress(String message, int currentStep);

    /** Progress by the given steps. */
    public void progress(int currentStep);

    /** Progress by one step. */
    public void progress();

    /** Progress by one step, and change the message */
    public void progress(String message);

    public void insertRow(int rows, String message);

    public void insertColumn(int columns, String message);

    public int getCurrentStep();
}

package at.tuwien.ifs.somtoolbox.util;

/**
 * A singleton factory creating {@link ProgressListener ProgressListeners}. If an instance of a ProgressListener is set, this instance is returned
 * instead of creating a new one. This allows calling applications to set their own ProgressListener, while preserving backwards compatibility.
 * 
 * @author Christoph Becker
 * @version $Id: ProgressListenerFactory.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ProgressListenerFactory {

    private static ProgressListenerFactory me = null;

    private ProgressListener listener = null;

    /**
     * @param numIterations
     * @return the singleton progress listener
     */
    public ProgressListener createProgressListener(int numIterations, String iteration) {
        return createProgressListener(numIterations, iteration, 1);
    }

    public ProgressListener createProgressListener(int numIterations, String iteration, int stepWidth) {
        if (listener != null) {
            return listener;
        }
        return new StdErrProgressWriter(numIterations, iteration, stepWidth);
    }

    public ProgressListener createProgressListener(int numIterations, String iteration, int stepWidth, int newLineWidth) {
        if (listener != null) {
            return listener;
        }
        return new StdErrProgressWriter(numIterations, iteration, stepWidth, newLineWidth);
    }

    public static ProgressListenerFactory getInstance() {
        if (me == null) {
            me = new ProgressListenerFactory();
        }
        return me;
    }

    public ProgressListener getListener() {
        return listener;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

}

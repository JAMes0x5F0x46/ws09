package at.tuwien.ifs.somtoolbox.layers;

/**
 * This interface should be implemented by classes that want to train a map
 * with regular interruptions to perform special operations during training.
 * 
 * @author Michael Dittenbach
 * @version $Id: TrainingInterruptionListener.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface TrainingInterruptionListener {

    /**
     * Is called when an interruption of the training process occurs.
     * 
     * @param currentIteration the iteration when the interruption occurred.
     * @param numIterations the target iteration number where the training will stop.
     */
    public void interruptionOccurred(int currentIteration, int numIterations);
    
}

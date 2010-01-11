package at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback;

/**
 * Plays different audio files ("one", "two" etc) on each output line one after another.
 * 
 * @author Ewald Peiszer
 * @version $Id: FindMeLoopThread.java 2874 2009-12-11 16:03:27Z frank $
 */
public class FindMeLoopThread extends Thread {
    protected int iIndex = 0;

    boolean bStop = false;

    public FindMeLoopThread(int iStart) {
        this.setName(getClass().getSimpleName() + "-" + iStart);
        this.setPriority(Thread.MIN_PRIORITY);
        iIndex = iStart;
    }

    @Override
    public void run() {
        while (!bStop) {
            // Define array this way because Java kinda sucks sometimes
            // Assume that no spoken number for the current line index
            // exists, take generic sound file
            String[][] aFilesToPlay = { // epei2
            { Commons.FINDME_INTRO, Commons.FINDME_GENERIC, Commons.FINDME_LEFT },
                    { Commons.FINDME_INTRO, Commons.FINDME_GENERIC, Commons.FINDME_SILENCE, Commons.FINDME_RIGHT } };
            if (iIndex < Commons.A_FINDME_FILES.length) {
                // if the spoken soundfile exists, replace the array entries
                aFilesToPlay[0][1] = Commons.A_FINDME_FILES[iIndex]; // epei2
                aFilesToPlay[1][1] = Commons.A_FINDME_FILES[iIndex];
            }
            Commons.playSound(aFilesToPlay, iIndex);
            // increment index
            iIndex++;
            if (iIndex == LineListModel.aMixer.length) {
                iIndex = 0;
            }
            // Sleep, until playing should be finished
            // (it would be more elegent if I wait until it _is_ finished.)
            try {
                Thread.sleep(Commons.SLEEPFOR);
            } catch (InterruptedException ex) {
            }
        }
    }

    /** Sets a flag that will stop the thread at the next possible time */
    public void stopIt() {
        bStop = true;
    }

}
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback;

/**
 * updates the time displayed in the control frame, interval: 1 second
 * 
 * @author Ewald Peiszer
 * @version $Id: TimeUpdateThread.java 2874 2009-12-11 16:03:27Z frank $
 */

public class TimeUpdateThread extends Thread {

    private boolean bQuitLoop = false;

    private boolean bPaused = false;

    private long lPauseStarted;

    /** Duration of all pauses accumulated in millisecs */
    private long lPausesDuration = 0;

    public TimeUpdateThread() {
        this.setName(getClass().getSimpleName());
        this.setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
        while (!bQuitLoop) {
            if (Commons.cf != null && !bPaused) {
                Commons.cf.updateTime(lPausesDuration);
            }
            try {
                sleep(1000); // wait one second
            } catch (InterruptedException ex) {
            }
        }
    }

    public void stop_it() {
        bQuitLoop = true;
    }

    public void start_pause() {
        bPaused = true;
        lPauseStarted = System.currentTimeMillis();
    }

    public void end_pause() {
        bPaused = false;
        lPausesDuration += System.currentTimeMillis() - lPauseStarted;
    }

}
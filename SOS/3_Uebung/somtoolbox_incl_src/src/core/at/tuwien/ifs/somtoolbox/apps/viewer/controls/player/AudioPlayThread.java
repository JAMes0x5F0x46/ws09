/**
 * 
 */
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.player;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

/**
 * @author frank
 */
public abstract class AudioPlayThread extends Thread {

    private final PlayerListener listener;

    protected final AudioVectorMetaData song;
    
    /**
     * @param audioFile the audioFile that will be played.
     * @param someoneToInform someone to inform when the playing has ended.
     */
    public AudioPlayThread(AudioVectorMetaData song, PlayerListener someoneToInform) {
        listener = someoneToInform;
        this.song = song;
        this.setName(this.getClass().getSimpleName() + " (" + song.getID() + ")");
        // System.out.printf("Playing \"%s\" (%s)%n", title, audioFile.getAbsolutePath());
    }

    @Override
    public final void run() {
        this.setPriority(NORM_PRIORITY + ((MAX_PRIORITY - NORM_PRIORITY) / 2));
         System.out.printf("Playing \"%s\" (%s)%n", song.getDisplayLabel(), song.getAudioFile().getAbsolutePath());
        boolean playedToEnd = doPlaying();
        if (playedToEnd) {
            listener.playStopped(PlayerListener.STOP_REASON_ENDED, song);
        } else {
            listener.playStopped(PlayerListener.STOP_REASON_STOPPED, song);
        }
    }

    /**
     * Play the audio file. This method must block until replay is finished.
     * 
     * @return <c>true</c> iff the file was played to the end, <c>false</c> otherwise.
     */
    public abstract boolean doPlaying();

    /**
     * Interrupt/Stop the playing. The method {@link #doPlaying()} should return <c>false</c> in this case.
     */
    public abstract void stopPlaying();

}

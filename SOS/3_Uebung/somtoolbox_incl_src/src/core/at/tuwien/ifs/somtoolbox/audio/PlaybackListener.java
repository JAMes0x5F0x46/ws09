package at.tuwien.ifs.somtoolbox.audio;

/**
 * Interface to be implemented by e.g. UI elements wanting to monitor & display the status of a {@link PlaybackThread}.
 * 
 * @author Rudolf Mayer
 * @version $Id: PlaybackListener.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface PlaybackListener {

    public void updateStats(int songCount);

    public void setMutedSpeaker(int x, int y, boolean muted);

    public void setDecodingAt(int x, int y, String str);

    public void setSongAt(int x, int y, String str);
}

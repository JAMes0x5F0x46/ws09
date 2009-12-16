/**
 * 
 */
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

/**
 * @author frank
 */
public class MP3PlayThread extends AudioPlayThread {

    private Player player;

    /**
     * @param audioFile
     */
    public MP3PlayThread(AudioVectorMetaData toPlay, PlayerListener caller) {
        super(toPlay, caller);
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.AudioPlayThread#run()
     */
    @Override
    public boolean doPlaying() {
        try {
            player = new Player(new FileInputStream(song.getAudioFile()));
            if (player == null) {
                throw new Exception("Could not start player");
            }
            player.play();
        } catch (JavaLayerException e) {
            /*
             * This error happens with some mp3s. Since we can't go further, we played to the end.
             */
            if (e.getException() instanceof ArrayIndexOutOfBoundsException) {
                System.err.println("MP3-Player: ERROR in " + song.getID());
                return true;
            }
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        return player.isComplete();

    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.AudioPlayThread#stopPlaying()
     */
    @Override
    public void stopPlaying() {
        if (player != null)
            player.close();
    }

}

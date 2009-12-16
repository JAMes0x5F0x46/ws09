/**
 * 
 */
package at.tuwien.ifs.somtoolbox.apps.viewer.controls.player;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

/**
 * @author frank
 */
public class FlatAudioPlayThread extends AudioPlayThread {

    private static final int EXTERNAL_BUFFER_SIZE = 524288 / 16;

    private boolean stopPlaying;

    public FlatAudioPlayThread(AudioVectorMetaData song, PlayerListener caller) {
        super(song, caller);
        stopPlaying = false;
    }

    @Override
    public boolean doPlaying() {
        AudioInputStream inStream;

        File audioFile = song.getAudioFile();
        if (audioFile == null || !audioFile.exists() || !audioFile.isFile()) {
            System.err.println("No file given or file not found!");
            return true;
        }

        try {
            inStream = AudioSystem.getAudioInputStream(audioFile);
        } catch (UnsupportedAudioFileException e2) {
            e2.printStackTrace();
            return true;
        } catch (IOException e2) {
            e2.printStackTrace();
            return true;
        }

        AudioFormat format = inStream.getFormat();
        SourceDataLine auline = null;

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e1) {
            e1.printStackTrace();
            return true;
        }

        auline.start();

        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try {
            while (!stopPlaying && nBytesRead != -1) {
                nBytesRead = inStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                    auline.write(abData, 0, nBytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        } finally {
            auline.drain();
            auline.close();
        }
        return !stopPlaying;
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.player.AudioPlayThread#stopPlaying()
     */
    @Override
    public void stopPlaying() {
        stopPlaying = true;
    }

}

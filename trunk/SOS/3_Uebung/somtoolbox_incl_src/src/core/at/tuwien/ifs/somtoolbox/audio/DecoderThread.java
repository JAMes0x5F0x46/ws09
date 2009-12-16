package at.tuwien.ifs.somtoolbox.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Ewald Peiszer
 * @version $Id: DecoderThread.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DecoderThread extends Thread {
    /**
     * Static data structure not to decode a file twice at the same time.<br>
     * <br>
     * That means that all files that are currently being decoded have been put into the structure together with the DecoderThread as the value.
     */
    static protected LinkedHashMap<File, DecoderThread> currentlyDecoding = new LinkedHashMap<File, DecoderThread>();

    private File encodedFile;

    private File pcmFile;

    private int channel;

    private boolean stats;

    private PlaybackThread playbackThread;

    public DecoderThread(PlaybackThread playbackThread, File encodedFile, int channel, boolean stats, String decodedOutputDir, String decodedSuffix)
            throws FileNotFoundException {
        setName(this.getClass().getSimpleName() + " for " + playbackThread.getName());
        this.playbackThread = playbackThread;
        this.encodedFile = encodedFile;
        this.channel = channel;
        this.stats = stats;
        if (!encodedFile.exists()) {
            throw new FileNotFoundException();
        }
        this.pcmFile = new File(getDecodedFileName(encodedFile, decodedOutputDir, decodedSuffix));
        this.setPriority(Thread.MIN_PRIORITY);
    }

    public static String getDecodedFileName(File encodedFile, String decodedOutputDir, String decodedSuffix) {
        return decodedOutputDir + StringUtils.stripSuffix(encodedFile.getName()) + decodedSuffix;
    }

    public void run() {
        // is being currently processed?
        if (currentlyDecoding.containsKey(encodedFile)) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").finer("Already in process, waiting: " + encodedFile);
            // only wait for other thread to finish
            while (currentlyDecoding.containsKey(encodedFile)) {
                try {
                    sleep(10000);
                } catch (InterruptedException ex1) {
                }
            }
            // finished, give msg to nodethread
            playbackThread.decodingFinished(pcmFile, channel, stats, this);
        } else {

            // do it the normal way
            AudioInputStream ais = null;
            try {
                // Put into hashmap
                currentlyDecoding.put(encodedFile, this);

                ais = AudioSystem.getAudioInputStream(encodedFile);
                ais = AudioSystem.getAudioInputStream(Constants.DATALINE_FORMAT, ais);

                AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.PCM_SIGNED;
                AudioInputStream pcmAIS = AudioSystem.getAudioInputStream(targetEncoding, ais);
                AudioFileFormat.Type fileType = AudioFileFormat.Type.AU;

                AudioSystem.write(pcmAIS, fileType, pcmFile);
                Thread.sleep(300);
                // finished, give msg to nodethread
                playbackThread.decodingFinished(pcmFile, channel, stats, this);
            } catch (ThreadDeath td) {
                System.out.println("Da haben wir aber noch mal Glück gehabt");
                throw td;
            } catch (Exception ex) {
                ex.printStackTrace();
                playbackThread.decodingFailed(channel, stats);
                Logger.getLogger("at.tuwien.ifs.somtoolbox.multichannel").warning(ex.getMessage());
            } finally {
                currentlyDecoding.remove(encodedFile);
            }
        }
    }

    public File getPcmFile() {
        return pcmFile;
    }

    public File getEncodedFile() {
        return encodedFile;
    }

}

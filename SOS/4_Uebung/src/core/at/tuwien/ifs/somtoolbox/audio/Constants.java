package at.tuwien.ifs.somtoolbox.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * @author Ewald Peiszer
 * @version $Id: Constants.java 2874 2009-12-11 16:03:27Z frank $
 */
public class Constants {

    public static final boolean BIG_ENDIAN = false;

    public static final AudioFormat DATALINE_FORMAT = new AudioFormat(44100, 16, 2, true, Constants.BIG_ENDIAN);

    public static final DataLine.Info DATALINE_FORMAT_INFO = new DataLine.Info(SourceDataLine.class, DATALINE_FORMAT);

    public static final String DECODED_SUFFIX = " Decoded.wav";

    public static final AudioFormat MONO_FORMAT = new AudioFormat(44100, 16, 1, true, BIG_ENDIAN);

}

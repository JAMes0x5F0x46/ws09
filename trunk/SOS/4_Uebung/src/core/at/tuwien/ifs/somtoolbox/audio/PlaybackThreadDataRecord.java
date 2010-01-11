package at.tuwien.ifs.somtoolbox.audio;

import java.awt.Point;
import java.util.Arrays;
import java.util.Vector;

/**
 * Record type for data for one PlaybackThread.<br>
 * Contains:
 * <ul>
 * <li>position of the two speakers
 * <li>Lists of music files
 * </ul>
 * 
 * @author Ewald Peiszer
 * @version $Id: PlaybackThreadDataRecord.java 2874 2009-12-11 16:03:27Z frank $
 */

public class PlaybackThreadDataRecord {
    /**
     * Positions of speakers. Array is initialized at the time the object is created.<br>
     * However, both entries ([0] and [1]) are <code>null</code> until {@link PlaybackThreadDataRecord#setPosition(int, int, int)} is called.
     */
    public Point[] position = new Point[2];

    /**
     * Lists of songs to play. Array is initialized at the time the object is created.<br>
     * However, both entries ([0] and [1]) are <code>null</code> until {@link PlaybackThreadDataRecord#addSongs(int, String[])} is called.
     */
    public Vector<String>[] listOfSongs = new Vector[2];

    /**
     * Sets the position of one speaker
     * 
     * @return false if the position has already be set earlier, true otherwise
     */
    public boolean setPosition(int channel, int x, int y) {
        if (position[channel] == null) {
            position[channel] = new Point(x, y);
            return true;
        } else {
            position[channel].setLocation(x, y);
            return false;
        }
    }

    public boolean addSongs(int channel, String[] songNames) {
        if (listOfSongs[channel] == null) {
            listOfSongs[channel] = new Vector<String>();
            listOfSongs[channel].addAll(Arrays.asList(songNames));
            return true;
        } else {
            listOfSongs[channel].addAll(Arrays.asList(songNames));
            return false;
        }
    }

}
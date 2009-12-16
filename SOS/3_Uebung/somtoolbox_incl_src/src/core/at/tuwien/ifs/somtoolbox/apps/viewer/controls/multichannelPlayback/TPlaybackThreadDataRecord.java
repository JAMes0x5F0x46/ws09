package at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback;

import java.awt.Point;
import java.util.Arrays;
import java.util.Vector;

/**
 * Record type for data for one PlaybackThread. <br>
 * Contains:
 * <ul>
 * <li>position of the two speakers
 * <li>Lists of music files
 * </ul>
 * 
 * @author Ewald Peiszer
 * @version $Id: TPlaybackThreadDataRecord.java 2874 2009-12-11 16:03:27Z frank $
 */

public class TPlaybackThreadDataRecord {
    /**
     * <p>
     * Positions of speakers.
     * <p>
     * Array is initialized at the time the object is created
     * <p>
     * However, both entries ([0] and [1]) are <code>null</code> until <code>setPos</code> is called.
     */
    public Point[] pPos = new Point[2];

    /**
     * <p>
     * Lists of songs to play
     * <p>
     * Array is initialized at the time the object is created
     * <p>
     * However, both entries ([0] and [1]) are <code>null</code> until <code>addSongs</code> is called.
     */
    public Vector[] avMusic = new Vector[2];

    public TPlaybackThreadDataRecord() {
    }

    /**
     * Sets the position of one speaker
     * 
     * @return false if the position has already be set earlier, true otherwise
     */
    public boolean setPos(int iChannel, int x, int y) {
        if (pPos[iChannel] == null) {
            pPos[iChannel] = new Point(x, y);
            return true;
        } else {
            pPos[iChannel].setLocation(x, y);
            return false;
        }
    }

    public boolean addSongs(int iChannel, String[] aStrings) {
        if (avMusic[iChannel] == null) {
            avMusic[iChannel] = new Vector();
            avMusic[iChannel].addAll(Arrays.asList(aStrings));
            return true;
        } else {
            avMusic[iChannel].addAll(Arrays.asList(aStrings));
            return false;
        }
    }

}
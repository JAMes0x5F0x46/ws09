package at.tuwien.ifs.somtoolbox.apps.viewer.fileutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Add songs to a .m3u playlist and write the playlist file to disk
 * 
 * @author Robert Neumayer
 * @version $Id: PlayList.java 2874 2009-12-11 16:03:27Z frank $
 */
public class PlayList {

    private Vector<String> songs;

    private File file;

    public PlayList(String fileName) {
        super();
        this.songs = new Vector<String>();
        this.file = new File(fileName);
    }

    public PlayList(File file) {
        super();
        this.songs = new Vector<String>();
        this.file = file;
    }

    /**
     * adds a song to the playlist
     * 
     * @param song - song to add
     * @return - true if okay, false otherwise
     */
    public boolean addSong(String song) {
        return this.songs.add(song);
    }

    /**
     * set the contents of an object array as playlist
     * 
     * @param sar - object array to be set as playlist (and casted to strings)
     */
    public void setSongs(Object[] sar) {
        for (int i = 0; i < sar.length; i++) {
            this.songs.add((String) sar[i]);
            // System.out.println("adding song: " + sar[i]);
        }
    }

    /**
     * write this playlist to the file specified in the constructor
     */
    public void writeToFile() {
        // was used to restore the whitespaces in filenames
        // from now on only URL-encoded names are supported
        // this.restoreUnderscores();
        // System.out.println(this.songs.size());
        try {
            FileWriter fw = new FileWriter(this.file);
            for (int i = 0; i < this.songs.size(); i++) {
                fw.write((String) songs.elementAt(i) + "\n");
            }
            fw.close();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Exported file: " + this.file.getAbsolutePath() + " to disk");
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Could not initialize FileWriter for file: " + this.file);
            e.printStackTrace();
        }
    }
}

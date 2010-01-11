/*
 * Created on Apr 22, 2009
 * Version: $Id: PLAnlyser.java 2874 2009-12-11 16:03:27Z frank $
 */

package at.tuwien.ifs.somtoolbox.apps.analysis;

import java.util.List;

/**
 * @author frank
 * @version $Id: PLAnlyser.java 2874 2009-12-11 16:03:27Z frank $
 *
 */
public interface PLAnlyser {

    /**
     * Initialize the Analyzer. Load required data, ...
     * @param playlistAnalysis 
     */
    void init(PlaylistAnalysis playlistAnalysis);
    
    /**
     * Prepare to exit, close streams, ...
     */
    void finish();
    
    /**
     * Analyze the playlist
     * @param plName The playlist name
     * @param playList The playlist
     */
    void analyse(String plName, List<String> playList);
    
}

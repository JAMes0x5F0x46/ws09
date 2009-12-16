package at.tuwien.ifs.somtoolbox.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A simple filename filter that checks for the correct prefix of files.
 * 
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: PrefixFilenameFilter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class PrefixFilenameFilter implements FilenameFilter {

    private String prefix = "";

    public PrefixFilenameFilter(String prefix) {
        this.prefix = prefix;
    }

    public boolean accept(File dir, String name) {
        return name.startsWith(this.prefix);
    }

}

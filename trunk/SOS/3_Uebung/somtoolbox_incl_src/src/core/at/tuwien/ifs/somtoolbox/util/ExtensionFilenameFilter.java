package at.tuwien.ifs.somtoolbox.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A simple filename filter that checks for the correct extension of files.
 * 
 * @author mayer
 * @version $Id: ExtensionFilenameFilter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ExtensionFilenameFilter implements FilenameFilter {
    private String[] extension;

    public ExtensionFilenameFilter(String... extension) {
        this.extension = extension;
    }

    public boolean accept(File dir, String name) {
        for (int i = 0; i < extension.length; i++) {
            if (name.endsWith("." + extension[i])) {
                return true;
            }
        }
        return false;
    }
}
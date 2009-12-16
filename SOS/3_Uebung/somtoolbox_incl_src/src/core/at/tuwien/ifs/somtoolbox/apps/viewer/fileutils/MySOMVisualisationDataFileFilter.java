package at.tuwien.ifs.somtoolbox.apps.viewer.fileutils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;

/**
 * A generic file filter specialised on displaying correct descriptions and file extensions for all {@link SOMVisualisationData} types.
 * 
 * @author Rudolf Mayer
 * @version $Id: MySOMVisualisationDataFileFilter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MySOMVisualisationDataFileFilter extends FileFilter {
    private SOMVisualisationData data;

    public MySOMVisualisationDataFileFilter(SOMVisualisationData data) {
        this.data = data;
    }

    public boolean accept(File pathname) {
        for (int i = 0; i < data.getExtensions().length; i++) {
            if (pathname.isDirectory() || pathname.getName().endsWith(data.getExtensions()[i])
                    || pathname.getName().endsWith(data.getExtensions()[i] + ".gz")) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        String types = "";
        for (int i = 0; data.getExtensions() != null && i < data.getExtensions().length; i++) {
            if (types.length() > 0) {
                types += ", ";
            }
            if (data.getExtensions()[i] != null && data.getExtensions()[i].length() > 0) {
                types += "*." + data.getExtensions()[i] + ", " + "*." + data.getExtensions()[i] + ".gz";
            } else {
                types += "*";
            }
        }
        return data.getType() + " files (" + types + ")";
    }
}

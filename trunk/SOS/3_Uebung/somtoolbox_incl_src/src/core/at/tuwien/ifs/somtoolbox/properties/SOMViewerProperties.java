package at.tuwien.ifs.somtoolbox.properties;

import java.io.FileInputStream;
import java.util.Properties;

import at.tuwien.ifs.somtoolbox.visualization.Palettes;

/**
 * Properties for SOMViewer application.
 * 
 * @author Thomas Lidy
 * @version $Id: SOMViewerProperties.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMViewerProperties extends Properties {
    private static final long serialVersionUID = 1L;

    private String audioPlayer = null;

    private String palettesDir = Palettes.DEFAULT_PALETTES_DIR;

    private String htmlMapTemplatesDir = System.getProperty("user.dir") + "/rsc/html/map";

    /**
     * Loads the properties (or preferences) for the SOMViewer application and GUI.
     * 
     * @param fname name of the properties file.
     * @throws PropertiesException thrown if properties file could not be opened or the values of the properties are illegal.
     */
    public SOMViewerProperties(String fname) throws PropertiesException {
        try {
            load(new FileInputStream(fname));
        } catch (Exception e) {
            throw new PropertiesException("Could not open SOMViewer properties file " + fname);
        }
        try {

            if (getAudioPlayer() == null) {
                throw new PropertiesException("audioPlayer not set in " + fname);
            }

        } catch (NumberFormatException e) {
            throw new PropertiesException("Illegal numeric value in SOMViewer properties file.");
        }
    }

    /**
     * Creates an empty SOMViewerProperties object.
     */
    public SOMViewerProperties() {
    }

    /**
     * Returns the path name to call the preferred audio player to play audio files from PlaySOM Panel.
     * 
     * @return path name to audio player.
     */
    public String getAudioPlayer() {
        return getProperty("audioPlayer");
    }

    public String getPalettesDir() {
        return getProperty("palettes.dir", Palettes.DEFAULT_PALETTES_DIR);
    }

    public String getHtmlMapTemplatesDir() {
        return getProperty("html.template.dir", htmlMapTemplatesDir);
    }

}

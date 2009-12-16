package at.tuwien.ifs.somtoolbox.data.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;

public class AudioVectorMetaData extends AbstractVectorMetaData {

    /**
     * Check if the file exists (absolut or with pre- and suffix)
     * 
     * @see CommonSOMViewerStateData#fileNamePrefix
     * @see CommonSOMViewerStateData#fileNameSuffix
     * @warning For internal usage only!
     * @param filename The filename
     * @return the File
     * @throws FileNotFoundException if the file does not exist.
     */
    protected static File checkFileExistence(String filename) throws FileNotFoundException {
        File f = new File(filename);
        if (!f.exists()) {
            f = new File(CommonSOMViewerStateData.fileNamePrefix + filename + CommonSOMViewerStateData.fileNameSuffix);
        }
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }
        return f;
    }

    protected static String stripFileNamePreAndSuffix(String filename) {
        return filename.replace(CommonSOMViewerStateData.fileNamePrefix, "").replace(CommonSOMViewerStateData.fileNameSuffix, "");
    }

    private final String orig;

    private final File audioFile;

    public AudioVectorMetaData(File file, String id) {
        audioFile = file;
        orig = id;
    }

    /**
     * Get the file this MetaData is describing.
     * 
     * @return the audioFile
     */
    public File getAudioFile() {
        return audioFile;
    }

    @Override
    public String getID() {
        return stripFileNamePreAndSuffix(orig);
    }
    
    public static AudioVectorMetaData createMetaData(String id, File audioFile) throws FileNotFoundException {
        if (audioFile.getName().endsWith(".mp3")) {
            return new MP3VectorMetaData(audioFile, id);
        }
        return new AudioVectorMetaData(audioFile, id);
    }

    public static AudioVectorMetaData createMetaData(File audioFile) throws FileNotFoundException {
        if (audioFile.getName().endsWith(".mp3")) {
            return new MP3VectorMetaData(audioFile);
        }
        return new AudioVectorMetaData(audioFile, audioFile.getPath());
    }

    @Override
    public String getDisplayLabel() {
        return audioFile.getName();
    }

    @Override
    public boolean matches(Pattern pattern) {
        Matcher origMatcher = pattern.matcher(orig);
        Matcher pathMatcher = pattern.matcher(audioFile.getPath());
        return origMatcher.find() || pathMatcher.find();
    }

    @Override
    public boolean matches(String pattern, boolean ignoreCase) {
        String lOrig = ignoreCase ? orig.toLowerCase() : orig;
        String lPath = ignoreCase ? audioFile.getPath().toLowerCase() : audioFile.getPath();

        String[] qs = (ignoreCase ? pattern.toLowerCase() : pattern).split("\\s+");
        for (String q : qs) {
            if (!(lOrig.contains(q) || lPath.contains(q))) {
                return false;
            }
        }
        return true;
    }
}
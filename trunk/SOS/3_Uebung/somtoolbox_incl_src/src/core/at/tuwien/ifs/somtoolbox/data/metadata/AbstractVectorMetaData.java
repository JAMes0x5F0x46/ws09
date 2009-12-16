package at.tuwien.ifs.somtoolbox.data.metadata;

import java.io.FileNotFoundException;
import java.util.regex.Pattern;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;

public abstract class AbstractVectorMetaData {

    /**
     * Returns a String to display on screen.
     * 
     * @return
     */
    public abstract String getDisplayLabel();

    /**
     * Return the unique ID of the Vector. (The name/label it is identified by)
     * @return the ID-String
     */
    public abstract String getID();
    
    /**
     * Tests if the vectors MetaData matches the given Pattern.
     * 
     * @param pattern the Pattern to test.
     * @return <code>true</code> if the pattern matches.
     * @see #matches(String)
     */
    public abstract boolean matches(Pattern pattern);

    /**
     * Tests if the vector's MetaData matches the given pattern. It is the vectors metadata's decision whether it wants to be found by the given
     * Pattern or not and how to handle wildcards.
     * 
     * @param pattern The pattern to match.
     * @param ignoreCase set <code>true</code> to ignore case considerations.
     * @return <code>true</code> if the pattern matches.
     * @see #matches(Pattern)
     * @see #matches(String)
     */
    public abstract boolean matches(String pattern, boolean ignoreCase);

    /**
     * Tests if the vector's MetaData matches the given pattern. It is the vectors metadata's decision whether it wants to be found by the given
     * Pattern or not and how to handle wildcards.
     * 
     * @param pattern The pattern to match
     * @return <code>true</code> if the pattern matches.
     * @see #matches(Pattern)
     * @see #matches(String, boolean)
     */
    public boolean matches(String pattern) {
        return matches(pattern, true);
    }

    @Override
    public String toString() {
        return getID();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractVectorMetaData) {
            AbstractVectorMetaData vmd = (AbstractVectorMetaData) obj;
            return getID().equals(vmd.getID());
        }
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
    
    public static AbstractVectorMetaData createMetaData(String label) {
        if (label.endsWith(".mp3") || CommonSOMViewerStateData.fileNameSuffix.endsWith(".mp3")) {
            try {
                return new MP3VectorMetaData(label);
            } catch (FileNotFoundException e) {
                // NOP;
            }
        }
        return new EmtpyVectorMetaData(label);
    }

}
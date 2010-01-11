/*
 * Created on Feb 9, 2009
 * Version: $Id: EmtpyVectorMetaData.java 2874 2009-12-11 16:03:27Z frank $
 */

package at.tuwien.ifs.somtoolbox.data.metadata;

import java.util.regex.Pattern;

/**
 * This is an empty MetaData object. All operations just work in the "name" (the ID).
 * @author frank
 * @version $Id: EmtpyVectorMetaData.java 2874 2009-12-11 16:03:27Z frank $
 * @see #getID()
 */
public class EmtpyVectorMetaData extends AbstractVectorMetaData {

    private final String id;
    
    public EmtpyVectorMetaData(String id) {
        this.id = id;
    }
    
    @Override
    public boolean matches(String pattern, boolean ignoreCase) {
        String lP = ignoreCase? pattern.toLowerCase():pattern;
        String lID = ignoreCase? id.toLowerCase():id;
            return lID.contains(lP);
    }
    
    @Override
    public boolean matches(Pattern pattern) {
        return pattern.matcher(id).find();
    }
    
    @Override
    public String getDisplayLabel() {
        return id;
    }
    
    @Override
    public String getID() {
        return id;
    }

}

/*
 * Created on Dec 1, 2009 Version: $Id: $
 */

package at.tuwien.ifs.somtoolbox.apps;

import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.martiansoftware.jsap.Parameter;

/**
 * Marker interface for all SOMToolbox Applications. Add this interface to each class conatining a main-method that is part of the "production"
 * framework. <strong>Attention!</strong> Classes implementing this Interface <b>must</b> contain a <code>main</code> method, and the following
 * members:
 * <ul>
 * <li><code>public static String DESCRIPTION</code></li>
 * <li><code>public static String LONG_DESCRIPTION</code></li>
 * <li><code>public static {@link Parameter}[] OPTIONS</code></li>
 * </ul>
 * 
 * @author Jakob Frank
 * @version $Id: $
 */
public interface SOMToolboxApp {

    public static final String VERSION = "0.7.4";

    @SuppressWarnings("unchecked")
    public static final Map<String, Class<?>> REQUIRED_MEMBERS = ArrayUtils.toMap(new Object[][] { { "DESCRIPTION", String.class },
            { "LONG_DESCRIPTION", String.class }, { "OPTIONS", Parameter[].class } });

    public static final String DEV_BY_STRING = "Developed by the IR Team at IFS (Vienna University of Technology)";

    public static final String HOMEPAGE = "http://olymp.ifs.tuwien.ac.at/somtoolbox/";
}

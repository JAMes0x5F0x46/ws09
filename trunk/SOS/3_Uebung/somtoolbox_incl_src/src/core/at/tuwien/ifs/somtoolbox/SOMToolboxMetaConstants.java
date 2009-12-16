package at.tuwien.ifs.somtoolbox;

import java.io.File;

public interface SOMToolboxMetaConstants {

    public static final File USER_CONFIG_DIR = new File(System.getProperty("user.home"), ".somtoolbox");

    public static final File USER_SOMVIEWER_PREFS = new File(USER_CONFIG_DIR, "somviewerrc");
}

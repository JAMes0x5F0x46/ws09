package at.tuwien.ifs.somtoolbox.apps.initEval;

import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Stefan Bischof
 * @author Leo Sklenitzka
 * @version $Id: LayerInitializer.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface LayerInitializer {
    /**
     * Initialize the SOM Layer
     * 
     * @return initialized SOM
     */
    Unit[][][] initialize();
}

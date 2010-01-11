package at.tuwien.ifs.somtoolbox.models;

import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;

/**
 * A GHSOMLayer represents all GrowingLayers of a Level.
 * 
 * @author Simon Tragatschnig
 */
public class GHSOMHierarchyRoot extends GHSOMLevelLayer {

    // ausgehend von root-knoten gibt es fuer jede unit im root-layer - untergeordnete layer, welche als ein level dargestellt werden
    public GHSOMHierarchyRoot(GrowingLayer root) {
        super(root);
    }

    /**
     * returns the levelLayer of <code>level</code>
     * 
     * @param level
     * @return
     */
    public GHSOMLevelLayer getLevel(int level) {
        GHSOMLevelLayer layer = this;
        for (int i = 0; i < level; i++) {
            layer = layer.getChildren();
        }
        return layer;
    }

}

package at.tuwien.ifs.somtoolbox.models;

import java.util.Vector;

import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * represents all layers of one level
 * 
 * @author Simon Tragatschnig
 */
public class GHSOMLevelLayer {

    /** the number of layers in the hierarchy * */
    private static int depth = 0;

    public static int getDepth() {
        return depth;
    }

    public static void setDepth(int depth) {
        GHSOMLevelLayer.depth = depth;
    }

    private GHSOMLevelLayer children = null;

    private int level;

    private Vector<GrowingLayer> levelLayer = new Vector<GrowingLayer>();

    private GHSOMLevelLayer root;

    /**
     * the constructor for the root-level
     */
    protected GHSOMLevelLayer(GrowingLayer root) {
        Vector<GrowingLayer> rootLayer = new Vector<GrowingLayer>();
        rootLayer.add(root);
        levelLayer = rootLayer;
        children = getSubLevelLayer(root);
        this.level = 0;
        this.root = this;
    }

    /**
     * the constructor for any other level
     * 
     * @param layer
     * @param level
     */
    protected GHSOMLevelLayer(Vector<GrowingLayer> layer, int level) {
        levelLayer = layer;
        this.level = level;
    }

    /**
     * creates the children-layer of a levelLayer
     * 
     * @param levelLayer
     * @return
     */
    private GHSOMLevelLayer getChildLevel(GHSOMLevelLayer levelLayer) {
        Vector<GrowingLayer> childrenLayer = new Vector<GrowingLayer>();

        for (GrowingLayer singleLayer : levelLayer.getLevelLayer()) {
            GHSOMLevelLayer layer = getSubLevelLayer(singleLayer);
            childrenLayer.addAll(layer.getLevelLayer());
        }

        GHSOMLevelLayer childlevellayer = new GHSOMLevelLayer(childrenLayer, this.getLevel() + 1);
        if (GHSOMLevelLayer.depth < this.getLevel() + 1)
            GHSOMLevelLayer.depth = this.getLevel() + 1;

        return childlevellayer;
    }

    /**
     * returns the childrenLevelLayer of this levelLayer or null, if no children exist
     * 
     * @return
     */
    public GHSOMLevelLayer getChildren() {
        if (children.levelLayer.size() > 0) {
            return children;
        }
        return null;
    }

    public int getLevel() {
        return level;
    }

    public Vector<GrowingLayer> getLevelLayer() {
        return levelLayer;
    }

    public GHSOMLevelLayer getRoot() {
        return root;
    }

    /**
     * returns the levelLayer of a layer - all sub-layers of any units will be added
     * 
     * @param layer
     * @return
     */
    private GHSOMLevelLayer getSubLevelLayer(GrowingLayer layer) {
        Vector<GrowingLayer> sublevel = new Vector<GrowingLayer>();
        // step through each unit and check, if there exists a sublevel
        for (int i = 0; i < layer.getUnits().length; i++) {
            for (int j = 0; j < layer.getUnits()[i].length; j++) {
                try {
                    Unit u = layer.getUnit(i, j);
                    if (u != null) {
                        if (u.getMappedSOM() != null) {
                            sublevel.add(layer.getUnit(i, j).getMappedSOM().getLayer());
                        }
                    }
                } catch (LayerAccessException e) {
                    // will not happen!
                    e.printStackTrace();
                }
            }
        }

        GHSOMLevelLayer sublevellayer = new GHSOMLevelLayer(sublevel, layer.getLevel() + 1);
        sublevellayer.setChildren(getChildLevel(sublevellayer));

        return sublevellayer;
    }

    public boolean hasChildren() {
        if (children != null && children.levelLayer.size() > 0) {
            return true;
        }
        return false;
    }

    public void setChildren(GHSOMLevelLayer children) {
        this.children = children;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLevelLayer(Vector<GrowingLayer> levelLayer) {
        this.levelLayer = levelLayer;
    }

    public void setRoot(GHSOMLevelLayer root) {
        this.root = root;
    }

}

package at.tuwien.ifs.somtoolbox.visualization;

/**
 * Represents a specific instance of a background image visualizer, i.e. the visualizer class and the used variant.
 * 
 * @author Rudolf Mayer
 * @version $Id: BackgroundImageVisualizerInstance.java 2874 2009-12-11 16:03:27Z frank $
 */
public class BackgroundImageVisualizerInstance {
    private BackgroundImageVisualizer vis;

    private int variant;

    private String displayName;

    public BackgroundImageVisualizer getVis() {
        return vis;
    }

    public int getVariant() {
        return variant;
    }

    public BackgroundImageVisualizerInstance(BackgroundImageVisualizer vis, int variant) {
        super();
        this.vis = vis;
        this.variant = variant;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public String getName() {
        return vis.getVisualizationName(variant);
    }

    public String getShortName() {
        return vis.getVisualizationShortName(variant);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "Visualisation: '" + vis.getVisualizationName(variant) + "', displayed as '" + displayName + ",";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BackgroundImageVisualizerInstance)) {
            return false;
        }
        return ((BackgroundImageVisualizerInstance) obj).vis.equals(vis) && ((BackgroundImageVisualizerInstance) obj).variant == variant;
    }
}

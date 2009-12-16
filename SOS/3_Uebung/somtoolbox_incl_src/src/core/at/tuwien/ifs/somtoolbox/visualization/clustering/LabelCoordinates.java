package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.awt.Point;

import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * @author Doris Baum
 * @version $Id: LabelCoordinates.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LabelCoordinates extends Point {
    private static final long serialVersionUID = 1L;

    public String label;

    public LabelCoordinates() {
        super();
        label = "";
    }

    public LabelCoordinates(int x, int y, String label) {
        super(x, y);
        this.label = label;
    }

    /**
     * Calculate euclidean distance between this point and the other point
     * 
     * @param other
     */
    public double distance(LabelCoordinates other) throws MetricException {
        // simple distance between two points
        return Math.sqrt(((x - other.x) * (x - other.x)) + ((y - other.y) * (y - other.y)));
    }
}

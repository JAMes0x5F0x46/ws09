package at.tuwien.ifs.somtoolbox.structures;

import java.awt.geom.Point2D;

/**
 * 2D version of a component line
 * 
 * @author neumayer
 * @version $Id: ComponentLine2D.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ComponentLine2D extends ElementWithIndex {

    private Point2D[] points;

    public ComponentLine2D(Point2D[] points) {
        super(-1);
        this.points = points;
    }

    public ComponentLine2D(Point2D[] points, Integer index) {
        super(index);
        this.points = points;
    }

    public Point2D get(int index) {
        return points[index];
    }

    public Point2D[] getPoints() {
        return points;
    }

    public int getLength() {
        return points.length;
    }

}
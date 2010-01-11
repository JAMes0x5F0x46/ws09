package at.tuwien.ifs.somtoolbox.structures;

import at.tuwien.ifs.somtoolbox.util.Point3d;

/**
 * We desperately needed a 3D version of this.
 * 
 * @author neumayer
 * @version $Id: ComponentLine3D.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ComponentLine3D extends ElementWithIndex {

    private Point3d[] points;

    public ComponentLine3D(Point3d[] points) {
        super(-1);
        this.points = points;
    }

    public ComponentLine3D(Point3d[] points, Integer index) {
        super(index);
        this.points = points;
    }

    public Point3d get(int index) {
        return points[index];
    }

    public Point3d[] getPoints() {
        return points;
    }

    public int getLength() {
        return points.length;
    }

}

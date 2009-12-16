package at.tuwien.ifs.somtoolbox.util;

import java.awt.Point;

/**
 * A representation of a 3-dimensional point, similar to {@link Point}, but using double (or float ({@link Float}) precision.
 * 
 * @author Rudolf Mayer
 * @version $Id: Point3D.java 2781 2009-11-30 16:42:09Z mayer $
 */
public class Point3d {

    public double x;

    public double y;

    public double z;

    public Point3d() {
        this(0, 0, 0);
    }

    public Point3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    /** Returns the Euclidean distance between this point and the other. */
    public double distance(Point3d other) {
        double distX = this.x - other.x;
        double distY = this.y - other.y;
        double distZ = this.z - other.z;
        return Math.sqrt(distX * distX + distY * distY + distZ * distZ);
    }

    @Override
    public Object clone() {
        return new Point3d(x, y, z);
    }

}

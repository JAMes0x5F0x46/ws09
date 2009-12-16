package at.tuwien.ifs.somtoolbox.util;

/**
 * Represents a cuboid with integer coordinates.
 * 
 * @author Rudolf Mayer
 * @version $Id: Cuboid.java 2874 2009-12-11 16:03:27Z frank $
 */
public class Cuboid {

    int startX;

    int endX;

    int startY;

    int endY;

    int startZ;

    int endZ;

    public Cuboid(int startX, int endX, int startY, int endY, int startZ, int endZ) {
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
        this.startZ = startZ;
        this.endZ = endZ;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public int getEndZ() {
        return endZ;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getStartZ() {
        return startZ;
    }

    @Override
    public String toString() {
        return startX + "/" + startY + "/" + startZ + " - " + endX + "/" + endY + "/" + endZ;
    }

}

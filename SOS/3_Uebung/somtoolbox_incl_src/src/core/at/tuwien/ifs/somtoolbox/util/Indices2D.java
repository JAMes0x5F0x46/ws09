package at.tuwien.ifs.somtoolbox.util;

/**
 * @author Rudolf Mayer
 * @version $Id: Indices2D.java 2874 2009-12-11 16:03:27Z frank $
 */
public class Indices2D {
    public int startX;

    public int startY;

    public int endX;

    public int endY;

    public Indices2D(int startX, int startY) {
        this(startX, startY, 0, 0);
    }

    public Indices2D(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public void setEnd(int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public String toString() {
        return startX + "/" + startY + " - " + endX + "/" + endY;
    }

}

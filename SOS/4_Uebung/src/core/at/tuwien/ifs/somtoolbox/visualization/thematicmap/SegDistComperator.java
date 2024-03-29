package at.tuwien.ifs.somtoolbox.visualization.thematicmap;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Comparator;

/**
 * @author Taha Abdel Aziz
 * @version $Id: SegDistComperator.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SegDistComperator implements Comparator<Grid> {
    Segment segment;

    Line2D line;

    Point2D pnt1;

    Point2D pnt2;

    double weight1;

    double weight2;

    /** Creates a new instance of SegDistComperator */
    public SegDistComperator(Segment segment, double weight1, double weight2) {
        this.segment = segment;
        this.weight1 = weight1;
        this.weight2 = weight2;
        line = new Line2D.Double(segment.end1.coord(0), segment.end1.coord(1), segment.end2.coord(0), segment.end2.coord(1));
        pnt1 = line.getP1();
        pnt2 = line.getP2();
    }

    public int compare(Grid grid1, Grid grid2) {
        Point2D p1 = new Point2D.Double(grid1.coord(0), grid1.coord(1));
        Point2D p2 = new Point2D.Double(grid2.coord(0), grid2.coord(1));

        double d_line1 = line.ptSegDist(p1);
        double d_line2 = line.ptSegDist(p2);

        double d_w1 = p1.distance(pnt1) * weight1 * weight1 + p1.distance(pnt2) * weight2 * weight2;
        double d_w2 = p2.distance(pnt1) * weight1 * weight1 + p2.distance(pnt2) * weight2 * weight2;

        double d1 = d_line1 + d_w1;
        double d2 = d_line2 + d_w2;

        if (d1 > d2) {
            return 1;
        } else if (d1 < d2) {
            return -1;
        }
        return 0;
    }
}

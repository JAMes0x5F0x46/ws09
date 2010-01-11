package at.tuwien.ifs.somtoolbox.visualization.thematicmap;

import edu.cornell.cs.voronoi.Pnt;

/**
 * 
 * @author Taha Abdel Aziz
 * @version $Id: Grid.java 2874 2009-12-11 16:03:27Z frank $
 * 
 */
public class Grid extends Pnt {
    public Pnt topLeft;

    public Pnt bottomRight;

    public Pnt center;

    public boolean occupied;

    public static final double SIZE = 1.0;

    public SOMClass clss;

    /** Creates a new instance of Grid */
    public Grid(Pnt _topLeft, Pnt _bottomRight) {
        super(_topLeft.coord(0) / 2 + _bottomRight.coord(0) / 2, _topLeft.coord(1) / 2 + _bottomRight.coord(1) / 2);
        this.topLeft = _topLeft;
        this.bottomRight = _bottomRight;
    }

    /** Creates a new instance of Grid */
    public Grid(Pnt _topLeft) {
        super(_topLeft.coord(0) / 2 + SIZE / 2, _topLeft.coord(1) / 2 + SIZE / 2);
        this.topLeft = _topLeft;
        this.bottomRight = new Pnt(_topLeft.coord(0) / 2 + SIZE, _topLeft.coord(1) / 2 + SIZE);
    }

}

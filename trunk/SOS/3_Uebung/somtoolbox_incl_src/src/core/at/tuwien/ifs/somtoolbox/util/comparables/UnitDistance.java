package at.tuwien.ifs.somtoolbox.util.comparables;

import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * A Comparable for distances between units.
 * 
 * @author Rudolf Mayer
 * @version $Id: UnitDistance.java 2874 2009-12-11 16:03:27Z frank $
 */
public class UnitDistance implements Comparable<UnitDistance> {
    private double distance;

    private Unit unit;

    public UnitDistance(Unit unit, double distance) {
        this.distance = distance;
        this.unit = unit;
    }

    public int compareTo(UnitDistance o) {
        return Double.compare(distance, o.distance);
    }

    public double getDistance() {
        return distance;
    }

    public Unit getUnit() {
        return unit;
    }
}

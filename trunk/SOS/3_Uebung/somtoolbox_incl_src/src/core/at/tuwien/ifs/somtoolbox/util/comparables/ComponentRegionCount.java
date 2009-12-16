package at.tuwien.ifs.somtoolbox.util.comparables;

/**
 * This class can be used to compare two regions of a metro map visualisation.
 * 
 * @author Rudolf Mayer
 * @version $Id: ComponentRegionCount.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ComponentRegionCount implements Comparable<ComponentRegionCount> {
    private int numberOfRegion;

    private Integer index;

    public ComponentRegionCount(int distance, Integer index) {
        this.numberOfRegion = distance;
        this.index = index;
    }

    public int compareTo(ComponentRegionCount other) {
        if (numberOfRegion != other.numberOfRegion) {
            return Double.compare(numberOfRegion, other.numberOfRegion);
        } else {
            return index.compareTo(other.index);
        }
    }

    public Integer getIndex() {
        return index;
    }

    public int getNumberOfRegion() {
        return numberOfRegion;
    }

    public double getFactor(int numberOfBins) {
        return (double) numberOfRegion / (double) numberOfBins;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ComponentRegionCount)) {
            return false;
        }
        return (((ComponentRegionCount) obj).numberOfRegion == numberOfRegion && ((ComponentRegionCount) obj).index.equals(index));
    }

}

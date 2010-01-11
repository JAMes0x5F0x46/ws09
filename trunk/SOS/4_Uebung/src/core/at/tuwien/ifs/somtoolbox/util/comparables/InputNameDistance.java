package at.tuwien.ifs.somtoolbox.util.comparables;

/**
 * This class can be used to compare two input names by their distance. Used for ordering distances in various places.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputNameDistance.java 2874 2009-12-11 16:03:27Z frank $
 */
public class InputNameDistance implements Comparable<InputNameDistance> {
    private double distance;

    private String label;

    public InputNameDistance(double distance, String input) {
        this.distance = distance;
        this.label = input;
    }

    public int compareTo(InputNameDistance otherInput) {
        if (distance != otherInput.distance) {
            return Double.compare(distance, otherInput.distance);
        } else {
            return label.compareTo(otherInput.label);
        }
    }

    public String getLabel() {
        return label;
    }

    public double getDistance() {
        return distance;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InputNameDistance)) {
            return false;
        }
        return (((InputNameDistance) obj).distance == distance && ((InputNameDistance) obj).label.equals(label));
    }

}

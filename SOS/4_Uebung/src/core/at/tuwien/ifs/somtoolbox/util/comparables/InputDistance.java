package at.tuwien.ifs.somtoolbox.util.comparables;

import at.tuwien.ifs.somtoolbox.data.InputDatum;

/**
 * This class can be used to compare two InputDatum objects by their distance. Used for ordering distances in various places.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputDistance.java 2874 2009-12-11 16:03:27Z frank $
 */
public class InputDistance implements Comparable<InputDistance> {
    private double distance;

    private InputDatum input;

    public InputDistance(double distance, InputDatum input) {
        this.distance = distance;
        this.input = input;
    }

    public int compareTo(InputDistance otherInput) {
        if (distance != otherInput.distance) {
            return Double.compare(distance, otherInput.distance);
        } else {
            return input.getLabel().compareTo(otherInput.getInput().getLabel());
        }
    }

    public InputDatum getInput() {
        return input;
    }

    public double getDistance() {
        return distance;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InputDistance)) {
            return false;
        }
        return (((InputDistance) obj).distance == distance && ((InputDistance) obj).input.equals(input));
    }

}

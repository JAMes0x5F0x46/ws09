package at.tuwien.ifs.somtoolbox.structures;

/**
 * @author mayer
 * @version $Id: DoubleVector2D.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DoubleVector2D extends ElementWithIndex {
    double[] vector;

    public DoubleVector2D(double[] vector) {
        this(vector, -1);
    }

    public DoubleVector2D(double[] vector, int index) {
        super(index);
        this.vector = vector;
    }

    public DoubleVector2D(double[] vector, int index, String label) {
        this(vector, index);
        this.label = label;
    }

    public double get(int index) {
        return vector[index];
    }

    public double[] getPoints() {
        return vector;
    }

    public int getLength() {
        return vector.length;
    }

}

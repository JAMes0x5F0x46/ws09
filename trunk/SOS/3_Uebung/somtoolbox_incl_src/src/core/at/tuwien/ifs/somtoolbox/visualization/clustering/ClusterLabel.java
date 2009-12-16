package at.tuwien.ifs.somtoolbox.visualization.clustering;

import at.tuwien.ifs.somtoolbox.layers.Label;

/**
 * Extends the Label class with an additional value for sorting to determine the order of the labels. The natural order of this class is by ascending
 * 'sortingValue'. If two ClusterLabels have equal sortingValue, they are compared by their names.
 * 
 * @author Angela Roiger
 * @version $Id: ClusterLabel.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClusterLabel extends Label implements Comparable<ClusterLabel> {
    private static final long serialVersionUID = 1L;

    private double sortingValue = 0.0d;

    public ClusterLabel(Label l, double sortingValue) {
        super(l.getName(), l.getValue(), l.getQe());
        this.sortingValue = sortingValue;
    }

    public ClusterLabel(String name) {
        super(name);
    }

    public ClusterLabel(String name, double value) {
        super(name, value);
    }

    public ClusterLabel(String name, double value, double qe) {
        super(name, value, qe);
    }

    /**
     * Constructs a ClusterLabel object with the given arguments.
     * 
     * @param name the name of the label.
     * @param value the label value.
     * @param qe the quantization error of the label.
     * @param sortingValue the value determining the order of labels
     */
    public ClusterLabel(String name, double value, double qe, double sortingValue) {
        super(name, value, qe);
        this.sortingValue = sortingValue;
    }

    public int compareTo(ClusterLabel c) {
        int comp = Double.compare(this.sortingValue, c.getSortingValue());
        if (comp != 0) {
            return comp;
        } else {
            // if values are equal compare the name
            return this.getName().compareTo(c.getName());
        }
    }

    public double getSortingValue() {
        return this.sortingValue;
    }

    public void setSortingValue(double d) {
        this.sortingValue = d;
    }
}

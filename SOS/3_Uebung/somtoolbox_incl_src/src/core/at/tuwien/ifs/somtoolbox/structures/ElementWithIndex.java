package at.tuwien.ifs.somtoolbox.structures;

public abstract class ElementWithIndex {

    protected int index;

    protected String label;

    protected ElementWithIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        if (label != null) {
            return label;
        } else {
            return "Element-" + index;
        }
    }

    public String getLabel() {
        return label;
    }

}

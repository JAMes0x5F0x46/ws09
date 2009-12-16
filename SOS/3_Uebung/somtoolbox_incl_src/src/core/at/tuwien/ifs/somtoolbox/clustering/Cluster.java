package at.tuwien.ifs.somtoolbox.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cluster<E> implements Iterable<E> {
    public static final String CONTENT_SEPARATOR_CHAR = "  |  ";

    protected List<E> data;

    protected String label;

    protected Cluster() {
        this.data = new ArrayList<E>();
    }

    public Cluster(E datum) {
        this(datum, null);
    }

    public Cluster(E datum, String label) {
        this.data = new ArrayList<E>();
        data.add(datum);
        this.label = label;
    }

    public Cluster(List<E> data) {
        this(data, null);
    }

    public Cluster(List<E> data, String label) {
        this.data = data;
        this.label = label;
    }

    public E get(int index) {
        return data.get(index);
    }

    public List<E> getData() {
        return data;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }

    public int size() {
        return data.size();
    }

    public String contentToString() {
        StringBuilder sb = new StringBuilder();
        for (E element : data) {
            if (sb.length() > 0) {
                sb.append(CONTENT_SEPARATOR_CHAR);
            }
            sb.append(element.toString());
        }
        return sb.toString();
    }

}

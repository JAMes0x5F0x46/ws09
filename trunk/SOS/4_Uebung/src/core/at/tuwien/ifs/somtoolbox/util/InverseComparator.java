package at.tuwien.ifs.somtoolbox.util;

import java.util.Comparator;

/**
 * @author Rudolf Mayer
 * @version $Id: InverseComparator.java 2874 2009-12-11 16:03:27Z frank $
 * @param <T>
 */
public class InverseComparator<T extends Comparable<T>> implements Comparator<T> {
    public int compare(T o1, T o2) {
        return o2.compareTo(o1);
    }
}
package at.tuwien.ifs.somtoolbox.util;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

/**
 * Comparator for two Strings, with special Integer comparison if these Strings are actually Integer values.
 * 
 * @author Rudolf Mayer
 * @version $Id: StringIntegerComparator.java 2874 2009-12-11 16:03:27Z frank $
 */
public class StringIntegerComparator implements Comparator<String> {
    boolean allIntegers = false;

    boolean checkedValues = false;

    public StringIntegerComparator() {
        allIntegers = false;
        checkedValues = false;
    }

    public StringIntegerComparator(String[] classNames) {
        allIntegers = true;
        for (String string : classNames) {
            if (!StringUtils.isNumeric(string.trim())) {
                allIntegers = false;
                break;
            }
        }
        checkedValues = true;
    }

    @Override
    public int compare(String o1, String o2) {
        if ((checkedValues && allIntegers) || // we checked before that we have only ints
                (!checkedValues && StringUtils.isNumeric(o1.trim()) && StringUtils.isNumeric(o2.trim()))) { // no checking, but both in
            return Integer.valueOf(o1.trim()).compareTo(Integer.valueOf(o2.trim())); // => do integer comparison
        } else { // otherwise => string comparison
            return o1.compareTo(o2);
        }
    }
}

package at.tuwien.ifs.somtoolbox.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Utility methods related to collections.
 * 
 * @author Rudolf Mayer
 * @version $Id: CollectionUtils.java 2894 2009-12-12 03:02:33Z mayer $
 */
public class CollectionUtils {

    /** Returns two ArrayLists, containing the elements only present in the first and second argument, respectively. */
    @SuppressWarnings("unchecked")
    public static ArrayList<String>[] getUniqueElements(Collection<? extends String> list1, Collection<? extends String> list2) {
        final ArrayList<String> onlyInOne = new ArrayList<String>(list1);
        onlyInOne.removeAll(list2);
        Collections.sort(onlyInOne);

        final ArrayList<String> onlyInTwo = new ArrayList<String>(list2);
        onlyInTwo.removeAll(list1);
        Collections.sort(onlyInTwo);
        return new ArrayList[] { onlyInOne, onlyInTwo };
    }

    public static ArrayList<String>[] getUniqueElements(String[] array1, String[] array2) {
        return getUniqueElements(Arrays.asList(array1), Arrays.asList(array2));
    }

    public static HashSet<String> getOrCreateValue(Map<String, HashSet<String>> map, String key) {
        if (map.get(key) == null) {
            map.put(key, new HashSet<String>());
        }
        return map.get(key);
    }

    public static HashMap<String, Integer> getOrCreateValue(HashMap<Integer, HashMap<String, Integer>> map, Integer key) {
        if (map.get(key) == null) {
            map.put(key, new HashMap<String, Integer>());
        }
        return map.get(key);
    }

    public static int indexOf(ArrayList<String> list, String value) {
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i);
            if (value.equals(name)) {
                return i;
            }
        }
        return -1;
    }
}

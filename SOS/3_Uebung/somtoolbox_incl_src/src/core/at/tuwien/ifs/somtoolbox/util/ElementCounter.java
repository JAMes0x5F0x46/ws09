package at.tuwien.ifs.somtoolbox.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

/**
 * FIXME: this is a copy from valhalla, merge back into IFS-commons!
 * 
 * @author Rudolf Mayer
 * @version $Id: ElementCounter.java 2874 2009-12-11 16:03:27Z frank $
 * @param <T>
 */
public class ElementCounter<T extends Comparable<T>> {
    private HashMap<T, Integer> map = new HashMap<T, Integer>();

    public void incCount(T key) {
        map.put(key, getCount(key) + 1);
    }

    public int getCount(T key) {
        if (map.get(key) == null) {
            return 0;
        } else {
            return map.get(key);
        }
    }

    public Set<T> keySet() {
        return map.keySet();
    }

    public ArrayList<T> keyList() {
        return new ArrayList<T>(map.keySet());
    }

    public ArrayList<T> keyList(int minCount) {
        ArrayList<T> result = new ArrayList<T>();
        for (Entry<T, Integer> entry : entrySet()) {
            if (entry.getValue() >= minCount) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Set<Entry<T, Integer>> entrySet() {
        return map.entrySet();
    }

    public Set<Entry<T, Integer>> entrySet(int minCount) {
        Set<Entry<T, Integer>> result = new HashSet<Entry<T, Integer>>();
        for (Entry<T, Integer> entry : entrySet()) {
            if (entry.getValue() >= minCount) {
                result.add(entry);
            }
        }
        return result;
    }

    public Collection<Integer> values() {
        return map.values();
    }

    public int totalCount() {
        int totalCount = 0;
        for (Integer count : map.values()) {
            totalCount += count;
        }
        return totalCount;
    }

    public int size() {
        return map.size();
    }

    @Override
    public String toString() {
        return toString(10);
    }

    public String toString(int width) {
        long sum = 0;
        StringBuilder sb = new StringBuilder();
        ArrayList<T> keyList = keyList();
        Collections.sort(keyList);
        int rows = keyList.size() / width;
        if (keyList.size() % width > 0) {
            rows++;
        }
        for (int i = 0; i < rows; i++) {
            for (int j = i * width; j < (i + 1) * width && j < keyList.size(); j++) {
                sb.append(keyList.get(j) + "\t");
            }
            sb.append("\n");
            for (int j = i * width; j < (i + 1) * width && j < keyList.size(); j++) {
                T key = keyList.get(j);
                Integer value = map.get(key);
                sb.append(value + "\t");
                sum += value;
            }
            sb.append("\n\n");
        }
        sb.append("Total: ").append(sum).append("\n\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        ElementCounter<Integer> counter = new ElementCounter<Integer>();
        int n = 100;
        for (int i = 0; i < n * 100; i++) {
            int x = (int) (Math.random() * 100) + 1;
            counter.incCount(x);
        }
        System.out.println(counter.toString(13));
    }

}

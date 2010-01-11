package at.tuwien.ifs.somtoolbox.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A least-recently-used LRU cache, based on {@link LinkedHashMap}. This cache can hold a fixed maximum number of elements; if a new element is added,
 * and the cache is full, the least recently used entry is removed.
 * 
 * @author Rudolf Mayer
 * @version $Id: LeastRecentlyUsedCache.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LeastRecentlyUsedCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;

    private int cacheSize;

    /**
     * Creates a new least-recently-used cache.
     * 
     * @param size the maximum number of entries that will be kept in this cache.
     */
    public LeastRecentlyUsedCache(int size) {
        // need to invoke constructor with all arguments, as there is no way to otherwise set LinkedHashMap.accessOrder to true
        super(((int) Math.ceil(size / 0.75f) + 1), 0.75f, true);
        this.cacheSize = size;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > LeastRecentlyUsedCache.this.cacheSize;
    }

}

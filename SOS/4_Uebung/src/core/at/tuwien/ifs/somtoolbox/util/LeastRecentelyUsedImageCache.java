package at.tuwien.ifs.somtoolbox.util;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A least-recently-used LRU cache, based on {@link LinkedHashMap}. This cache can hold a fixed a number of {@link BufferedImage} elements, until the
 * specified memory limit is reached. If a new element is added, and the cache is full, the least recently used entry is removed.<br/>
 * 
 * @author Rudolf Mayer
 * @version $Id: LeastRecentlyUsedCache.java,v 1.1 2009-02-06 01:36:59 mayer Exp $
 */
public final class LeastRecentelyUsedImageCache extends LinkedHashMap<String, BufferedImage> {
    private static final long serialVersionUID = 1L;

    private long maxCacheSize;

    private String maxCacheSizeReadable;

    public LeastRecentelyUsedImageCache(long maxCacheSize) {
        super();
        this.maxCacheSize = maxCacheSize;
        maxCacheSizeReadable = StringUtils.readableBytes(maxCacheSize);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Initialised visualisation image cache with " + maxCacheSizeReadable);
    }

    protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
        long totalSize = 0;
        Collection<BufferedImage> values = values();
        for (BufferedImage bufferedImage : values) {
            long imageSize = bufferedImage.getColorModel().getPixelSize() * bufferedImage.getHeight() * bufferedImage.getWidth();
            totalSize += imageSize;
            if (totalSize > maxCacheSize) {
                break;
            }
        }
        if (totalSize > maxCacheSize) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Occupied cache size of " + StringUtils.readableBytes(totalSize) + " exceeds max cache size of " + maxCacheSizeReadable
                            + " - removing eldest entry.");
            return true;
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").fine(
                    "Current cache size of " + StringUtils.readableBytes(totalSize) + " is " + (int) ((totalSize / (double) maxCacheSize) * 100)
                            + "% of max cache (" + maxCacheSizeReadable + ")");
            return false;
        }
    }
}
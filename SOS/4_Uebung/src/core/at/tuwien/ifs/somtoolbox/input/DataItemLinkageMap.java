package at.tuwien.ifs.somtoolbox.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * This input object represents a mapping between input items. E.g. for an email corpus the mapping reply->original mail would be saved.
 * 
 * @author Rudolf Mayer
 * @version $Id: DataItemLinkageMap.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DataItemLinkageMap extends Hashtable<String, String> {
    private static final long serialVersionUID = 1L;

    public DataItemLinkageMap(String fileName) throws IOException {
        BufferedReader reader = FileUtils.openFile(SOMVisualisationData.LINKAGE_MAP, fileName);
        String line = null;
        int lineCount = 0;
        while ((line = reader.readLine()) != null) {
            lineCount++;
            String[] items = line.split(" ");
            if (items == null || items.length < 2 || (items.length == 2 && items[1] == null)) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Line #" + lineCount + "oes not contain 2 elements: '" + line + "'. Aborting.");
            } else {
                put(items[0].trim(), items[1].trim());
            }
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Linkage file read, loaded " + size() + " mappings.");
    }

}

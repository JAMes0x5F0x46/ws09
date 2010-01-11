package at.tuwien.ifs.somtoolbox.clustering;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rudolf Mayer
 * @version $Id: ClusteringTools.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClusteringTools {
    public static List<Cluster<?>> toCluster(List<List<?>> data) {
        ArrayList<Cluster<?>> list2 = new ArrayList<Cluster<?>>(data.size());
        for (int i = 0; i < data.size(); i++) {
            list2.add(new Cluster(data.get(i), "Cluster " + i));
        }
        return list2;
    }

}

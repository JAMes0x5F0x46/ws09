package at.tuwien.ifs.somtoolbox.util;

import java.io.IOException;
import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * Compares the contents of two template vectors.
 * 
 * @author Rudolf Mayer
 * @version $Id: TemplateVectorComparator.java 2874 2009-12-11 16:03:27Z frank $
 */
public class TemplateVectorComparator  implements SOMToolboxApp {

    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, new UnflaggedOption("templateVectorFile", JSAP.STRING_PARSER, true,
                "First template vector file."), new UnflaggedOption("templateVectorFile2", JSAP.STRING_PARSER, true, "Second template vector file."));
        SOMLibTemplateVector tv1 = new SOMLibTemplateVector(config.getString("templateVectorFile"));
        SOMLibTemplateVector tv2 = new SOMLibTemplateVector(config.getString("templateVectorFile2"));
        ArrayList<String>[] uniqueElements = CollectionUtils.getUniqueElements(tv1.getLabelsAsList(), tv2.getLabelsAsList());
        printTerms(uniqueElements[0], tv1, 1, config.getString("templateVectorFile"));
        printTerms(uniqueElements[1], tv2, 2, config.getString("templateVectorFile2"));
    }

    private static void printTerms(final ArrayList<String> onlyInOne, SOMLibTemplateVector tv1, int i, String title) {
        System.out.println("\n==============================================================");
        System.out.println("Terms only in vector " + i + " (" + onlyInOne.size() + ", " + title + ")");
        for (String s : onlyInOne) {
            System.out.println("\t" + tv1.getElement(s));
        }
    }

}

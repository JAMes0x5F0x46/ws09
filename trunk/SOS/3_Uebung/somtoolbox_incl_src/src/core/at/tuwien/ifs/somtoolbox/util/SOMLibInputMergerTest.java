package at.tuwien.ifs.somtoolbox.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;

/**
 * Testing file for {@link SOMLibInputMerger}.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMLibInputMergerTest.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMLibInputMergerTest {
    public static void main(String[] args) throws SOMLibFileFormatException, URISyntaxException, IOException {
        File classPath = new File(SOMLibInputMergerTest.class.getResource("/").toURI());
        File mapDir = new File(classPath, "../doc/datasets/");
        String animals = mapDir.getCanonicalPath() + "/animals";
        String zoo = mapDir.getCanonicalPath() + "/zoo";
        System.out.println(animals);
        System.out.println(zoo);
        String[] args2 = { "/tmp/merged", zoo, animals };
        SOMLibInputMerger.main(args2);
        String[] args3 = { "--mode", "intersection", "/tmp/merged", zoo, animals };
        SOMLibInputMerger.main(args3);
    }

}

package at.tuwien.ifs.somtoolbox.visualization.thematicmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 
 * @author Taha Abdel Aziz
 * @version $Id: RndIndexGenerator.java 2874 2009-12-11 16:03:27Z frank $
 * 
 */
public class RndIndexGenerator {
    double[] scale;

    ArrayList<SOMClass> classes;

    /** Creates a new instance of IdexGenerator */
    public RndIndexGenerator(ArrayList<SOMClass> classes) {
        this.classes = classes;
        Collections.sort(classes, new ClassComperator());
        scale = new double[classes.size() + 1];
        double x = 0;
        for (int i = 1; i <= classes.size(); i++) {
            SOMClass c = (SOMClass) classes.get(i - 1);
            scale[i] = x + c.share;
            x = scale[i];
        }
    }

    public SOMClass getNextIndex() {
        double rnd = Math.random();
        for (int i = 1; i <= scale.length; i++) {
            double d0 = scale[i - 1];
            double d1 = scale[i];
            if (rnd > d0 && rnd <= d1) {
                return (SOMClass) classes.get(i - 1);
            }
        }
        return null;
    }

    class ClassComperator implements Comparator<SOMClass> {

        public int compare(SOMClass c1, SOMClass c2) {
            if (c1.share > c2.share) {
                return 1;
            } else if (c1.share < c2.share) {
                return -1;
            } else {
                return 0;
            }
        }
    }

}

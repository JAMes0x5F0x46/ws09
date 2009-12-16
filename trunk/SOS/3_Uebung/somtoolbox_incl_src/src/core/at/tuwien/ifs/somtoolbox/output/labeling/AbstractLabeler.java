package at.tuwien.ifs.somtoolbox.output.labeling;

/**
 * This class provides basic functionality all Labelers can use. Classes providing labelling algortihm implementations are advised to extend this
 * class.
 * 
 * @author Michael Dittenbach
 * @version $Id: AbstractLabeler.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractLabeler implements Labeler {

    public static Labeler instantiate(String lName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (!lName.startsWith("at.tuwien.ifs.somtoolbox.output.labeling.")) {
            lName = "at.tuwien.ifs.somtoolbox.output.labeling." + lName;
        }
        Labeler labeler = null;
        labeler = (Labeler) Class.forName(lName).newInstance();
        return labeler;
    }

}

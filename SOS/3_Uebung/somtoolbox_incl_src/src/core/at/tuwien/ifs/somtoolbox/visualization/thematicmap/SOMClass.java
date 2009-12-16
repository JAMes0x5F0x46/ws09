package at.tuwien.ifs.somtoolbox.visualization.thematicmap;

/**
 * 
 * @author Taha Abdel Aziz
 * @version $Id: SOMClass.java 2874 2009-12-11 16:03:27Z frank $
 * 
 */
public class SOMClass {
    public int classIndex;

    public boolean finished;

    public double hits;

    public int relationNum;

    public double relationWeight;

    public double share;

    // public ArrayList polygons=new ArrayList();
    // public int order;

    /** Creates a new instance of SOMClass */
    public SOMClass(int classIndex, double hits) {
        this.classIndex = classIndex;
        this.hits = hits;
    }

}

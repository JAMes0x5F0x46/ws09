package at.tuwien.ifs.somtoolbox.apps.server;

/**
 * @author Rudolf Mayer
 * @version $Id: HTMLMapInformation.java 2874 2009-12-11 16:03:27Z frank $
 */
public class HTMLMapInformation {
    private String imageMap;

    private String imagePath;

    private String[] nNearest;

    public HTMLMapInformation(String imagePath, String imageMap, String[] nNearest) {
        this.imageMap = imageMap;
        this.imagePath = imagePath;
        this.nNearest = nNearest;
    }

    public HTMLMapInformation(String imagePath, String imageMap) {
        this(imagePath, imageMap, null);
    }

    public String getImageMap() {
        return imageMap;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String[] getNNearest() {
        return nNearest;
    }

}

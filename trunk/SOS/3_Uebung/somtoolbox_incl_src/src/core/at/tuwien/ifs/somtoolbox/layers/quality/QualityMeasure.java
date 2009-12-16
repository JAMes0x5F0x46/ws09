package at.tuwien.ifs.somtoolbox.layers.quality;

/**
 * All quality measure algorithm implementations should implement this interface.
 * 
 * @author Michael Dittenbach
 * @version $Id: QualityMeasure.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface QualityMeasure {

    public double getMapQuality(String name) throws QualityMeasureNotFoundException;

    public String[] getMapQualityDescriptions();

    public String[] getMapQualityNames();

    public double[][] getUnitQualities(String name) throws QualityMeasureNotFoundException;

    public String[] getUnitQualityDescriptions();

    public String[] getUnitQualityNames();

}

package at.tuwien.ifs.somtoolbox.properties;

import java.io.FileInputStream;
import java.util.logging.Logger;

/**
 * Properties for GHSOM training.
 * 
 * @author Michael Dittenbach
 * @version $Id: GHSOMProperties.java 2874 2009-12-11 16:03:27Z frank $
 */
public class GHSOMProperties extends SOMProperties {
    private static final long serialVersionUID = 1L;

    private boolean reIndex = false;

    private double tau2 = 1;

    private String expandQualityMeasureName = null;

    /**
     * Loads and encapsulated properties for the GHSOM training process.
     * 
     * @param fname Name of the properties file.
     */
    public GHSOMProperties(String fname) throws PropertiesException {
        super(fname);
        try {
            load(new FileInputStream(fname));
        } catch (Exception e) {
            throw new PropertiesException("Could not open properties file " + fname);
        }
        try {
            tau2 = Double.parseDouble(getProperty("tau2", "1"));
            if (tau2 == 1) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("tau2 = 1 implies single flat layer");
            } else if ((tau2 <= 0) || (tau2 > 1)) {
                throw new PropertiesException("Tau2 less than or equal zero or greater than 1.");
            }
            expandQualityMeasureName = getProperty("expandQualityMeasureName");
            if (expandQualityMeasureName == null) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("No expandQualityMeasureName given. Defaulting to QuantizationError.qe.");
                expandQualityMeasureName = "at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError.qe";
            } else {
                expandQualityMeasureName = "at.tuwien.ifs.somtoolbox.layers.quality." + expandQualityMeasureName;
            }

            reIndex = Boolean.valueOf(getProperty("reIndex", "false")).booleanValue();
        } catch (NumberFormatException e) {
            throw new PropertiesException("Illegal numeric value in properties file.");
        }

    }

    /**
     * Not used at the moment.
     * 
     * @return Returns the reIndex.
     */
    public boolean reIndex() {
        return reIndex;
    }

    /**
     * Returns tau2 determining the maximum data representation granularity.
     * 
     * @return tau2 determining the maximum data representation granularity.
     */
    public double tau2() {
        return tau2;
    }

    /**
     * Returns the name of the used quality measure.
     * 
     * @return the name of the used quality measure.
     */
    public String expandQualityMeasureName() {
        return expandQualityMeasureName;
    }

}

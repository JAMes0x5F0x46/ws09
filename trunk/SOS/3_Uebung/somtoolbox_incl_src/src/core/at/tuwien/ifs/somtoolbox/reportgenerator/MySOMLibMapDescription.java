package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * It's strange, but when I tried to use this file it wasn't able to deal with SOMLib map files. I had to make some changes to get it to work A class
 * handling SOMLib map files.
 * 
 * @author Rudolf Mayer - edited by Sebastian Skritek
 * @version $Id: MySOMLibMapDescription.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MySOMLibMapDescription {
    public MySOMLibMapDescription(String fileName) throws IOException {
        readMapDescriptionFile(fileName);
    }

    private static final String keyType = "$TYPE";

    private static final String keyTopology = "$TOPOLOGY";

    private static final String keyXDim = "$XDIM";

    private static final String keyYDim = "$YDIM";

    private static final String keyVecDim = "$VEC_DIM";

    private static final String keyStorageDate = "$STORAGE_DATE";

    private static final String keyTrainingTime = "$TRAINING_TIME";

    private static final String keyLearnrateType = "$LEARNRATE_TYPE";

    private static final String keyLearnRateInit = "$LEARNRATE_INIT";

    private static final String keyNeighbourhoodType = "$NEIGHBORHOOD_TYPE";

    private static final String keyNeighbourhoodInit = "$NEIGHBORHOOD_INIT";

    private static final String keyRandomInit = "$RAND_INIT";

    private static final String keyTotalIterations = "$ITERATIONS_TOTAL";

    private static final String keyTotalTrainingVectors = "$NR_TRAINVEC_TOTAL";

    private static final String keyVectorsNormailised = "$VEC_NORMALIZED";

    private static final String keyQuantErrMap = "$QUANTERROR_MAP";

    private static final String keyQuantErrVector = "$QUANTERROR_VEC";

    private static final String keyUrlTrainingVector = "$URL_TRAINING_VEC";

    private static final String keyUrlTrainingVectorDescription = "$URL_TRAINING_VEC_DESCR";

    private static final String keyUrlWeightVector = "$URL_WEIGHT_VEC";

    private static final String keyUrlQuantErrMap = "$URL_QUANTERR_MAP";

    private static final String keyUrlMappedInputVector = "$URL_MAPPED_INPUT_VEC";

    private static final String keyUrlMappedInputVectorDescription = "$URL_MAPPED_INPUT_VEC_DESCR";

    private static final String keyUrlUnitDescription = "$URL_UNIT_DESCR";

    private static final String keyMetric = "$METRIC";

    private static final String keyLayerRevision = "$LAYER_REVISION";

    private static final String keyDescription = "$DESCRIPTION";

    Properties prop = new Properties();

    private static final String[] propertyNames = { keyType, keyTopology, keyXDim, keyYDim, keyVecDim, keyStorageDate, keyTrainingTime,
            keyLearnrateType, keyLearnRateInit, keyNeighbourhoodType, keyNeighbourhoodInit, keyRandomInit, keyTotalIterations,
            keyTotalTrainingVectors, keyVectorsNormailised, keyQuantErrMap, keyQuantErrVector, keyUrlTrainingVector, keyUrlTrainingVectorDescription,
            keyUrlWeightVector, keyUrlQuantErrMap, keyUrlMappedInputVector, keyUrlMappedInputVectorDescription, keyUrlUnitDescription, keyMetric,
            keyLayerRevision, keyDescription };

    private static final String[] integerFields = { keyXDim, keyYDim, keyVecDim, keyRandomInit, keyTotalIterations, keyTotalTrainingVectors };

    private static final String[] doubleFields = { keyLearnRateInit, keyNeighbourhoodInit };

    private static final String[] longFields = { keyTrainingTime };

    private static final String[] dateFields = { keyStorageDate };

    private static final String[] booleanFields = { keyVectorsNormailised };

    /**
     * this function has been changed compared to SOMLibMapDescription.java changed way how the inputfile is read, and weakend the warnings in case of
     * an error.
     * 
     * @param fileName
     * @throws IOException
     */
    public void readMapDescriptionFile(String fileName) throws IOException {
        prop = new Properties();
        BufferedReader reader = FileUtils.openFile("", fileName);
        String line = null;
        while ((line = reader.readLine()) != null) {
            // System.out.println(line);
            // System.out.flush();
            line = line.trim();
            if (!(line.equals("") || line.startsWith("#"))) {
                String[] property = line.split(" ", 2);
                if (property.length >= 2) {
                    prop.setProperty(property[0], property[1]);
                } else {
                    prop.setProperty(property[0], "");
                }
            }
        }
        reader.close();

        // check for well-formed
        for (String key : integerFields) {
            try {
                Integer.parseInt(prop.getProperty(key));
            } catch (NumberFormatException e) {
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                // "Error reading map file: not an <Integer> for value '" + key + "': " + prop.getProperty(key));
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Error reading map file: not an <Integer> for value '" + key + "': " + prop.getProperty(key));
            }
        }
        for (String key : doubleFields) {
            try {
                Double.parseDouble(prop.getProperty(key));
            } catch (NumberFormatException e) {
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                // "Error reading map file: not a <Double> for value '" + key + "': " + prop.getProperty(key));
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Error reading map file: not a <Double> for value '" + key + "': " + prop.getProperty(key));
            }
        }
        for (String key : longFields) {
            try {
                Long.parseLong(prop.getProperty(key));
            } catch (NumberFormatException e) {
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error reading map file: not a <Long> for value '" + key + "': " +
                // prop.getProperty(key));
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Error reading map file: not a <Long> for value '" + key + "': " + prop.getProperty(key));
            }
        }
        for (String key : booleanFields) {
            if (!prop.getProperty(key).equalsIgnoreCase("true") || !prop.getProperty(key).equalsIgnoreCase("false")) {
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                // "Error reading map file: not a <Boolean> for value '" + key + "': " + prop.getProperty(key));
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Error reading map file: not a <Boolean> for value '" + key + "': " + prop.getProperty(key));
            }
        }
        for (String key : dateFields) {
            try {
                new SimpleDateFormat().parse(prop.getProperty(key));
            } catch (ParseException e) {
                // Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error reading map file: not a <Date> for value '" + key + "': " +
                // prop.getProperty(key));
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Error reading map file: not a <Date> for value '" + key + "': " + prop.getProperty(key));
            }
        }

    }

    public Object getProperty(String property) {
        return prop.get(property);
    }

    public void writeMapDescriptionFile(String fileName) throws IOException {
        FileWriter w = new FileWriter(fileName);
        for (String propertyName : propertyNames) {
            w.write(propertyName + "=" + prop.getProperty(propertyName) + "\n");
        }
        w.close();
    }

    public static void main(String[] args) {
        try {
            new MySOMLibMapDescription(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package at.tuwien.ifs.somtoolbox.models;

import java.util.Date;

import at.tuwien.ifs.somtoolbox.data.DataBaseSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.database.MySQLConnector;
import at.tuwien.ifs.somtoolbox.properties.FileProperties;
import at.tuwien.ifs.somtoolbox.util.DateUtils;

/**
 * This class provides basic support for implementing a NetworkModel.
 * 
 * @author Rudolf Mayer
 * @version $Id: AbstractNetworkModel.java 2874 2009-12-11 16:03:27Z frank $
 */
public class AbstractNetworkModel implements NetworkModel {

    // FIXME: this value should be bigger if we use kaski/gate labels
    public static final int DEFAULT_LABEL_COUNT = 5;

    protected Date trainingStart = new Date();

    /** whether or not the SOM is labelled */
    protected boolean labelled = false;

    protected static InputData getInputData(FileProperties fileProps) {
        InputData data;
        if (fileProps.isUsingDatabase()) {// Invoke database driven SOMLib input reader
            MySQLConnector dbConnector = new MySQLConnector(fileProps.getDatabaseServerAddress(), fileProps.getDatabaseName(),
                    fileProps.getDatabaseUser(), fileProps.getDatabasePassword(), fileProps.getDatabaseTableNamePrefix());
            data = new DataBaseSOMLibSparseInputData(dbConnector, fileProps.sparseData(), fileProps.isNormalized(), fileProps.numCacheBlocks(),
                    fileProps.randomSeed());
        } else {// Invoke regular SOMLib input reader
            data = SOMLibSparseInputData.create(fileProps.vectorFileName(true), fileProps.templateFileName(true), fileProps.sparseData(),
                    fileProps.isNormalized(), fileProps.numCacheBlocks(), fileProps.randomSeed());
        }
        return data;
    }

    protected String printTrainingTime() {
        return DateUtils.formatDuration(new Date().getTime() - trainingStart.getTime());
    }

    public void setSharedInputObjects(SharedSOMVisualisationData sharedInputObjects) {
        this.sharedInputObjects = sharedInputObjects;
    }

    public SharedSOMVisualisationData getSharedInputObjects() {
        return sharedInputObjects;
    }

    protected SharedSOMVisualisationData sharedInputObjects;

    public boolean isLabelled() {
        return labelled;
    }

    public void setLabelled(boolean labelled) {
        this.labelled = labelled;
    }

}

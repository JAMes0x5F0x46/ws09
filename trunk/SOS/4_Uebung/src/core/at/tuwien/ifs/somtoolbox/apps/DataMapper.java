package at.tuwien.ifs.somtoolbox.apps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.output.labeling.AbstractLabeler;
import at.tuwien.ifs.somtoolbox.output.labeling.Labeler;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Maps inputs to an already trained SOM.
 * 
 * @author Angela Roiger
 * @author Rudolf Mayer
 * @version $Id: DataMapper.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DataMapper  implements SOMToolboxApp {

    public static void main(String[] args) throws FileNotFoundException, IOException, SOMToolboxException {
        new DataMapper(args);
    }

    public DataMapper(String[] args) throws FileNotFoundException, IOException, SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_DATA_MAPPER);
        String mapDescFileName = config.getString("mapDescriptionFile");
        String weightVectorFileName = config.getString("weightVectorFile");
        String classInformationFileName = config.getString("classInformationFile");
        String skipClassesString = config.getString("classList");
        boolean skipDataWinnerMapping = config.getBoolean("skipDataWinnerMapping", false);
        int numDataWinners = config.getInt("numberWinners");
        String labelerName = config.getString("labeling", null);
        int numLabels = config.getInt("numberLabels", AbstractNetworkModel.DEFAULT_LABEL_COUNT);

        ArrayList<String> mappingExceptions = new ArrayList<String>();
        if (StringUtils.isNotBlank(skipClassesString)) {
            String[] tmp = (skipClassesString.split(","));
            for (int i = 0; i < tmp.length; i++) {
                mappingExceptions.add(tmp[i]);
            }
        }

        GrowingSOM som = null;
        /* restore SOM */
        try {
            som = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFileName, null, mapDescFileName));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            e.printStackTrace();
            System.exit(-1);
        }

        SOMLibClassInformation classInfo = null;
        if (classInformationFileName != null) {
            try {
                classInfo = new SOMLibClassInformation(classInformationFileName);
            } catch (SOMToolboxException e1) {
                e1.printStackTrace();
            }
        }

        /* get input vectors */
        InputData data = new SOMLibSparseInputData(config.getString("inputVectorFile"));
        mapCompleteDataAfterTraining(som, data, classInfo, mappingExceptions, labelerName, numLabels);

        final String name = FileUtils.extractSOMLibInputPrefix(FileUtils.stripPathPrefix(weightVectorFileName)) + ".remapped";
        try {
            SOMLibMapOutputter.writeUnitDescriptionFile(som, "", name, true);
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not open or write to output file " + name + ": " + e.getMessage());
            System.exit(-1);
        }
        if (!skipDataWinnerMapping) {
            int numWinners = Math.min(numDataWinners, som.getLayer().getXSize() * som.getLayer().getYSize());
            try {
                SOMLibMapOutputter.writeDataWinnerMappingFile(som, data, numWinners, "", name, true);
            } catch (IOException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not open or write to output file " + name + ": " + e.getMessage());
                System.exit(-1);
            }
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Skipping writing data winner mapping file");
        }
        // just copy along the class information file, so we have a copy with the same name-prefix, eases SOMViewer starting..
        if (classInformationFileName != null) {
            String classInfoDestination = name + ".cls" + (classInformationFileName.endsWith(".gz") ? ".gz" : "");
            FileUtils.copyFile(classInformationFileName, classInfoDestination);
        }
    }

    // FIXME: this is just a copy of GrowingLayer#mapCompleteDataAfterTraining, would be good to have some code re-used..
    // FIXME: this would also profit from multi-threading...
    private void mapCompleteDataAfterTraining(GrowingSOM som, InputData data, SOMLibClassInformation classInfo, ArrayList<String> mappingExceptions,
            String labelerName, int numLabels) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start mapping data.");
        InputDatum datum = null;
        Unit winner = null;
        int numVectors = data.numVectors();

        int skippedInstances = 0;
        for (int i = 0; i < data.numVectors(); i++) {
            try {
                InputDatum currentInput = data.getInputDatum(i);
                String inpLabel = currentInput.getLabel();
                if (classInfo != null && mappingExceptions.contains(classInfo.getClassName(inpLabel))) {
                    skippedInstances++;
                }
            } catch (SOMLibFileFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (mappingExceptions.size() > 0) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    "Skipping classes: " + mappingExceptions + ", containing a total of " + skippedInstances + " inputs.");
        }

        StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors - skippedInstances, "Mapping datum ", 50);
        for (int i = 0; i < numVectors; i++) {
            datum = data.getInputDatum(i);

            String inpLabel = datum.getLabel();
            try {
                if (classInfo != null && mappingExceptions.contains(classInfo.getClassName(inpLabel))) {
                    continue; // Skips this mapping step
                } else {
                    winner = som.getLayer().getWinner(datum, new L2Metric());
                    winner.addMappedInput(datum, false); // TODO: think about recursion
                    progressWriter.progress();
                }
            } catch (SOMLibFileFormatException e) {
                // TODO Auto-generated catch block
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("This should never happen");
                e.printStackTrace();
            }
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished mapping data.");
        som.getLayer().calculateQuantizationErrorForUnits();
        som.getLayer().clearLabels();

        Labeler labeler = null;

        if (labelerName != null) { // if labeling then label
            try {
                labeler = AbstractLabeler.instantiate(labelerName);
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Instantiated labeler " + labelerName);
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not instantiate labeler \"" + labelerName + "\".");
                System.exit(-1);
            }
        }

        if (labelerName != null) { // if labeling then label
            labeler.label(som, data, numLabels);
        }
    }

}

package at.tuwien.ifs.somtoolbox.apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.SOMMerger;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

/**
 * Maps inputs on a merged SOM. The merged SOM is trained from the weight vectors of two or more initial SOMs, e.g. by using {@link SOMMerger}. The
 * input vectors mapped to the initial SOMs will be put on the units in the merged SOM on which the weight-vector initially representing them is
 * merged.
 * 
 * @author Rudolf Mayer
 * @version $Id: MergedSOMDataMapper.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MergedSOMDataMapper {
    private static final Logger logger = Logger.getLogger("at.tuwien.ifs.somtoolbox");

    public static void main(String[] args) throws IOException, SOMToolboxException {
        new MergedSOMDataMapper(args);
    }

    public MergedSOMDataMapper(String[] args) throws IOException, SOMToolboxException {
        // register and parse all options
        // Labelling is an optional step, and requires input and template vectors
        JSAPResult config = OptionFactory.parseResults(args, new Parameter[] { OptionFactory.getOptWeightVectorFile(true),
                OptionFactory.getOptUnitDescriptionFile(true), OptionFactory.getOptInputVectorFile(false), OptionFactory.getOptNumberWinners(false),
                OptionFactory.getSwitchSkipDataWinnerMapping(), OptionFactory.getOptClassInformationFile(false),
                OptionFactory.getOptProperties(true), OptionFactory.getOptSOMLibMaps(true) });

        String unitDescFileName = config.getString("unitDescriptionFile");
        String weightVectorFileName = config.getString("weightVectorFile");
        String classInformationFile = config.getString("classInformationFile");
        String[] originalMaps = config.getStringArray("maps");

        boolean skipDataWinnerMapping = config.getBoolean("skipDataWinnerMapping", false);
        int numDataWinners = config.getInt("numberWinners");

        GrowingSOM som = null;
        /* restore SOM */
        try {
            som = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFileName, unitDescFileName, null));
        } catch (Exception e) {
            logger.severe(e.getMessage() + " Aborting.");
            e.printStackTrace();
            System.exit(-1);
        }

        ArrayList<String> originalMappedUnitvectorNames = som.getLayer().getAllMappedDataNamesAsList();

        for (int i = 0; i < originalMaps.length; i++) {
            String orignalMapPrefix = FileUtils.extractSOMLibInputPrefix(FileUtils.stripPathPrefix(originalMaps[i]));
            GrowingSOM originalSOM = new GrowingSOM(new SOMLibFormatInputReader(null, originalMaps[i], null));
            final GrowingLayer originalMap = originalSOM.getLayer();
            for (Unit originalMapUnit : originalMap.getAllUnits()) {
                // find the unit where this weight vector is mapped to
                final String unitInputName = orignalMapPrefix + "_" + originalMapUnit.printCoordinates();
                final Unit unitForDatum = som.getLayer().getUnitForDatum(unitInputName);

                // replace the mapping of the unit's weight vector with the inputs it represents
                if (originalMapUnit.getNumberOfMappedInputs() > 0) {
                    for (String string : originalMapUnit.getMappedInputNames()) {
                        unitForDatum.addMappedInput(new InputDatum(string, new double[som.getLayer().getDim()]), false);
                    }
                }
                // remove this weight vector from the mapped unit files
                unitForDatum.removeMappedInput(unitInputName);
            }
        }

        ArrayList<String> finalMappedVectorNames = som.getLayer().getAllMappedDataNamesAsList();

        originalMappedUnitvectorNames.retainAll(finalMappedVectorNames);
        logger.info("New unit file contains " + finalMappedVectorNames.size() + " vectors.");
        if (originalMappedUnitvectorNames.size() > 0) {
            logger.warning("Could not replace " + originalMappedUnitvectorNames.size() + " unit weight vectors: " + originalMappedUnitvectorNames);
        }

        final String outfilePrefix = FileUtils.extractSOMLibInputPrefix(FileUtils.stripPathPrefix(weightVectorFileName)) + ".mergeMapped";
        try {
            SOMLibMapOutputter.writeUnitDescriptionFile(som, "", outfilePrefix, true);
        } catch (IOException e) { // TODO: create new exception type
            logger.severe("Could not open or write to output file " + outfilePrefix + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        if (!skipDataWinnerMapping) {
            int numWinners = Math.min(numDataWinners, som.getLayer().getXSize() * som.getLayer().getYSize());
            try {
                InputData data = new SOMLibSparseInputData(config.getString("inputVectorFile"));
                SOMLibMapOutputter.writeDataWinnerMappingFile(som, data, numWinners, "", outfilePrefix, true);
            } catch (IOException e) {
                logger.severe("Could not open or write to output file " + outfilePrefix + ": " + e.getMessage());
                System.exit(-1);
            }
        } else {
            logger.info("Skipping writing data winner mapping file");
        }
        // just copy along the class information file, so we have a copy with the same name-prefix, eases SOMViewer starting..
        if (classInformationFile != null) {
            String classInfoDestination = outfilePrefix + ".cls" + (classInformationFile.endsWith(".gz") ? ".gz" : "");
            FileUtils.copyFile(classInformationFile, classInfoDestination);
        }
    }
}

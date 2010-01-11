package at.tuwien.ifs.somtoolbox.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

/**
 * Converts a SOM map to an input vector file.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMMerger.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMMerger  implements SOMToolboxApp {
    public static void main(String[] args) throws SOMLibFileFormatException, IOException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptMergeMode(), new Switch("skipConversion", JSAP.NO_SHORTFLAG,
                "skipConversion", "Skip conversion of map files to input vector files, if you already did that before."), new FlaggedOption(
                "mapSize", JSAP.STRING_PARSER, null, false, JSAP.NO_SHORTFLAG, "mapSize",
                "The size of the map to be used to write the properties files, e.g. 4x5.\n"
                        + "If not specified, a default map size will be computed, depending on the number of input vectors."),
                OptionFactory.getOptInputDirectory(false), OptionFactory.getOptOutputFileName(true), OptionFactory.getOptSOMLibMaps(false));

        String[] inputs = FileUtils.findAllSOMLibFiles(config, "inputs", "inputDir", ".wgt", ".tv");
        String[] vectorFiles = new String[inputs.length];
        int[] mapSize = parseMapSize(config);
        final boolean skipConversion = config.getBoolean("skipConversion", false);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Merging SOMs " + Arrays.toString(inputs) + ", skipping conversion: " + skipConversion);

        // first step: convert all map vectors to input vectors
        for (int i = 0; i < inputs.length; i++) {
            vectorFiles[i] = inputs[i] + "_converted";
            if (!skipConversion) { // do actual conversion only if no skipping specified
                SOMLibFormatInputReader ir = new SOMLibFormatInputReader(inputs[i] + SOMLibFormatInputReader.weightFileNameSuffix, null, null);
                GrowingLayer layer = new GrowingLayer(ir.getXSize(), ir.getYSize(), ir.getZSize(), ir.getMetricName(), ir.getDim(), false, false, 7,
                        null);
                PrintWriter writer = FileUtils.openFileForWriting("Input Vector File", vectorFiles[i] + InputData.inputFileNameSuffix, true);
                AbstractSOMLibSparseInputData.writeHeaderToFile(writer, layer.getUnitCount(), layer.getDim());
                String name = FileUtils.extractSOMLibInputPrefix(FileUtils.stripPathPrefix(inputs[i]));
                for (Unit unit : layer.getAllUnits()) {
                    AbstractSOMLibSparseInputData.writeInputDatumToFile(writer, new InputDatum(name + "_" + unit.printCoordinates(),
                            unit.getWeightVector()));

                }
                writer.flush();
                writer.close();
            }
        }

        // then, merge them
        SOMLibInputMerger.mergeVectors(inputs, vectorFiles, config.getString("output"), config.getString("mode"), mapSize);
    }

    private static int[] parseMapSize(JSAPResult config) {
        int[] mapSize = null;
        if (config.getString("mapSize") != null) {
            final String[] split = config.getString("mapSize").split("x");
            mapSize = new int[split.length];
            for (int i = 0; i < split.length; i++) {
                mapSize[i] = Integer.parseInt(split[i]);
            }
        }
        return mapSize;
    }
}

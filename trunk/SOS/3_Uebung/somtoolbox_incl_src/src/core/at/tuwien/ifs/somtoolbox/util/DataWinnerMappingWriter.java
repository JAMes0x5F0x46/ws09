package at.tuwien.ifs.somtoolbox.util;

import java.io.IOException;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;

import com.martiansoftware.jsap.JSAPResult;

/**
 * This class writes a data winner mapping file from a trained map.
 * 
 * @author Rudolf Mayer
 * @version $Id: DataWinnerMappingWriter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DataWinnerMappingWriter  implements SOMToolboxApp {
    public static void main(String[] args) throws SOMLibFileFormatException, IOException {
        // register and parse all options for the Data Winner Mapping writer
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptInputVectorFile(true), OptionFactory.getOptWeightVectorFile(true),
                OptionFactory.getOptUnitDescriptionFile(true), OptionFactory.getOptMapDescriptionFile(false),
                OptionFactory.getOptNumberWinners(true), OptionFactory.getOptOutputFileName(true), OptionFactory.getOptOutputDirectory(false));

        String vectorFileName = config.getString("inputVectorFile");
        String weightVectorFile = config.getString("weightVectorFile");
        String unitDescriptionFile = config.getString("unitDescriptionFile");
        String mapDescriptionFile = config.getString("mapDescriptionFile");
        String outputDir = config.getString("outputDirectory", ".");
        String outputFileName = config.getString("output");
        int numWinners = config.getInt("numberWinners", 50);

        SOMLibSparseInputData inputData = new SOMLibSparseInputData(vectorFileName);
        GrowingSOM gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFile, unitDescriptionFile, mapDescriptionFile));

        SOMLibMapOutputter.writeDataWinnerMappingFile(gsom, inputData, numWinners, outputDir, outputFileName, true);
    }

}

package at.tuwien.ifs.somtoolbox.input;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;

import com.martiansoftware.jsap.JSAPResult;

/**
 * This class converts between various file formats for trained SOMs. Currently supported formats are listed in {@link #FILE_FORMAT_TYPES}.
 * 
 * @author Rudolf Mayer
 * @version $Id: MapFileFormatConverter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MapFileFormatConverter  implements SOMToolboxApp {

    /** Supported File Format Types, currently SOMLib and SOMPak. */
    public static final String[] FILE_FORMAT_TYPES = { SOMLibFormatInputReader.getFormatName(), SOMPAKFormatInputReader.getFormatName() };

    /**
     * Method for stand-alone execution.
     */
    public static void main(String[] args) throws IOException, SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_MAP_FILEFORMAT_CONVERTER);
        String inputFormat = config.getString("inputFormat", null);
        String outputFormat = config.getString("outputFormat", null);
        if (!ArrayUtils.contains(FILE_FORMAT_TYPES, inputFormat)) {
            throw new SOMToolboxException("Invalid input format '" + inputFormat + "'!. valid values are: " + Arrays.toString(FILE_FORMAT_TYPES));
        }
        if (!ArrayUtils.contains(FILE_FORMAT_TYPES, outputFormat)) {
            throw new SOMToolboxException("Invalid output format '" + outputFormat + "'!. valid values are: " + Arrays.toString(FILE_FORMAT_TYPES));
        }
        String inputFileName = config.getString("input", null);
        String templateVectorFile = config.getString("templateVectorFile", null);
        String unitDescriptionFile = config.getString("unitDescriptionFile", null);

        SOMInputReader reader = null;
        SOMLibTemplateVector tv = null;
        if (inputFormat.equals(SOMLibFormatInputReader.getFormatName())) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading SOMLib Input Format.");
            reader = new SOMLibFormatInputReader(inputFileName, unitDescriptionFile, null);
            tv = new SOMLibTemplateVector(templateVectorFile);
        } else if (inputFormat.equals(SOMPAKFormatInputReader.getFormatName())) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading SOMPak Input Format.");
            reader = new SOMPAKFormatInputReader(inputFileName);
            tv = new SOMLibTemplateVector(reader.getXSize() * reader.getYSize(), reader.getDim());
            tv.setComponentNames(((SOMPAKFormatInputReader) reader).getComponentNames());
        }
        GrowingSOM gsom = new GrowingSOM(reader);
        String fDir = config.getString("outputDirectory", null);
        String fName = config.getString("output", null);
        boolean gzipped = true;
        if (!new File(fDir).exists()) {
            new File(fDir).mkdirs();
        }
        if (outputFormat.equals(SOMLibFormatInputReader.getFormatName())) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing SOMLib Output Format.");
            SOMLibMapOutputter.writeUnitDescriptionFile(gsom, fDir, fName, gzipped);
            SOMLibMapOutputter.writeWeightVectorFile(gsom, fDir, fName, gzipped);
            tv.writeToFile(fDir + File.separator + fName + ".tv");
        } else if (outputFormat.equals(SOMPAKFormatInputReader.getFormatName())) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing SOMPak Output Format.");
            throw new SOMToolboxException("SOMPak output is not yet implemented");
        }
    }
}

package at.tuwien.ifs.somtoolbox.data;

import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.normalisation.MinMaxNormaliser;
import at.tuwien.ifs.somtoolbox.data.normalisation.StandardScoreNormaliser;
import at.tuwien.ifs.somtoolbox.data.normalisation.UnitLengthNormaliser;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;

/**
 * Handles the normalization of vector files in SOMLib format. This class can be run in standalone mode taking two arguments, i.e. input and output
 * file. If the input file is gzip-compressed, the output will also be written gzip-compressed. The .gz suffix has to be specified manually in order
 * not to alter filenames to something other than intended by the user.
 * <p>
 * <i>Created on Mar 16, 2004</i>
 * </p>
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMLibVectorNormalization.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMLibVectorNormalization  implements SOMToolboxApp {

    /**
     * Static method for standalone invocation.
     * 
     * @param args Usage: method-type input-filename output-filename
     */
    public static void main(String[] args) {
        // -n normalization type, opt., default=UNIT_LEN
        // input file
        // output file

        // register and parse all options for the SOMLibVectorNormalization
        JSAP jsap = OptionFactory.registerOptions(OptionFactory.OPTIONS_SOMLIB_VEC_NORMALIZER);
        JSAPResult config = OptionFactory.parseResults(args, jsap);

        String method = config.getString("method");
        String inputFileName = config.getString("input");
        String outputFileName = config.getString("output");

        try {
            if (method.equals("UNIT_LEN")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting normalisation to unit length");
                new UnitLengthNormaliser().normalise(inputFileName, outputFileName);
            } else if (method.equals("MIN_MAX")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting min-max normalisation");
                new MinMaxNormaliser().normalise(inputFileName, outputFileName);
            } else if (method.equals("STANDARD_SCORE")) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting standard score normalisation");
                new StandardScoreNormaliser().normalise(inputFileName, outputFileName);
            } else {
                OptionFactory.printUsage(jsap, SOMLibVectorNormalization.class.getName(), config, "wrong method.");
            }
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("finished Normalization");
    }

    /**
     * No normalisation.
     */
    public static final int NONE = 0;

    /**
     * Normalise vectors to unit length.
     */
    public static final int UNIT_LEN = 1;
}

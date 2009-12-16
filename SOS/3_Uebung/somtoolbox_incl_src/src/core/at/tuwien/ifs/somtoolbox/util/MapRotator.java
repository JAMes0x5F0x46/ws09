package at.tuwien.ifs.somtoolbox.util;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Rotates a map by the given degrees, and writes a new unit- and weight-vector file.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: MapRotator.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MapRotator  implements SOMToolboxApp {

    public static void main(String[] args) {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptUnitDescriptionFile(true),
                OptionFactory.getOptWeightVectorFile(true), OptionFactory.getOptDataWinnerMappingFile(false),
                OptionFactory.getOptOutputFileName(true), OptionFactory.getOptRotation(false), OptionFactory.getOptFlip(false));
        String unitDescriptionFile = config.getString("unitDescriptionFile");
        String weightVectorFile = config.getString("weightVectorFile");
        String output = config.getString("output");
        final String dir = ".";

        try {
            GrowingSOM gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFile, unitDescriptionFile, null));

            // Flip map?
            if (config.userSpecified("flip")) {
                char flip = config.getString("flip").charAt(0);
                switch (flip) {
                    case 'h':
                        gsom.getLayer().flipH();
                        break;
                    case 'v':
                        gsom.getLayer().flipV();
                        break;
                    default:
                        System.err.printf("Invalid flip operation: %s%n", config.getString("filp"));
                        System.exit(1);
                        break;
                }
                System.out.printf("Flip: %s%n", flip);
            }

            if (config.userSpecified("rotation")) {
                int rotation = config.getInt("rotation");
                try {
                    GrowingLayer.checkRotation(rotation);
                    System.out.printf("Rotate: %d%n", rotation);
                } catch (SOMToolboxException e) {
                    System.err.printf("Invalid rotation operatrion: %d%n", rotation);
                    System.exit(1);
                }
                gsom.getLayer().rotate(rotation);
            }

            // FIXME: also do something with the data winner mapping file
            SOMLibMapOutputter.writeUnitDescriptionFile(gsom, dir, output, true);
            SOMLibMapOutputter.writeWeightVectorFile(gsom, dir, output, true);
        } catch (Exception e) {
            System.err.printf("%s%n", e.getMessage());
            System.exit(2);
        }
    }

}

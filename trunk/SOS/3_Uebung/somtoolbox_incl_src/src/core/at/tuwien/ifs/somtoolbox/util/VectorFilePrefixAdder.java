package at.tuwien.ifs.somtoolbox.util;

import java.io.IOException;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;

import com.martiansoftware.jsap.JSAPResult;

/**
 * @author Rudolf Mayer
 * @version $Id: VectorFilePrefixAdder.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VectorFilePrefixAdder {

    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_VECTORFILE_PREFIX_ADDER);
        String prefix = config.getString("prefix");

        String fileName = config.getString("inputFile");
        String outputFileName = config.getString("output");
        SOMLibSparseInputData data = new SOMLibSparseInputData(fileName);
        int numVectors = data.numVectors();
        for (int i = 0; i < numVectors; i++) {
            data.setLabel(i, prefix + data.getLabel(i));
        }
        data.writeToFile(outputFileName);
    }

}

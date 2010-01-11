package at.tuwien.ifs.somtoolbox.data.normalisation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: AbstractNormaliser.java 2874 2009-12-11 16:03:27Z frank $
 */
abstract class AbstractNormaliser extends SOMLibSparseInputData {
    protected BufferedWriter writer;

    public void normalise(String inputFileName, String outputFileName) throws IOException {
        String line = null;
        BufferedReader br = openFile(inputFileName);
        writer = new BufferedWriter(new FileWriter(outputFileName));
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#")) { // ignore comment lines
                Logger.getLogger("at.tuwien.ifs.somtoolbox").finest("Read comment '" + line + "'.");
            } else if (line.startsWith("$")) { // just write the headers to the output file
                writer.write(line);
                writer.newLine();
                if (line.startsWith("$VEC_DIM") || line.startsWith("$VECDIM")) {
                    dim = Integer.parseInt(line.split((StringUtils.REGEX_SPACE_OR_TAB))[1]);
                }
            } else if (line.length() > 0) { // we reached a content line, stop reading
                break;
            }
        }
        writer.write("$NORMALISATION " + getClass().getName());
        writer.newLine();
        preReading();
        super.init(sparse, false, SOMLibSparseInputData.DEFAULT_RANDOM_SEED);
        super.readVectorFile(inputFileName, sparse);
        postReading();
        writer.flush();
        writer.close();
        br.close();
    }

    public abstract void postReading() throws IOException;

    public abstract void preReading();
}

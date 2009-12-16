package at.tuwien.ifs.somtoolbox.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Handles the removal of zero vectors in vector files in SOMLib format. This class can be run in standalone mode taking two arguments, i.e. input and
 * output file. If the input file is gzip-compressed, the output will also be written gzip-compressed. The .gz suffix has to be specified manually in
 * order not to alter filenames to something other than intended by the user.
 * <p>
 * <i>Created on Mar 16, 2004</i>
 * </p>
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMLibZeroVectorRemover.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMLibZeroVectorRemover  implements SOMToolboxApp {

    private static final String ERROR_FILE_FORMAT_CORRUPT = "Input vector file format corrupt. Aborting.";

    /**
     * Static method for standalone invocation.
     * 
     * @param args Usage: input-filename output-filename
     */
    public static void main(String[] args) {
        // input file
        // output file

        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_SOMLIB_ZERO_VEC_REMOVER);

        String inputFileName = config.getString("input");
        String outputFileName = config.getString("output");

        try {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("starting removal of vectors with zeros only");
            removeZeroVectors(inputFileName, outputFileName);
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("finished removal of vectors with zeros only");
    }

    /**
     * Static method taking input and output filename as argument. File handling is done in this method and throws a FileNotFoundException if the
     * input file can not be found and an IOException if some other file handling error occurs. If the input file is gzip-compressed, the output will
     * also be written gzip-compressed. The .gz suffix has to be specified manually in order not to alter filenames to something other than intended
     * by the user.
     * 
     * @param inFileName Name of input file.
     * @param outFileName Name of output file.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void removeZeroVectors(String inFileName, String outFileName) throws FileNotFoundException, IOException {
        BufferedReader inFile = null;
        boolean gzipped = false;
        try {
            inFile = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inFileName))));
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(inFileName + " is gzip compressed. Trying compressed read. Creating compressed output.");
            gzipped = true;
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Input vector file " + inFileName + " not found. Aborting.");
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                    inFileName + " is not gzip compressed. Trying uncompressed read. Creating uncompressed output.");
            try {
                inFile = new BufferedReader(new FileReader(inFileName));
                gzipped = false;
            } catch (FileNotFoundException e2) {
                throw new FileNotFoundException("Input vector file " + inFileName + " not found. Aborting.");
            }
        }
        BufferedWriter outFile = null;
        try {
            if (gzipped == false) {
                outFile = new BufferedWriter(new FileWriter(outFileName));
            } else {
                outFile = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outFileName))));
            }
        } catch (IOException e) {
            throw new IOException("Can not open output vector file " + outFileName + ". Aborting.");
        }
        removeZeroVectors(inFile, outFile);
        try {
            inFile.close();
            outFile.close();
        } catch (IOException e) {
            String errorMessage = "Problem closing vector files. Aborting.";
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Static method taking a Buffered Reader and BufferedWriter as argument. The method exits the program if the file format is corrupt. TODO: This
     * should be weakened in future by throwing a SOMLibVectorFileFormatException.
     * 
     * @param inReader BufferedReader reading the file continaing unnormalized vectors.
     * @param outWriter BuffererWriter writing the normalized vector file.
     */
    public static void removeZeroVectors(BufferedReader inReader, BufferedWriter outWriter) {
        String line = null;
        int xDim = 0;
        int yDim = 0;
        int vectorDim = 0;
        int numVectors = 0;
        String type = "";
        String tmpFileName = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "somtoolboxzerotmp."
                + System.currentTimeMillis();
        BufferedWriter tmpOutWriter = null;
        try {
            tmpOutWriter = new BufferedWriter(new FileWriter(tmpFileName));
        } catch (IOException ioe) {
            String errorMessage = "Could not write temporary file " + tmpFileName + ". Aborting";
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            int index = 1;
            int numZeroVecs = 0;
            while ((line = inReader.readLine()) != null) {
                if (line.startsWith("$")) { // write fields through without change
                    // outWriter.write(line);
                    // outWriter.newLine();
                    if (line.startsWith("$TYPE")) {
                        type = line;
                    } else if (line.startsWith("$XDIM")) {
                        String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                        if (lineElements.length > 1) {
                            xDim = Integer.parseInt(lineElements[1]);
                            numVectors = Integer.parseInt(lineElements[1]);
                        } else {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_FILE_FORMAT_CORRUPT);
                            throw new IllegalArgumentException(ERROR_FILE_FORMAT_CORRUPT);
                        }
                    } else if (line.startsWith("$YDIM")) {
                        String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                        if (lineElements.length > 1) {
                            yDim = Integer.parseInt(lineElements[1]);
                            numVectors *= Integer.parseInt(lineElements[1]);
                        } else {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_FILE_FORMAT_CORRUPT);
                            throw new IllegalArgumentException(ERROR_FILE_FORMAT_CORRUPT);
                        }
                    } else if (line.startsWith("$VEC_DIM") || line.startsWith("$VECDIM")) {
                        String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                        if (lineElements.length > 1) {
                            vectorDim = Integer.parseInt(lineElements[1]);
                        } else {
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(ERROR_FILE_FORMAT_CORRUPT);
                            throw new IllegalArgumentException(ERROR_FILE_FORMAT_CORRUPT);
                        }
                    }
                } else {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length != vectorDim + 1) {
                        String errorMessage = "Input vector file format corrupt in vector number " + index + ". Aborting.";
                        Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
                        throw new IllegalArgumentException(errorMessage);
                    }
                    double[] vector = new double[vectorDim];
                    boolean nonZero = false;
                    for (int ve = 0; ve < vectorDim; ve++) {
                        try {
                            vector[ve] = Double.parseDouble(lineElements[ve]);
                            if (vector[ve] > 0) {
                                nonZero = true;
                            }
                        } catch (NumberFormatException e) {
                            String errorMessage = "Input vector number format corrupt in vector number " + index + ". Aborting.";
                            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
                            throw new IllegalArgumentException(errorMessage);
                        }
                    }
                    String label = lineElements[vectorDim];

                    // vector = Normalization.normalizeVectorToUnitLength(vector);
                    if (nonZero == true) {
                        for (int ve = 0; ve < vectorDim; ve++) {
                            if (vector[ve] == 0) {
                                tmpOutWriter.write("0 ");
                            } else {
                                tmpOutWriter.write(vector[ve] + " ");
                            }
                        }
                        tmpOutWriter.write(label);
                        tmpOutWriter.newLine();
                    } else {
                        numZeroVecs++;
                    }
                    index++;
                }
            }
            if (index - 1 != numVectors) {
                String errorMessage = "Input vector file format corrupt. Incorrect number of vectors. Aborting.";
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Number of vectors removed: " + numZeroVecs);

            tmpOutWriter.close();

            outWriter.write(type);
            outWriter.newLine();
            outWriter.write("$XDIM " + (xDim - numZeroVecs));
            outWriter.newLine();
            outWriter.write("$YDIM " + yDim);
            outWriter.newLine();
            outWriter.write("$VEC_DIM " + vectorDim);
            outWriter.newLine();

            char cbuf[] = new char[1024];
            BufferedReader tmpInReader = new BufferedReader(new FileReader(tmpFileName));
            int i = 0;
            while ((i = tmpInReader.read(cbuf)) != -1) {
                outWriter.write(cbuf, 0, i);
            }
            tmpInReader.close();
            File tmpFile = new File(tmpFileName);
            tmpFile.delete();

        } catch (IOException e) {
            String errorMessage = "Problem writing output vector file. Aborting.";
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

}

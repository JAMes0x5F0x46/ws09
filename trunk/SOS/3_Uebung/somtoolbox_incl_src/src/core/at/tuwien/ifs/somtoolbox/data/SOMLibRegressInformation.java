package at.tuwien.ifs.somtoolbox.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class provides information about predicted output variables for the {@link InputData} input vectors.<br>
 * <p>
 * The file format consists of a <code>header</code> and the content as follows:
 * </p>
 * <b>$TYPE</b> string, mandatory. Fixed to <i>output_information.</i> <br>
 * <b>$NUM_OUTPUTS</b> integer, mandatory: gives the number of outpus. <br>
 * <b>$OUTPUT_NAMES</b> mandatory: a space-seperated list of output names; the count has to be the same as in $NUM_CLASSES. <br>
 * <b>$XDIM</b> integer, mandatory: number of units in x-direction.<br>
 * <b>$YDIM</b> integer, mandatory: dimensionality class information vector, equals the number of input vectors ({@link InputData#numVectors()}).
 * <br>
 * <b>labelName_n&nbsp;outputValue_n1 [outputValue_n2..[outputValue_nn] ]</b> the $YDIM number of mappings from the input vector label name to the
 * class label index [0...($NUM_CLASSES-1)]. <br>
 * <p>
 * See also an example file from the <a href="../../../../../examples/iris.cls">Iris data set</a>.
 * </p>
 * <p>
 * Alternatively, the file format can be more simple, and not contain any file header. Then, there is only a list of lines with two
 * tabulator-seperated <code>Strings</code> in the form of <code>labelName&nbsp;className</code>.<br>
 * The number of classes, and the indices of those classes, are computer automatically.
 * </p>
 * 
 * @author Michael Dittenbach
 * @author Thomas Lidy
 * @author Rudolf Mayer
 * @version $Id: SOMLibRegressInformation.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMLibRegressInformation {

    /**
     * The file name to read from.
     */
    private String outputInformationFileName = null;

    /**
     * The number of classes. Either read from the file header, or computed from the distinct number of class names in the tab-seperated file.
     */
    private int numOutputs = 0;

    /**
     * The names of the classes. Either read from the file header, or computed from the distinct class names in the tab-seperated file.
     */
    private String[] outputNames = null;

    /**
     * The number of inputs in each class.
     */
    private int[] classMemberCount = null;

    /**
     * The number of input vectors. Either read from the file header, or computed from the number pf data lines in the tab-seperated file.
     */
    private int numData = 0;

    // FIXME: not used?
    private String[] dataNames = null;

    /**
     * A mapping input index => class index, for fast lookup.
     */
    private int[] dataClasses = null;

    private HashMap<String, double[]> dataHash = new HashMap<String, double[]>();

    private double[][] outputValues;

    private double[] maxValues;

    private double[] minValues;

    private double[] meanValues;

    /** Constructor intended to be used when generating data. */
    public SOMLibRegressInformation() {
    }

    /**
     * Creates a new class information object by trying to read the given file in both the versions with a file header ({@link #readSOMLibClassInformationFile()})
     * and the tab seperated file ({@link #readTabSepClassInformationFile()}).
     * 
     * @param classInformationFileName
     * @throws SOMToolboxException
     */
    public SOMLibRegressInformation(String outputInformationFileName) throws SOMToolboxException {
        this.outputInformationFileName = outputInformationFileName;
        try {
            readSOMLibClassInformationFile();
        } catch (IOException e) {
            throw new SOMLibFileFormatException("Problems reading class information file " + outputInformationFileName + ": ' " + e.getMessage()
                    + "'. Aborting.");
        }

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Regression information file correctly loaded.");
    }

    /**
     * Reads a regression information file containing a header and class indices.
     * 
     * @throws IOException
     * @throws SOMToolboxException
     */
    private void readSOMLibClassInformationFile() throws IOException, SOMToolboxException {
        String line = null;
        int index = 0; // line counter
        int columns = 0;
        numData = 0;
        BufferedReader br = FileUtils.openFile("Regression information file", outputInformationFileName);

        // PROCESS HEADER as long as lines start with $
        while ((line = br.readLine()) != null) {

            // we ignore comment lines
            if (line.startsWith("#")) {
                continue;
            }

            if (!line.startsWith("$")) {
                break;
            }

            index++;

            if (line.startsWith("$TYPE")) {
                // ignore
            } else if (line.startsWith("$NUM_OUTPUTS")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    numOutputs = Integer.parseInt(lineElements[1]);
                } else {
                    throw new SOMLibFileFormatException("Regression information file format corrupt in $NUM_OUTPUTS line. Aborting.");
                }
            } else if (line.startsWith("$OUTPUT_NAMES")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    outputNames = new String[numOutputs];
                    for (int c = 0; c < numOutputs; c++) {
                        outputNames[c] = lineElements[c + 1];
                    }
                } else {
                    throw new SOMLibFileFormatException("Regression information file format corrupt in $OUTPUT_NAMES line. Aborting.");
                }
            } else if (line.startsWith("$XDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    columns = Integer.parseInt(lineElements[1]);
                    if (columns < 2) {
                        throw new SOMLibFileFormatException(
                                "Regression information file format corrupt. At least 2 columns (name, predictedValue) required. Aborting.");
                    }
                } else {
                    throw new SOMLibFileFormatException();
                }
            } else if (line.startsWith("$YDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    numData = Integer.parseInt(lineElements[1]);
                } else {
                    throw new SOMLibFileFormatException("Regression information file format corrupt in $YDIM line. Aborting.");
                }
            }
        }

        if (index == 0) {
            throw new ClassInfoHeaderNotFoundException("Regression information file: no header line starting with $ found");
        }

        classMemberCount = new int[numOutputs];

        // READ REST OF THE FILE
        if (numData == 0) {
            throw new SOMLibFileFormatException("Regression information file format corrupt. Missing $YDIM value. Aborting.");
        }

        outputValues = new double[numData][numOutputs];
        maxValues = new double[numOutputs];
        minValues = new double[numOutputs];
        meanValues = new double[numOutputs];
        for (int i = 0; i < maxValues.length; i++) {
            maxValues[i] = Double.MIN_VALUE;
            minValues[i] = Double.MAX_VALUE;
        }

        index = 0;

        while (line != null) {
            // TODO if line is no comment line ($)
            index++;
            String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
            if (lineElements.length != columns) {
                throw new SOMLibFileFormatException("Regression information file format corrupt in element number " + index
                        + ", incorrect number of columns: XDIM:   " + columns + ", columns: " + lineElements.length + ". Aborting.");
            } else {
                try {

                    double[] values = new double[outputNames.length];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = Double.parseDouble(lineElements[i + 1]);
                        if (minValues[i] > values[i]) {
                            minValues[i] = values[i];
                        }
                        if (maxValues[i] < values[i]) {
                            maxValues[i] = values[i];
                        }
                        meanValues[i] += values[i];
                    }
                    dataHash.put(lineElements[0], values);
                } catch (NumberFormatException e) { // does not happen at the moment
                    throw new SOMLibFileFormatException("Output id number format corrupt in element number " + index + ": '" + lineElements[1]
                            + "'. Aborting.");
                }
            }

            line = br.readLine();
        }

        if (index != numData) {
            throw new SOMLibFileFormatException("Output information file format corrupt. Incorrect number of data items. Aborting.\n"
                    + Integer.toString(index) + " " + Integer.toString(numData));
        }
        for (int i = 0; i < meanValues.length; i++) {
            meanValues[i] = meanValues[i] / numData;
        }
        br.close();
    }

    /**
     * Gets the number of classes, as read from $NUM_CLASSES, or computed.
     * 
     * @return the number of classes.
     */
    public int numClasses() {
        return numOutputs;
    }

    /**
     * Returns all the distinct class names.
     * 
     * @return the class names.
     */
    public String[] classNames() {
        return outputNames;
    }

    /**
     * Gets the index number for a given class label.
     * 
     * @param className the class label.
     * @return the index of that label.
     */
    public int getClassIndex(String className) {
        Object classid = dataHash.get(className);
        if (classid == null) {
            return -1;
        } else {
            return ((Integer) classid).intValue();
        }
    }

    /**
     * Gets the class label name for a given input vector index.
     * 
     * @param index index of the input vector.
     * @return the name of the class.
     */
    public String getClassName(int index) {
        return outputNames[dataClasses[index]];
    }

    public double getPrediction(String vectorname, int predictionIndex) {
        return dataHash.get(vectorname)[predictionIndex];
    }

    public static void main(String[] args) throws SOMToolboxException {
        String file = "/home/mayer/workspace/somtoolbox/bayesian-daten/bbn_all.reginf";
        SOMLibRegressInformation x = new SOMLibRegressInformation(file);
        System.out.println(Arrays.toString(x.minValues));
        System.out.println(Arrays.toString(x.maxValues));
        System.out.println(Arrays.toString(x.meanValues));
    }

}

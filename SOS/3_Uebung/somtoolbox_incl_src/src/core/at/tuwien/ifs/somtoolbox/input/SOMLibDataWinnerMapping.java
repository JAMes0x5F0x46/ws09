package at.tuwien.ifs.somtoolbox.input;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.visualization.SmoothedDataHistograms;

/**
 * Reads and encapsules the input data - winner information. This means units that are the best-matching ones for the single input data, and the
 * distances to those units (the distances are used e.g. by the {@link SmoothedDataHistograms} in their weighted & normalised form.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SOMLibDataWinnerMapping.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMLibDataWinnerMapping {
    /** Maximum data winners that will be written per unit, unless specified otherwise. */
    public static final int MAX_DATA_WINNERS = 300;

    /**
     * Inner class holding the list of a certain number of best-matching units for an certain input datum.
     * 
     * @author Michael Dittenbach
     */
    private class DataInformation {
        private double[] dists = null;

        private String label = null;

        private int[] xPos = null;

        private int[] yPos = null;

        private int[] zPos = null;

        /**
         * Sole constructor taking the number of units as an argument.
         * 
         * @param numUnits the number of best-matching units.
         */
        private DataInformation(int numUnits) {
            xPos = new int[numUnits];
            yPos = new int[numUnits];
            zPos = new int[numUnits];
            dists = new double[numUnits];
        }
    }

    private DataInformation dataInfo[] = null;

    private int numBMUs = 0;

    private int numVectors = 0;

    private String metric;

    /**
     * Sole constructor taking the name of the data-winner mapping file as an argument.
     * 
     * @param fileName the name of the data-winner mapping file.
     * @throws FileNotFoundException if the file with the given name is not found.
     * @throws SOMLibFileFormatException if the format of the file is corrupt.
     */
    public SOMLibDataWinnerMapping(String fileName) throws FileNotFoundException, SOMLibFileFormatException {
        readDataWinnerMappingFile(fileName);
    }

    /**
     * Returns an array of <code>double</code> values containing the distances between the input datum and the best-matching units sorted ascending.
     * If the argument <code>datum</code> is invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not
     * obliged to catch it.
     * 
     * @param datum the index of the input datum in the list.
     * @return an array of double values containing the distances between the input datum and the best-matching units sorted ascending.
     */
    public double[] getDists(int datum) {
        return dataInfo[datum].dists;
    }

    /**
     * Returns the number of best-matching units per input datum.
     * 
     * @return the number of best-matching units per input datum.
     */
    public int getNumBMUs() {
        return numBMUs;
    }

    /**
     * Returns the number of input vectors.
     * 
     * @return the number of input vectors.
     */
    public int getNumVectors() {
        return numVectors;
    }

    /**
     * Returns an array of <code>int</code> values containing the horizontal positions of the the best-matching units of the input datum. If the
     * argument <code>datum</code> is invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to
     * catch it.
     * 
     * @param datum the index of the input datum in the list.
     * @return an array of <code>int</code> values containing the horizontal positions of the the best-matching units of the input datum.
     */
    public int[] getXPos(int datum) {
        return dataInfo[datum].xPos;
    }

    /**
     * Returns an array of <code>int</code> values containing the vertical positions of the the best-matching units of the input datum. If the
     * argument <code>datum</code> is invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to
     * catch it.
     * 
     * @param datum the index of the input datum in the list.
     * @return an array of <code>int</code> values containing the vertical positions of the the best-matching units of the input datum.
     */
    public int[] getYPos(int datum) {
        return dataInfo[datum].yPos;
    }

    /**
     * Returns an array of <code>int</code> values containing the depth positions of the the best-matching units of the input datum. If the argument
     * <code>datum</code> is invalid, an <code>ArrayIndexOutOfBoundsException</code> will be thrown. The calling function is not obliged to catch it.
     * 
     * @param datum the index of the input datum in the list.
     * @return an array of <code>int</code> values containing the depth positions of the the best-matching units of the input datum.
     */
    public int[] getZPos(int datum) {
        return dataInfo[datum].zPos;
    }

    /**
     * Finds the position of input vector by comparing its label.
     * 
     * @param label The label of the input vector
     * @return Position of the input vector
     * @throws SOMToolboxException when label is not found in data winner mapping file
     */
    public int getVectPos(String label) throws SOMToolboxException {
        if (label != null && label.length() > 0) {
            for (int i = 0; i < dataInfo.length; i++) {
                if (label.equals(dataInfo[i].label))
                    return i;
            }
        }
        throw new SOMToolboxException("Could not find label '" + label + "' in DataWinnerMapping file!");
    }

    /**
     * Reads from the file and fills the data structure.
     * 
     * @param fileName the name of the file to open.
     * @throws FileNotFoundException if the file with the given name is not found.
     * @throws SOMLibFileFormatException if the format of the file is corrupt.
     */
    public void readDataWinnerMappingFile(String fileName) throws FileNotFoundException, SOMLibFileFormatException {
        BufferedReader br = FileUtils.openFile("Data winner mapping file", fileName);
        String line = null;
        double fileFormatVersion = 1.0;
        boolean numVectorsRead = false;
        boolean numBMUsRead = false;
        int lineNumber = 0;

        // PROCESS HEADER with arbitrary number of comment lines & lines starting with $
        try {
            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (line.startsWith("#") || line.equals("")) { // ignore comments and empty lines
                    continue;
                }
                if (!line.startsWith("$")) {
                    break;
                }

                if (line.startsWith("$FILE_FORMAT_VERSION")) {
                    try {
                        fileFormatVersion = Double.parseDouble(line.split(StringUtils.REGEX_SPACE_OR_TAB, 2)[1]);
                    } catch (Exception e) {
                        throw new SOMLibFileFormatException("Unknown $FILE_FORMAT_VERSION");
                    }
                } else if (line.startsWith("$NUM_WINNERS ")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        numBMUs = Integer.parseInt(lineElements[1]);
                        numBMUsRead = true;
                    } else {
                        throw new SOMLibFileFormatException("Data winner mapping file format corrupt in line # " + lineNumber
                                + ". $NUM_WINNERS corrupt.");
                    }
                } else if (line.startsWith("$NUM_VECTORS ")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length > 1) {
                        numVectors = Integer.parseInt(lineElements[1]);
                        numVectorsRead = true;
                    } else {
                        throw new SOMLibFileFormatException("Data winner mapping file format corrupt in line # " + lineNumber
                                + ". $NUM_VECTORS corrupt.");
                    }
                } else if (line.startsWith("$METRIC ")) {
                    String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                    if (lineElements.length == 2) {
                        metric = lineElements[1];
                    } else {
                        System.out.println();
                        throw new SOMLibFileFormatException("Data winner mapping file format corrupt in line #" + lineNumber + ". $METRIC corrupt.");
                    }
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Unkown Header line #" + lineNumber + ": '" + line + "', ingoring.");
                }
            }
            if (!numVectorsRead || !numBMUsRead) {
                throw new SOMLibFileFormatException("Data winner mapping file format corrupt: Incomplete header!");
            }

            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Data winner mapping file version " + fileFormatVersion);

            /* read rest of files */
            dataInfo = new DataInformation[numVectors];
            String[] lineElements = null;
            StdErrProgressWriter progressWriter = new StdErrProgressWriter(numVectors, "Reading winners of input datum ", 10);
            for (int d = 0; d < numVectors; d++) {
                dataInfo[d] = new DataInformation(numBMUs);

                if (fileFormatVersion >= 1.1) {
                    // in this version the file label is on a single line (allowing the label to contain spaces)
                    dataInfo[d].label = line;
                    // the elements are then in the next line
                    line = br.readLine();
                }

                lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                int el = 0; // line element position
                if (fileFormatVersion == 1.0) {
                    dataInfo[d].label = lineElements[el++];
                } else if (lineElements[el].equals("")) {
                    el++; // in fileformat >= 1.1 there is a leading space on that line, so that the first element has to be skipped
                }

                for (int w = 0; w < numBMUs; w++) { // numUnits
                    dataInfo[d].xPos[w] = Integer.parseInt(lineElements[el++]);
                    dataInfo[d].yPos[w] = Integer.parseInt(lineElements[el++]);
                    if (fileFormatVersion >= 1.2) { // zPos exists since v1.2
                        dataInfo[d].zPos[w] = Integer.parseInt(lineElements[el++]);
                    } else {
                        dataInfo[d].zPos[w] = 0;
                    }
                    dataInfo[d].dists[w] = Double.parseDouble(lineElements[el++]);
                }
                progressWriter.progress(d + 1);
                line = br.readLine();
                lineNumber++;
            }
            br.close();
        } catch (IOException e) {
            throw new SOMLibFileFormatException("Could not read from data winner mapping file. " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new SOMLibFileFormatException("Data winner mapping number format corrupt in line #" + lineNumber + ":" + e.getMessage());
        }
    }

    public String getMetric() {
        return metric;
    }

    public static void main(String[] args) {
        try {
            new SOMLibDataWinnerMapping(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SOMLibFileFormatException e) {
            e.printStackTrace();
        }
    }

}

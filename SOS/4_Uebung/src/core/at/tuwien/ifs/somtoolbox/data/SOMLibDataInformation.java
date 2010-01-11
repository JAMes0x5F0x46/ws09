package at.tuwien.ifs.somtoolbox.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class provides more detailed information about the {@link InputData} input vectors. For example, if the input vectors have IDs as labels, this
 * file can specify descriptive labels.<br>
 * <p>
 * <i>Created on Nov 22, 2004</i>
 * </p>
 * 
 * @author Michael Dittenbach
 * @version $Id: SOMLibDataInformation.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMLibDataInformation {

    private String dataInformationFileName;

    private String baseDir;

    private int numData;

    // private String[] dataNames;
    private Hashtable<String, String> dataDisplayNames = new Hashtable<String, String>();

    private Hashtable<String, String> dataLocations = new Hashtable<String, String>();

    public SOMLibDataInformation() {
    }

    public SOMLibDataInformation(String fileName) throws FileNotFoundException, SOMToolboxException {
        dataInformationFileName = fileName;
        BufferedReader br = FileUtils.openFile("Data information file", dataInformationFileName);

        try {
            readDataInformationFileHeader(br);
            // dataNames = new String[numData];
            dataDisplayNames = new Hashtable<String, String>(numData);
            dataLocations = new Hashtable<String, String>(numData);

            String line = null;
            int index = 0;
            while ((line = br.readLine()) != null) {
                index++;
                String[] lineElements = line.split("[\t]");
                if (lineElements.length != 3) {
                    String msg = "Data information file format corrupt in element number " + index + ": found " + lineElements.length
                            + " elements in '" + line + "'";
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(msg + ". Aborting.");
                    throw new SOMToolboxException(msg);
                } else {
                    // dataNames[index-1] = lineElements[0];
                    dataDisplayNames.put(lineElements[0], lineElements[1]);
                    dataLocations.put(lineElements[0], lineElements[2]);
                }
            }
            if (index != numData) {
                String msg = "Data information file format corrupt. Incorrect number of data items - $XDIM*$YDIM: " + numData + ", read: " + index
                        + ". Aborting.";
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(msg);
                throw new SOMToolboxException(msg);
            }

        } catch (IOException e) {
            String msg = "Data information file format corrupt: " + e.getMessage() + ". Aborting.";
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(msg);
            throw new SOMToolboxException(msg);
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Data information file format seems to be correct. Riding on ...");

    }

    public void addItem(String label, String displayName, String location) {
        dataDisplayNames.put(label, displayName);
        dataLocations.put(label, location);
        numData = dataDisplayNames.size();
    }

    private void readDataInformationFileHeader(BufferedReader br) throws IOException {
        String line = null;
        // TODO Assuming comment-free SOMLib template file. Stupid read implemented.
        for (int i = 0; i < 4; i++) {
            line = br.readLine(); // dummy
            if (line.startsWith("$TYPE")) {
                // ignore
            } else if (line.startsWith("$BASE_DIR")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    baseDir = lineElements[1];
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Data information file format corrupt. Aborting.");
                    System.exit(-1);
                }
            } else if (line.startsWith("$XDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    numData = Integer.parseInt(lineElements[1]);
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Data information file format corrupt. Aborting.");
                    System.exit(-1);
                }
            } else if (line.startsWith("$YDIM")) {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                if (lineElements.length > 1) {
                    numData *= Integer.parseInt(lineElements[1]);
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Data information file format corrupt. Aborting.");
                    System.exit(-1);
                }
            }
        }
    }

    /** Writes the class information to a file. */
    public void writeToFile(String fileName) throws IOException, SOMLibFileFormatException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write("$TYPE data_information");
        writer.newLine();
        writer.write("$BASE_DIR " + baseDir);
        writer.newLine();
        writer.write("$XDIM " + numData);
        writer.newLine();
        writer.write("$YDIM " + 1);
        writer.newLine();
        Enumeration<String> keys = dataDisplayNames.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            writer.write(key + "\t" + dataDisplayNames.get(key) + "\t" + dataLocations.get(key));
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public String getBaseDir() {
        return baseDir;
    }

    public String getDataDisplayName(String name) {
        if (name == null) {
            return null;
        } else {
            return dataDisplayNames.get(name);
        }
    }

    public String getDataLocation(String name) {
        if (name == null) {
            return null;
        } else {
            return dataLocations.get(name);
        }
    }

    /**
     * Method for standalone execution to test a data information file.
     */
    public static void main(String[] args) {
        try {
            new SOMLibDataInformation(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SOMToolboxException e) {
            e.printStackTrace();
        }
    }
}

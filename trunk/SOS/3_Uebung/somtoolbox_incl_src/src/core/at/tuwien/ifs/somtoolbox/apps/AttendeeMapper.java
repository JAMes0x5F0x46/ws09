package at.tuwien.ifs.somtoolbox.apps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.output.HTMLOutputter;

import com.martiansoftware.jsap.JSAPResult;

/**
 * <p>
 * <i>Created on Jun 4, 2004</i>
 * </p>
 * 
 * @author Michael Dittenbach
 * @version $Id: AttendeeMapper.java 2874 2009-12-11 16:03:27Z frank $
 */
public class AttendeeMapper  implements SOMToolboxApp {

    /**
     * Method for stand-alone execution of the attendee mapper. Options are:<br>
     * <ul>
     * <li>-d data names file, mandatory</li>
     * <li>-u unit description file, mandatory</li>
     * <li>htmlName name of output HTML file, mandatory</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     */
    public static void main(String[] args) {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_ATTENDEE_MAPPER);

        String dataNamesFileName = config.getString("dataNamesFile");
        String unitDescriptionFileName = config.getString("unitDescriptionFile");
        String htmlFileName = config.getString("htmlFile");

        GHSOM ghsom = null;
        try {
            ghsom = new GHSOM(new SOMLibFormatInputReader(null, unitDescriptionFileName, null));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }

        String[] dataNames = readDataNames(dataNamesFileName);

        String fDir = htmlFileName.substring(0, htmlFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        String fName = htmlFileName.substring(htmlFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        if (fName.endsWith(".html")) {
            fName = fName.substring(0, (fName.length() - 5));
        }

        try {
            new HTMLOutputter().write(ghsom, fDir, fName, dataNames);
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not open or write to output file " + htmlFileName + ": " + e.getMessage());
            System.exit(-1);
        }
    }

    private static String[] readDataNames(String fName) {
        ArrayList<String> tmpList = new ArrayList<String>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fName));
            String line = null;
            while ((line = br.readLine()) != null) {
                StringTokenizer strtok = new StringTokenizer(line, " \t", false);
                while (strtok.hasMoreTokens()) {
                    tmpList.add(strtok.nextToken());
                }
            }
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not open or read from file " + fName + " containing the data names. Aborting.");
            System.exit(-1);
        }
        if (tmpList.isEmpty()) {
            return null;
        } else {
            String[] res = new String[tmpList.size()];
            for (int i = 0; i < tmpList.size(); i++) {
                res[i] = (String) tmpList.get(i);
            }
            return res;
        }
    }

}
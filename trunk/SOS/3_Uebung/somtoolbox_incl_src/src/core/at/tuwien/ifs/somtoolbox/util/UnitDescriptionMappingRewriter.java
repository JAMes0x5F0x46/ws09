package at.tuwien.ifs.somtoolbox.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Provides rewriting of the vector labels in an unit description file.<br>
 * Note: Does not work with the "file format version 1.1" files, as they are written in a different format.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: UnitDescriptionMappingRewriter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class UnitDescriptionMappingRewriter {

    private static final String DELIMITER = "->";

    /**
     * Starts the rewriting process. Options are:
     * <ul>
     * <li>-u name of the unit description gile, mandatory</li>
     * <li>--nameMap name of the mapping file, mandatory</li>
     * <li>output name of output file, mandatory</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_UNIT_DESCRIPTION_REWRITER);

        BufferedReader br = FileUtils.openFile("Unit description file", config.getString("unitDescriptionFile"));

        BufferedWriter bw = null;
        String finalName = config.getString("output");
        bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(finalName))));

        Hashtable<String, String> mapping = readMappingFile(config.getString("nameMappingFile"));

        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("$MAPPED_VECS ")) {
                StringTokenizer strtok = new StringTokenizer(line, DELIMITER, false);
                String res = "";
                while (strtok.hasMoreTokens()) {
                    String token = strtok.nextToken();
                    String target = (String) mapping.get(token);
                    if (target == null) {
                        res += (token + DELIMITER);
                    } else {
                        res += (target + DELIMITER);
                    }
                }
                bw.write(res.substring(0, res.length() - 1));
                bw.newLine();
            } else {
                bw.write(line);
                bw.newLine();
            }
        }
        br.close();
        bw.close();
    }

    /**
     * Reads the mapping file into a <code>Hashtable</code>
     * 
     * @param fileName the mapping file.
     * @return a <code>Hashtable</code> of the mapping.
     * @throws IOException if the file can't be read.
     */
    static private Hashtable<String, String> readMappingFile(String fileName) throws IOException {
        Hashtable<String, String> res = new Hashtable<String, String>();
        res.put("$MAPPED_VECS", "$MAPPED_VECS");
        BufferedReader br = FileUtils.openFile("Name mapping file", fileName);

        String line = null;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(DELIMITER);
            res.put(parts[0], parts[1]);
        }
        br.close();
        return res;
    }

}

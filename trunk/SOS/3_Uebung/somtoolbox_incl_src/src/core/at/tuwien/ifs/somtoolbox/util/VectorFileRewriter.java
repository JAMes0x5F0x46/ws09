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
 * Re-writes an unit description file by replacing the label names of the input vectors as defined by the given mapping file.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: VectorFileRewriter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VectorFileRewriter {

    public static void main(String[] args) throws IOException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_VECTORFILE_REWRITER);

        String inputFileName = config.getString("input");
        String mappingFile = config.getString("mapping");
        String outputFileName = config.getString("output");

        BufferedReader br = FileUtils.openFile("Input vector", inputFileName);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFileName))));

        Hashtable<String, String> mapping = readMappingFile(mappingFile);

        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("$")) {
                bw.write(line);
                bw.newLine();
            } else {
                StringTokenizer strtok = new StringTokenizer(line, " ", false);
                String res = "";
                String token = strtok.nextToken();
                while (strtok.hasMoreTokens()) {
                    res += token + " ";
                    token = strtok.nextToken();
                }
                String target = (String) mapping.get(token);
                if (target == null) {
                    res += token;
                } else {
                    System.out.println(token + " -> " + target);
                    res += (String) mapping.get(token);
                }
                bw.write(res);
                bw.newLine();
            }
        }

        br.close();
        bw.close();

    }

    static private Hashtable<String, String> readMappingFile(String fName) throws IOException {
        Hashtable<String, String> res = new Hashtable<String, String>();
        res.put("$MAPPED_VECS", "$MAPPED_VECS");
        BufferedReader br = FileUtils.openFile("Mapping file", fName);

        String line = null;
        StringTokenizer strtok = null;
        while ((line = br.readLine()) != null) {
            strtok = new StringTokenizer(line, " ", false);
            String origString = null;
            String targetString = null;
            while (strtok.hasMoreTokens()) {
                origString = strtok.nextToken();
                // origString = origString.substring(0,origString.length()-4);
                strtok.nextToken();
                targetString = strtok.nextToken("\n");
                // System.out.println(origString);
                targetString = targetString.substring(11).replaceAll("\\W", "_");
                // System.out.println(targetString);
            }
            res.put(origString, targetString);
        }

        br.close();

        return res;
    }

}

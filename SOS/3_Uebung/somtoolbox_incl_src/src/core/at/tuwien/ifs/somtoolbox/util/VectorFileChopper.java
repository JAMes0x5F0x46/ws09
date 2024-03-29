package at.tuwien.ifs.somtoolbox.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Chops a vector file down to the instance names provided in a separate file.
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorFileChopper.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VectorFileChopper {

    public static void main(String[] args) throws IOException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_VECTORFILE_CHOPPER);

        String inputFileName = config.getString("input");
        String keepFile = config.getString("keepFile");
        String outputFileName = config.getString("output");
        boolean gzip = false; // TODO: make parameter

        ArrayList<String> keepInputs = FileUtils.readLinesAsList(keepFile);
        ArrayList<String> missingInputs = new ArrayList<String>(keepInputs);
        ArrayList<String> lines = new ArrayList<String>();

        int keptInputsCounter = 0;
        int lineCounter = 1;

        BufferedReader br = FileUtils.openFile("Input vector", inputFileName);
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("$")) {
                lines.add(line);
            } else {
                String[] lineElements = line.split(StringUtils.REGEX_SPACE_OR_TAB);
                String label = lineElements[lineElements.length - 1];
                if (keepInputs.contains(label)) {
                    lines.add(line);
                    keptInputsCounter++;
                    missingInputs.remove(label);
                } else {
                    // System.out.println("Skipping input " + label);
                }
            }
            System.out.print("\rReading line " + lineCounter);
            lineCounter++;
        }
        System.out.println("... finished!");

        System.out.println("Kept " + keptInputsCounter + " input vectors.");
        if (missingInputs.size() > 0) {
            System.out.println("\tDid not find the following inputs supposed to keep: " + missingInputs);
        } else {
            System.out.println("\tFound all inputs supposed to keep");
        }
        System.out.println("Writing to " + outputFileName);

        PrintWriter pw = FileUtils.openFileForWriting("Vector file", outputFileName, gzip);

        for (String string : lines) {
            if (string.trim().startsWith("$XDIM")) {
                pw.println("$XDIM " + keptInputsCounter);
            } else {
                pw.println(string);
            }
        }

        br.close();
        pw.close();
    }
}

package at.tuwien.ifs.somtoolbox.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;

import com.martiansoftware.jsap.JSAPResult;

/**
 * @author Rudolf Mayer
 * @version $Id: StringReplacer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class StringReplacer  implements SOMToolboxApp {

    public static void main(String[] args) throws IOException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_STRING_REWRITER);
        String toReplace = config.getString("replace");
        String replacement = config.getString("replacement", "");

        String fileName = config.getString("inputFile");
        String directoryName = config.getString("inputDirectory");

        if (fileName != null) {
            replaceInFile(toReplace, replacement, fileName);
        } else if (directoryName != null) {
            String[] files = new File(directoryName).list();
            for (int i = 0; i < files.length; i++) {
                toReplace = replaceInFile(toReplace, replacement, files[i]);
            }
        } else {
            System.out.println("You should specificy either the input file or directory!");
        }
    }

    private static String replaceInFile(String toReplace, String replacement, final String fileName) throws FileNotFoundException, IOException {
        System.out.println("Replacing '" + toReplace + "' with '" + replacement + "' in file '" + fileName + "'.");
        BufferedReader br = FileUtils.openFile("", fileName);
        BufferedWriter out;
        File file = new File(fileName + ".tmp");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        if (fileName.endsWith(".gz")) {
            out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fileOutputStream)));
        } else {
            out = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        }
        String line = null;
        if (toReplace.equals("+")) { // escape regex characters
            toReplace = "\\" + toReplace;
        }
        while ((line = br.readLine()) != null) {
            out.write(line.replaceAll(toReplace, replacement) + "\n");
        }
        br.close();
        out.flush();
        out.close();
        file.renameTo(new File(fileName));
        return toReplace;
    }
}

package at.tuwien.ifs.somtoolbox.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.filechooser.FileFilter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;

import com.martiansoftware.jsap.JSAPResult;

/**
 * This class bundles file-related utilities.
 * 
 * @author Rudolf Mayer
 * @author Doris Baum
 * @version $Id: FileUtils.java 2874 2009-12-11 16:03:27Z frank $
 */
public class FileUtils {

    public static class SOMDescriptionFileFilter extends FileFilter {
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String[] allowedFileEndings = { SOMLibFormatInputReader.unitFileNameSuffix, SOMLibFormatInputReader.weightFileNameSuffix,
                    SOMLibFormatInputReader.mapFileNameSuffix };

            String fileName = file.getName();
            if (fileName != null) {
                for (int i = 0; i < allowedFileEndings.length; i++) {
                    if (fileName.endsWith(allowedFileEndings[i]) || fileName.endsWith(allowedFileEndings[i] + ".gz")) {
                        return true;
                    }
                }
            }
            return false;
        }

        public String getDescription() {
            return "SOM description files";
        }
    }

    /**
     * Opens a file specified by argument <code>fileName</code> and returns a <code>BufferedReader</code>. This method opens both, uncompressed and
     * gzipped files transparent to the calling function. If the specified file is not found, the suffix .gz is appended. If this name is again not
     * found, a <code>FileNotFoundException</code> is thrown.
     * 
     * @param fileType
     * @param fileName the name of the file to open.
     * @return a <code>BufferedReader</code> to the requested file.
     * @throws FileNotFoundException if the file with the given name is not found.
     */
    public static BufferedReader openFile(String fileType, String fileName) throws FileNotFoundException {
        BufferedReader br = null;
        String gzFileName = fileName + ".gz";

        if (new File(fileName) == null || !new File(fileName).exists()) { // we don't find a file with the original file name
            if (new File(gzFileName).exists()) { // we check if a '.gz' file exists
                fileName = gzFileName; // if yes, we use this file name
            } else {
                throw new FileNotFoundException("File " + fileName + " or " + gzFileName + " not found (trying file "
                        + new File(fileName).getAbsolutePath() + ".");
            }
        }

        try {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(fileName + " is gzip compressed. Trying compressed read.");
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(fileType + " " + fileName + " not found.");
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info(fileName + " is not gzip compressed. Trying uncompressed read.");
            try {
                br = new BufferedReader(new FileReader(fileName));
            } catch (FileNotFoundException e2) {
                throw new FileNotFoundException("File " + " " + fileName + " not found.");
            }
        }
        return br;
    }

    public static PrintWriter openFileForWriting(String fileType, String fileName) throws IOException {
        return openFileForWriting(fileType, fileName, false);
    }

    public static PrintWriter openFileForWriting(String fileType, String fileName, boolean gzipped) throws IOException {
        if (gzipped) {
            return new PrintWriter(new GZIPOutputStream(new FileOutputStream(fileName.endsWith(".gz") ? fileName : fileName + ".gz")));
        } else {
            return new PrintWriter(new FileWriter(fileName));
        }
    }

    /**
     * Extracts the prefix from a SOM description filename so that the corresponding other two description files can be found
     * 
     * @param filename
     */
    public static String extractSOMLibInputPrefix(String filename) {
        String prefix = new String(filename);
        int index = prefix.indexOf(".gz");
        if (index != -1) {
            prefix = prefix.substring(0, index);
        }
        if (prefix.endsWith(".unit") || prefix.endsWith(".wgt") || prefix.endsWith(".map")) {
            prefix = prefix.substring(0, prefix.lastIndexOf("."));
        }
        return prefix;
    }

    public static String extractSOMLibDataPrefix(String filename) {
        String prefix = new String(filename);
        int index = prefix.indexOf(".gz");
        if (index != -1) {
            prefix = prefix.substring(0, index);
        }
        if (prefix.endsWith(".tv") || prefix.endsWith(".vec")) {
            prefix = prefix.substring(0, prefix.lastIndexOf("."));
        }
        return prefix;
    }

    /**
     * Reads the headers of a SOMLib File, and stores the values in a map.
     */
    public static Map<String, String> readSOMLibFileHeaders(BufferedReader br) throws IOException {
        Hashtable<String, String> map = new Hashtable<String, String>();
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#")) { // ignore comment lines
                Logger.getLogger("at.tuwien.ifs.somtoolbox").finest("Read comment '" + line + "'.");
            } else if (line.startsWith("$")) { // 
                StringTokenizer tokenizer = new StringTokenizer(line, " \t");
                String key = tokenizer.nextToken();
                String value = null;
                if (tokenizer.hasMoreElements()) {
                    value = tokenizer.nextToken("").trim();
                } else {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Header in input vector file corrupt!");
                    throw new IOException("Header in input vector file corrupt!");
                }
                map.put(key, value);
            } else if (line.length() > 0) { // we reached a content line, stop reading
                map.put("FIRST_CONTENT_LINE", line);
                return map;
            }
        }
        return map;
    }

    public static String[] findAllSOMLibFiles(JSAPResult config, final String optNameInputs, final String optNameInputDir,
            final String extensionToFind, String extensionToCheck) {
        String[] inputs = config.getStringArray(optNameInputs);
        String inputDirectory = config.getString(optNameInputDir);

        if (((inputs == null || inputs.length == 0) && inputDirectory == null) || ((inputs != null && inputs.length > 0) && inputDirectory != null)) {
            System.out.println("You need to specify exactly one out of '" + optNameInputs + "' or '" + optNameInputDir + "'");
            System.exit(-1);
        }

        if (inputDirectory != null) {
            File dir = new File(inputDirectory);
            System.out.println("Checking for input files in " + dir.getAbsolutePath());
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(extensionToFind) || name.endsWith(extensionToFind + ".gz");
                }
            });
            Arrays.sort(files);
            ArrayList<String> validInputs = new ArrayList<String>();
            for (File file : files) {
                String baseFileName = StringUtils.stripSuffix(file.getAbsolutePath());
                if (extensionToCheck != null && new File(baseFileName + extensionToCheck).exists()
                        || new File(baseFileName + extensionToCheck + ".gz").exists()) {
                    validInputs.add(baseFileName);
                    System.out.println("Adding input " + baseFileName);
                } else {
                    System.out.println("Found template vector file '" + file.getAbsolutePath() + "', but no fitting '" + extensionToCheck + "' or '"
                            + extensionToCheck + ".gz' file as '" + new File(baseFileName + extensionToCheck).getAbsolutePath() + "', skipping!");
                }
            }
            inputs = validInputs.toArray(new String[validInputs.size()]);
        } else {
            // we should validate that we also have the matching other type of files present, i.e. all .vec files for all .tv files, etc..
        }
        return inputs;
    }

    public static String stripPathPrefix(final String fileName) {
        if (fileName.contains(File.separator)) {
            return fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        } else {
            return fileName;
        }
    }

    public static void copyFile(String source, String destination) throws FileNotFoundException, IOException, SOMToolboxException {
        // some checks on whether we can write..
        if (!new File(source).canRead()) {
            throw new SOMToolboxException("Can't read from source file '" + source + "'. Not copying file.");
        }
        new File(destination).createNewFile();
        if (!new File(destination).canWrite()) {
            throw new SOMToolboxException("Can't write to destination file '" + destination + "'. Not copying file.");
        }
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.flush();
        out.close();

    }

    public static boolean fileStartsWith(String fileName, String match) throws FileNotFoundException, IOException {
        CharBuffer cbuf = CharBuffer.allocate(match.length());
        BufferedReader reader = openFile("", fileName);
        reader.read(cbuf);
        reader.close();
        return cbuf.toString().equals(match);
    }

    public static String[] readLines(String filename) throws IOException {
        ArrayList<String> lines = readLinesAsList(filename);
        return lines.toArray(new String[lines.size()]);
    }

    public static ArrayList<String> readLinesAsList(String filename) throws FileNotFoundException, IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        ArrayList<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

    public static String prepareOutputDir(String dir) {
        if (org.apache.commons.lang.StringUtils.isNotEmpty(dir) && !dir.endsWith(File.separator)) {
            return dir + File.separator;
        } else {
            return dir;
        }
    }

    public static void clearOutputDir(String outputDir) {
        File outDir = new File(outputDir);
        if (outDir.exists() && outDir.isDirectory()) {
            File[] content = outDir.listFiles();
            for (File element : content) {
                element.delete();
            }
        }
        outDir.mkdir();
    }

    public static void writeToFile(String content, String pathname) throws IOException {
        File file = new File(pathname);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(content);
        output.close();
    }

    public static String getPathFrom(String unitDescriptionFileName) {
        try {
            return new File(unitDescriptionFileName).getParentFile().getAbsolutePath();
        } catch (Exception e) {
            if (unitDescriptionFileName.contains("/")) {
                return unitDescriptionFileName.substring(0, unitDescriptionFileName.lastIndexOf("/") - 1);
            } else if (unitDescriptionFileName.contains("\\")) {
                return unitDescriptionFileName.substring(0, unitDescriptionFileName.lastIndexOf("\\") - 1);
            }
        }
        return "";
    }

}
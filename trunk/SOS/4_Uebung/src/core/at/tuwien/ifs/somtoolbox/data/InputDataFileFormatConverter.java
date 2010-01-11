package at.tuwien.ifs.somtoolbox.data;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;

import com.martiansoftware.jsap.JSAPResult;

/**
 * This class converts between various file formats for trained SOMs. Currently supported formats are listed in {@link #INPUT_FILE_FORMAT_TYPES} and {@link #OUTPUT_FILE_FORMAT_TYPES} respective.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputDataFileFormatConverter.java 2949 2009-12-15 15:21:15Z frank $
 */
public class InputDataFileFormatConverter implements SOMToolboxApp {
    private static final HashMap<String, String> knownExtensions = new HashMap<String, String>();

    private static final HashMap<String, Class<? extends AbstractSOMLibSparseInputData>> outputClasses = new HashMap<String, Class<? extends AbstractSOMLibSparseInputData>>();

    private static final HashMap<String, Class<? extends AbstractSOMLibSparseInputData>> inputClasses = new HashMap<String, Class<? extends AbstractSOMLibSparseInputData>>();

    static {
        // knownExtensions.put(InputData.inputFileNameSuffix, AbstractSOMLibSparseInputData.getFormatName());
        // knownExtensions.put(".arff", ARFFFormatInputData.getFormatName());
        // knownExtensions.put(".bin", RandomAccessFileSOMLibInputData.getFormatName());
        // knownExtensions.put(".matrix", SimpleMatrixInputData.getFormatName());
        String[] inputDataTypes = new String[] { "at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData",
                "at.tuwien.ifs.somtoolbox.data.ARFFFormatInputData", "at.tuwien.ifs.somtoolbox.data.RandomAccessFileSOMLibInputData",
                "at.tuwien.ifs.somtoolbox.data.SimpleMatrixInputData" };
        for (String inputDataType : inputDataTypes) {
            try {
                @SuppressWarnings("unchecked")
                Class<AbstractSOMLibSparseInputData> c = (Class<AbstractSOMLibSparseInputData>) Class.forName(inputDataType);
                String name = (String) c.getMethod("getFormatName", new Class<?>[] {}).invoke(null, new Object[] {});
                String ext = (String) c.getMethod("getFileNameSuffix", new Class<?>[] {}).invoke(null, new Object[] {});
                knownExtensions.put(ext, name);
            } catch (Exception e) {
            }
        }

        String[] inFC = new String[] { "at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData", "at.tuwien.ifs.somtoolbox.data.ARFFFormatInputData",
                "at.tuwien.ifs.somtoolbox.data.RandomAccessFileSOMLibInputData", "at.tuwien.ifs.somtoolbox.data.SimpleMatrixInputData",
                "at.tuwien.ifs.somtoolbox.data.MarsyasARFFInputData" };
        ArrayList<String> stringArrayBuilder = new ArrayList<String>();
        for (String string : inFC) {
            try {
                @SuppressWarnings("unchecked")
                Class<AbstractSOMLibSparseInputData> c = (Class<AbstractSOMLibSparseInputData>) Class.forName(string);
                String name = (String) c.getMethod("getFormatName", new Class<?>[] {}).invoke(null, new Object[] {});
                stringArrayBuilder.add(name);
                inputClasses.put(name, c);
            } catch (Exception e) {
            }
        }
        INPUT_FILE_FORMAT_TYPES = inputClasses.keySet().toArray(new String[inputClasses.size()]);

        String[] outFC = new String[] { "at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData", "at.tuwien.ifs.somtoolbox.data.ARFFFormatInputData",
                "at.tuwien.ifs.somtoolbox.data.RandomAccessFileSOMLibInputData" };
        stringArrayBuilder = new ArrayList<String>();
        for (String string : outFC) {
            try {
                @SuppressWarnings("unchecked")
                Class<AbstractSOMLibSparseInputData> c = (Class<AbstractSOMLibSparseInputData>) Class.forName(string);
                String name = (String) c.getMethod("getFormatName", new Class<?>[] {}).invoke(null, new Object[] {});
                stringArrayBuilder.add(name);
                outputClasses.put(name, c);
            } catch (Exception e) {
            }
        }
        OUTPUT_FILE_FORMAT_TYPES = outputClasses.keySet().toArray(new String[outputClasses.size()]);

    }

    private static final Logger logger = Logger.getLogger("at.tuwien.ifs.somtoolbox");

    /** Supported Input File Format Types */
    public static final String[] INPUT_FILE_FORMAT_TYPES;// = { AbstractSOMLibSparseInputData.getFormatName(), ARFFFormatInputData.getFormatName(),

    // RandomAccessFileSOMLibInputData.getFormatName(), SimpleMatrixInputData.getFormatName(), MarsyasARFFInputData.getFormatName() };

    /** Supported Output File Format Types */
    public static final String[] OUTPUT_FILE_FORMAT_TYPES;// = { AbstractSOMLibSparseInputData.getFormatName(), ARFFFormatInputData.getFormatName(),

    // RandomAccessFileSOMLibInputData.getFormatName() };

    /**
     * Method for stand-alone execution.
     */
    public static void main(String[] args) throws IOException, SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_INPUT_DATA_FILEFORMAT_CONVERTER);

        String inputFileName = config.getString("input");
        String templateVectorFile = config.getString("templateVectorFile");
        String classInformationFile = config.getString("classInformationFile");
        String fName = config.getString("output");
        boolean skipInstanceNames = config.getBoolean("skipInstanceNames");
        boolean skipInputsWithoutClass = config.getBoolean("skipInputsWithoutClass");
        boolean tabSeparatedClassFile = config.getBoolean("tabSeparatedClassFile");

        String inputFormat = config.getString("inputFormat");
        if (inputFormat == null) {
            inputFormat = detectFormat(inputFileName, "input");
        }
        String outputFormat = config.getString("outputFormat");
        if (outputFormat == null) {
            outputFormat = detectFormat(fName, "output");
        }

        // FIXME: check if range checking can't be done with JSAP itself
        if (!ArrayUtils.contains(INPUT_FILE_FORMAT_TYPES, inputFormat)) {
            throw new SOMToolboxException("Invalid input format '" + inputFormat + "'!. valid values are: "
                    + Arrays.toString(INPUT_FILE_FORMAT_TYPES));
        }
        if (!ArrayUtils.contains(OUTPUT_FILE_FORMAT_TYPES, outputFormat)) {
            throw new SOMToolboxException("Invalid output format '" + outputFormat + "'!. valid values are: "
                    + Arrays.toString(OUTPUT_FILE_FORMAT_TYPES));
        }

        InputData data = null;

        // read the specific input date
        // if (inputFormat.equals(AbstractSOMLibSparseInputData.getFormatName())) {
        // logger.info("Reading SOMLib Input data format.");
        // data = new SOMLibSparseInputData(inputFileName, templateVectorFile, classInformationFile);
        // } else if (inputFormat.equals(RandomAccessFileSOMLibInputData.getFormatName())) {
        // logger.info("Reading RandomAccessFile SOMLib Input data format.");
        // data = new RandomAccessFileSOMLibInputData(inputFileName);
        // } else if (inputFormat.equals(ARFFFormatInputData.getFormatName())) {
        // logger.info("Reading ARFF Input data format.");
        // data = new ARFFFormatInputData(inputFileName);
        // } else if (inputFormat.equals(MarsyasARFFInputData.getFormatName())) {
        // logger.info("Reading Marsyas ARFF format.");
        // data = new MarsyasARFFInputData(inputFileName);
        // } else if (inputFormat.equals(SimpleMatrixInputData.getFormatName())) {
        // logger.info("Reading simple matrix format.");
        // data = new SimpleMatrixInputData(inputFileName);
        // } else {
        // // check for logical programming mistakes, basically
        // throw new SOMToolboxException("Didn't parse input format of type '" + inputFormat + "', most likely a programming error.");
        // }
        data = getInputDataInstance(inputFormat, inputFileName);
        if (templateVectorFile != null)
            data.setTemplateVector(new SOMLibTemplateVector(templateVectorFile));
        if (classInformationFile != null)
            data.setClassInfo(new SOMLibClassInformation(classInformationFile));

        // write
        if (outputFormat.equals(AbstractSOMLibSparseInputData.getFormatName())) {
            logger.info("Writing SOMLib Data Format.");
            if (data.templateVector() != null) {
                data.templateVector().writeToFile(fName + ".tv");
            }
            if (data.classInformation() != null) {
                if (tabSeparatedClassFile) {
                    data.classInformation().writeToFileTabSeparated(fName + ".cls");
                } else {
                    data.classInformation().writeToFile(fName + ".cls");
                }
            }
            data.writeToFile(fName + ".vec");
        } else if (outputFormat.equals(RandomAccessFileSOMLibInputData.getFormatName())) {
            logger.info("Writing Random Access Binary Data Format.");
            RandomAccessFileSOMLibInputData.write(data, fName);
        } else if (outputFormat.equals("ARFF")) {
            // FIXME: Hard Coded string, should be solved via refelction
            logger.info("Writing ARFF Data Format, skipping instance names: " + skipInstanceNames);
            data.writeAsWekaARFF(fName, !skipInstanceNames, skipInputsWithoutClass);
        } else {
            // check for logical programming mistakes, basically
            throw new SOMToolboxException("Didn't write format of type '" + inputFormat + "', most likely a programming error.");
        }
    }

    private static InputData getInputDataInstance(String formatName, String inputFileName) throws SOMToolboxException {
        try {
            Class<? extends AbstractSOMLibSparseInputData> c = inputClasses.get(formatName);
            if (c == null) {
                throw new SOMToolboxException("Unknown Format: '" + formatName + "', possible formats are: " + inputClasses.keySet());
            }
            Constructor<? extends AbstractSOMLibSparseInputData> constr = c.getConstructor(String.class);
            return constr.newInstance(inputFileName);
        } catch (SOMToolboxException e) {
            throw e; // just throw it on
        } catch (Exception e) {
            throw new SOMToolboxException("Could not instanciate reader for '" + formatName + "': " + e.getMessage());
        }
    }

    private static String detectFormat(String inputFileName, String type) throws SOMToolboxException {
        logger.info("No " + type + " format specified, detecting from file extension...");
        if (inputFileName.endsWith(".gz")) {
            inputFileName = inputFileName.substring(0, inputFileName.length() - 3);
        }
        for (String extension : knownExtensions.keySet()) {
            if (inputFileName.endsWith(extension)) {
                logger.info("... found '" + extension + "' extension, assuming '" + knownExtensions.get(extension));
                return knownExtensions.get(extension);
            }
        }
        throw new SOMToolboxException("Unknown " + type + " format for file '" + inputFileName + "', please specify the " + type
                + " format via the option.");
    }
}

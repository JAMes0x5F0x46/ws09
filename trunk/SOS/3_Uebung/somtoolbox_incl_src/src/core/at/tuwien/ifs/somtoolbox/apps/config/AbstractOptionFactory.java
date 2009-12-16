package at.tuwien.ifs.somtoolbox.apps.config;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxMain;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;

public class AbstractOptionFactory {

    public static Logger logger;

    public static JSAPResult parseResults(Class<?> callingClass, String[] args, Parameter... options) {
        logger = Logger.getLogger(callingClass.getPackage().getName());
        return parseResults(args, registerOptions(options), callingClass.getName());
    }

    public static JSAPResult parseResults(Class<?> callingClass, String[] args, boolean printParameterValues, Parameter... options) {
        JSAPResult parseResults = parseResults(args, registerOptions(options), callingClass.getName());
        if (printParameterValues)
            logger.info(AbstractOptionFactory.toString(parseResults, options));
        return parseResults;
    }

    public static JSAPResult parseResults(String[] args, JSAP jsap) {
        return parseResults(args, jsap, null);
    }

    public static JSAPResult parseResults(String[] args, Parameter... options) {
        return parseResults(args, registerOptions(options), null);
    }

    public static JSAPResult parseResults(String[] args, boolean printParameterValues, Parameter... options) {
        JSAPResult parseResults = parseResults(args, registerOptions(options), null);
        if (printParameterValues) {
            logger.info(AbstractOptionFactory.toString(parseResults, options));
        }
        return parseResults;
    }

    public static JSAPResult parseResults(String[] args, JSAP jsap, String className) {
        try {
            jsap.registerParameter(new Switch("help", JSAP.NO_SHORTFLAG, "help", "Print this help and exit."));
        } catch (JSAPException e2) {
        }
        try {
            jsap.registerParameter(new Switch("version", JSAP.NO_SHORTFLAG, "version", "Print the version and exit."));
        } catch (JSAPException e1) {
        }
        JSAPResult config = jsap.parse(args);

        if (className == null) {
            className = computeClassName();
        }

        if (config.getBoolean("help")) {
            printHelp(jsap, className, System.out);
            System.exit(0);
        }
        if (config.getBoolean("version")) {
            printVersion(className);
            System.exit(0);
        }

        if (!config.success()) {
            printUsage(jsap, className, config, null);
        }
        try {
            logger = Logger.getLogger(Class.forName(className).getPackage().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static String toString(JSAPResult result, Parameter[] options) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < options.length; i++) {
            String optionsId = options[i].getID();
            String optionsValue = String.valueOf(result.getObject(optionsId));
            sb.append(optionsId + ": " + optionsValue + "\n");
        }
        return sb.toString();
    }

    private static String computeClassName() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!stackTraceElement.getClassName().equals(AbstractOptionFactory.class.getName())) {
                return stackTraceElement.getClassName();
            }
        }
        return "<unknown class>";
    }

    public static void printUsage(JSAP jsap, String className, JSAPResult config, String errorMessage) {
        // print out specific error messages describing the problems
        // with the command line, THEN print usage, THEN print full
        // help. This is called "beating the user with a clue stick."
        System.err.println();
        for (Iterator<?> errs = config.getErrorMessageIterator(); errs.hasNext();) {
            System.err.println("Error: " + errs.next());
        }
        if (errorMessage != null && !errorMessage.trim().equals("")) {
            System.err.println("Error: " + errorMessage);
        }
        System.err.println();
        printHelp(jsap, className, System.err);
        System.exit(-1);
    }

    private static void printHelp(JSAP jsap, String className, PrintStream outStream) {
        outStream.println("Usage: java " + className + " " + jsap.getUsage());
        outStream.println();
        outStream.println("Options:");
        // Replacements for better man-pages
        outStream.println(jsap.getHelp(120).replaceAll("[\\[\\]()]", "").replaceAll("\\|", ", "));
    }

    private static void printVersion(String className) {
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        SOMToolboxMain.printVersion(simpleClassName);
    }

    /**
     * Register a given set of options to the given JSAP object.
     * 
     * @param jsap The JSAP to register options to.
     * @param options The options to register.
     */
    public static void registerOptions(JSAP jsap, Parameter[] options) {
        for (Parameter element : options) {
            try {
                jsap.registerParameter(element);
            } catch (JSAPException e) {
                logger.severe(e.getMessage());
            }
        }
    }

    public static JSAP registerOptions(Parameter[] options) {
        JSAP jsap = new JSAP();
        for (Parameter element : options) {
            try {
                jsap.registerParameter(element);
            } catch (JSAPException e) {
                logger.severe(e.getMessage());
            }
        }
        return jsap;
    }
}
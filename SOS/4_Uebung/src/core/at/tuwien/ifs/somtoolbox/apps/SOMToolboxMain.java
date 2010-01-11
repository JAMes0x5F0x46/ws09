/*
 * Created on Dec 1, 2009 Version: $Id: $
 */

package at.tuwien.ifs.somtoolbox.apps;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;

/**
 * Searches the classpath for classes implementig the {@link SOMToolboxApp} interface.
 * 
 * @author Jakob Frank
 * @version $Id: $
 */
public class SOMToolboxMain {

    /**
     * Central Main for SOMToolbox.
     * @param args the Commandline args
     */
    public static void main(String[] args) {
        String cols = System.getenv("COLUMNS");
        int screenWidth;
        try {
            screenWidth = Integer.parseInt(cols);
        } catch (Exception e) {
            screenWidth = 80;
        }

        ArrayList<Class<? extends SOMToolboxApp>> runnables = SubClassFinder.findSubclassesOf(SOMToolboxApp.class, true);
        if (args.length > 0 && !args[0].startsWith("-")) {
            boolean exe = false;
            for (Class<? extends SOMToolboxApp> runnable : runnables) {
                if (runnable.getSimpleName().equalsIgnoreCase(args[0])) {
                    try {
                        Method main = runnable.getMethod("main", String[].class);
                        main.invoke(null, new Object[] { Arrays.copyOfRange(args, 1, args.length) });
                        exe = true;
                        break;
                    } catch (InvocationTargetException e) {
                        // If main throws an exception, print it...
                        e.getCause().printStackTrace();
                        exe = true;
                    } catch (Exception e) {
                        // Everything else is hidden...
                    }
                }
            }
            if (!exe) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Runnable \"" + args[0] + "\" not found.");
                printAvailableRunnables(screenWidth, runnables);
            }
        } else if (args.length > 0 && args[0].equalsIgnoreCase("--list-mains")) {
            printAvailableRunnables(screenWidth, runnables);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("--version")) {
            printVersion("somtoolbox");
        } else if (args.length > 0 && args[0].equalsIgnoreCase("--help")) {
            if (args.length > 1) {
                boolean found = false;
                for (Class<? extends SOMToolboxApp> runnable : runnables) {
                    if (runnable.getSimpleName().equalsIgnoreCase(args[1])) {
                        try {
                            Method main = runnable.getMethod("main", String[].class);
                            main.invoke(null, new Object[] { new String[] { "--help" } });
                            found = true;
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!found) {
                    printHelp();
                }
            } else {
                printHelp();
            }
        } else {
            printAvailableRunnables(screenWidth, runnables);
        }
    }

    private static void printHelp() {
        System.out.println("somtoolbox " + SOMToolboxApp.VERSION);
        System.out.println();
        System.out.println("Usage:");
        System.out.println("\tsomtoolbox --list-mains");
        System.out.println("\tsomtoolbox --version");
        System.out.println("\tsomtoolbox --help [<Runnable>]");
        System.out.println("\tsomtoolbox <Runnable> ...");
        System.out.println();
        System.out.println("Description:");
        System.out.println("\tsomtoolbox is a framework for creating, viewing, exploring and");
        System.out.println("\tanalysing Self-Organising Maps (SOMs).");
        System.out.println();
        System.out.println("\tsomtoolbox can create different types of SOMs, including (but not");
        System.out.println("\tlimited to) GrowingSOMs, HierachicalSOMs, ToroidSOMs, 3D-Soms,");
        System.out.println("\tMenomicSOMs.");
        System.out.println();
        System.out.println("\tsomtoolbox can apply different visualisations to a SOM, including");
        System.out.println("\t(but not limited to U-, U*-, D- and P-Matrix; Smoothed Data Histogram,");
        System.out.println("\tGradient Field and many more");
        System.out.println();
        System.out.println("\tsomtoolbox is an academic prototype under constant development.");
        System.out.println();
        System.out.println("Options:");
        System.out.println("\t--list-mains");
        System.out.println("\t\tList available Runnables and exit.");
        System.out.println();
        System.out.println("\t--version");
        System.out.println("\t\tPrint version and exit.");
        System.out.println();
        System.out.println("\t--help [<Runnable>]");
        System.out.println("\t\tPrint this help or the <Runnable>-specific help message");
        System.out.println();
        System.out.println("\t<Runnable> ...");
        System.out.println("\t\tExecute the specified <Runnable>,");
        System.out.println("\t\tsee somtoolbox --help <Runnable> for details");
        System.out.println();
        System.out.println(SOMToolboxApp.DEV_BY_STRING);
        System.out.println("somtoolbox home page: <" + SOMToolboxApp.HOMEPAGE + ">");
    }

    /**
     * @param screenWidth the with of the screen
     * @param runnables {@link ArrayList} of available runnables.
     */
    private static void printAvailableRunnables(int screenWidth, ArrayList<Class<? extends SOMToolboxApp>> runnables) {
        Comparator<Class<? extends SOMToolboxApp>> somToolboxAppComp = new Comparator<Class<? extends SOMToolboxApp>>() {
            @Override
            public int compare(Class<? extends SOMToolboxApp> o1, Class<? extends SOMToolboxApp> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        };
        Collections.sort(runnables, somToolboxAppComp);

        ArrayList<String> runnableNamesList = new ArrayList<String>();
        ArrayList<String> runnableDeskr = new ArrayList<String>();

        for (Class<? extends SOMToolboxApp> c : runnables) {
            try {
                // Ignore abstract classes and interfaces
                if (Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) {
                    continue;
                }
                runnableNamesList.add(c.getSimpleName());

                String desk = null;
                try {
                    Field f = c.getDeclaredField("DESCRIPTION");
                    desk = (String) f.get(null);
                } catch (Exception e) {
                }

                if (desk != null) {
                    runnableDeskr.add(desk);
                } else {
                    runnableDeskr.add("");
                }
            } catch (SecurityException e) {
                // Should not happen - no Security
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        int maxLen = StringUtils.getLongestStringLength(runnableNamesList);
        System.out.println("Runnable classes: ");
        for (int i = 0; i < runnableNamesList.size(); i++) {

            System.out.print("  " + runnableNamesList.get(i) + StringUtils.getSpaces(maxLen - runnableNamesList.get(i).length() + 2));
            if (maxLen + 4 + runnableDeskr.get(i).length() > screenWidth) {
                System.out.println();
                System.out.print(StringUtils.getSpaces(8));
            }
            System.out.println(runnableDeskr.get(i));
        }
    }

    /**
     * 
     */
    public static void printVersion(String executable) {
        System.out.println(executable + " " + SOMToolboxApp.VERSION);
        System.out.println();
        System.out.println(SOMToolboxApp.DEV_BY_STRING);
        System.out.println();
        System.out.println(SOMToolboxApp.HOMEPAGE);
        System.out.println();
    }

}

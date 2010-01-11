package at.tuwien.ifs.somtoolbox.visualization;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMInputReader;
import at.tuwien.ifs.somtoolbox.util.ExtensionFilenameFilter;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;

/**
 * @author Rudolf Mayer
 * @version $Id: Visualizations.java 2884 2009-12-11 18:37:22Z mayer $
 */
public class Visualizations {

    private static final String[] EXCLUDE_CLASSES = new String[] { "SmoothedCountHistograms" };

    public static BackgroundImageVisualizer[] singleton;

    public static int maxVariants = 0;

    private static Integer initFrom = null;

    public static BackgroundImageVisualizer[] getAvailableVisualizations() {
        if (singleton == null) {
            ArrayList<BackgroundImageVisualizer> visClasses = new ArrayList<BackgroundImageVisualizer>();
            ArrayList<String> visClassNames = new ArrayList<String>();

            ArrayList<Class<? extends BackgroundImageVisualizer>> viss = SubClassFinder.findSubclassesOf(BackgroundImageVisualizer.class);
            for (Class<? extends BackgroundImageVisualizer> vis : viss) {
                // Ignore abstract classes and interfaces
                if (Modifier.isAbstract(vis.getModifiers()) || Modifier.isInterface(vis.getModifiers())) {
                    continue;
                }

                // Ignore exclude classes
                if (StringUtils.equalsAny(vis.getSimpleName(), EXCLUDE_CLASSES)) {
                    continue;
                }

                // Ignore 3D vis (without directly referencing them)
                boolean is3DVis = false;
                final Class<?>[] interfaces = vis.getInterfaces();
                for (Class<?> i : interfaces) {
                    if (i.getSimpleName().equals("TerrainHeightGenerator")) {
                        is3DVis = true;
                        break;
                    }
                }
                if (is3DVis) {
                    continue;
                }

                try {
                    visClasses.add(vis.newInstance());
                    visClassNames.add(vis.getSimpleName());
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").severe("Error loading visualisation class : " + vis.getName());
                    e.printStackTrace();
                }
            }
            if (visClasses.size() == 0) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").severe("Did not find any matching visualisation classes. Aborting.");
                System.exit(-1);
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info("Found " + visClasses.size() + " visualisation classes.");
            }

            singleton = visClasses.toArray(new BackgroundImageVisualizer[visClasses.size()]);
            Arrays.sort(singleton);

            Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info(
                    "Registered total of " + singleton.length + " visualisations " + StringUtils.toString(visClassNames) + ".");
        }
        return singleton;
    }

    /**
     * Constructs an array of all known visualisations. If you implement a new visualisation, you have to adapt this method.
     * 
     * @return all known visualisations.
     */
    public static BackgroundImageVisualizer[] getAvailableVisualizationsO() {
        if (singleton == null) {
            ArrayList<BackgroundImageVisualizer> visClasses = new ArrayList<BackgroundImageVisualizer>();
            ArrayList<String> visClassNames = new ArrayList<String>();

            String path = "/" + Visualizations.class.getName().replaceAll("\\.", "/") + ".class";
            // load the classes dynamically from the source directory
            URL visDir = Visualizations.class.getResource(path);

            String packageName = Visualizations.class.getPackage().getName() + ".";

            ArrayList<String> classesList = new ArrayList<String>();

            String[] classes = null;

            if (visDir.toExternalForm().startsWith("jar:")) { // load from jar file
                Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info("Trying to load visualisations from JAR file @ : " + visDir);
                try {
                    JarFile jarFile = ((JarURLConnection) visDir.openConnection()).getJarFile();
                    JarEntry jarEntry = jarFile.getJarEntry(visDir.toExternalForm().split("!")[1].substring(1));
                    String[] split2 = jarEntry.getName().split("/");
                    String pathToVisualisations = jarEntry.getName().substring(0, jarEntry.getName().indexOf(split2[split2.length - 1]));
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) { // check all entries in the jar file (there is no File.list() equivalen for jar files
                        JarEntry entry = entries.nextElement();
                        // check if this entry is in the same path as our root
                        if (entry.getName().startsWith(pathToVisualisations) && !entry.getName().replaceAll(pathToVisualisations, "").contains("/")) {
                            if (entry.getName().endsWith(".class")) {
                                String replaceAll = entry.getName().replace(pathToVisualisations, "").replaceAll("/", ".");
                                classesList.add(replaceAll);
                            }
                        }
                    }
                    classes = classesList.toArray(new String[classesList.size()]);
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info(
                            "Found " + classesList.size() + " visualisation classes in jar file.");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    classes = new File(visDir.toURI()).getParentFile().list(new ExtensionFilenameFilter("class"));
                    if (ArrayUtils.isEmpty(classes)) {
                        Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").severe(
                                "Did not find any class files in directory '" + visDir.getFile() + "'. Aborting.");
                        System.exit(-1);
                    }
                } catch (URISyntaxException e1) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").severe(
                            "Did not find any class files in directory '" + visDir.getFile() + "'. Aborting.");
                    System.exit(-1);
                }
            }
            Arrays.sort(classes);
            Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info(
                    "Searching for plugin classes in " + visDir.getFile() + ", found " + classes.length + " potential classes.");
            for (String classe : classes) {
                String className = classe.substring(0, classe.length() - 6);
                String qualifiedClassName = packageName + className;
                try {
                    final Class<?> c = Class.forName(qualifiedClassName);
                    if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && BackgroundImageVisualizer.class.isAssignableFrom(c)
                            && !StringUtils.equalsAny(className, EXCLUDE_CLASSES)) {
                        visClasses.add((BackgroundImageVisualizer) c.getConstructor().newInstance());
                        visClassNames.add(c.getSimpleName());
                        Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").fine("Registered visualisation " + qualifiedClassName);
                    }
                } catch (Exception e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").severe("Error loading visualisation class : " + qualifiedClassName);
                    e.printStackTrace();
                }
            }
            if (visClasses.size() == 0) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").severe(
                        "Did not find any matching visualisation classes in directory '" + visDir.getFile() + "'. Aborting.");
                System.exit(-1);
            }

            Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info(
                    "Registered total of " + visClasses.size() + " visualisations " + StringUtils.toString(visClassNames) + ".");
            singleton = visClasses.toArray(new BackgroundImageVisualizer[visClasses.size()]);
            Arrays.sort(singleton);
        }
        return singleton;
    }

    public static BackgroundImageVisualizer[] getReadyVisualizations() {
        ArrayList<BackgroundImageVisualizer> vis = new ArrayList<BackgroundImageVisualizer>();
        for (BackgroundImageVisualizer v : getAvailableVisualizations()) {
            if (ArrayUtils.isEmpty(v.needsAdditionalFiles())) { // only take vis that have all files
                vis.add(v);
                // System.out.println("adding " + v);
            } else {
                // System.out.println("skipping " + v);
            }
        }
        return vis.toArray(new BackgroundImageVisualizer[vis.size()]);
    }

    public static String[] getReadyVisualizationNames() {
        BackgroundImageVisualizer[] readyVisualizations = getReadyVisualizations();
        ArrayList<String> names = new ArrayList<String>(readyVisualizations.length * 2);
        for (BackgroundImageVisualizer readyVisualization : readyVisualizations) {
            BackgroundImageVisualizer vis = readyVisualization;
            for (int j = 0; j < vis.getNumberOfVisualizations(); j++) {
                names.add(readyVisualization.getVisualizationShortName(j));
            }
        }
        return names.toArray(new String[names.size()]);
    }

    public static void initVisualizations(SharedSOMVisualisationData inputObjects, SOMInputReader reader, int defaultPaletteIndex,
            Palette defaultPalette, Palette[] palettes) {
        initVisualizations(inputObjects, reader, defaultPaletteIndex, defaultPalette, palettes, null);
    }

    /**
     * Initialises all registered visualisation - sets the {@link SharedSOMVisualisationData} input objects, the {@link MapPNode} map, and default
     * palettes.
     */
    public static void initVisualizations(SharedSOMVisualisationData inputObjects, SOMInputReader reader, int defaultPaletteIndex,
            Palette defaultPalette, Palette[] palettes, MapPNode map) {
        // only initialise if we did not do it before, or if we have new data (indicated by a new hashcode of SharedSOMVisualisationData
        if (inputObjects != null && initFrom != null && inputObjects.dataHashCode() == initFrom) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info("Not initialising visualisations again, provided data is still the same.");
            return;
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization").info("Initialising visualisations...");
        initFrom = inputObjects.dataHashCode();
        maxVariants = 0;
        BackgroundImageVisualizer[] visualizations = getAvailableVisualizations();
        for (BackgroundImageVisualizer visualization : visualizations) {
            visualization.setSOMData(reader);
            visualization.setInputObjects(inputObjects);
            visualization.setMap(map);

            if (visualization instanceof AbstractMatrixVisualizer) {
                ((AbstractMatrixVisualizer) visualization).setPalette(defaultPaletteIndex, defaultPalette);
                ((AbstractMatrixVisualizer) visualization).reversePalette(); // FIXME: why reverse??
            }
            maxVariants = Math.max(maxVariants, visualization.getNumberOfVisualizations());
        }
    }

    /**
     * Tries to locate a visualisation by the given name.
     * 
     * @param name the name of the visualisation.
     * @return the visualisation matching the given name, or <code>null</code> otherwise.
     */
    public static BackgroundImageVisualizerInstance getVisualizationByName(String name) {
        BackgroundImageVisualizer[] availableVisualizations = getAvailableVisualizations();
        for (BackgroundImageVisualizer vis : availableVisualizations) {
            for (int j = 0; j < vis.getNumberOfVisualizations(); j++) {
                if (vis.getVisualizationName(j).equals(name) || vis.getVisualizationShortName(j).equals(name)) {
                    return new BackgroundImageVisualizerInstance(vis, j);
                }
            }
        }
        return null;
    }

    public static String getVisualizationShortName(String longName) {
        BackgroundImageVisualizerInstance vis = getVisualizationByName(longName);
        return vis.getVis().getVisualizationShortName(vis.getVariant());
    }

    public static String[] getAvailableVisualizationNames() {
        BackgroundImageVisualizer[] availableVisualizations = getAvailableVisualizations();
        ArrayList<String> names = new ArrayList<String>(availableVisualizations.length * 2);
        for (BackgroundImageVisualizer vis : availableVisualizations) {
            for (int j = 0; j < vis.getNumberOfVisualizations(); j++) {
                names.add(vis.getVisualizationName(j) + " (or " + vis.getVisualizationShortName(j) + " )");
            }
        }
        return names.toArray(new String[names.size()]);
    }

}

/*
 * Created on Nov 30, 2009 Version: $Id: SubClassFinder.java 2874 2009-12-11 16:03:27Z frank $
 */

package at.tuwien.ifs.somtoolbox.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;

/**
 * Utility class to find subclasses of a class/interface.
 * 
 * @author frank
 * @version $Id: SubClassFinder.java 2874 2009-12-11 16:03:27Z frank $
 * @see #findSubclassesOf(Class, boolean)
 */
public class SubClassFinder {

    /**
     * For testing: Searches and prints all subclasses of {@link BackgroundImageVisualizer}.
     * 
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            List<Class<? extends BackgroundImageVisualizer>> viss = findSubclassesOf(BackgroundImageVisualizer.class);
            System.out.println("Available Visualisations:");
            for (Class<? extends BackgroundImageVisualizer> vis : viss) {
                if (!(Modifier.isAbstract(vis.getModifiers()) && Modifier.isInterface(vis.getModifiers()))) {
                    System.out.println("  " + vis.getSimpleName());
                }
            }
        } else {
            for (String cls : args) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<Object> c = (Class<Object>) Class.forName(cls);
                    List<Class<? extends Object>> viss = findSubclassesOf(c);
                    System.out.println("Subclasses of " + cls);
                    for (Class<?> vis : viss) {
                        if (!(Modifier.isAbstract(vis.getModifiers()) && Modifier.isInterface(vis.getModifiers()))) {
                            System.out.println("\t" + vis.getName());
                        }
                    }
                    System.out.println();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Find all subclasses of the given <code>superClass</code>. Searches through the complete classpath.
     * 
     * @param <E>
     * @param superClass the class
     * @return ArrayList containing all found subclasses.
     */
    public static <E> ArrayList<Class<? extends E>> findSubclassesOf(Class<E> superClass) {
        return findSubclassesOf(superClass, false);
    }

    /**
     * Find all subclasses of the given <code>superClass</code>. Searches through the complete classpath.
     * 
     * @param <E>
     * @param superClass the class (e.g. <code>Object.class</code>)
     * @param onlyDirect it <code>true</code> only <b>direct</b> subclasses are found, i.e. classes that directly extend/implement
     *            <code>superClass</code>
     * @return ArrayList containing all found subclasses.
     * @see #checkClass(Class, Class, List, boolean)
     */
    public static <E> ArrayList<Class<? extends E>> findSubclassesOf(Class<E> superClass, boolean onlyDirect) {

        ArrayList<Class<? extends E>> result = new ArrayList<Class<? extends E>>();
        String[] classPathEntries = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        Pattern p = Pattern.compile("somtoolbox", Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < classPathEntries.length; i++) {
            File f = new File(classPathEntries[i]);
            if (f.isDirectory()) {
                searchDir(f, superClass, result, onlyDirect);
            } else if (f.getName().endsWith(".jar")) {
                /*
                 * FIXME: hard-coded classpath! For the future we should consider a "plugin" directory to search through, and a "lib" directory with
                 * jars to ignore here...
                 */

                if (p.matcher(f.getName()).find()) {
                    searchJarFile(f, superClass, result, onlyDirect);
                }
            }
        }
        return result;

    }

    /**
     * Recursivly search through the given directory for subclasses of <code>dir</code>.
     * 
     * @param <E>
     * @param dir the Directory to search through.
     * @param superClass the class to search subclasses of.
     * @param resultList List where to add found subclasses to.
     * @param onlyDirect only consider direct subclasses.
     * @see #checkClass(Class, Class, List, boolean)
     */
    private static <E> void searchDir(File dir, Class<E> superClass, List<Class<? extends E>> resultList, boolean onlyDirect) {
        searchDir(dir, superClass, resultList, "", onlyDirect);
    }

    /**
     * Recursivly search through the given directory for subclasses of <code>dir</code>.
     * 
     * @param <E>
     * @param dir the Directory to search through.
     * @param superClass the class to search subclasses of.
     * @param resultList List where to add found subclasses to.
     * @param relPath relative path within dir to search from, needed for recursion.
     * @param onlyDirect only consider direct subclasses.
     * @see #searchDir(File, Class, List, boolean)
     */
    private static <E> void searchDir(File dir, Class<E> superClass, List<Class<? extends E>> resultList, String relPath, boolean onlyDirect) {
        // System.out.println(f.getPath() + " -- " + relPath);
        File current = new File(dir, relPath);
        File[] entries = current.listFiles();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].isDirectory()) {
                searchDir(dir, superClass, resultList, relPath + entries[i].getName() + "/", onlyDirect);
            } else if (entries[i].getName().endsWith(".class")) {
                String cname = entries[i].getName();
                String e = relPath + cname.substring(0, cname.length() - 6);
                String fqClassName = e.replace('/', '.');
                // System.out.println(" " + fqClassName);
                checkClass(fqClassName, superClass, resultList, onlyDirect);
            }
        }
    }

    /**
     * Check if the class <code>fqClassName</code> is a subclass of <code>c</code>.
     * 
     * @param <E>
     * @param fqClassName the fully qualified class name to check
     * @param c the superClass
     * @param result ArrayList where to add <code>fqClassName</code> if it is a subclass
     * @param onlyDirect if <code>true</code> only consider direct subclasses, i.e. classes that directly extend/implement <code>superClass</code>
     * @see #checkClass(Class, Class, List, boolean)
     */
    private static <E> void checkClass(String fqClassName, Class<E> c, List<Class<? extends E>> result, boolean onlyDirect) {
        try {
            // if (fqClassName.contains("$")) return;
            Class<?> d = Class.forName(fqClassName, false, c.getClassLoader());
            // Check if d is a subclass of / is implementing c
            checkClass(d, c, result, onlyDirect);
        } catch (ClassNotFoundException e1) {
        } catch (NoClassDefFoundError e2) {
        }
    }

    /**
     * Check if the class <code>fqClassName</code> is a subclass of <code>c</code>.
     * 
     * @param <E>
     * @param subClass the class to check
     * @param superClass the superClass
     * @param resultList ArrayList where to add <code>fqClassName</code> if it is a subclass
     * @param onlyDirect if <code>true</code> only consider direct subclasses, i.e. classes that directly extend/implement <code>superClass</code>
     * @see #checkClass(Class, Class, List, boolean)
     */
    private static <E> void checkClass(Class<?> subClass, Class<E> superClass, List<Class<? extends E>> resultList, boolean onlyDirect) {
        if (superClass.isAssignableFrom(subClass)) {
            // System.err.println(fqClassName);
            @SuppressWarnings("unchecked")
            Class<? extends E> d2 = (Class<? extends E>) subClass;

            if (onlyDirect) { // Add only if d2 is a direct subclass of c
                if (ArrayUtils.contains(d2.getInterfaces(), superClass))
                    resultList.add(d2);
            } else {
                resultList.add(d2);
            }
        }
    }

    /**
     * Search through the jar-file <code>jarFile</code> for subclasses of <code>superClass</code>.
     * 
     * @param <E>
     * @param jarFile the jar-file to search
     * @param superClass the superClass
     * @param resultList ArrayList where to add subClasses of <code>superClass</code>
     * @param onlyDirectif <code>true</code> only consider direct subclasses, i.e. classes that directly extend/implement <code>superClass</code>
     * @see #checkClass(Class, Class, List, boolean)
     */
    private static <E> void searchJarFile(File jarFile, Class<E> superClass, List<Class<? extends E>> resultList, boolean onlyDirect) {
        try {
            JarFile jFile = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.toString().endsWith(".class")) {
                    String fname = entry.toString();
                    String cpath = fname.substring(0, fname.length() - 6);
                    String fqClassName = cpath.replace('/', '.');
                    checkClass(fqClassName, superClass, resultList, onlyDirect);
                }
            }
            jFile.close();
        } catch (IOException e) {
        }
    }

}

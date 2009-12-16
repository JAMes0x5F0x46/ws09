package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.util.ExtensionFilenameFilter;

/**
 * This class collects all available palettes.
 * 
 * @author Rudolf Mayer
 * @version $Id: Palettes.java 2874 2009-12-11 16:03:27Z frank $
 */
public class Palettes {
    // we need to find the palettes dir dynamically from the class path, using user.dir or other approaches won't work
    public static String DEFAULT_PALETTES_DIR = "rsc/palettes";

    private static Logger log = Logger.getLogger("at.tuwien.ifs.somtoolbox.visualization.Palettes");

    private static Palette defaultPalette;

    private static ArrayList<Palette> paletteList;

    public static Palette getDefaultPalette() {
        if (paletteList == null) {
            initPalettes();
        }
        return defaultPalette;
    }

    public static int getPaletteIndex(Palette palette) {
        for (int i = 0; i < paletteList.size(); i++) {
            if (paletteList.get(i) == palette) {
                return i;
            }
        }
        return 0;
    }

    public static Palette[] getAvailablePalettes() {
        if (paletteList == null) {
            initPalettes();
        }
        return paletteList.toArray(new Palette[paletteList.size()]);
    }

    public static void addPalette(Palette palette) {
        paletteList.add(palette);
    }

    private static void initPalettes() {
        paletteList = new ArrayList<Palette>();
        String palettesDir = DEFAULT_PALETTES_DIR;
        CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance();
        if (state != null && state.getSOMViewerProperties() != null && StringUtils.isNotBlank(state.getSOMViewerProperties().getPalettesDir())) {
            palettesDir = state.getSOMViewerProperties().getPalettesDir();
        }
        URL url = Palettes.class.getResource("/rsc/palettes/");

        if (url.toExternalForm().startsWith("jar:")) { // load from jar file
            log.info("Trying to load palettes from JAR file @ : " + url);
            try {
                JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                String[] jarURL = url.toExternalForm().split("!");
                // JarEntry jarEntry = jarFile.getJarEntry(jarURL[1].substring(1));
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) { // check all entries in the jar file (there is no File.list() equivalen for jar files
                    JarEntry entry = (JarEntry) entries.nextElement();
                    if (entry.getName().startsWith(jarURL[1].substring(1))) { // check if this entry is in the same path as our root
                        String s = entry.getName().substring(jarURL[1].substring(1).length());
                        while (s.length() > 0 && s.startsWith("/")) { // remove all possibly existing leading slashes
                            s = s.substring(1);
                        }
                        if (s.indexOf("/") == -1) { // if we don't have any more /, we have an entry (and not another dir)
                            if (s.length() > 0) {
                                paletteList.add(Palette.loadPaletteFromXML(jarFile, jarURL[1].substring(1) + s));
                            }
                        }
                    }
                }
                log.info("Loaded " + paletteList.size() + " palettes.");
            } catch (SOMToolboxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            log.info("Trying to load palettes from file-system @ : " + url); // load from file system
            try {
                File dir = new File(url.toURI());
                if (dir.isDirectory()) {
                    File[] list = dir.listFiles(new ExtensionFilenameFilter("xml"));
                    for (File element : list) {
                        try {
                            paletteList.add(Palette.loadPaletteFromXML(element));
                        } catch (Exception e) {
                            log.warning("Error reading palettes from dir " + palettesDir + ": " + e.getMessage()
                                    + "!. Loading some default palettes...");
                            initPredefinedPalettes();
                        }
                    }
                    log.info("Loaded " + paletteList.size() + " palettes.");
                } else {
                    log.warning(palettesDir + " is not a directory. Loading some default palettes...");
                    initPredefinedPalettes();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (paletteList.size() == 0) {
            // if the dir was correct, but empty, also load some defaults
            log.warning(palettesDir + " did not contain any palettes! Loading default palettes...");
            initPredefinedPalettes();
        }
        // sort the list alphabetically
        Collections.sort(paletteList);

        defaultPalette = getPaletteByName("CartographyLessWater256");
        if (defaultPalette == null && paletteList.size() > 0) {
            defaultPalette = paletteList.get(0);
        }
        log.info("Default palette " + defaultPalette.getName());

    }

    private static void initPredefinedPalettes() {
        /* Gradient palettes should be created using the at.tuwien.ifs.somtoolbox.visualisation.ColorGradientFactory */

        paletteList = new ArrayList<Palette>();

        /* 256 Colour palette */
        Palette colorPalette256 = new Palette("Cartography Color, 256 Gradient", "Cartography256", "",
                ColorGradientFactory.CartographyColorGradient(), 256);
        paletteList.add(colorPalette256);

        /* 256 Colour palette, less water */
        Palette colorPalette256LessWater = new Palette("Cartography Color, 256 Gradient, less water", "CartographyLessWater256", "",
                ColorGradientFactory.CartographyColorGradientLessWater(), 256);
        paletteList.add(colorPalette256LessWater);

        /* 8 grayscale palette */
        paletteList.add(new Palette("Grayscale, 8 Gradient", "Grayscale8", "Grayscale palette well suited for b/w screenshots",
                ColorGradientFactory.GrayscaleGradient(), 8));

        /* 32 red-scale palette */
        Color[] tmpColors = new Color[32];
        for (int c = 0; c < tmpColors.length; c++) {
            int r = 255;
            int g = 255 - (int) (c * 225d / tmpColors.length);
            int b = 255 - (int) (c * 255d / tmpColors.length);
            tmpColors[c] = new Color(r, g, b);
        }
        paletteList.add(new Palette("Redscale, 32 Gradient", "Redscale32", "Original palette for the Component Planes", tmpColors));

        /* 16 Grayscale palette for paper screenshots */
        tmpColors = new Color[16];
        for (int c = 0; c < tmpColors.length; c++) {
            int r = 50 + (int) (c * 205d / tmpColors.length);
            int g = 50 + (int) (c * 205d / tmpColors.length);
            int b = 50 + (int) (c * 205d / tmpColors.length);
            tmpColors[c] = new Color(r, g, b);
        }
        paletteList.add(new Palette("Grayscale, 16 Gradient", "Grayscale16", "Grayscale palette well suited for b/w screenshots", tmpColors));

        /* 256 grayscale palette */
        tmpColors = new Color[256];
        for (int c = 0; c < tmpColors.length; c++) {
            int r = 0 + (int) (c * 255d / tmpColors.length);
            int g = 0 + (int) (c * 255d / tmpColors.length);
            int b = 0 + (int) (c * 255d / tmpColors.length);
            tmpColors[c] = new Color(r, g, b);
        }
        paletteList.add(new Palette("GrayScale, 256 Gradient", "Grayscale256", "Grayscale palette well suited for b/w screenshots", tmpColors));

        /* Angela: Testpalette "Mountains1a" */
        paletteList.add(new Palette("Cartography Color, Mountains 1a, 256 Gradient", "Mountains256-1a", "",
                ColorGradientFactory.CartographyMountain1aGradient(), 256));

        /* Angela: Testpalette "Mountains1b" */
        paletteList.add(new Palette("Cartography Color, Mountains 1b, 256 Gradient", "Mountains256-1b", "",
                ColorGradientFactory.CartographyMountain1bGradient(), 256));

        /* Angela: Testpalette "Mountains2a" */
        paletteList.add(new Palette("Cartography Color, Mountains 2a, 256 Gradient", "Mountains256-2a", "",
                ColorGradientFactory.CartographyMountain2aGradient(), 256));

        /* Angela: Testpalette "Mountains2b mit tuerkis" */
        paletteList.add(new Palette("Cartography Color, Mountains 2b, 256 Gradient", "Mountains256-2b", "",
                ColorGradientFactory.CartographyMountain2bGradient(), 256));

        /* Angela: Testpalette "Mountains2c mit tuerkis2" */
        paletteList.add(new Palette("Cartography Color, Mountains 2c, 256 Gradient", "Mountains256-2c", "",
                ColorGradientFactory.CartographyMountain2cGradient(), 256));

        /* Angela: Testpalette "Mountains2d" */
        paletteList.add(new Palette("Cartography Color, Mountains 2d, 256 Gradient", "Mountains256-2d", "",
                ColorGradientFactory.CartographyMountain2dGradient(), 256));

        /* Angela: Testpalette "Mountains10" */
        tmpColors = new Color[10];
        tmpColors[0] = new Color(175, 240, 232);
        tmpColors[1] = new Color(220, 250, 178);
        tmpColors[2] = new Color(83, 183, 59);
        tmpColors[3] = new Color(101, 148, 52);
        tmpColors[4] = new Color(240, 157, 1);
        tmpColors[5] = new Color(139, 11, 0);
        tmpColors[6] = new Color(104, 40, 15);
        tmpColors[7] = new Color(139, 95, 70);
        tmpColors[8] = new Color(189, 189, 189);
        tmpColors[9] = new Color(254, 252, 255);
        paletteList.add(new Palette("Cartography Color, Mountains 10, 10 Colors", "Mountains10", "", tmpColors));

        /* Angela: Testpalette RGB */
        paletteList.add(new Palette("RGB, 256 Gradient", "RGB256", "", ColorGradientFactory.RGBGradient(), 256));

        /* Angela: Random Colors :) */
        tmpColors = new Color[256];
        Random rnd = new Random();
        for (int c = 0; c < tmpColors.length; c++) {
            tmpColors[c] = new Color(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
        }
        paletteList.add(new Palette("Random Colours (256)", "Random256", "", tmpColors));

        /* Angela: Random Colors :) more colors */
        tmpColors = new Color[2048];
        for (int c = 0; c < tmpColors.length; c++) {
            tmpColors[c] = new Color(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
        }
        paletteList.add(new Palette("Random Colours (2048)", "Random2048", "", tmpColors));

        /* Angela: Random Colors :) more light colors */
        tmpColors = new Color[2048];
        for (int c = 0; c < tmpColors.length; c++) {
            tmpColors[c] = new Color(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat(), 0.6f);
        }
        paletteList.add(new Palette("Random Colours (2048, lighter)", "Random2048lighter", "", tmpColors));

        /* Angela: Random Colors :) more light colors */
        tmpColors = new Color[256];
        for (int c = 0; c < tmpColors.length; c++) {
            float a = rnd.nextFloat();
            tmpColors[c] = new Color(a, a, a, 0.6f);
        }
        Palette randomPalette3 = new Palette("Random Gray", "RandomGray", "", tmpColors);
        paletteList.add(randomPalette3);

        /* Test palette for Crime Data */
        Color[] crimeDataColors = { new Color(255, 51, 51), new Color(255, 149, 51), new Color(255, 255, 51), new Color(255, 255, 129),
                new Color(210, 255, 147), new Color(197, 255, 163), new Color(161, 255, 203), new Color(141, 255, 219), new Color(89, 217, 255),
                new Color(98, 177, 255), new Color(98, 156, 255) };
        paletteList.add(new Palette("Crime Data", "Crime", "", crimeDataColors));

        /* GIS-like colour palette */
        Color[] gisColors = { new Color(55, 165, 75), new Color(115, 195, 125), new Color(140, 205, 145), new Color(170, 160, 100),
                new Color(195, 185, 130), new Color(235, 225, 165), new Color(200, 155, 100), new Color(180, 120, 70), new Color(185, 90, 35) };
        paletteList.add(new Palette("GIS colours", "GIS", "", gisColors));

        // for the smooth palette, each colour of the GIS palette is surrounded by two more colours with very similar values
        // formatting is to have the similar colors staying on one line
        Color[] gisColorsSmooth = { new Color(55, 165, 75), new Color(60, 170, 80), //
                new Color(110, 190, 120), new Color(115, 195, 125), new Color(120, 200, 130), //
                new Color(135, 200, 140), new Color(140, 205, 145), new Color(145, 210, 150), //
                new Color(170, 160, 100), new Color(170, 160, 100), new Color(170, 160, 100), //
                new Color(190, 180, 125), new Color(195, 185, 130), new Color(200, 190, 135), //
                new Color(230, 220, 160), new Color(235, 225, 165), new Color(230, 220, 160), //
                new Color(205, 160, 105), new Color(200, 155, 100), new Color(195, 150, 95), //
                new Color(185, 125, 75), new Color(180, 120, 70), new Color(185, 115, 65),//
                new Color(185, 95, 40), new Color(185, 90, 35) };
        paletteList.add(new Palette("GIS colours, smooth", "GISsmooth", "", gisColorsSmooth));

        /* old SOMToolbox 0.4.x palette */
        tmpColors = new Color[128];

        for (int c = 0; c < tmpColors.length / 2; c++) {
            int r = 0 + (int) (c * 255d / (tmpColors.length / 2d));
            int g = 255;
            int b = 0;
            tmpColors[c] = new Color(r, g, b);
        }
        for (int c = tmpColors.length / 2; c < tmpColors.length; c++) {
            int r = 255 - (int) ((c - tmpColors.length / 2) * 255d / (tmpColors.length / 2d));
            int g = 255 - (int) ((c - tmpColors.length / 2) * 255d / (tmpColors.length / 2d));
            int b = 0 + (int) ((c - tmpColors.length / 2) * 255d / (tmpColors.length / 2d));
            tmpColors[c] = new Color(r, g, b);
        }
        paletteList.add(new Palette("Cartography Color, 128 Gradient (SOMToolbox 0.4.x)", "Cartography128",
                "The original palette from SOMToolbox 0.4.x; might need to reverse it to get the original look&feel", tmpColors));

        /* Sky palette - yellow */
        Palette galaxyYellow = new Palette("Sky with Galaxies - Yellow", "SkyYellow", "A night view of galaxies on the sky",
                ColorGradientFactory.galaxyGradientYellow(), 256);
        paletteList.add(galaxyYellow);

        /* Sky palette -grey */
        Palette galaxyGrey = new Palette("Sky with Galaxies - Grey", "SkyGrey", "A night view of galaxies on the sky",
                ColorGradientFactory.galaxyGradientGrey(), 256);
        paletteList.add(galaxyGrey);

        Collections.sort(paletteList);

        defaultPalette = colorPalette256LessWater; // set as the default palette
        log.info("Loaded " + paletteList.size() + " predefined palettes.");
    }

    public static Palette getPaletteByName(String name) {
        if (paletteList == null) {
            initPalettes();
        }
        for (int i = 0; i < paletteList.size(); i++) {
            Palette palette = paletteList.get(i);
            if (palette.getName().equals(name) || palette.getShortName().equals(name)) {
                return palette;
            }
        }
        return null;
    }

    public static Color[] reversePalette(Palette palette) {
        Color[] colors = palette.getColors();
        ArrayUtils.reverse(palette.getColors());
        return colors;
    }

}

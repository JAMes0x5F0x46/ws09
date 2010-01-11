package at.tuwien.ifs.somtoolbox.apps.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.input.SOMLibMapDescription;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

/**
 * @author Rudolf Mayer
 * @version $Id: ServerSOM.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ServerSOM {
    public static final String basicDirectory = "map";

    public static final String mapFile = "som.map";

    public static final String inputDirectory = basicDirectory + "/input/";

    public static final String outputDirectory = basicDirectory + "/output/";

    public static String labelsPath = basicDirectory + "/labelImages/";

    public static final String prefix = "fodok";

    public static final String SEPARATOR = java.io.File.separator;

    public String weightVectorFileName;

    public String unitDescriptionFileName;

    public String mapDescriptionFileName;

    public String templateVectorFileName;

    public String inputVectorFileName;

    public String dataWinnerMappingFileName;

    public String classInformationFile;

    public SOMLibFormatInputReader somdata;

    public SharedSOMVisualisationData inputDataObjects;

    public GrowingSOM growingSOM;

    public GrowingLayer growingLayer;

    private Palette defaultPalette;

    public Palette[] palettes;

    public int defaultPaletteIndex;

    private int currentPalette;

    private String dataInformationFile;

    private String linkageMapFile = null;

    public static ArrayList<BackgroundImageVisualizerInstance> availableVis = new ArrayList<BackgroundImageVisualizerInstance>();

    public void load(String som_path) throws ServletException {
        // weightVectorFileName = som_path + "/" + outputDirectory + prefix + ".wgt.gz";
        // unitDescriptionFileName = som_path + "/" + outputDirectory + prefix + ".unit.gz";
        // mapDescriptionFileName = som_path + "/" + outputDirectory + prefix + ".map.gz";

        // Some visualizations need addtional files
        // templateVectorFileName = som_path + "/" + inputDirectory + "fodok.tv";
        // inputVectorFileName = som_path + "/" + inputDirectory + "fodoknorm.tfxidf";

        // dataWinnerMappingFileName = som_path + "/" + outputDirectory + prefix + ".dwm-small.gz";

        // classInformationFile = som_path + "/" + inputDirectory + prefix + ".cls";
        // System.out.println("\n\n\n***************************************************");
        info("path: " + som_path);
        try {
            final String path = som_path + "/" + basicDirectory;
            SOMLibMapDescription mapDescription = new SOMLibMapDescription(path + "/" + mapFile);
            weightVectorFileName = mapDescription.getProperty(SOMLibMapDescription.URL_WEIGHT_VECTOR);
            if (weightVectorFileName != null && !weightVectorFileName.startsWith("/")) {
                weightVectorFileName = path + "/" + weightVectorFileName;
            }
            unitDescriptionFileName = mapDescription.getProperty(SOMLibMapDescription.URL_UNIT_DESCRIPTION);
            if (unitDescriptionFileName != null && !unitDescriptionFileName.startsWith("/")) {
                unitDescriptionFileName = path + "/" + unitDescriptionFileName;
            }
            templateVectorFileName = mapDescription.getProperty(SOMLibMapDescription.URL_TEMPLATE_VECTOR);
            if (templateVectorFileName != null && !templateVectorFileName.startsWith("/")) {
                templateVectorFileName = path + "/" + templateVectorFileName;
            }
            dataInformationFile = mapDescription.getProperty(SOMLibMapDescription.URL_TRAINING_VECTOR_DESCRIPTION);
            if (dataInformationFile != null && !dataInformationFile.startsWith("/")) {
                dataInformationFile = path + "/" + dataInformationFile;
            }
            dataWinnerMappingFileName = mapDescription.getProperty(SOMLibMapDescription.URL_DATA_WINNER_MAPPING);
            if (dataWinnerMappingFileName != null && !dataWinnerMappingFileName.startsWith("/")) {
                dataWinnerMappingFileName = path + "/" + dataWinnerMappingFileName;
            }
            classInformationFile = mapDescription.getProperty(SOMLibMapDescription.URL_CLASS_INFO);
            if (classInformationFile != null && !classInformationFile.startsWith("/")) {
                classInformationFile = path + "/" + classInformationFile;
            }
            labelsPath = mapDescription.getProperty(SOMLibMapDescription.URL_LABELS);
            if (labelsPath != null && !labelsPath.startsWith("/")) {
                labelsPath = path + "/" + labelsPath;
            }
            if (StringUtils.isNotBlank(labelsPath) && !labelsPath.endsWith("/")) {
                labelsPath += "/";
            }
            info("reading files in readsomfiles");
            info(weightVectorFileName);
            info(unitDescriptionFileName);
            info(mapDescriptionFileName);
            info(classInformationFile);
            info(templateVectorFileName);

            somdata = new SOMLibFormatInputReader(weightVectorFileName, unitDescriptionFileName, mapDescriptionFileName);

            String[] split = mapDescription.getProperty(SOMLibMapDescription.AVAILABLE_VIS).split(",");
            for (int i = 0; i < split.length; i++) {
                String[] split2 = split[i].split("=");
                BackgroundImageVisualizerInstance vis = Visualizations.getVisualizationByName(split2[0].trim());
                vis.setDisplayName(split2[1].trim());
                availableVis.add(vis);
            }

            if (classInformationFile != null && !new File(classInformationFile).canRead()) {
                System.out.println("*** Cannot read class info file " + new File(classInformationFile).getAbsolutePath());
                classInformationFile = null;
            }

            // dataInformationFile = som_path + "/" + inputDirectory + "fodok.dataInfo";
            if (dataInformationFile != null && !new File(dataInformationFile).canRead()) {
                System.out.println("*** Cannot read data info file " + new File(dataInformationFile).getAbsolutePath());
                dataInformationFile = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ServletException("Server SOM: Couldn't find one of the SOM Files: " + e.getMessage(), e);
            // Logger.getLogger("at.tuwien.ifs.somtoolbox.server").info("SOMMap: Couldn't find one of the SOM Files.");
            // info("SOMMap: Stopping Service.");
        } catch (SOMLibFileFormatException e) {
            e.printStackTrace();
            throw new ServletException("Server SOM:  Format of one of the SOMFiles is corrupt: " + e.getMessage(), e);
            // Logger.getLogger("at.tuwien.ifs.somtoolbox.server").info("SOMMap: Format of one of the SOMFiles is corrupt.");
            // info("SOMMap: Stopping Service.");
            // return false;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServletException("Server SOM: Couldn't read one of the SOM Files: " + e.getMessage(), e);
            // TODO Auto-generated catch block
        }
        // return true;

        inputDataObjects = new SharedSOMVisualisationData(classInformationFile, null, dataInformationFile, dataWinnerMappingFileName,
                inputVectorFileName, templateVectorFileName, linkageMapFile);
        inputDataObjects.readAvailableData();

        // create GrowingSOM
        growingSOM = new GrowingSOM(somdata);
        growingSOM.setSharedInputObjects(inputDataObjects);
        growingLayer = growingSOM.getLayer();
        palettes = Palettes.getAvailablePalettes();
        Visualizations.initVisualizations(inputDataObjects, somdata, defaultPaletteIndex, defaultPalette, palettes);
        // ((ThematicClassMapVisualizer)Visualizations.getVisualizationByShortName("ClassMap")[0]).setZoom(zoom);
    }

    private void info(String message) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox.server").info(message);
    }

    public Palette getDefaultPalette() {
        return defaultPalette;
    }

    public void setDefaultPalette(Palette defaultPalette) {
        this.defaultPalette = defaultPalette;
        defaultPaletteIndex = Palettes.getPaletteIndex(defaultPalette);
        currentPalette = defaultPaletteIndex;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 150; i++) {
            System.out.println(i + "\t" + "input " + i + "\t" + "location" + i);
        }
    }

}

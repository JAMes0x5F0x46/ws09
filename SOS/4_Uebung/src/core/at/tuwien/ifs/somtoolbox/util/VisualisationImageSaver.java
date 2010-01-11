package at.tuwien.ifs.somtoolbox.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Save Visualisations of a map to an image file.
 * 
 * @author Jakob Frank
 * @author Rudolf Mayer
 * @version $Id: VisualisationImageSaver.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VisualisationImageSaver  implements SOMToolboxApp {

    public static void main(String[] args) {
        JSAPResult res = OptionFactory.parseResults(args, OptionFactory.OPTIONS_VISUALISATION_IMAGE_SAVER);

        String uFile = res.getString("unitDescriptionFile");
        String wFile = res.getString("weightVectorFile");
        String dwmFile = res.getString("dataWinnerMappingFile");
        String cFile = res.getString("classInformationFile");
        String vFile = res.getString("inputVectorFile");
        String tFile = res.getString("templateVectorFile");
        String ftype = res.getString("filetype");
        boolean unitGrid = res.getBoolean("unitGrid");

        String basename = res.getString("basename");
        if (basename == null) {
            basename = FileUtils.extractSOMLibInputPrefix(uFile);
        }
        int unitW = res.getInt("width");

        String[] vizs = res.getStringArray("vis");

        GrowingSOM gsom = null;
        CommonSOMViewerStateData state = CommonSOMViewerStateData.getInstance();
        try {
            SOMLibFormatInputReader inputReader = new SOMLibFormatInputReader(wFile, uFile, null);
            gsom = new GrowingSOM(inputReader);

            SharedSOMVisualisationData d = new SharedSOMVisualisationData(cFile, null, null, dwmFile, vFile, tFile, null);
            d.readAvailableData();
            state.inputDataObjects = d;
            state.loadPalettes();
            gsom.setSharedInputObjects(d);

            Visualizations.initVisualizations(d, inputReader, 0, Palettes.getDefaultPalette(), Palettes.getAvailablePalettes());

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        } catch (SOMLibFileFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }

        if (ArrayUtils.isEmpty(vizs)) {
            System.out.println("No specific visualisation specified - saving all available visualisations.");
            vizs = Visualizations.getReadyVisualizationNames();
            System.out.println("Found " + vizs.length + ": " + Arrays.toString(vizs));
        }

        for (String viz : vizs) {
            BackgroundImageVisualizerInstance v = Visualizations.getVisualizationByName(viz);
            if (v == null) {
                System.out.println("Visualization '" + viz + "' not found!");
                continue;
            }
            BackgroundImageVisualizer i = v.getVis();

            GrowingLayer layer = gsom.getLayer();
            try {
                int height = unitW * layer.getYSize();
                int width = unitW * layer.getXSize();
                HashMap<String, BufferedImage> visualizationFlavours = i.getVisualizationFlavours(v.getVariant(), gsom, width, height);
                ArrayList<String> keys = new ArrayList<String>(visualizationFlavours.keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    File out = new File(basename + "_" + viz + key + "." + ftype);
                    System.out.println("Generating visualisation '" + viz + "' as '" + out.getPath() + "'.");
                    BufferedImage image = visualizationFlavours.get(key);
                    if (unitGrid) {
                        AbstractBackgroundImageVisualizer.drawUnitGrid(image, gsom, width, height);
                    }
                    ImageIO.write(image, ftype, out);
                }
            } catch (SOMToolboxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}

package at.tuwien.ifs.somtoolbox.apps.viewer.fileutils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GenericPNodeScrollPane;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.PieChartPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.RhythmPattern;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Label;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer;

/**
 * This class provides methods to export visualisations as images and complete maps as HTML.
 * 
 * @author Rudolf Mayer
 * @version $Id: ExportUtils.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ExportUtils {
    public static final String DATA_ITEM_SEPERATOR = "&#10;&#13;";

    public static final String RESOURCE_PATH_XHTML = "/rsc/export/xhtml/";

    public static final String RESOURCE_PATH_ICONS = "/rsc/export/images/";

    public static final String RESOURCE_PATH_CSS = "/rsc/export/css/";

    public static BufferedImage scaleBackgroundImage(CommonSOMViewerStateData state, int width) throws SOMToolboxException {
        return scaleImage(state.mapPNode.getBackgroundImage(), width);
    }

    public static BufferedImage scaleImage(BufferedImage buim, int width) {
        int height = (int) ((double) width / ((double) buim.getWidth() / (double) buim.getHeight()));
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaledImage.createGraphics().drawImage(buim.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        return scaledImage;
    }

    public static BufferedImage scaleImageByHeight(BufferedImage buim, int height) {
        int width = (int) ((double) height / ((double) buim.getHeight() / (double) buim.getWidth()));
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaledImage.createGraphics().drawImage(buim.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        return scaledImage;
    }

    public static void drawClassInfo(GrowingLayer growingLayer, MapPNode mapPnode, double unitWidth, Graphics2D graphics) {
        for (int x = 0; x < growingLayer.getXSize(); x++) {
            for (int y = 0; y < growingLayer.getYSize(); y++) {
                try {
                    if (growingLayer.getUnit(x, y) != null && growingLayer.getUnit(x, y).getNumberOfMappedInputs() > 0) {
                        double sizeFactor = 1.1; // you can choose the relative size of the pie charts here
                        double offset = unitWidth * (1.0 - sizeFactor) / 2; // moves the pie chart automatically by half the reduced proportion
                        PieChartPNode classPieChart = (PieChartPNode) mapPnode.getUnit(x, y).getClassPieChart((int) (unitWidth * sizeFactor),
                                (int) (unitWidth * sizeFactor));

                        // for some reason y offset was still slightly off -> move by 2 pixel
                        classPieChart.drawChart(graphics, x * unitWidth + offset, y * unitWidth + offset + 2);
                    }
                } catch (LayerAccessException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }
        }
    }

    public static void drawLinkInfo(GrowingLayer growingLayer, MapPNode mapPnode, double unitWidth, Graphics2D graphics, String dataFilesPrefix) {
        int width = (int) unitWidth;
        for (int x = 0; x < growingLayer.getXSize(); x++) {
            for (int y = 0; y < growingLayer.getYSize(); y++) {
                try {
                    if (growingLayer.getUnit(x, y) != null) {
                        String[] mappedData = growingLayer.getUnit(x, y).getMappedInputNames();
                        if (mappedData != null) {
                            boolean hasValidLink = false;
                            for (int i = 0; i < mappedData.length; i++) {
                                if (new File(dataFilesPrefix + mappedData[i]).exists()) {
                                    hasValidLink = true;
                                    break;
                                }
                            }
                            if (hasValidLink) {
                                try {
                                    BufferedImage icon = ImageIO.read(new FileInputStream(ExportUtils.class.getResource(
                                            RESOURCE_PATH_ICONS + "note.png").getFile()));
                                    // icon = scaleImageByHeight(icon, (int) (unitWidth - 2));
                                    int iconHeight = (int) (unitWidth * 0.7);
                                    int iconWidth = (int) (((double) iconHeight / (double) icon.getHeight()) * icon.getWidth());
                                    int restWidth = (width - iconWidth) / 2;
                                    int restHeight = (width - iconHeight) / 2;
                                    graphics.drawImage(icon, (x * width) + restWidth, (y * width) + restHeight, iconWidth, iconHeight, null);
                                } catch (FileNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (LayerAccessException e) {
                    // should not happen
                    e.printStackTrace();
                }

            }
        }
    }

    public void saveImageMap(GrowingLayer layer, int unitWidth, String fullPath, String baseFileName, String[][] visualisations, boolean isAudioSOM,
            SOMLibClassInformation classInfo, Color[] colors, Properties cleanDataNamesMapping, String inputDataFilesPrefix,
            String outputDataFilesPrefix, String htmlTemplatesDir, String imageMapTitle, boolean generateRhythmPatterns, boolean forceLinkGeneration)
            throws SOMToolboxException {

        String XHTML_FRAMESET = "";
        String XHTML_FRAMESET_INDEXFRAME = "";
        String XHTML_FRAMESET_UNITDETAILS = "";
        String IMAGE_MAP_PAGE = "";
        String UNIT_DETAIL_PAGE = "";
        String VISNAV_PAGE = "";

        String absolutePath = new File((fullPath + "x").substring(0, (fullPath + "x").lastIndexOf(File.separator))).getAbsolutePath();

        String detailsDir = fullPath + "_details/";
        String detailsDirRelative = baseFileName + "_details/";
        new File(detailsDir).mkdirs();

        String playlistDir = fullPath + "_playlist/";
        String playlistDirRelative = baseFileName + "_playlist/";
        if (isAudioSOM) {
            new File(playlistDir).mkdir();
        }

        String imageMapDir = fullPath + "_map/";
        String imageMapBaseName = baseFileName + "_map/";
        new File(imageMapDir).mkdir();

        String imageDir = fullPath + "_map/"; // if changed here, must be changed in ExportDialog.java as well
        String imageDirRelative = "../" + baseFileName + "_map/";
        new File(imageDir).mkdir();

        // We currently don't use fileTpye icons for generic files
        // (may be later introduced for images, videos, etc.)
        String iconFileType = ""; // = "file.png";
        String unitDetailsTarget = "unitDetails";
        if (isAudioSOM) {
            iconFileType = "note.png";
            unitDetailsTarget = "unitDetailsIndex";
        }

        // copy icons
        // copyResource(imageDir, RESOURCE_PATH_ICONS, iconFileType);
        if (isAudioSOM) {
            copyResource(imageDir, RESOURCE_PATH_ICONS, "play.png");
            copyResource(imageDir, RESOURCE_PATH_ICONS, "download.gif");
            copyResource(detailsDir, RESOURCE_PATH_ICONS, "rp_horizontal_scale.gif");
            copyResource(detailsDir, RESOURCE_PATH_ICONS, "rp_vertical_scale.gif");
        }

        // copy HTML style sheets & templates
        if (htmlTemplatesDir == null) {
            htmlTemplatesDir = ExportUtils.class.getResource(RESOURCE_PATH_CSS).getFile();// + RESOURCE_PATH_CSS;
        }
        if (!htmlTemplatesDir.endsWith(File.separator)) {
            htmlTemplatesDir += File.separator;
        }
        copyFile(imageMapDir + "style.css", htmlTemplatesDir + "style.css");
        copyFile(detailsDir + "styleUnitDetails.css", htmlTemplatesDir + "styleUnitDetails.css");
        copyResource(detailsDir, RESOURCE_PATH_XHTML, "UnitDetails_empty.html");

        try {
            XHTML_FRAMESET = readFromFile(RESOURCE_PATH_XHTML, "Frameset.html");
            XHTML_FRAMESET_INDEXFRAME = readFromFile(RESOURCE_PATH_XHTML, "IndexFrame.html");
            XHTML_FRAMESET_UNITDETAILS = readFromFile(RESOURCE_PATH_XHTML, "UnitDetailsContentFrame.html");
            IMAGE_MAP_PAGE = readFromFile(RESOURCE_PATH_XHTML, "ImageMap.html");
            VISNAV_PAGE = readFromFile(RESOURCE_PATH_XHTML, "VisualisationSelection.html");
            if (isAudioSOM && generateRhythmPatterns) {
                UNIT_DETAIL_PAGE = readFromFile(RESOURCE_PATH_XHTML, "UnitDetailsRhythmPattern.html");
            } else {
                UNIT_DETAIL_PAGE = readFromFile(RESOURCE_PATH_XHTML, "UnitDetails.html");
            }

            StringBuffer classLegend = new StringBuffer();
            if (classInfo != null) {
                classLegend.append("      <h3>Class Legend</h3>");
                classLegend.append("      <table>\n");
                String[] classNames = classInfo.classNames();
                for (int i = 0; i < classNames.length; i++) {
                    if (i % 2 == 0) {
                        classLegend.append("        <tr>\n");
                    }
                    classLegend.append("          <td class=\"classLegend\" width=\"50%\">\n");
                    classLegend.append("            <span style=\"background-color: rgb(" + colors[i].getRed() + "," + colors[i].getGreen() + ","
                            + colors[i].getBlue() + ")\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;" + StringUtils.beautifyForHTML(classNames[i])
                            + "\n");
                    classLegend.append("          </td>\n");
                    if (i % 2 == 1 || i + 1 == classNames.length) {
                        classLegend.append("        </tr>\n");
                    }
                }
                classLegend.append("      </table>\n");
            }

            XHTML_FRAMESET_INDEXFRAME = XHTML_FRAMESET_INDEXFRAME.replaceAll("PLACEHOLDER_MAPINDEX", imageMapBaseName + "mapIndex.html").replaceAll(
                    "PLACEHOLDER_TITLE", imageMapTitle);
            if (!isAudioSOM) { // we have a content frame on the right side
                XHTML_FRAMESET_INDEXFRAME = XHTML_FRAMESET_INDEXFRAME.replaceAll("PLACEHOLDER_DETAIL_FILE", imageMapBaseName
                        + "unitDetailsContentFrame.html");
            }

            // the visualisation links
            if (visualisations != null && visualisations.length > 0) {
                ArrayList<String[]> realVis = new ArrayList<String[]>();
                StringBuffer mapNavigation = new StringBuffer();
                for (int i = 0; i < visualisations.length; i++) {
                    if (visualisations[i][0].equals("SPACER")) {
                        mapNavigation.append("  " + visualisations[i][1] + "\n");
                    } else {
                        mapNavigation.append("  <a href=\"" + visualisations[i][0] + ".html" + "\" target=\"map\" class=\"visualisationLink\" id=\""
                                + visualisations[i][0] + "\" name=\"visLink\" onclick=\"javascript:selectVis('" + visualisations[i][0] + "')\">"
                                + visualisations[i][1] + "</a>\n");
                        realVis.add(visualisations[i]);
                    }
                }
                visualisations = new String[realVis.size()][2];
                for (int i = 0; i < visualisations.length; i++) {
                    visualisations[i] = (String[]) realVis.get(i);
                }
                writeFile(imageMapDir + "mapNavigation.html",
                        VISNAV_PAGE.replaceAll("PLACE_HOLDER_VISUALISATION_LINKS", mapNavigation.toString()).replaceAll("PLACE_HOLDER_DEFAULT_VIS",
                                visualisations[visualisations.length - 1][0]));

                // the main frameset index, dividing into map & navigtation frame and details frame
                StringBuffer map = new StringBuffer();
                map.append("<frame src=\"" + visualisations[visualisations.length - 1][0]
                        + ".html\" name=\"map\" noresize=\"noresize\" frameborder=\"0\" marginwidth=\"0\" marginheight=\"0\" />\n");
                map.append("<frame src=\"mapNavigation.html\" name=\"mapNavigation\" noresize=\"noresize\" frameborder=\"0\" marginwidth=\"0\" marginheight=\"0\" />\n");
                writeFile(imageMapDir + "mapIndex.html", XHTML_FRAMESET.replaceAll("PLACEHOLDER_TITLE", imageMapTitle).replaceAll(
                        "PLACE_HOLDER_FRAME_LAYOUT", "rows=\"*, 32\"").replaceAll("PLACEHOLDER_FRAMES", map.toString()));
            }

            boolean firstUnit = true;
            boolean firstDataItem = true;

            StringBuffer imageMap = new StringBuffer();
            for (int x = 0; x < layer.getXSize(); x++) {
                for (int y = 0; y < layer.getYSize(); y++) {
                    Unit unit = layer.getUnit(x, y);
                    if (unit != null) {
                        if (firstUnit) {
                            firstUnit = false;
                            if (isAudioSOM) {
                                XHTML_FRAMESET_INDEXFRAME = XHTML_FRAMESET_INDEXFRAME.replaceAll("PLACEHOLDER_DETAIL_FILE",
                                        detailsDirRelative + "unit_" + x + "_" + y + ".html").replaceAll("PLACEHOLDER_TITLE", imageMapTitle);
                            } else {
                                XHTML_FRAMESET_UNITDETAILS = XHTML_FRAMESET_UNITDETAILS.replaceAll("PLACEHOLDER_SOURCE_UNITDETAILS", "../"
                                        + detailsDirRelative + "unit_" + x + "_" + y + ".html");
                            }
                        }
                        String coordinates = x * unitWidth + "," + y * unitWidth + " " + (x + 1) * unitWidth + "," + (y + 1) * unitWidth;
                        String[] mappedData = unit.getMappedInputNames();
                        Label[] labels = unit.getLabels();
                        boolean hasLabels = !isAudioSOM && labels != null && labels.length > 0;
                        if (mappedData != null) {
                            imageMap.append("  <area shape=\"rect\" href=\"../" + detailsDirRelative + "unit_" + x + "_" + y + ".html"
                                    + "\" target=\"" + unitDetailsTarget + "\"");
                            imageMap.append("coords=\"" + coordinates + "\" title=\"");

                            StringBuffer unitDetails = new StringBuffer();
                            String playListLink = "";
                            String playList = "";
                            for (int i = 0; i < mappedData.length; i++) {
                                String dataName = mappedData[i];

                                // in an audio SOM we don't have labels - we just use the data names as tooltip pop-up
                                if (!hasLabels) {
                                    if (cleanDataNamesMapping != null) {
                                        dataName = cleanDataNamesMapping.getProperty(dataName, dataName);
                                    } else {
                                        dataName = dataName.substring(dataName.lastIndexOf("/") + 1).replaceAll(".mp3", "").replaceAll("_", " ");
                                    }
                                    imageMap.append(dataName);
                                    if ((i + 1) < mappedData.length) {
                                        imageMap.append(DATA_ITEM_SEPERATOR);
                                    }
                                }

                                if (classInfo != null) {
                                    int classNr = classInfo.getClassIndex(mappedData[i]);
                                    if (classNr != -1) {
                                        Color c = colors[classNr];
                                        String dataItemDetails = "      <span title=\"" + classInfo.classNames()[classNr]
                                                + "\" style=\"background-color: rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue()
                                                + ")\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;";
                                        unitDetails.append(dataItemDetails);
                                    }
                                }

                                String filePath = inputDataFilesPrefix + File.separator + mappedData[i];
                                boolean haveFile = new File(filePath).exists();
                                // add iconFileType
                                // if (haveFile || forceLinkGeneration) {
                                // unitDetails.append("<img src=\"" + imageDirRelative + iconFileType + "\" height=\"12\" border=\"0\" />&nbsp;");
                                // }
                                unitDetails.append(dataName);
                                if (haveFile || forceLinkGeneration) {
                                    if (isAudioSOM) { // generate playlist
                                        // add iconFileType
                                        unitDetails.append("<img src=\"" + imageDirRelative + iconFileType + "\" height=\"12\" border=\"0\" />&nbsp;");

                                        new File(
                                                (playlistDir + mappedData[i]).substring(0, (playlistDir + mappedData[i]).lastIndexOf(File.separator))).mkdirs();

                                        String mediaLocation;
                                        if (outputDataFilesPrefix != null) {
                                            if (isURL(outputDataFilesPrefix)) {
                                                mediaLocation = outputDataFilesPrefix + File.separator + StringUtils.URLencode(mappedData[i]);
                                            } else {
                                                mediaLocation = outputDataFilesPrefix + File.separator + mappedData[i];
                                                // + playlistDirRelative + mappedData[i];
                                            }
                                        } else {
                                            mediaLocation = mappedData[i].substring(mappedData[i].lastIndexOf(File.separator) + 1);
                                        }

                                        String playListName = mappedData[i].substring(0, mappedData[i].length() - 4) + ".m3u";
                                        FileWriter playlistFileOut = new FileWriter(playlistDir + playListName);

                                        playlistFileOut.write(mediaLocation + "\n");
                                        playList += mediaLocation + "\n";
                                        playlistFileOut.close();
                                        unitDetails.append("&nbsp;&nbsp; ");
                                        unitDetails.append("<a href=\"../" + playlistDirRelative + playListName
                                                + "\" title=\"Play as m3u stream\" target=\"_download\">[<img src=\"" + imageDirRelative
                                                + "play.png\" height=\"12\" border=\"0\" >stream]</a>");
                                        unitDetails.append("&nbsp; ");
                                        unitDetails.append("<a href=\"" + mediaLocation
                                                + "\" title=\"Download as MP3\" target=\"_download\">[<img src=\"" + imageDirRelative
                                                + "download.gif\" height=\"12\" border=\"0\" >mp3]</a>");
                                    } else { // just add link to view content in browser
                                        unitDetails.append("&nbsp;&nbsp; ");
                                        String dataLink = outputDataFilesPrefix + File.separator + mappedData[i];
                                        if (new File(absolutePath + File.separator + dataLink + ".html").exists()) {
                                            dataLink += ".html";
                                        }
                                        unitDetails.append("<a href=\"../" + dataLink + "\" target=\"contentDetails\" title=\"View\">[");
                                        // add iconFileType
                                        // unitDetails.append("<img src=\"" + imageDirRelative + iconFileType + "\" height=\"12\" border=\"0\" >");
                                        unitDetails.append("view]</a>");
                                        if (firstDataItem) {
                                            firstDataItem = false;
                                            XHTML_FRAMESET_UNITDETAILS = XHTML_FRAMESET_UNITDETAILS.replaceAll("PLACEHOLDER_SOURCE_CONTENTFILE",
                                                    detailsDirRelative + "UnitDetails_empty.html");
                                            // "../" + dataLink);
                                        }
                                    }
                                }
                                unitDetails.append("\n      <br/>\n");
                            }
                            // we have a SOM with labels
                            if (hasLabels) {
                                for (int i = 0; i < labels.length; i++) {
                                    imageMap.append(labels[i].getName());
                                    if ((i + 1) < labels.length) {
                                        imageMap.append(DATA_ITEM_SEPERATOR);
                                    }
                                }
                            }
                            imageMap.append("\" />\n");

                            if (playList.trim().length() > 0) {
                                String playListName = "unit_" + x + "_" + y + ".m3u";
                                FileWriter playlistUnitFileOut = new FileWriter(playlistDir + playListName);
                                playlistUnitFileOut.write(playList);
                                playlistUnitFileOut.close();
                                playListLink = "<a href=\"../" + playlistDirRelative + playListName
                                        + "\" title=\"Play all songs on this unit\" target=\"_download\">[<img src=\"" + imageDirRelative
                                        + "play.png\" height=\"12\" border=\"0\" >play all]</a>";
                            }

                            String unitDetail = UNIT_DETAIL_PAGE.replaceAll("PLACEHOLDER_UNIT_DETAILS", unitDetails.toString()).replaceAll(
                                    "PLACEHOLDER_UNIT_ID", x + "/" + y).replaceAll("PLACEHOLDER_PLAYLIST", playListLink);
                            unitDetail = unitDetail.replaceAll("PLACEHOLDER_CLASS_LEGEND", classLegend.toString());
                            if (isAudioSOM) {
                                if (generateRhythmPatterns) {
                                    unitDetail = unitDetail.replaceAll("PLACEHOLDER_RP_IMAGE", "rp_" + x + "_" + y + ".jpg");
                                    // TODO be careful: RP dimensions hardcoded again!
                                    ExportUtils.saveImageToFile(detailsDir + "rp_" + x + "_" + y + ".jpg", new RhythmPattern(
                                            layer.getUnit(x, y).getWeightVector(), 60, 24).getImage());
                                }
                            }
                            writeFile(detailsDir + "unit_" + x + "_" + y + ".html", unitDetail);
                        }
                    }
                }
            }

            writeFile(fullPath + "_index.html", XHTML_FRAMESET_INDEXFRAME);

            if (imageMapTitle == null) {
                imageMapTitle = "";
            } else {
                imageMapTitle = "<h2>" + imageMapTitle + "</h2>";
            }
            String imageMapPage = IMAGE_MAP_PAGE.replaceAll("PLACEHOLDER_MAP_AREAS", imageMap.toString()).replaceAll("PLACEHOLDER_IMAGE_MAP_TITLE",
                    imageMapTitle);
            for (int i = 0; i < visualisations.length; i++) {
                if (!visualisations[i][0].equals("SPACER")) {
                    writeFile(imageMapDir + visualisations[i][0] + ".html", imageMapPage.replaceAll("PLACE_HOLDER_MAP_IMAGE",
                            imageDirRelative + visualisations[i][0] + ".png").replaceAll("PLACEHOLDER_TITLE", "Play-SOM - " + visualisations[i][0]));
                }
            }
            if (!isAudioSOM) {
                // the unit details frame on the right side
                writeFile(imageMapDir + "unitDetailsContentFrame.html", XHTML_FRAMESET_UNITDETAILS);
            }
        } catch (FileNotFoundException e) {
            throw new SOMToolboxException(e.getMessage());
        } catch (IOException e) {
            throw new SOMToolboxException(e.getMessage());
        }
    }

    private static void writeFile(String fileName, String data) throws FileNotFoundException, IOException {
        FileOutputStream fos;
        fos = new FileOutputStream(fileName);
        fos.write(data.getBytes());
        fos.flush();
        fos.close();
    }

    private static String readFromFile(String resourcePath, String fileName) throws FileNotFoundException, IOException {
        return readFromFile(ExportUtils.class.getResource(resourcePath + fileName).getFile());
    }

    private static String readFromFile(String fileName) throws FileNotFoundException, IOException {
        String line;
        String content = "";
        line = null;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }
        return content;
    }

    private static void copyResource(String destinationDirectory, String sourceDirectory, String fileName) {
        copyFile(destinationDirectory + File.separator + fileName,
                ExportUtils.class.getResource(sourceDirectory + File.separator + fileName).getFile());
    }

    public static void copyFile(String destinationFileName, String sourceFileName) {
        copyFile(new File(destinationFileName), new File(sourceFileName));
    }

    public static void copyFile(File destinationFileName, File sourceFileName) {
        try {
            FileInputStream in = new FileInputStream(sourceFileName);
            FileOutputStream out = new FileOutputStream(destinationFileName);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * @param potentialURL
     * @return whether the given String is a valid URL or not
     */
    public static boolean isURL(String potentialURL) {
        try {
            new URL(potentialURL);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    public static void saveRhythmPatternsOfWeightVectors(String basefileName, GrowingLayer layer) throws SOMToolboxException {
        Unit[][] units = layer.get2DUnits();
        RhythmPattern rp;
        String fileName;

        for (int x = 0; x < layer.getXSize(); x++) {
            for (int y = 0; y < layer.getYSize(); y++) {
                fileName = basefileName + x + "_" + y + ".jpg";
                // TODO be careful: RP dimensions hardcoded again!
                // last two ints: block size of graphics
                rp = new RhythmPattern(units[x][y].getWeightVector(), 60, 24, 5, 5);
                ExportUtils.saveImageToFile(fileName, rp.getImage());
            }
        }
    }

    public static void saveImageToFile(String fileName, BufferedImage buim) throws SOMToolboxException {
        if (!fileName.endsWith(".png")) {
            fileName += ".png";
        }
        try {
            ImageIO.write(buim, "png", new File(fileName));
        } catch (FileNotFoundException e) {
            throw new SOMToolboxException(e.getMessage());
        } catch (IOException e) {
            throw new SOMToolboxException(e.getMessage());
        } catch (Exception e) {
            throw new SOMToolboxException(e.getMessage());
        }
    }

    public static void saveVisualizationAsImage(CommonSOMViewerStateData state, int unitWidth, String imagePath) throws SOMToolboxException {
        BufferedImage visualization = getVisualization(state, unitWidth);
        ExportUtils.saveImageToFile(imagePath, visualization);
    }

    public static void saveMapPaneAsImage(GenericPNodeScrollPane mapPane, String imagePath) throws SOMToolboxException {
        Rectangle r = mapPane.getCanvas().getBounds();
        Image image = mapPane.getCanvas().createImage(r.width, r.height);
        Graphics g = image.getGraphics();
        mapPane.getCanvas().paint(g);
        ExportUtils.saveImageToFile(imagePath, (BufferedImage) image);
    }

    public static BufferedImage getVisualization(CommonSOMViewerStateData state, int unitWidth) throws SOMToolboxException {
        return getVisualization((AbstractBackgroundImageVisualizer) state.mapPNode.getCurrentVisualization(), state.currentVariant, state.growingSOM,
                unitWidth);
    }

    public static BufferedImage getVisualization(AbstractBackgroundImageVisualizer abiv, int currentVariant, GrowingSOM gsom, int unitWidth)
            throws SOMToolboxException {
        if (abiv == null) {
            throw new SOMToolboxException("No visualization selected (yet)");
        }
        if (unitWidth == -1) {
            unitWidth = MapPNode.DEFAULT_UNIT_WIDTH; // default value
        }
        return abiv.createVisualization(currentVariant, gsom, gsom.getLayer().getXSize() * unitWidth, gsom.getLayer().getYSize() * unitWidth);
    }

    public static File getFilePath(Component parent, JFileChooser fileChooser, String title) {
        return getFilePath(parent, fileChooser, title, null);
    }

    public static File getFilePath(Component parent, JFileChooser fileChooser, String title, FileFilter filter) {
        if (fileChooser.getSelectedFile() != null) { // reusing the dialog
            fileChooser = new JFileChooser(fileChooser.getSelectedFile().getPath());
        }
        if (filter != null) {
            fileChooser.setFileFilter(filter);
        }

        int returnVal = fileChooser.showDialog(parent, title);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public static void copyData(String fileNamePrefix, String dataDir, String styleSheetLink, String[] dataNames) throws FileNotFoundException,
            IOException {
        String page = readFromFile(RESOURCE_PATH_XHTML, "ViewDocument.html");
        for (int i = 0; i < dataNames.length; i++) {
            String sourceFileName = fileNamePrefix + File.separator + dataNames[i];
            String destinationFileName = dataDir + File.separator + dataNames[i];
            if (new File(sourceFileName).exists()) {
                if (dataNames[i].endsWith(".txt")) { // wrap it into an HTML
                    String text = readFromFile(sourceFileName);
                    writeFile(destinationFileName + ".html", page.replaceAll("PLACE_HOLDER_CONTENT", text).replaceAll("PLACEHOLDER_TITLE",
                            dataNames[i].substring(0, dataNames[i].length() - 4)).replaceAll("PLACEHOLDER_STYLESHEET", styleSheetLink));
                } else {
                    copyFile(sourceFileName, destinationFileName);
                }
            }
        }
    }

}

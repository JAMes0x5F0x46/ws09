package at.tuwien.ifs.somtoolbox.reportgenerator.output;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.EditableReportProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResultCollection;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class generates the Report as HTML output. It holds a reference to an instance of ReportFileWriter, to which all Strings that shall appear in
 * the output are send. This class does only handle how this strings schall look like, and not with any technical detail about how these strings are
 * actually written to a file or elsewhere.
 * 
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: OutputReportHtml.java 2874 2009-12-11 16:03:27Z frank $
 */
public class OutputReportHtml implements OutputReport {

    /** handles the actual writing of the output. All Strings that shall appear in the output are handed over to it */
    private ReportFileWriter writer;

    private DatasetInformation datasetInformation;

    private TestRunResultCollection testruns;

    private String outputDirPath = "";

    private String imagePath;

    private int type;

    private EditableReportProperties EP = null;

    /**
     * Creates a new Object to finally print the report in HTML
     * 
     * @param outputDir the path to the directory where the files shall be saved to
     */
    public OutputReportHtml(String outputDir, EditableReportProperties EP) {
        outputDir = FileUtils.prepareOutputDir(outputDir);
        this.writer = new ReportFileWriter(outputDir + "index.html", 1);
        this.EP = EP;
        this.outputDirPath = outputDir;
        imagePath = outputDirPath + "images" + System.getProperty("file.separator");
    }

    /**
     * gives an Object to the report writer that stores all information about the used dataset. Should be specified before the createOutput function
     * is called, otherwise no information about the dataset can be reported
     * 
     * @param infoObj object containing all available information about the used dataset
     */
    public void setDatasetInformation(DatasetInformation infoObj) {
        this.datasetInformation = infoObj;
    }

    /**
     * gives an Object to the report writer that stores all information about the performed that shall be documented within the report. Should be
     * specified before the createOutput function is called, otherwise no information about the testruns can be reported
     * 
     * @param testruns object containting all available information about the testruns performed
     */
    public void setTestrunInformation(TestRunResultCollection testruns, int type) {
        this.testruns = testruns;
        this.type = type;
    }

    /**
     * creates the report about as HTML file. All necessary setXXX functions should be called before this function is called and the output starts.
     */
    public void createOutput() {
        this.printReportStart();
        if (this.datasetInformation != null) {
            this.printDatasetReport();
        }
        if (this.testruns != null && this.testruns.getNumberOfRuns() > 0) {
            this.printTestrunsReport();
        }
        this.printReportEnd();
    }

    /**
     * prints the head of the report this includes the title and all tags or markup required at the beginning of the report
     */
    private void printReportStart() {
        /* Creates "reporter.js" */
        this.writer.appendOutput("<html>\n\t<head>\n\t\t<title>Report for SOMs</title>\n\t<script language=\"JavaScript\" src=\"reporter.js\"></script>"
                + this.getHeadContent() + "</head>\n\t<body><div id=\"classInfoDiv\"></div>");
        // input info div for mouse over
        this.writer.appendOutput("<div id=\"inputInfoDiv\"><span>dist. to weight vector = </span><span id=\"inputInfoDiv_dist\">0</span><br/>"
                + "<span>on unit:</span><span id=\"inputInfoDiv_unit\">0</span>" + "<span> having qe = </span><span id=\"inputInfoDiv_qe\">0</span>"
                + "<span> and mqe = </span><span id=\"inputInfoDiv_mqe\">0</span>" + "</div>");
        this.writer.appendOutput("<h1>Automatic Report of training results</h1>\n");
        /*
         * "<h2>Contents:</h2>"+ "<a href =\"#1.0\">1.0 The Dataset</a><br>"+ "<a href =\"#1.1\">1.1 Class details</a><br>"+
         * "<a href =\"#2.0\">2.0 Results of the SOM trainings:</a><br>"+
         * "<a href =\"#2.1\">2.1 Distribution of the input data on the trained SOM</a><br>"+
         * "<a href =\"#2.2\">2.2 Location and quantization errors of selected input items:</a><br>"+
         * "<a href =\"#2.3\">2.3 Class mix on the units</a><br>"+ "<a href =\"#2.3.1\">2.3.1 Class locations and Entropy</a><br>"+
         * "<a href =\"#3.0\">3.0 Quality Measures of the SOM</a><br>"+ "<a href =\"#3.1\">3.1 Quanization errors of the SOM</a><br>"+
         * "<a href =\"#3.2\">3.2 Topographic errors of the SOM</a><br>"+ "<a href =\"#4.0\">4.0 Clusters on the SOM</a><br>"+
         * "<a href =\"#4.1\">4.1 The cluster tree</a><br>"+ "<a href =\"#5.0\">5.0 The The Semantic Interpretation</a><br>");
         */
    }

    /**
     * prints the foot of the report this includes any fixed data to be displayed at the end, as well as all markup required to finish and close the
     * report
     */
    private void printReportEnd() {
        this.writer.appendOutput("\n\t</body>\n</html>");
        this.writer.finish();
        this.createJavaScriptFile();
        Logger.getLogger("at.tuwien.ifs.somtoolbox.reportgenerator").info("Finished Report");
    }

    /**
     * outputs the section concerning the dataset Information included in this section are:
     * <ul>
     * <li>number of inputs</li>
     * <li>dimension of inputs</li>
     * <li>information about the data distribution within the different dimensions</li>
     * <li>...</li>
     * </ul>
     * Also, if class information are provided, these are printed. This includes: the number of classes, the number of inputs within each class, ...
     */
    private void printDatasetReport() {
        /* Some inizialisations.. we will need them later */
        double[][] dim_array = this.datasetInformation.getPCAdeterminedDims(); /*
                                                                                * Get the Array with the most important Dims, ordered decreasingly by
                                                                                * importance
                                                                                */

        /*
         * Here we calculate how many Dimensions are still "left over" so to say, that is, which ones have not been visualized due to their small
         * importance. Anyway we want to see how many dimensions share what remaining Percantage of the total variance
         */
        double perc = this.datasetInformation.calculateAccumulatedVariance();

        // header
        String pca_desc = this.testruns.getRun(0).getScientificDescription("Principal Component Analysis");
        this.writer.appendOutput("<h2><a name =\"1.0 The Dataset\"><font color=\"black\">1.0 The Dataset:</a></h2></font>\n");
        this.writer.writeTableofContentsEntry("1.0 The Dataset");
        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.appendOutput("<p>This section describes the dataset that was used for training the SOM, and therefore shall be represented by the SOM.<br/>"
                + "After some general information, a closer look is taken onto each dimension of the input vectors. Input Vectors are chosen according to the "
                + "Results of a <u onclick=\"javascript:showVisualisationDescriptions('"
                + pca_desc
                + "')\"><font color =\"blue\">PCA</font></u>. Since Variance can be used "
                + "as a measure of Quality in the Data, the corresponding Percentage of the total Variance is"
                + "displayed, to underline the Goodness of the Dimension.");
        if (dim_array.length != this.datasetInformation.getVectorDim()) { /*
                                                                           * , if the Dim vector had more than the amount of selected dimensions by
                                                                           * the user
                                                                           */
            this.writer.appendOutput(" The remaining " + (this.datasetInformation.getVectorDim() - dim_array.length) + " "
                    + "dimensions that are not displayed share an accumulated Variance of " + String.format("%.2f", 100.0 - (perc * 100)) + " %.");
        }

        if (this.datasetInformation.classInfoAvailable()) {
            this.writer.appendOutput("<br/>At the end of this section, some information about the existing classes and how the input items are distributed on them "
                    + "are given."
                    + "For all scientific methods, or Visualizations, more detailed information can be obtained by clicking on the blue Link.");
        }
        this.writer.appendOutput("</p>");

        // Information about the input data vectors:
        this.writer.appendOutput("<span class=\"header\">Dataset:</span><br/>");
        this.writer.appendOutput("Number of input vectors: " + datasetInformation.getNumberOfInputVectors() + "<br/>\n");
        this.writer.appendOutput("Dimensionality of input vectors: " + datasetInformation.getVectorDim() + "<br/>\n");
        this.writer.appendOutput("Dataset has been normalized: " + StringUtils.formatBooleanValue(0, this.datasetInformation.isNormalized())
                + "<br/>\n");
        this.writer.appendOutput("Number of Classes: " + this.classNumberString() + "<br/>\n");
        this.writer.appendOutput("</div>");

        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.appendOutput("<span class=\"header\">Attribute details:</span>");
        this.writer.appendOutput("<table>\n<thead>\n<tr>\n</tr>\n" + "<tr>\n" + "<th>label</th>\n" + "<th>min value</th>\n" + "<th>max value</th>\n"
                + "<th>mean value</th>" + "<th>variance</th>" + "<th># of zeros</th>" + "<th>discrete (integer) values</th>\n"
                + "<th>only 0/1 values found</th>\n" + "<th>Percentage of Total Variance </th>\n" + "<th>Metro Map Component Visualization</th>\n"
                + "</tr>\n" + "</thead>\n" + "<tbody>\n");

        for (int i = (int) dim_array[0][0], counter = 0; counter < dim_array.length; counter++, i = (counter < dim_array.length)
                ? (int) dim_array[counter][0] : (int) dim_array[0][0]) {

            this.writer.appendOutput("<tr>\n");
            this.writer.appendOutput("\t<td>" + this.datasetInformation.getAttributeLabel(i) + "</td>\n");
            this.writer.appendOutput("\t<td class=\"middleText\">"
                    + String.format("%.3f", this.datasetInformation.getNumericalDataProps(DatasetInformation.MIN_VALUE, i)) + "</td>\n");
            this.writer.appendOutput("\t<td class=\"middleText\">"
                    + String.format("%.3f", this.datasetInformation.getNumericalDataProps(DatasetInformation.MAX_VALUE, i)) + "</td>\n");
            this.writer.appendOutput("\t<td class=\"middleText\">"
                    + String.format("%.5f", this.datasetInformation.getNumericalDataProps(DatasetInformation.MEAN_VALUE, i)) + "</td>\n");
            this.writer.appendOutput("\t<td class=\"middleText\">"
                    + String.format("%.5f", this.datasetInformation.getNumericalDataProps(DatasetInformation.VAR_VALUE, i)) + "</td>\n");
            this.writer.appendOutput("\t<td class=\"middleText\">"
                    + (int) this.datasetInformation.getNumericalDataProps(DatasetInformation.ZERO_VALUE, i)
                    + " ("
                    + String.format("%.2f", this.datasetInformation.getNumericalDataProps(DatasetInformation.ZERO_VALUE, i)
                            / this.datasetInformation.getNumberOfInputVectors() * 100) + "%)" + "</td>\n");
            this.writer.appendOutput("\t<td class=\"middleText\">"
                    + StringUtils.formatBooleanValue(0, this.datasetInformation.getBoolDataProps(DatasetInformation.DISCRETE, i)) + "</td>\n");
            this.writer.appendOutput("\t<td class=\"middleText\">"
                    + StringUtils.formatBooleanValue(0, this.datasetInformation.getBoolDataProps(DatasetInformation.ONLY01, i)) + "</td>\n");

            this.writer.appendOutput("\t<td class=\"middleText\">" + String.format("%.2f", dim_array[counter][1] * 100) + "%</td>\n");

            // Gets the first run from the Testruncollection (with default SOMViewer unit,dw,and weight files)*/

            // testruns.getRun(0).getVisualizer().createSingleMetroMapComponentImage(this.outputDirPath +"images"+
            // System.getProperty("file.separator"), i+"_SingleMetroMapComponent.jpg",i);
            testruns.getRun(0).createSingleMetroMapComponentImage(this.outputDirPath + SOMDescriptionHTML.imgSubdir + File.separator,
                    i + "_SingleMetroMapComponent.jpg", i);

            this.writer.appendOutput("\t<td class=\"middleText\">" + "<img src=\"" + SOMDescriptionHTML.imgSubdir + "/run_"
                    + testruns.getRun(0).getRunId() + "_component_" + i + "_SingleMetroMapComponent.jpg\"/></td>" + "<td>&nbsp;</td>" + "</tr>\n");

        }
        this.writer.appendOutput("</tbody>" + "</table>");
        this.writer.appendOutput("</div>");

        // more detailed class information
        if (this.datasetInformation.getNumberOfClasses() > 0) {
            this.writer.appendOutput("<div class=\"infoBlock\">\n");
            this.writer.appendOutput("<span class=\"header\"><a name =\"1.1 Class details\" ><font color=\"black\">1.1 Class details</a></font>:</span>\n");
            this.writer.writeTableofContentsSubEntry("1.1 Class details");
            this.writer.appendOutput("<table>\n<thead>\n<tr>\n</tr>\n" + "<tr>\n" + "<th>Class label</th>\n" + "<th>Number of Classmembers</th>\n"
                    + "<th>% of input belong to</th>\n" + "<th>Class colour used</th>" + "</tr>\n" + "</thead>\n" + "<tbody>\n");
            for (int i = 0; i < this.datasetInformation.getNumberOfClasses(); i++) {

                this.writer.appendOutput("<tr>\n");
                this.writer.appendOutput("\t<td>" + this.datasetInformation.getNameOfClass(i) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + this.datasetInformation.getNumberOfClassmembers(i) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">"
                        + String.format("%.2f", (double) this.datasetInformation.getNumberOfClassmembers(i)
                                / (double) this.datasetInformation.getNumberOfInputVectors() * 100) + "%</td>\n");
                this.writer.appendOutput("\t<td style=\"background:" + StringUtils.getRGBString(this.datasetInformation.getClassColorRGB(i))
                        + "\">&nbsp;</td>\n");
                this.writer.appendOutput("</tr>\n");
            }
            this.writer.appendOutput("</tbody>\n" + "</table>");
            this.writer.appendOutput("</div>");
        }
    }

    /**
     * first outputs the section containing the results from the different testruns specified, then a short comparison of the testruns (SOM
     * configuration and basic results) is printed. for the output of the sections for the different training runs, this.printReportOnTestrun(run) is
     * used
     */
    private void printTestrunsReport() {

        // header
        if (this.testruns.getNumberOfRuns() > 1) {
            this.writer.appendOutput("<h2><a name =\"2.0 Results of the SOM trainings\" ><font color=\"black\">2.0 Results of the SOM trainings:</a></h2></font>\n");
            this.writer.appendOutput("<p>In this section, details about the different specified trainings and their results are "
                    + "presented. For each training,");
        } else {
            this.writer.appendOutput("<h2><a name =\"2.0 Results of the SOM trainings\"><font color=\"black\">2.0 Results of the SOM trainings:</a></h2></font>\n");
            this.writer.appendOutput("<p>In this section, details about the training and the resuling SOM are " + "presented. Therefore,");

        }
        this.writer.writeTableofContentsEntry("2.0 Results of the SOM trainings");
        this.writer.appendOutput(" first some basic properties describing the training parameters and the resulting "
                + "SOM are given. Then, the distribution of the input data on the trained SOM is described. This includes the quantization and "
                + "topographic errors existing on the map.");
        if (this.datasetInformation.classInfoAvailable()) {
            this.writer.appendOutput("<br/>Together with the distribution of the input data items, also information about how and where the classes"
                    + " are distributed on the map are given.");
        }
        this.writer.appendOutput("<br/>At the end, a selection of 10 clusters (and therefore possibilities to group the unit on the SOM) is described.</p>");

        // first print details about the different runs
        for (int r = 0; r < this.testruns.getNumberOfRuns(); r++) {
            this.printReportOnTestrun(r, this.testruns.getNumberOfRuns() > 1);
        }

        // then print the information retrieved by comparing the runs
        if (this.testruns.getNumberOfRuns() > 1) {
            this.printComparingReport();
        }// otherwise comparing runs would not make any sense
    }

    /**
     * prints the results of/information about one som training and its results. What infromation is actual written to the report depends on the type
     * of som that has been trained, as all kind of SOMs may need different information to be given. In general, first, a list of parameters for the
     * training process and some basic properties of the som are outputted. Then, if a class file has been selected, a table visualizing the class
     * distribution is given. If input vectors has been selected for retrieving information about where they are located, another table storing these
     * information is printed. After some quantization errors, information about the clusters found are written.
     * 
     * @param r the index of the testrun (start counting by 0)
     * @param moreRuns true if the results of more than one training has been spcified, false otherwise
     */
    private void printReportOnTestrun(int r, boolean moreRuns) {

        if (moreRuns) {
            this.writer.appendOutput("<h2>" + (r + 1) + ". trained SOM:</h2>");
        }
        this.writer.appendOutput("<h3> SOM & training properties:</h3>\n");

        // to distinguish the different SOMs, we need its idetinfying String
        String somtype = (String) this.testruns.getProperty(TestRunResultCollection.keyTopology, r);
        SOMDescriptionHTML somdescr = null;

        if (somtype != null && somtype.equals("gg")) {
            // growing grid: seems to be produced by GHSOM if no child map is created
            somdescr = new SOMGGDescriptionHTML(this.writer, this.datasetInformation, this.testruns.getRun(r), this.outputDirPath,
                    this.datasetInformation.getEP());
        } else if (somtype != null && somtype.equals("ghsom")) {
            // a growing hierachical som: has more than one level of soms
            somdescr = new SOMGHSOMDescriptionHTML(this.writer, this.datasetInformation, this.testruns.getRun(r), this.outputDirPath,
                    this.datasetInformation.getEP());
        } else {
            // mainly GrowingSOM, but we use it also as generic outputter for types with only small differences to GrowingSOM
            somdescr = new SOMDescriptionHTML(this.writer, this.datasetInformation, this.testruns.getRun(r), this.outputDirPath,
                    this.datasetInformation.getEP());
        }

        // now that we have the correct handler for our SOM: handle ;)
        somdescr.printSOMDescription();

    }

    /**
     * creates the sections that compares the results of the different specified trainings this comparison constists of a table containing some basic
     * properties of the SOMs, the visualizations of the class distribution, the distribution of the input items and the U-Matrices of the SOM.
     */
    private void printComparingReport() {

        this.writer.appendOutput("<h2>Comparison of the SOMs</h2>");

        this.writer.appendOutput("<p>This section tries to compare the SOMs retrieved within the different trainings specified. Therefore, besides a list of the "
                + "basic properties that can be used to describe the training and the SOM, different visualizations are presented.</p>");

        // first: a table comparing the basic properties of the SOMs
        this.writer.appendOutput("<h3>Basic properties of trained SOMs</h3>");
        this.writer.appendOutput("<div class=\"infoBlock\">");
        if (this.testruns.getNumberOfRuns() < 2) {
            this.writer.appendOutput("<p class=\"header2\">Only one run specified, please see the property list for this run</p>");
        } else {
            // at least two SOMs to compare
            this.writer.appendOutput("<table>\n<thead>\n<tr>\n</tr>\n" + "<tr>\n" + "<th>som type</th>\n" + "<th>dimensions</th>\n"
                    + "<th>sigma</th>\n" + "<th>tau</th>" + "<th>tau2</th>" + "<th>init learn rate</th>" + "<th>init ngbh. range</th>"
                    + "<th>iterations</th>" + "<th>mqe of map</th>" + "</tr>\n" + "</thead>\n" + "<tbody>\n");

            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                TestRunResult temp = this.testruns.getRun(i);

                this.writer.appendOutput("<tr>\n");
                this.writer.appendOutput("\t<td>" + temp.getMapProperty(TestRunResultCollection.keyTopology) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + this.getSOMDimensions(temp) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + StringUtils.formatDouble(temp.getSigma()) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + StringUtils.formatDouble(temp.getTau()) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + StringUtils.formatDouble(temp.getTau2()) + "</td>\n");
                this.writer.appendOutput("<td class=\"middleText\">"
                        + StringUtils.formatString((String) temp.getMapProperty(TestRunResultCollection.keyLearnRateInit)) + "</td>\n");
                this.writer.appendOutput("<td class=\"middleText\">"
                        + StringUtils.formatString((String) temp.getMapProperty(TestRunResultCollection.keyNeighbourhoodInit)) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + temp.getMapProperty(TestRunResultCollection.keyTotalIterations) + "</td>\n");
                this.writer.appendOutput("\t<td class=\"middleText\">" + StringUtils.formatDouble(temp.getMapMQE().getQE()) + "</td>\n");
                this.writer.appendOutput("</tr>\n");
            }
            this.writer.appendOutput("</tbody>\n" + "</table>");

        }
        this.writer.appendOutput("</div>");

        // the next thing are the images showing the class distribution
        if (this.datasetInformation.classInfoAvailable()) {
            this.writer.appendOutput("<h3>Class distribution on the trained SOMs</h3>");
            this.writer.appendOutput("<div class=\"infoBlock\">");
            this.writer.appendOutput("The following images show the distribution of the classes on the trained SOM. Thereby, SOMs having "
                    + "different dimensions are displayed in different size. Each unit on the SOM got 10x10 pixel within the image.");

            this.writer.appendOutput("<table><thead></thead><tbody><tr>");
            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                TestRunResult temp = this.testruns.getRun(i);
                temp.createClassDistributionImage(imagePath, "classDistribution.jpg", -1);
                this.writer.appendOutput("<td><img src=\"images/run_" + temp.getRunId() + "_classDistribution.jpg\" alt=\"class distribution of som "
                        + i + "\"/></td>");
            }
            this.writer.appendOutput("</tr></tr>");
            for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
                this.writer.appendOutput("<td>run " + (i + 1) + "</td>");
            }
            this.writer.appendOutput("</tr></tbody></table>");
            this.writer.appendOutput("</div>");
        }

        // then an image with the comparison of the input data distribution
        this.writer.appendOutput("<h3>Distribution of the input vectors on the trained SOMs</h3>");
        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.appendOutput("The following images show the distribution of the input vectors on the trained SOM. Green (and other light colors) indicate areas "
                + "where only a small number of input items is mapped to, whereas dark colors mark the areas with more items. "
                + "Again, different sizes of the maps are preserved by the images.");
        this.writer.appendOutput("<table><thead></thead><tbody><tr>");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            TestRunResult temp = this.testruns.getRun(i);
            temp.createInputDistributionImage(imagePath, "inputDistribution.jpg");
            this.writer.appendOutput("<td><img src=\"images/run_" + temp.getRunId() + "_inputDistribution.jpg\" alt=\"class distribution of som " + i
                    + "\"/></td>");
        }
        this.writer.appendOutput("</tr></tr>");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            this.writer.appendOutput("<td>run " + (i + 1) + "</td>");
        }
        this.writer.appendOutput("</tr></tbody></table>");
        this.writer.appendOutput("</div>");

        // the last thing is an image of the umatrix for comparison
        this.writer.appendOutput("<h3>U-Matrices of the trained SOM</h3>");
        this.writer.appendOutput("<div class=\"infoBlock\">");
        this.writer.appendOutput("Although an visual description of the U-Matrix of the SOMs is contained in the detail description, to give a "
                + "better possibility to compare them, a smaller version of the images is put here again. <br/>"
                + "Therefore, the following images show the distance between neighboured units on the SOM. Again, light areas (especially green) represent"
                + "a small distance between the units, whereas dark colors (especially red) show bigger distances. Like before, different sizes of the "
                + "SOMs are preserved within their representation");
        this.writer.appendOutput("<table><thead></thead><tbody><tr>");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            TestRunResult temp = this.testruns.getRun(i);
            temp.createUDMatrixImage(imagePath, "umatrix_small.jpg", 10, 0);
            this.writer.appendOutput("<td><img src=\"images/run_" + temp.getRunId() + "_umatrix_small.jpg\" alt=\"class distribution of som " + i
                    + "\"/></td>");
        }
        this.writer.appendOutput("</tr></tr>");
        for (int i = 0; i < this.testruns.getNumberOfRuns(); i++) {
            this.writer.appendOutput("<td>run " + (i + 1) + "</td>");
        }
        this.writer.appendOutput("</tr></tbody></table>");
        this.writer.appendOutput("</div>");
    }

    /**
     * returns the dimension of the SOM nicley formatted. returned as string x-size x y- size
     * 
     * @param testrun the testrun for which the dimensions are needed
     * @return unknown or "x-size x y-size" (in units)
     */
    private String getSOMDimensions(TestRunResult testrun) {
        String xunits = (String) testrun.getMapProperty(TestRunResultCollection.keyXDim);
        String yunits = (String) testrun.getMapProperty(TestRunResultCollection.keyYDim);
        String dim = "unknown";
        if (xunits != null && yunits != null) {
            dim = xunits + " x " + yunits;
        }
        return dim;
    }

    /**
     * returns the information (or missing information) about classes nicely formatted
     * 
     * @return the number of classes or information that this information is missing
     */
    protected String classNumberString() {
        if (this.datasetInformation.getNumberOfClasses() < 0) {
            return "There are no class information attached to this input";
        } else {
            return "" + this.datasetInformation.getNumberOfClasses();
        }
    }

    /**
     * Creates the string that is printed in the html head. (that is the part containing CSS definitions, used to display the report.
     * 
     * @return a String containing script and style tags (with according content)
     */
    private String getHeadContent() {

        String content = "";
        content += "<style>\n" + "#classInfoDiv{" + "	display: none;" + "	position: absolute;" + "	border: 1px solid #000000;"
                + "	background: #ffffff;" + "	width: 4em;" + "}\n" + "#inputInfoDiv{\n" + "	display: none;" + "	position: absolute;"
                + "	border: 2px solid orange;" + "	background: #ffffff;" + "	padding: 0.2em 0.3em 0.2em 0.3em;" + "	z-index: 1;" + "}\n" + "body{\n"
                + "	font-family:Georgia, Verdana, Arial;\n" + "	padding-left: 2em;\n" + "	padding-right: 2em;\n" + "}\n" + "h1{\n"
                + "	text-align: center;\n" + "}\n" + ".infoBlock{" + "	margin-left: 2em;" + "	margin-bottom: 1.3em;" + "}\n" + ".header{"
                + "	font-weight: bold;" + "}\n" + ".header2{" + "	font-style: italic;" + "}\n" + "th{" + "	font-weight: normal;"
                + "	font-style: italic;" + "	/*border-left: 1px solid black;" + "	border-right: 1px solid black;*/" + "	padding-left: 0.3em;"
                + "	padding-right: 0.3em;" + "}\n" + "td{\n" + "	padding-left: 0.3em;" + "	padding-right: 0.3em;" + "}\n" + ".middleText{"
                + "	text-align: center;" + "}\n" + ".classTableTD{\n" + "	vertical-align: top;\n" + "}\n" + "</style>\n";

        return content;
    }

    /**
     * Returns the Visualisation Data,according to the default DW-file. The Clasinfo,Inputvector & Template Vector are specified by the
     * ReportGenWindow
     * 
     * @return
     */
    protected void setDefaultVisData() {
        SharedSOMVisualisationData visData = new SharedSOMVisualisationData();
        // visData.setFileName(SOMVisualisationData.DATA_WINNER_MAPPING, this.datasetInformation.getDefaultDwPath());
        visData.setFileName(SOMVisualisationData.CLASS_INFO, this.datasetInformation.getClassInformationFilename());
        visData.setFileName(SOMVisualisationData.INPUT_VECTOR, this.datasetInformation.getInputDataFilename());
        visData.setFileName(SOMVisualisationData.TEMPLATE_VECTOR, this.datasetInformation.getTemplateFilename());
        visData.setFileName(SOMVisualisationData.LINKAGE_MAP, "");
        visData.readAvailableData();

    }

    /**
     * Creates the "reporter.js" file
     */
    // FIXME: read this from an external source as in other exports
    private void createJavaScriptFile() {

        String content = "";
        content += " var sdhArray = new Array(" + this.testruns.getNumberOfRuns() + ");\n";
        content += " var tpArray = new Array(" + this.testruns.getNumberOfRuns() + ");\n";
        content += " var twArray = new Array(" + this.testruns.getNumberOfRuns() + ");\n";
        content += "var maxSDHAnzahl;\n";
        content += "var maxTWAnzahl;\n";
        content += "var maxTPAnzahl;\n";

        // JS code for the mouse function of the pie charts
        content += "function showClassInfo(elem, number, event){\n"
                + "	setClassInfoText(number + ' elements on unit');\n"
                + "	showClassInfoDiv();\n"
                + "	positionClassInfoDiv(event);\n"
                + "}\n"
                + "function setClassInfoText(text){\n"
                + "	var classInfoDiv = document.getElementById('classInfoDiv');\n"
                + "	while(classInfoDiv.lastChild != null)\n"
                + "		classInfoDiv.removeChild(classInfoDiv.lastChild);\n"
                + "	classInfoDiv.appendChild(document.createTextNode(text));\n"
                + "	var w = Math.ceil(text.length*0.6);\n"
                + "	classInfoDiv.style.width = w + \"em\";"
                + "}\n"
                + "function showClassInfoDiv(){"
                + "	var classInfoDiv = document.getElementById('classInfoDiv');\n"
                + "	classInfoDiv.style.display = 'block';\n"
                + "}\n"
                + "function hideClassInfoDiv(){\n"
                + "	var classInfoDiv = document.getElementById('classInfoDiv');\n"
                + "	classInfoDiv.style.display = 'none';\n"
                + "}\n"
                + "function positionClassInfoDiv(event){\n"
                + "	var classInfoDiv = document.getElementById('classInfoDiv');\n"
                + "	var values = getPositionForMouseObject(event);\n"
                + "	classInfoDiv.style.top = values[1];\n"
                + "	classInfoDiv.style.left = values[0];\n"
                + "}\n"
                +
                // JS code for the mouse over information of the mapped input vectors
                "function showInputInfo(elem, qe, mqe, dist, unit, event){\n"
                + "	setInputInfoText(qe, mqe, dist, unit);\n"
                + "	showInputInfoDiv();\n"
                + "	positionInputInfoDiv(event);\n"
                + "}\n"
                + "function setInputInfoText(qe, mqe, dist, unit){\n"
                + "	var inputInfoDiv = document.getElementById('inputInfoDiv');\n"
                + "	var inputfield;"
                + "	inputfield = document.getElementById('inputInfoDiv_qe');"
                + "	while(inputfield.lastChild != null) inputfield.removeChild(inputfield.lastChild);"
                + "	inputfield.appendChild(document.createTextNode(qe));"
                + "	inputfield = document.getElementById('inputInfoDiv_mqe');"
                + "	while(inputfield.lastChild != null) inputfield.removeChild(inputfield.lastChild);"
                + "	inputfield.appendChild(document.createTextNode(mqe));"
                + "	inputfield = document.getElementById('inputInfoDiv_dist');"
                + "	while(inputfield.lastChild != null) inputfield.removeChild(inputfield.lastChild);"
                + "	inputfield.appendChild(document.createTextNode(dist));"
                + "	inputfield = document.getElementById('inputInfoDiv_unit');"
                + "	while(inputfield.lastChild != null) inputfield.removeChild(inputfield.lastChild);"
                + "	inputfield.appendChild(document.createTextNode(unit));"
                + "}\n"
                + "function showInputInfoDiv(){"
                + "	var inputInfoDiv = document.getElementById('inputInfoDiv');\n"
                + "	inputInfoDiv.style.display = 'block';\n"
                + "}\n"
                + "function hideInputInfoDiv(){\n"
                + "	var inputInfoDiv = document.getElementById('inputInfoDiv');\n"
                + "	inputInfoDiv.style.display = 'none';\n"
                + "}\n"
                + "function positionInputInfoDiv(event){\n"
                + "	var inputInfoDiv = document.getElementById('inputInfoDiv');\n"
                + "	var values = getPositionForMouseObject(event);\n"
                + "	inputInfoDiv.style.top = values[1];\n"
                + "	inputInfoDiv.style.left = values[0];\n"
                + "}\n"
                +
                // helper function
                "function getPositionForMouseObject(event){"
                + "	if(!event){"
                + "		event = window.event;"
                + "	}"
                + "	var yoffset = 0;\n"
                + "	if(window.pageYOffset > 0){\n"
                + "		yoffset = window.pageYOffset;\n"
                + "	}else{\n"
                + "		yoffset = document.documentElement.scrollTop;\n"
                + "	}\n"
                + "	var xoffset = 0;\n"
                + "	if(window.pageXOffset > 0){\n"
                + "		xoffset = window.pageXOffset;\n"
                + "	}else{\n"
                + "		xoffset = document.documentElement.scrollLeft;\n"
                + "	}\n"
                + "	var x = event.clientX+20+xoffset+'px';\n"
                + "	if(this.status == 2){\n"
                + "		x = event.clientX - 20 - 550 -xoffset+'px';"
                + "	}"
                + "	return new Array(x, event.clientY+yoffset+'px');"
                + "}\n"
                +
                // JS function for expanding and collapsing the cluster tree
                "function swapClusterDisp(level){\n"
                + "	var content = document.getElementById(\"clusterNode_\"+level);\n"
                + "	if(content.style.display == \"none\"){\n"
                + "		content.style.display = \"block\";\n"
                + "	}else{\n"
                + "		content.style.display = \"none\";\n"
                + "	}\n"
                + "}\n"
                +

                // JS function for expanding the classlist within a cluster
                "function showClassesInCluster(level,runId){\n"
                + "	var content = document.getElementById(\"classes_\"+level+\"_\"+runId);\n"
                + "	if(content.style.display == \"none\"){\n"
                + "		content.style.display = \"block\";\n"
                + "	}else{\n"
                + "		content.style.display = \"none\";\n"
                + "	}\n"
                + "}\n"
                +

                // JS function for showing the descriptions from the individual Visualizations
                "function showVisualisationDescriptions(text){\n "
                + "	desc_win = window.open(\"\",\"\",\"width=600,height=300,scrollbars=yes,resizable=yes\");\n"
                + "	desc_win.document.write(text);\n"
                + "	desc_win.document.close();}\n"
                +

                // JS helper funktion for initializing the sdh,tp,and TW array
                "function initSDH(){"
                + "for(i = 0; i < sdhArray.length;i++){\n"
                + "sdhArray[i] = 1;\n"
                + "}\n"
                + "}\n"
                +

                "function initTP(){"
                + "for(i = 0; i < tpArray.length;i++){\n"
                + "tpArray[i] = 1;\n"
                + "}\n"
                + "}\n"
                +

                "function initTW(){"
                + "for(i = 0; i < twArray.length;i++){\n"
                + "twArray[i] = 1;\n"
                + "}\n"
                + "}\n"
                +

                "function setMaxAnzahlSDH(max){"
                + "maxSDHAnzahl = max\n"
                + "}\n"
                +

                "function setMaxAnzahlTW(max){"
                + "maxTWAnzahl = max\n"
                + "}\n"
                +

                "function setMaxAnzahlTP(max){"
                + "maxTPAnzahl = max\n"
                + "}\n"
                +
                // JSfunction for showing the SDH,TP & TW Images
                "function nextSDH(runId,step){\n" + "if(sdhArray[runId] >= maxSDHAnzahl){\n"
                + "document.getElementById(\"sdh_\"+ runId +\"_\"+sdhArray[runId]).style.display='none';\n"
                + "document.getElementById(\"sdh_\"+ runId +\"_\"+1).style.display='block';\n" + "sdhArray[runId]=1;\n" + "}\n" + "else{\n"
                + "document.getElementById(\"sdh_\"+ runId +\"_\"+sdhArray[runId]).style.display='none';\n"
                + "document.getElementById(\"sdh_\"+ runId +\"_\"+(sdhArray[runId]+step)).style.display='block';\n" + "sdhArray[runId]+=step;\n"
                + "}" + "}\n" + "function prevSDH(runId,step){\n" + "if(sdhArray[runId]<=1){\n"
                + "document.getElementById(\"sdh_\"+ runId +\"_\"+1).style.display='none';\n"
                + "document.getElementById(\"sdh_\"+ runId +\"_\"+maxSDHAnzahl).style.display='block';\n" + "sdhArray[runId]=maxSDHAnzahl;\n" + "}"
                + "else{\n" + "document.getElementById(\"sdh_\"+ runId +\"_\"+sdhArray[runId]).style.display='none';\n"
                + "document.getElementById(\"sdh_\"+ runId +\"_\"+(sdhArray[runId]-step)).style.display='block';\n" + "sdhArray[runId]-=step;\n"
                + "}" + "};\n" +

                "function nextTP(runId,step){\n" + "if(tpArray[runId] >= maxTPAnzahl){\n"
                + "document.getElementById(\"tp_\"+ runId +\"_\"+tpArray[runId]).style.display='none';\n"
                + "document.getElementById(\"tp_\"+ runId +\"_\"+1).style.display='block';\n" + "tpArray[runId]=1;\n" + "}\n" + "else{\n"
                + "document.getElementById(\"tp_\"+ runId +\"_\"+tpArray[runId]).style.display='none';\n"
                + "document.getElementById(\"tp_\"+ runId +\"_\"+(tpArray[runId]+step)).style.display='block';\n" + "tpArray[runId]+=step;\n" + "}"
                + "}\n" + "function prevTP(runId,step){\n" + "if(tpArray[runId]<=1){\n"
                + "document.getElementById(\"tp_\"+ runId +\"_\"+1).style.display='none';\n"
                + "document.getElementById(\"tp_\"+ runId +\"_\"+maxTPAnzahl).style.display='block';\n" + "tpArray[runId]=maxTPAnzahl;\n" + "}"
                + "else{\n" + "document.getElementById(\"tp_\"+ runId +\"_\"+tpArray[runId]).style.display='none';\n"
                + "document.getElementById(\"tp_\"+ runId +\"_\"+(tpArray[runId]-step)).style.display='block';\n" + "tpArray[runId]-=step;\n" + "}"
                + "};\n" +

                "function nextTW(runId,step){\n" + "if(twArray[runId] >= maxTWAnzahl){\n"
                + "document.getElementById(\"tw_\"+ runId +\"_\"+twArray[runId]).style.display='none';\n"
                + "document.getElementById(\"tw_\"+ runId +\"_\"+1).style.display='block';\n" + "twArray[runId]=1;\n" + "}\n" + "else{\n"
                + "document.getElementById(\"tw_\"+ runId +\"_\"+twArray[runId]).style.display='none';\n"
                + "document.getElementById(\"tw_\"+ runId +\"_\"+(twArray[runId]+step)).style.display='block';\n" + "twArray[runId]+=step;\n" + "}"
                + "}\n"
                + "function prevTW(runId,step){\n"
                + "if(twArray[runId]<=1){\n"
                + "document.getElementById(\"tw_\"+ runId +\"_\"+1).style.display='none';\n"
                + "document.getElementById(\"tw_\"+ runId +\"_\"+maxTWAnzahl).style.display='block';\n"
                + "twArray[runId]=maxTWAnzahl;\n"
                + "}"
                + "else{\n"
                + "document.getElementById(\"tw_\"+ runId +\"_\"+twArray[runId]).style.display='none';\n"
                + "document.getElementById(\"tw_\"+ runId +\"_\"+(twArray[runId]-step)).style.display='block';\n"
                + "twArray[runId]-=step;\n"
                + "}"
                + "};\n"
                +
                // Functions to display Semantic Classes
                "function showSemanticClassesinRegion(text,index,runId){\n "
                + "var content = document.getElementById(\"classes_\"+text+\"_\"+index+\"_\"+runId);" + "if(content.style.display==\"none\"){\n"
                + "content.style.display=\"block\";\n" + "}else{\n" + "content.style.display=\"none\";\n" + "}\n" + "}\n"
                + "function showSemanticDescription(text){\n " + "top.consoleRef=window.open('','myconsole','width=450','height=550'\n"
                + ",'menubar=0'\n" + ",'toolbar=1'\n" + ",'status=0'\n" + ",'scrollbars=1'\n" + ",'resizable=1');\n"
                + "top.consoleRef.document.writeln(\n" + "'<html><head><title>Console</title></head>'+\n"
                + "'<script LANGUAGE=\"JavaScript\" TYPE=\"text/javascript\" src=\"reporter.js\"></script>'+\n"
                + "'<body bgcolor=white onLoad=\"self.focus()\">'\n" + "+text+\n" + "'</body></html>');\n" + "top.consoleRef.document.close();\n"
                + "}\n";

        try {
            FileUtils.writeToFile(content, this.outputDirPath + "reporter.js");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

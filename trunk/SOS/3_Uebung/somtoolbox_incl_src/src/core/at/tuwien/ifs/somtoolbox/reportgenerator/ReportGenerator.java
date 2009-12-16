package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.MnemonicSOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.reportgenerator.gui.ReportGenWindow;
import at.tuwien.ifs.somtoolbox.reportgenerator.output.OutputReport;
import at.tuwien.ifs.somtoolbox.reportgenerator.output.OutputReportHtml;
import at.tuwien.ifs.somtoolbox.reportgenerator.output.OutputReportLATEX;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: ReportGenerator.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ReportGenerator {

    public static final int HTML_REPORT = 1;

    public static final int LATEX_REPORT = 2;

    /**
     * the main method if the report generator is called as a stand alone application
     * 
     * @throws SOMLibFileFormatException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, SOMLibFileFormatException {
        Parameter[] options = new Parameter[] { OptionFactory.getOptUnitDescriptionFile(true), OptionFactory.getOptWeightVectorFile(true),
                OptionFactory.getOptClassInformationFile(false), OptionFactory.getOptMapDescriptionFile(false),
                OptionFactory.getOptDataInformationFileFile(false), OptionFactory.getOptDataWinnerMappingFile(false),
                OptionFactory.getOptInputVectorFile(false), OptionFactory.getOptTemplateVectorFile(false), OptionFactory.getOptOutputFileName(false),
                OptionFactory.getOptProperties(false) };
        JSAPResult config = OptionFactory.parseResults(args, options);

        MnemonicSOMLibFormatInputReader inputReader = new MnemonicSOMLibFormatInputReader(config.getString("weightVectorFile"),
                config.getString("getOptUnitDescriptionFile"), config.getString("mapDescriptionFile"));
        GrowingSOM gsom = new GrowingSOM(inputReader);

        CommonSOMViewerStateData state = new CommonSOMViewerStateData();
        state.growingSOM = gsom;
        state.growingLayer = gsom.getLayer();
        state.somInputReader = inputReader;

        SharedSOMVisualisationData data = new SharedSOMVisualisationData();
        data.setFileName(SOMVisualisationData.CLASS_INFO, config.getString("classInformationFile"));
        data.setFileName(SOMVisualisationData.DATA_WINNER_MAPPING, config.getString("dataWinnerMappingFile"));
        data.setFileName(SOMVisualisationData.INPUT_VECTOR, config.getString("inputVectorFile"));
        data.setFileName(SOMVisualisationData.TEMPLATE_VECTOR, config.getString("templateVectorFile"));
        data.readAvailableData();
        state.inputDataObjects = data;
        UiUtils.setSOMToolboxLookAndFeel();
        new ReportGenerator(true, state, config.getString("output"), config.getString("properties"));
    }

    public ReportGenerator(boolean standalone, CommonSOMViewerStateData state) {
        this(standalone, state, null, null);
    }

    public ReportGenerator(boolean standalone, CommonSOMViewerStateData state, String outputFile, String propertiesFile) {
        new ReportGenWindow(standalone, this, state, outputFile, propertiesFile);
    }

    public void createReport(int type, String outputDirectory, DatasetInformation datasetInfo, TestRunResultCollection testruns) {

        OutputReport report = null;

        switch (type) {
            case HTML_REPORT:
                report = new OutputReportHtml(outputDirectory, datasetInfo.getEP());
                break;

            case LATEX_REPORT:
                report = new OutputReportLATEX(outputDirectory);
                break;

            default:
                Logger.getLogger("at.tuwien.ifs.somtoolbox.reports").severe("unkown report type ... no report created.");
                return;
        }
        report.setDatasetInformation(datasetInfo);
        report.setTestrunInformation(testruns, type);
        report.createOutput();
    }
}

package at.tuwien.ifs.somtoolbox.reportgenerator.output;

import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.EditableReportProperties;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;

/**
 * Objects of this type handle the HTML report-writing for Growing Grids. In additon to the information outputtet by the SOMDescription, also
 * information about tau2 is given
 * 
 * @author Sebastian Skritek (0226286) Sebastian.Skritek@gmx.at
 * @author Martin Waitzbauer (0226025)
 * @version $Id: SOMGHSOMDescriptionHTML.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMGHSOMDescriptionHTML extends SOMGGDescriptionHTML {

    public SOMGHSOMDescriptionHTML(ReportFileWriter writer, DatasetInformation dataset, TestRunResult testrun, String imgDir,
            EditableReportProperties EP) {
        super(writer, dataset, testrun, imgDir, EP);
    }

    /**
     * initiates the creation of the output Creates the description of the SOM and training properties
     */
    @Override
    public void printSOMDescription() {

        // SOM & training properties
        this.writer.appendOutput("<ul>");
        this.printSOMProperties();
        this.writer.appendOutput("</ul>");

        // something about the different levels of the SOM
        // this.printMapLevels();

        // data distribution on SOM
        this.printDataDistribution();
    }

    @Override
    protected void printTopologyOfSOM() {
        this.writer.appendOutput("<li>Type of SOM: Growing Hierachical SOM</li>\n");
    }

    /**
     * initiates the creation of the output Creates the description of the SOM and training properties
     */
    @Override
    protected void printSOMProperties() {

        this.printTopologyOfSOM();
        this.printSOMDimensions();
        this.printMapNumbers();
        this.printSigma();
        this.printTau();
        this.printTau2();
        this.printLearningRate();
        this.printNeighbourhoodFunction();
        this.printMetricUsed();
        this.printNumberOfIterations();
        this.printRandomSeed();
        this.printTrainingTime();
        this.printTrainingDate();

    }

    protected void printMapNumbers() {
        int numb = this.testrun.getNumberOfMaps();
        if (numb > 1) {
            this.writer.appendOutput("<li>This SOM consists of " + numb + " maps</li>\n");
        } else {
            this.writer.appendOutput("<li>This SOM consists of " + numb + " map</li>\n");
        }
    }
}

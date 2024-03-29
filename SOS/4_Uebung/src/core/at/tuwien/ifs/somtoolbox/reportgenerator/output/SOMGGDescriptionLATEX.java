package at.tuwien.ifs.somtoolbox.reportgenerator.output;

import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;

/**
 * Objects of this type handle the HTML report-writing for Growing Grids. In additon to the information outputtet by the SOMDescription, also
 * information about tau2 is given
 * 
 * @author Sebastian Skritek (0226286) Sebastian.Skritek@gmx.at
 * @author Martin Waitzbauer (0226025)
 * @version $Id: SOMGGDescriptionLATEX.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMGGDescriptionLATEX extends SOMDescriptionLATEX {

    public SOMGGDescriptionLATEX(ReportFileWriter writer, DatasetInformation dataset, TestRunResult testrun, String imgDir) {
        super(writer, dataset, testrun, imgDir);
    }

    /**
     * initiates the creation of the output Creates the description of the SOM and training properties
     */
    @Override
    protected void printSOMProperties() {

        this.printTopologyOfSOM();
        this.printSOMDimensions();
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

    protected void printTau2() {
        String tau2 = "Property file not defined";
        if (this.testrun.getTau2() >= 0) {
            tau2 = "" + this.testrun.getTau2();
        }
        this.writer.appendOutput("\\item Maximum data representation granularity (tau2): " + tau2 + "\n");
    }

    @Override
    protected void printTopologyOfSOM() {
        this.writer.appendOutput("\\item Type of SOM: Growing Grid\n");
    }
}

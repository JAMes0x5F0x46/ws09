package at.tuwien.ifs.somtoolbox.reportgenerator.output;

import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResult;

/**
 * Objects of this type handle the HTML report-writing for Growing Grids. In additon to the information outputtet by the SOMDescription, also
 * information about tau2 is given
 * 
 * @author Sebastian Skritek (0226286) Sebastian.Skritek@gmx.at
 * @author Martin Waitzbauer (0226025)
 * @version $Id: SOMGHSOMDescriptionLATEX.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMGHSOMDescriptionLATEX extends SOMGGDescriptionLATEX {

    public SOMGHSOMDescriptionLATEX(ReportFileWriter writer, DatasetInformation dataset, TestRunResult testrun, String imgDir) {
        super(writer, dataset, testrun, imgDir);
    }

    /**
     * initiates the creation of the output Creates the description of the SOM and training properties
     */
    @Override
    public void printSOMDescription() {

        // SOM & training properties
        this.writer.appendOutput("\\begin{itemize}");
        this.printSOMProperties();
        this.writer.appendOutput("\\end{itemize}");

        // something about the different levels of the SOM
        // this.printMapLevels();

        // data distribution on SOM
        this.printDataDistribution();
    }

    @Override
    protected void printTopologyOfSOM() {
        this.writer.appendOutput("\\item Type of SOM: Growing Hierachical SOM\n");
    }

    /**
     * initiates the creation of the output. Creates the description of the SOM and training properties
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
            this.writer.appendOutput("\\item This SOM consists of " + numb + " maps\n");
        } else {
            this.writer.appendOutput("\\item This SOM consists of " + numb + " map\n");
        }
    }
}

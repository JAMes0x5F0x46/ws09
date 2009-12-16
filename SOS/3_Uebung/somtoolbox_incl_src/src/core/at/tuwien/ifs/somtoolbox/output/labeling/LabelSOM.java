package at.tuwien.ifs.somtoolbox.output.labeling;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.Label;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Implements the <code>LabelSOM</code> labelling method, as described in <i><b>A. Rauber, and D. Merkl</b>: Automatic Labeling of Self-Organizing
 * Maps for Information Retrieval In: Journal of Systems Research and Information Systems (JSRIS), Vol. 10, Nr. 10, pp 23-45, OPA, Gordon and Breach
 * Science Publishers, December 2001.</i>
 * 
 * @author Michael Dittenbach
 * @version $Id: LabelSOM.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LabelSOM extends AbstractLabeler implements Labeler , SOMToolboxApp {

    /**
     * Method for stand-alone execution of map labelling. Options are:<br/>
     * <ul>
     * <li>-v Input file containing the input vectors of.</li>
     * <li>-t Template vector file containing vector element labels.</li>
     * <li>-w Weight vector filename, mand.</li>
     * <li>-u Unit description file, mand.</li>
     * <li>-n Number of labels, opt., default = 5</li>
     * <li>-d Set if input data vectors are densely populated.</li>
     * <li>-m Map description file, opt.</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     */
    public static void main(String[] args) {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_LABEL_SOM);

        int numLabels = config.getInt("numberLabels", AbstractNetworkModel.DEFAULT_LABEL_COUNT);
        String inputVectorFilename = config.getString("inputVectorFile");
        boolean denseData = config.getBoolean("denseData", false);
        boolean ignoreLabelsWithZero = config.getBoolean("ignoreLabelsWithZero", false);
        String templateVectorFilename = config.getString("templateVectorFile", null);
        String unitDescriptionFilename = config.getString("unitDescriptionFile", null);
        String weightVectorFilename = config.getString("weightVectorFile");
        String mapDescriptionFilename = config.getString("mapDescriptionFile", null);

        String outputDirName = unitDescriptionFilename.substring(0, unitDescriptionFilename.lastIndexOf(System.getProperty("file.separator")) + 1);
        if (StringUtils.isBlank(outputDirName)) {
            outputDirName = ".";
        }
        String outputFileName = unitDescriptionFilename.substring(unitDescriptionFilename.lastIndexOf(System.getProperty("file.separator")) + 1,
                unitDescriptionFilename.indexOf('.', unitDescriptionFilename.lastIndexOf(System.getProperty("file.separator")) + 1));

        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Labelling map '" + outputFileName + "' to output directory: " + outputDirName);

        GrowingSOM gsom = null;
        try {
            gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFilename, unitDescriptionFilename, mapDescriptionFilename));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            return;
        }
        // TODO: cacheBlock=1, no problem
        InputData data = SOMLibSparseInputData.create(inputVectorFilename, templateVectorFilename, !denseData, true, 1, 7);
        LabelSOM labeler = new LabelSOM();
        labeler.label(gsom, data, numLabels, ignoreLabelsWithZero);

        try {
            // TODO: make output format an argument, zipped output
            SOMLibMapOutputter.writeUnitDescriptionFile(gsom, outputDirName, outputFileName, true);
        } catch (IOException e) { // TODO: create new exception type
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Could not open or write to output file: " + e.getMessage() + ": " + e.getMessage());
            return;
        }

    }

    public void label(GHSOM ghsom, InputData data, int num) {
        label(ghsom.topLayerMap(), data, num);
    }

    public void label(GrowingSOM gsom, InputData data, int num) {
        label(gsom, data, num, false);
    }

    public void label(GrowingSOM gsom, InputData data, int num, boolean ignoreLabelsWithZero) {
        if (num > data.templateVector().dim()) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                    "Specified number of labels (" + num + ") exceeds number of features in template vector (" + data.templateVector().dim()
                            + ") - defaulting to number of features as maximum possible value.");
            num = data.templateVector().dim();
        }
        Unit[] units = gsom.getLayer().getAllUnits();
        StdErrProgressWriter progress = new StdErrProgressWriter(units.length, "Labelling unit ");
        for (int i = 0; i < units.length; i++) { // do labeling for each unit
            progress.progress(i);
            if (units[i].getNumberOfMappedInputs() != 0) {
                InputDatum[] unitData = data.getInputDatum(units[i].getMappedInputNames());
                Label[] allLabels = new Label[data.dim()];

                // for each feature, check all inputs (qe und durchschnittsvalue)
                for (int ve = 0; ve < data.dim(); ve++) {
                    double meanVal = 0;
                    double qeVal = 0;
                    for (int j = 0; j < unitData.length; j++) {
                        meanVal += unitData[j].getVector().get(ve);
                        qeVal += (Math.abs(unitData[j].getVector().get(ve) - units[i].getWeightVector()[ve]));
                    }
                    meanVal = meanVal / unitData.length;
                    qeVal = qeVal / unitData.length;

                    // if we shall ignore zero labels, ignore those with mean==0, and very small qe
                    if (ignoreLabelsWithZero && meanVal == 0 && (qeVal * 100 < 0.1)) {
                        allLabels[ve] = new Label("", meanVal, qeVal);
                    } else {
                        allLabels[ve] = new Label(data.templateVector().getLabel(ve), meanVal, qeVal);
                    }
                }
                Label[] labelSortedByQe = new Label[data.dim()];
                Label[] labelSortedByMean = new Label[data.dim()];
                for (int j = 0; j < data.dim(); j++) {
                    labelSortedByQe[j] = allLabels[j];
                    labelSortedByMean[j] = allLabels[j];
                }
                Label.sortByQe(labelSortedByQe, Label.SORT_ASC);
                Label.sortByValue(labelSortedByMean, Label.SORT_ASC);

                // determine select num top labels
                Label[] labels = new Label[num];
                int found = 0;
                int lab = 0;
                while ((found < num) && (lab < data.dim())) { // go through list sorted by qe
                    boolean found2 = false;
                    int lab2 = data.dim() - 1;
                    while ((found2 == false) && (lab2 >= (data.dim() - num))) {
                        if (labelSortedByMean[lab2].equals(labelSortedByQe[lab])) {
                            found2 = true;
                            labels[found] = labelSortedByQe[lab];
                            found++;
                        }
                        lab2--;
                    }
                    lab++;
                }
                Label.sortByValueQe(labels, Label.SORT_DESC, Label.SORT_ASC);
                units[i].setLabels(labels);

                if (units[i].getMappedSOM() != null) { // label subordinate maps as well
                    label(units[i].getMappedSOM(), data, num);
                }

            }
        }
        gsom.setLabelled(true);
    }
}

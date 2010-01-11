package at.tuwien.ifs.somtoolbox.apps.metricEval;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L1Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.LInfinityMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.LnAlphaMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.LnMetric;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

/**
 * @author Rudolf Mayer
 * @version $Id: MetricEvaluation.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MetricEvaluation {

    public static void main(String[] args) throws IOException, SOMToolboxException {
        String root = args[0] + File.separator;
        String collection = args[1];
        String[] featureMethods;
        String outputDir = "results";
        if (args.length > 2) {
            outputDir = args[2];
        }
        if (!outputDir.endsWith(File.separator)) {
            outputDir += File.separator;
        }

        new File(outputDir).mkdirs();

        if (args.length > 3) {
            featureMethods = new String[] { args[3] };
        } else {
            featureMethods = new String[] { "rh", "ssd", "rp" };
        }

        // Test parameters
        DistanceMetric[] metrics = new DistanceMetric[] { new L1Metric(), new LnMetric(1.5), new L2Metric(), new LnMetric(2.5), new LInfinityMetric() };
        int[] precisions = new int[] { 100, 50, 30, 20, 10, 5, 3, 1 };

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM, HH:mm:ss");

        for (int featureIndex = 0; featureIndex < featureMethods.length; featureIndex++) { // DIFFERENT FEATURES

            Date startDate = new Date();
            PrintWriter writer = new PrintWriter(new FileWriter(outputDir + collection + "-" + featureMethods[featureIndex] + "_results_"
                    + new SimpleDateFormat("yyyyMMdd_HHmmss").format(startDate) + ".csv"));
            writer.println("Collection: " + collection);
            writer.println("Feature: " + featureMethods[featureIndex]);
            writer.println("Started: " + startDate);
            String baseFileName = root + File.separator + collection + File.separator;
            baseFileName = root + File.separator + collection + File.separator;
            SOMLibClassInformation classInfo = new SOMLibClassInformation(baseFileName + collection + ".cls");
            String vectorBaseFileName = baseFileName + "vec" + File.separator + collection + ".";
            // DistanceMetric[] metrics = new DistanceMetric[];

            writer.print("\n\nFeatures,Metric,Alpha,");
            for (int k = 0; k < precisions.length; k++) {
                writer.print("Precision at " + precisions[k]);
                if (k + 1 < precisions.length) {
                    writer.print(",");
                }
            }
            writer.println("");

            AbstractSOMLibSparseInputData data = SOMLibSparseInputData.create(vectorBaseFileName + featureMethods[featureIndex] + ".norm.gz");
            System.out.println("\r**** " + simpleDateFormat.format(new Date()) + ": feature " + featureMethods[featureIndex].toUpperCase() + " ["
                    + data.dim() + " dims]" + "                                      ");

            for (int metricIndex = 0; metricIndex < metrics.length; metricIndex++) { // METRIC
                System.out.println("\r\t" + simpleDateFormat.format(new Date()) + ": " + metrics[metricIndex]
                        + "                                      ");
                // for (double alpha = 0.05; alpha < 1.05; alpha += 0.05) { // ALPHA Values
                for (int alphaValue = 1; alphaValue <= 20; alphaValue++) { // ALPHA Values
                    double alpha = alphaValue * 0.05;
                    System.out.println("\r\t\t" + simpleDateFormat.format(new Date()) + ": alpha: " + alpha
                            + "                                      ");
                    data.transformValues(new LnAlphaMetric(alpha, 1));
                    data.initDistanceMatrix(metrics[metricIndex]);
                    StdErrProgressWriter progress = new StdErrProgressWriter(data.numVectors(), "\t\tCalculating precision for vector ");

                    double[] averagePrecisions = new double[precisions.length];
                    for (int i = 0; i < averagePrecisions.length; i++) {
                        averagePrecisions[i] = 0;
                    }

                    for (int vectorIndex = 0; vectorIndex < data.numVectors(); vectorIndex++) {
                        progress.progress(vectorIndex);
                        InputDatum input = data.getInputDatum(vectorIndex);
                        int classIndex = classInfo.getClassIndex(input.getLabel());
                        // classInfo.getNumberOfClassMembers(classIndex);
                        InputDatum[] matches = data.getNearestN(vectorIndex, metrics[metricIndex], precisions[0]);

                        for (int precisionIndex = 0; precisionIndex < precisions.length; precisionIndex++) { // calucalte precision
                            int sameClassCount = 0;
                            for (int currentMatch = 0; currentMatch < precisions[precisionIndex]; currentMatch++) {
                                if (classInfo.getClassIndex(matches[currentMatch].getLabel()) == classIndex) {
                                    sameClassCount++;
                                }
                            }
                            double precision = ((double) sameClassCount) / ((double) precisions[precisionIndex]);
                            averagePrecisions[precisionIndex] += precision;
                        }
                    }
                    writer.print(featureMethods[featureIndex] + "," + metrics[metricIndex] + "," + alpha + ",");
                    for (int i = 0; i < averagePrecisions.length; i++) { // caluclate average precision for each feature
                        averagePrecisions[i] = averagePrecisions[i] / data.numVectors();
                        writer.print(averagePrecisions[i]);
                        if (i + 1 < averagePrecisions.length) {
                            writer.print(",");
                        }
                    }
                    writer.println("");
                    writer.flush();
                }
                writer.println("\n");
            }
            writer.println("\n\n");
            Date endDate = new Date();
            double duration = endDate.getTime() - startDate.getTime() / 1000;
            String endMessage = "Finished: " + endDate + " (" + duration / (60 * 60) + " minutes)";
            System.out.println(endMessage);
            writer.println(endMessage);
            writer.close();
        }
    }

}

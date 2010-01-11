package at.tuwien.ifs.somtoolbox.apps.analysis;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.apache.commons.math.stat.StatUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.util.ElementCounter;
import at.tuwien.ifs.somtoolbox.util.InverseComparator;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

import com.martiansoftware.jsap.JSAPResult;

/**
 * @author Rudolf Mayer
 * @version $Id: FeatureDistributionAnalysis.java 2874 2009-12-11 16:03:27Z frank $
 */
public class FeatureDistributionAnalysis {
    private static final DecimalFormat format = StringUtils.format5FractionDigits;

    private static final String separator = "---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";

    public static void main(String[] args) throws SOMToolboxException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptInputVectorFile(true),
                OptionFactory.getOptClassInformationFile(false));
        String inputVectorFileName = config.getString("inputVectorFile");
        String classInfoFileName = config.getString("classInformationFile");

        final int paddingLength = 9;

        SOMLibSparseInputData input = new SOMLibSparseInputData(inputVectorFileName);
        SOMLibClassInformation classInfo = new SOMLibClassInformation(classInfoFileName);
        input.setClassInfo(classInfo);
        String[] classNames = classInfo.getClassNames();

        double[][] means = new double[classNames.length][];
        double[][] variances = new double[classNames.length][input.dim()];
        double[] classVariances = new double[input.dim()];
        double[] totalVariances = new double[input.dim()];
        double[] aggMeans = new double[input.dim()];
        int[] occurrences = new int[input.dim()];

        final L2Metric metric = new L2Metric();
        input.initDistanceMatrix(metric);

        final double[][] data = input.getData();

        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            double[][] calssData = input.getData(className);
            means[i] = VectorTools.meanVector(calssData);
            for (int j = 0; j < means[i].length; j++) {
                if (means[i][j] > 0) {
                    occurrences[j]++;
                }
            }
            for (int j = 0; j < variances[i].length; j++) {
                variances[i][j] = StatUtils.variance(VectorTools.slice(calssData, j));
            }
        }
        for (int i = 0; i < classVariances.length; i++) {
            // System.out.println(Arrays.toString(VectorTools.slice(means, i)));
            classVariances[i] = StatUtils.variance(VectorTools.slice(means, i));
            aggMeans[i] = StatUtils.mean(VectorTools.slice(means, i));
            totalVariances[i] = StatUtils.variance(VectorTools.slice(data, i));
        }

        // output
        System.out.println("\n");
        String[] classNameDifferences = StringUtils.getDifferences(classNames);
        for (int i = 0; i < classNameDifferences.length; i++) {
            String classNameDifference = classNameDifferences[i];
            System.out.print(pad(StringUtils.formatEndMaxLengthEllipsis(classNameDifference, paddingLength, ".."), paddingLength));
            System.out.print(pad("Var", paddingLength) + (i + 1 < classNameDifferences.length ? " | " : ""));
        }
        System.out.print("|| " + pad("Mean", paddingLength) + pad("Var", paddingLength) + pad("Var.Mean", paddingLength) + pad("#Occ", paddingLength));
        System.out.println("\n" + separator);
        for (int i = 0; i < means[0].length; i++) {
            for (int j = 0; j < means.length; j++) {
                System.out.print(pad(means[j][i], paddingLength));
                System.out.print(pad(variances[j][i], paddingLength) + (j + 1 < means.length ? " | " : ""));
            }
            System.out.print("|| " + pad(aggMeans[i], paddingLength));
            System.out.print(pad(classVariances[i], paddingLength));
            System.out.print(pad(totalVariances[i], paddingLength));
            System.out.print(pad(occurrences[i], paddingLength));
            System.out.println();
        }

        System.out.println(separator + "\n");
        System.out.println("Nearest neighbours");
        System.out.print(pad("Weight-Vec", 15));

        int paddingLength2 = paddingLength - 2;
        for (int i = 0; i < classNameDifferences.length; i++) {
            String classNameDifference = classNameDifferences[i];
            System.out.print(pad(StringUtils.formatEndMaxLengthEllipsis(classNameDifference, paddingLength2, ".."), paddingLength2));
            System.out.print(" | ");
        }
        System.out.print(pad("Purity", paddingLength2));
        System.out.print(pad("MapSize", paddingLength2));
        System.out.println(pad("# Neighb", paddingLength2));

        String[] differences = StringUtils.getDifferences(input.getLabels());
        for (int i = 0; i < input.numVectors(); i++) {
            int[] perClass = new int[classNames.length];
            InputDatum inputDatum = input.getInputDatum(i);
            int classIndex = classInfo.getClassIndexForInput(inputDatum.getLabel());

            int classMemberCount = classInfo.getNumberOfClassMembers(classIndex) - 1;
            int number = classMemberCount * 1;
            final InputDatum[] nearestN = input.getNearestN(i, metric, number);
            for (InputDatum neighbour : nearestN) {
                perClass[classInfo.getClassIndexForInput(neighbour.getLabel())]++;
            }
            System.out.print(pad(differences[i], 15));
            for (int j = 0; j < perClass.length; j++) {
                int index = perClass[j];
                System.out.print(pad(index, paddingLength2) + " | ");
            }
            System.out.print(pad(StringUtils.format2FractionDigits.format(perClass[classIndex] * 100.0 / classMemberCount) + "%", paddingLength2));
            System.out.print(pad(classInfo.getNumberOfClassMembers(classIndex), paddingLength2));
            System.out.println(number);
        }

        System.out.println(separator + "\n");
        System.out.println("Total features: " + input.dim());
        ElementCounter<Integer> counter = new ElementCounter<Integer>();
        for (int d : occurrences) {
            counter.incCount(d);
        }

        Integer[] keys = counter.keySet().toArray(new Integer[counter.size()]);
        Arrays.sort(keys, new InverseComparator<Integer>());
        for (Integer key : keys) {
            System.out.println(key + " times: " + counter.getCount(key));
        }

        System.out.println(separator + "\n");
        System.out.println("Co-occurence of terms with other classes ");
        int paddingLength3 = paddingLength+4;
        System.out.print(pad("Class/Count", paddingLength3) + " | ");
        for (int i = 0; i < classNames.length; i++) {
            System.out.print(pad(i, paddingLength2));
        }
        System.out.print(pad(" | Total", paddingLength2));
        System.out.print(pad(" | Dim", paddingLength2));
        System.out.println();
        System.out.println(StringUtils.repeatString((classNames.length+2) * paddingLength2 + paddingLength3, "-"));

        for (int i = 0; i < classNameDifferences.length; i++) {
            String classNameDifference = classNameDifferences[i];
            System.out.print(pad(classNameDifference, paddingLength3) + " | ");

            // check terms this class uses
            ElementCounter<Integer> counter2 = new ElementCounter<Integer>();
            for (int j = 0; j < means[i].length; j++) {
                if (means[i][j] > 0) {
                    // count how often these terms are used in other classes
                    int otherClassTerms = 0;
                    for (int k = 0; k < classNameDifferences.length; k++) {
                        if (k != i && means[k][j] > 0) {
                            otherClassTerms++;
                        }
                    }
                    counter2.incCount(otherClassTerms);
                }
            }
            // System.out.println(counter2.keySet());
            for (int j = 0; j < classNameDifferences.length; j++) {
                System.out.print(pad(counter2.getCount(j), paddingLength2));
            }
            System.out.print(pad(" | " + counter2.totalCount(), paddingLength2));
            System.out.print(pad(" | " + input.dim(), paddingLength2));
            System.out.println();
        }
    }

    private static String pad(double value, int len) {
        return pad(format.format(value), len);
    }

    private static String pad(final String s, int len) {
        return s + StringUtils.getSpaces(len - s.length());
    }
}

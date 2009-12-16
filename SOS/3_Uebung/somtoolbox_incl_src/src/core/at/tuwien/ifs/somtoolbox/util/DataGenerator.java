package at.tuwien.ifs.somtoolbox.util;

import java.io.IOException;

import org.apache.commons.math.random.RandomDataImpl;

import at.tuwien.ifs.somtoolbox.apps.DataSetViewer;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

public class DataGenerator {
    static private RandomDataImpl rand = new RandomDataImpl();

    public static void main(String[] args) throws IOException, SOMLibFileFormatException {
        double[][] dataDef1 = { { 2.0, 2.0 }, { 0.5, 0.5 } };
        double[][] dataDef2 = { { 0.0, 0.0 }, { 1, 1 } };
        double[][] dataDef3 = { { -3, 3 }, { 1.5, 1.5 } };
        double[][][] dataDef = { dataDef1, dataDef2, dataDef3 };
        int[] numberOfPoints = { 150, 100, 75 };
        int dimension = 2;

        int totalNumber = 0;
        for (int i = 0; i < numberOfPoints.length; i++) {
            totalNumber += numberOfPoints[i];
        }

        double[][][] data = new double[dataDef.length][][];
        String[] labels = new String[dataDef.length];
        String[][] dataNames = new String[dataDef.length][];

        InputDatum[] d = new InputDatum[totalNumber];

        int index = 0;
        for (int i = 0; i < dataDef.length; i++) {
            labels[i] = "Cluster_" + i;
            data[i] = new double[numberOfPoints[i]][];
            dataNames[i] = new String[numberOfPoints[i]];
            for (int j = 0; j < numberOfPoints[i]; j++) {
                dataNames[i][j] = labels[i] + "_" + (j + 1);
                d[index] = generatePoint(dataNames[i][j], dataDef[i][0], dataDef[i][1]);
                data[i][j] = d[index].getVector().toArray();
                index++;
            }
        }
        String basicFileName = args[0];
        SOMLibClassInformation classInfo = new SOMLibClassInformation(labels, dataNames);
        classInfo.writeToFile(basicFileName + ".cls");

        SOMLibSparseInputData inputData = new SOMLibSparseInputData(d, classInfo);
        inputData.writeToFile(basicFileName + ".vec");

        SOMLibTemplateVector templateVector = new SOMLibTemplateVector(totalNumber, dimension);
        templateVector.writeToFile(basicFileName + ".tv");

        DataSetViewer viewer = new DataSetViewer(null, "Viewer", labels, data, null);
        viewer.setVisible(true);
        index++;
    }

    public static InputDatum[] generatePoints(String name, int num, double[] mean, double[] sigma) {
        InputDatum[] res = new InputDatum[num];
        for (int i = 0; i < num; i++) {
            res[i] = generatePoint(name + (i + 1), mean, sigma);
        }
        return res;
    }

    private static InputDatum generatePoint(String name, double[] mean, double[] sigma) {
        double[] values = new double[mean.length];
        for (int j = 0; j < mean.length; j++) {
            values[j] = rand.nextGaussian(mean[j], sigma[j]);
        }
        return new InputDatum(name, new DenseDoubleMatrix1D(values));
    }

}

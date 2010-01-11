package at.tuwien.ifs.somtoolbox.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

import com.martiansoftware.jsap.JSAPResult;

/**
 * This implementation stores the distances in memory. It is constructed either by calculating distances from the given input data on the fly, or by
 * reading them from an ASCII file. or memory is an issue (consider using {@link RandomAccessFileInputVectorDistanceMatrix} instead).
 * 
 * @author Rudolf Mayer
 * @version $Id: MemoryInputVectorDistanceMatrix.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MemoryInputVectorDistanceMatrix extends InputVectorDistanceMatrix {
    public static final String FILE_TYPE = "Distance Matrix File";

    protected double[][] distanceMatrix;

    protected MemoryInputVectorDistanceMatrix() {
    }

    /**
     * Constructs the distance matrix by computing the distances on the fly. Not suited for large data sets, where the computation time might take
     * long (consider reading it from a file using {@link #MemoryInputVectorDistanceMatrix(String)})
     */
    public MemoryInputVectorDistanceMatrix(InputData data, DistanceMetric metric) throws MetricException {
        numVectors = data.numVectors();
        distanceMatrix = new double[numVectors][numVectors];
        StdErrProgressWriter progress = new StdErrProgressWriter(numVectors, "Calculating distance matrix: ", numVectors / 10);
        for (int i = 0; i < numVectors; i++) {
            for (int j = i + 1; j < numVectors; j++) {
                distanceMatrix[i][j] = metric.distance(data.getInputDatum(i), data.getInputDatum(j));
                distanceMatrix[j][i] = distanceMatrix[i][j];
            }
            progress.progress(i);
        }
    }

    /** Reads the distance matrix from an ASCII file, and stores it in memory. */
    public MemoryInputVectorDistanceMatrix(String fileName) throws IOException, SOMToolboxException {
        BufferedReader br = FileUtils.openFile(FILE_TYPE, fileName);
        Map<String, String> headers = FileUtils.readSOMLibFileHeaders(br);
        numVectors = Integer.parseInt(headers.get("$NUM_VECTORS"));
        metric = AbstractMetric.instantiateNice(headers.get(SOMLibMapDescription.METRIC));

        distanceMatrix = new double[numVectors][numVectors];
        String line = headers.get("FIRST_CONTENT_LINE");
        int lineNumber = 0;
        while (line != null) {
            line = line.trim();
            String[] distances = line.split(" ");
            if (distances.length != numVectors - (lineNumber + 1)) {
                throw new SOMToolboxException("Distance Matrix File corrupt in data line " + lineNumber + ", contains " + distances.length
                        + " instead of " + (numVectors - (lineNumber + 1)) + " expected elements!");
            }
            for (int i = 0; i < distances.length; i++) {
                if (distances[i].trim().length() == 0) {
                    System.out.println("empty element in " + lineNumber + ", " + i);
                }
                distanceMatrix[lineNumber][i + lineNumber + 1] = Double.parseDouble(distances[i]);
                distanceMatrix[i + lineNumber + 1][lineNumber] = distanceMatrix[lineNumber][i + lineNumber + 1];
            }
            line = br.readLine();
            lineNumber++;
        }
    }

    @Override
    public double getDistance(int x, int y) {
        return distanceMatrix[x][y];
    }

    @Override
    public double[] getDistances(int x) {
        return distanceMatrix[x];
    }

    /** Write input distance matrix to ASCII file, computing distances on the fly. */
    public static void writeFileInputVectorDistanceMatrix(InputData data, String fileName, DistanceMetric metric) throws IOException, MetricException {
        int numVec = data.numVectors();
        PrintWriter out = printHeader(numVec, fileName, metric);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Storing input distance matrix with metric " + metric + " to ASCII file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        for (int i = 0; i < numVec; i++) {
            for (int j = i + 1; j < numVec; j++) {
                out.print(metric.distance(data.getInputDatum(i), data.getInputDatum(j)));
                if (j + 1 < numVec) {
                    out.print(" ");
                }
            }
            if (i + 1 < numVec) { // only print newline if lines are not empty
                out.println();
            }
            progress.progress();
        }
        out.flush();
        out.close();
    }

    /** Write pre-calculated input distance matrix to ASCII file. */
    public static void writeFileInputVectorDistanceMatrix(double[][] distances, String fileName, DistanceMetric metric) throws IOException,
            MetricException {
        int numVec = distances[0].length;
        PrintWriter out = printHeader(distances.length, fileName, metric);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                "Storing pre-calculated input distance matrix with metric " + metric + " to ASCII file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        for (int i = 0; i < numVec; i++) {
            for (int j = i + 1; j < distances[i].length; j++) {
                out.print(distances[i][j]);
                if (j + 1 < distances[i].length) {
                    out.print(" ");
                }
            }
            if (i + 1 < distances[i].length) { // only print newline if lines are not empty
                out.println();
            }
            progress.progress();
        }
        out.flush();
        out.close();
    }

    private static PrintWriter printHeader(int numVectors, String fileName, DistanceMetric metric) throws IOException {
        final PrintWriter out = FileUtils.openFileForWriting(FILE_TYPE, fileName, true);
        out.println("$NUM_VECTORS " + numVectors);
        out.println(SOMLibMapDescription.METRIC + " " + metric.getClass().getCanonicalName());
        return out;
    }

    /** Writes the given input data to an ASCII file. */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
            SOMToolboxException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_INPUT_DISTANCE_COMPUTER);
        String inputVectorFileName = config.getString("inputVectorFile");
        String outputFileName = config.getString("output");
        String metricName = config.getString("metric");
        InputData data = new SOMLibSparseInputData(inputVectorFileName);
        final DistanceMetric metric = AbstractMetric.instantiate(metricName);
        writeFileInputVectorDistanceMatrix(data, outputFileName, metric);
        // MemoryInputVectorDistanceMatrix matrix = new MemoryInputVectorDistanceMatrix(outputFileName);
        // System.out.println(matrix.getMetric());
    }

}

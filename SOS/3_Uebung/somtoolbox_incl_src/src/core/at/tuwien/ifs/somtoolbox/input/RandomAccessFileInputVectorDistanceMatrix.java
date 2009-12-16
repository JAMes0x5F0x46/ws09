package at.tuwien.ifs.somtoolbox.input;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;

import com.martiansoftware.jsap.JSAPResult;

/**
 * A distance matrix based on a binary {@link RandomAccessFile}. This implementation does not read the matrix into the memory, and is thus suited
 * especially for big datasets.
 * <p>
 * The file is built as follows:
 * <ul>
 * <li>One integer value, giving the number of vectors</li>
 * <li>A series of double values representing the upper-right half of the symmetric distance matrix, not containing the values in the diagonal itself
 * (as they are all 0).<br>
 * Thus, there are (n-1)! double values, and the matrix file contains the following (x, y) tuples:
 * 
 * <pre>
 * [(2,1) (3,1) (4,1) (5,1) (6,1)]
 * [      (3,2) (4,2) (5,1) (6,2)]
 * [            (4,3) (5,3) (6,3)]
 * [                  (5,4) (6,4)]
 * [                        (6,5)]
 * [                             ]
 * </pre>
 * 
 * </li>
 * <li>The name of the metric used, as String (until the end of the file).</li>
 * </ul>
 * </p>
 * 
 * @author Rudolf Mayer
 * @version $Id: RandomAccessFileInputVectorDistanceMatrix.java 2874 2009-12-11 16:03:27Z frank $
 */
public class RandomAccessFileInputVectorDistanceMatrix extends InputVectorDistanceMatrix {
    public static final int BYTES_HEADER = Integer.SIZE / 8;

    private static final int BYTES_CHAR = Character.SIZE / 8;

    private static final int BYTES_DOUBLE = Double.SIZE / 8;

    private RandomAccessFile file;

    public RandomAccessFileInputVectorDistanceMatrix(String fileName) throws IOException, SOMToolboxException {
        file = new RandomAccessFile(fileName, "rw");
        numVectors = file.readInt();
    }

    @Override
    public DistanceMetric getMetric() {
        if (metric == null) {
            try {
                long offset = (getOffset(numVectors - 1, numVectors - 1, numVectors) + 1) * Double.SIZE / 8 + BYTES_HEADER;
                file.seek(offset);
                String metricName = "";
                for (long i = offset; i < file.length(); i += BYTES_CHAR) {
                    final char readChar = file.readChar();
                    metricName += readChar;
                }
                metricName = metricName.trim();
                metric = AbstractMetric.instantiateNice(metricName);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SOMToolboxException e) {
                e.printStackTrace();
            }
        }
        return metric;
    }

    @Override
    public double getDistance(int x, int y) {
        if (x == y) {
            return 0;
        } else if (x < y) {
            return getDistance(y, x);
        } else {
            try {
                file.seek(getOffset(x, y, numVectors) * BYTES_DOUBLE + BYTES_HEADER);
                return file.readDouble();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    /** Write input distance matrix to a binary file, computing distances on the fly. */
    public static void writeRandomAccessFileInputVectorDistanceMatrix(InputData data, String fileName, DistanceMetric metric) throws IOException,
            MetricException {
        int numVec = data.numVectors();
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        file.writeInt(numVec);
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Storing input distance matrix with metric " + metric + " to BINARY file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        for (int i = 0; i < numVec; i++) {
            for (int j = i + 1; j < numVec; j++) {
                file.writeDouble(metric.distance(data.getInputDatum(i), data.getInputDatum(j)));
            }
            progress.progress();
        }
        file.writeChars(metric.getClass().getCanonicalName().trim());
        file.close();
    }

    /** Write pre-calculated input distance matrix to a binary file. */
    public static void writeRandomAccessFileInputVectorDistanceMatrix(double[][] distances, String fileName, DistanceMetric metric)
            throws IOException, MetricException {
        int numVec = distances[0].length;
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Storing pre-calculated input distance matrix with metric " + metric + " to file " + fileName);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVec, "Calculating distances for vector ", 1);
        file.writeInt(numVec);
        for (int i = 0; i < numVec; i++) {
            for (int j = i + 1; j < distances[j].length; j++) {
                file.writeDouble(distances[i][j]);
            }
            progress.progress();
        }
        file.writeChars(metric.getClass().getCanonicalName().trim());
        file.close();
    }

    /** Method for stand-alone execution, calls {@link #writeRandomAccessFileInputVectorDistanceMatrix(InputData, String, DistanceMetric)}. */
    public static void main(String[] args) throws IOException, SOMToolboxException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_INPUT_DISTANCE_COMPUTER);
        String inputVectorFileName = config.getString("inputVectorFile");
        String outputFileName = config.getString("output");
        String metricName = config.getString("metric");
        String metricParams = config.getString("metricParams");
        DistanceMetric metric = AbstractMetric.instantiateNice(metricName);
        metric.setMetricParams(metricParams);
        InputData data = new SOMLibSparseInputData(inputVectorFileName);
        writeRandomAccessFileInputVectorDistanceMatrix(data, outputFileName, metric);
    }

    /** Find the offset of a specific value in the input file. Always assumes the x value to be larger than the y value! */
    private static long getOffset(long x, long y, long numVectors) {
        // we need to use long, cause otherwise for larger files, we get an overflow and thus negative numbers!
        long factor = (long) ((y + 1) / 2d * y);
        long pos = (y * numVectors) - factor - 1 + (x - y);
        return pos;
    }
}

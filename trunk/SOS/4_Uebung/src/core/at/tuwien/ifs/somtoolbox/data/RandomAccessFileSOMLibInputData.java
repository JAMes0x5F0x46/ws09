package at.tuwien.ifs.somtoolbox.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;

/**
 * Reads SOMLib input from a random access file.
 * 
 * @see RandomAccessFile
 * @author Rudolf Mayer
 * @version $Id: RandomAccessFileSOMLibInputData.java 2874 2009-12-11 16:03:27Z frank $
 */
public class RandomAccessFileSOMLibInputData extends AbstractSOMLibSparseInputData {
    private static final int BYTES_CHAR = Character.SIZE / 8;

    private static final int BYTES_INT = Integer.SIZE / 8;

    private static final int BYTES_DOUBLE = Double.SIZE / 8;

    private RandomAccessFile inputFile;

    int headerOffset = 2 * BYTES_INT; // values for num vectors & dim

    public RandomAccessFileSOMLibInputData(String fileName) throws IOException {
        this(SOMLibSparseInputData.DEFAULT_NORMALISED, new Random(SOMLibSparseInputData.DEFAULT_RANDOM_SEED), null, null, fileName);
    }

    public RandomAccessFileSOMLibInputData(boolean norm, Random rand, TemplateVector tv, SOMLibClassInformation clsInfo, String fileName)
            throws IOException {
        super(norm, rand);

        meanVector = new DenseDoubleMatrix1D(dim);
        this.templateVector = tv;
        this.classInfo = clsInfo;

        inputFile = new RandomAccessFile(fileName, "r");
        numVectors = inputFile.readInt();
        dim = inputFile.readInt();
        System.out.println("num vecs: " + numVectors);
        System.out.println("dim: " + dim);

        double[] mean = new double[dim];
        for (int i = 0; i < numVectors(); i++) {
            for (int j = 0; j < dim(); j++) {
                mean[j] += inputFile.readDouble();
            }
        }
        StdErrProgressWriter progress = new StdErrProgressWriter(mean.length, "Calculating mean vector ", 10);
        for (int i = 0; i < mean.length; i++) {
            mean[i] = mean[i] / numVectors();
            progress.progress();
        }
        this.meanVector = new DenseDoubleMatrix1D(mean);

        dataNames = new String[numVectors];
        final int labelNameOffset = getOffset(numVectors) + BYTES_CHAR;
        inputFile.seek(labelNameOffset);
        String labelName = "";
        int pos = 0;
        for (int i = labelNameOffset; i < inputFile.length(); i += BYTES_CHAR) {
            final char readChar = inputFile.readChar();
            if (readChar != '\n') {
                labelName += readChar;
            } else {
                dataNames[pos] = labelName;
                labelName = "";
                pos++;
            }
        }
        inputFile.seek(labelNameOffset);
    }

    public InputDatum getInputDatum(int d) {
        try {
            int offset = getOffset(d);
            inputFile.seek(offset);
            SparseDoubleMatrix1D vec = new SparseDoubleMatrix1D(dim);
            for (int i = 0; i < dim; i++) {
                double readDouble = inputFile.readDouble();
                vec.setQuick(i, readDouble);
            }
            InputDatum datum = new InputDatum(dataNames[d], vec);
            return datum;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getOffset(int i) {
        return headerOffset + i * dim * BYTES_DOUBLE;
    }

    private int getOffset(int i, int j) {
        return headerOffset + i * dim * BYTES_DOUBLE + j * BYTES_DOUBLE;
    }

    public double mqe0(DistanceMetric metric) {
        if (mqe0 == -1) { // mqe0 for data was not yet calculated
            mqe0 = 0;
            try {
                for (int i = 0; i < numVectors; i++) {
                    mqe0 += metric.distance(meanVector, getInputDatum(i));
                }
            } catch (MetricException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return mqe0;
    }

    public InputData subset(String[] names) {
        // TODO Auto-generated method stub
        return null;
    }

    public double getValue(int x, int y) {
        try {
            inputFile.seek(getOffset(x, y));
            return inputFile.readDouble();
        } catch (IOException e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("");
            System.exit(-1);
            return -1;
        }
    }

    public static void write(InputData data, String outputFile) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(outputFile, "rw");
        writeHeader(randomAccessFile, data.numVectors(), data.dim());
        writeContent(randomAccessFile, data.getData());
        writeVectorLabels(randomAccessFile, data.getLabels());
        randomAccessFile.close();
    }

    public static boolean writeHeader(RandomAccessFile randomAccessFile, int numVectors, int dim) throws IOException {
        randomAccessFile.writeInt(numVectors);
        randomAccessFile.writeInt(dim);
        return true;
    }

    private static void writeContent(RandomAccessFile randomAccessFile, double[][] data) throws IOException {
        for (int documentIndex = 0; documentIndex < data.length; documentIndex++) {
            for (int termIndex = 0; termIndex < data[documentIndex].length; termIndex++) {
                randomAccessFile.writeDouble(data[documentIndex][termIndex]);
            }
        }
    }

    public static void writeVectorLabels(RandomAccessFile randomAccessFile, String[] labels) throws IOException {
        for (int i = 0; i < labels.length; i++) {
            randomAccessFile.writeChars("\n" + labels[i]);
        }
        randomAccessFile.writeChars("\n");
        System.out.println("wrote file, length: " + randomAccessFile.length());
    }

    /**
     * Main method for testing purposes, either writes & reads a random access file, or compares a random access file with an ascii version of the
     * same input data.
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            compare(args[0], args[1]);
        } else {
            test("test.bin");
        }

    }

    /** Compare the ascii & binary version of some SOMLib input data. */
    private static void compare(String ascii, String binary) throws IOException {
        SOMLibSparseInputData memoryData = new SOMLibSparseInputData(ascii);
        RandomAccessFileSOMLibInputData fileData = new RandomAccessFileSOMLibInputData(true, new Random(), null, null, binary);
        System.out.println(fileData.equals(memoryData));
    }

    /** Create & read a random access file SOMLib input data. */
    private static void test(String fileName) throws FileNotFoundException, IOException {
        double[][] bytes = { { 1, 2.4, 5.2 }, { 1.5, 8.2, 9.0 } };
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile f = new RandomAccessFile(fileName, "rw");

        f.writeInt(bytes.length);
        System.out.println("wrote " + bytes.length);
        f.writeInt(bytes[0].length);
        System.out.println("wrote " + bytes[0].length);
        String[] names = new String[bytes.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = "label" + i;
        }

        for (double[] element : bytes) {
            System.out.print("wrote: ");
            for (double element2 : element) {
                f.writeDouble(element2);
                System.out.print(element2 + ", ");
            }
            System.out.println();
        }
        for (String element : names) {
            f.writeChars("\n" + element);
        }
        f.writeChars("\n");
        System.out.println("wrote file, length: " + f.length());
        System.out.println();

        f.close();
        RandomAccessFileSOMLibInputData data = new RandomAccessFileSOMLibInputData(true, new Random(), null, null, fileName);

        System.out.println("num vec: " + data.numVectors());
        System.out.println("dim: " + data.dim());
        for (int i = 0; i < data.numVectors(); i++) {
            final InputDatum inputDatum = data.getInputDatum(i);
            System.out.println(inputDatum + " -> " + inputDatum.equals(new InputDatum("label" + i, new DenseDoubleMatrix1D(bytes[i]))));
        }
    }

    public static String getFormatName() {
        return "randomAccess";
    }
    
    public static String getFileNameSuffix() {
        return ".bin";
    }

}

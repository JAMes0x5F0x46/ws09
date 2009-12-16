package at.tuwien.ifs.somtoolbox.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.util.comparables.InputDistance;
import cern.colt.Sorting;
import cern.colt.function.IntComparator;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * This abstract implementation provides basic support for operating on a {@link InputData}. Sub-classes have to implement constructors and methods to
 * read input vectors and create an <code>InputData</code> object, for example by reading from a file or a database.
 * 
 * @author Rudolf Mayer
 * @version $Id: AbstractSOMLibSparseInputData.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractSOMLibSparseInputData implements InputData {
    protected static final String ERROR_MESSAGE_FILE_FORMAT_CORRUPT = "Input vector file format corrupt. Aborting.";

    /**
     * Any class label information attached to the input vectors.
     */
    protected SOMLibClassInformation classInfo = null;

    /**
     * The label/name of the vector.
     */
    public String[] dataNames = null;

    /**
     * The content type of the vectors ("text", "audio", ...). <br>
     * <p>
     * An input file should use the following header format for content types: <br>
     * <code>$DATA_TYPE text</code> <br>
     * or <br>
     * <code>$DATA_TYPE audio-rp-uservalues</code>
     * </p>
     */
    protected String content_type = "";

    /**
     * The specific subtype of content type (user-definable, for example "rp", "rh", or "ssd" for Rhythm Patterns, Rhythm Historgrams or Statistical
     * Spectrum Descriptor audio feature types).
     */
    protected String content_subtype = "";

    /**
     * Row dimension of the feature matrix before having been vectorized to input vector.
     */
    protected int featureMatrixRows = -1;

    /**
     * Column dimension of the feature matrix before having been vectorized to input vector.
     */
    protected int featureMatrixCols = -1;

    /**
     * The dimension of the input vectors, i.e. the number of attributes
     */
    protected int dim = 0;

    /**
     * Indicates whether the input data has been normalised.
     */
    protected boolean isNormalized = true;

    /**
     * The mean of all the input vectors.
     */
    protected DenseDoubleMatrix1D meanVector = null;

    protected double mqe0 = -1; // value of -1 means the mqe was not yet calculated

    /**
     * The number of vectors in this input data collection.
     */
    protected int numVectors = 0;

    protected Random rand = null;

    /**
     * A {@link TemplateVector} attached to this input data.
     */
    protected TemplateVector templateVector = null;

    /**
     * A transformation of the input vectors. This can be used to perform for example a transformation of the input data for distance calculations
     * once for all vectors to improve performance.
     */
    private double[][] transformedVectors;

    /**
     * A matrix containing the pairwise distances between two vectors.
     */
    private double[][] distanceMatrix;

    /**
     * A mapping from the name to the index of an input vector, for faster access.
     */
    protected LinkedHashMap<String, Integer> nameCache = null;

    private double[][] intervals;

    protected AbstractSOMLibSparseInputData(String[] dataNames, int dim, boolean norm, Random rand, TemplateVector tv, SOMLibClassInformation clsInfo) {
        this(norm, rand);

        this.dataNames = dataNames;
        this.dim = dim;
        this.numVectors = dataNames.length;

        meanVector = new DenseDoubleMatrix1D(dim);

        this.templateVector = tv;
        this.classInfo = clsInfo;
    }

    protected AbstractSOMLibSparseInputData(boolean norm, Random random) {
        this.isNormalized = norm;
        this.rand = random;
    }

    protected AbstractSOMLibSparseInputData() {
    }

    public int dim() {
        return dim;
    }

    @Override
    public String getContentType() {
        return content_type;
    }

    /**
     * Gets the content sub-type.
     * 
     * @return the content sub-type
     */
    public String getContentSubType() {
        return content_subtype;
    }

    @Override
    public int getFeatureMatrixRows() {
        return featureMatrixRows;
    }

    @Override
    public int getFeatureMatrixColumns() {
        return featureMatrixCols;
    }

    @Override
    public DoubleMatrix1D getMeanVector() {
        return meanVector;
    }

    public DoubleMatrix1D getMeanVector(String[] labels) {
        if (labels.length == 0) {
            return null;
        }
        InputDatum[] vectors = getInputDatum(labels);
        meanVector = new DenseDoubleMatrix1D(dim);

        for (int i = 0; i < labels.length; i++) {
            meanVector.assign(vectors[i].getVector(), Functions.plus); // add to mean vector
        }
        meanVector.assign(Functions.div(labels.length)); // calculating mean vector
        return meanVector;
    }

    public boolean isNormalizedToUnitLength() {
        return isNormalized;
    }

    public int numVectors() {
        return numVectors;
    }

    public TemplateVector templateVector() {
        return templateVector;
    }

    @Override
    public SOMLibClassInformation classInformation() {
        return classInfo;
    }

    public void setTemplateVector(TemplateVector templateVector) {
        this.templateVector = templateVector;
    }

    public InputDatum getInputDatum(String label) {
        if (nameCache.get(label) != null) {
            return getInputDatum(nameCache.get(label).intValue());
        } else {
            return null;
        }
    }

    public int getInputDatumIndex(String label) {
        if (nameCache.get(label) != null) {
            return nameCache.get(label).intValue();
        } else {
            return -1;
        }
    }

    public InputDatum getRandomInputDatum(int iteration, int numIterations) {
        // Get a random number
        int randIndex = rand.nextInt(numVectors);
        return this.getInputDatum(randIndex);
    }

    public InputDatum[] getInputDatum(String[] labels) {
        if (labels == null) {
            return null;
        } else {
            InputDatum[] res = new InputDatum[labels.length];
            int[] indices = new int[labels.length];

            for (int i = 0; i < labels.length; i++) {
                indices[i] = nameCache.get(labels[i]).intValue();
            }

            IntComparator comp = new IntComparator() {
                /**
                 * @see cern.colt.function.IntComparator#compare(int, int)
                 */
                public int compare(int o1, int o2) {
                    return o1 < o2 ? -1 : o1 == o2 ? 0 : 1;
                }
            };
            Sorting.quickSort(indices, 0, indices.length - 1, comp);

            for (int i = 0; i < labels.length; i++) {
                res[i] = this.getInputDatum(indices[i]);
            }
            return res;
        }
    }

    /**
     * Calculates the matrix of {@link #transformedVectors} using {@link DistanceMetric#transformVector(double[])} of the given metric.
     * 
     * @param metric the metric to be used to transform the values.
     */
    public void transformValues(DistanceMetric metric) {
        transformedVectors = new double[numVectors()][dim()];
        for (int i = 0; i < numVectors(); i++) {
            transformedVectors[i] = metric.transformVector(getInputDatum(i).getVector().toArray());
        }
    }

    /**
     * Calculates the {@link #distanceMatrix} - careful, this is a lengthy process and should be done only if needed. Requires the matrix of
     * {@link #transformedVectors} being initialised (e.g. via {@link #transformValues(DistanceMetric)}).
     * 
     * @param metric the metric to use for calculating the distances.
     * @throws MetricException if {@link DistanceMetric#distance(double[], double[])} encounters a problem.
     */
    public void initDistanceMatrix(DistanceMetric metric) throws MetricException {
        distanceMatrix = new double[numVectors()][numVectors()];
        if (transformedVectors == null) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Empty transformed matrix, taking vector values");
            transformedVectors = new double[numVectors()][dim()];
            for (int i = 0; i < numVectors(); i++) {
                transformedVectors[i] = getInputDatum(i).getVector().toArray();
            }
        }
        StdErrProgressWriter progress = new StdErrProgressWriter(numVectors(), "pre-calculating distances: ", numVectors() / 10);
        for (int i = 0; i < numVectors(); i++) {
            for (int j = i + 1; j < numVectors(); j++) {
                distanceMatrix[i][j] = metric.distance(transformedVectors[i], transformedVectors[j]);
                distanceMatrix[j][i] = distanceMatrix[i][j];
            }
            progress.progress(i);
        }
    }

    /**
     * Returns the n nearest input vectors for the given vector. Uses a pre-calculated distance metric, if existing, otherwise calculates the
     * distances as needed.
     * 
     * @param inputIndex the index of the vector.
     * @param metric the metric to use for the distance comparison. Only used when the {@link #distanceMatrix} is not pre-calculated.
     * @param number the number of nearest input vectors desired.
     * @return the n nearest input vectors.
     * @throws MetricException if {@link DistanceMetric#distance(DoubleMatrix1D, double[])} encounters a problem.
     */
    public InputDatum[] getNearestN(int inputIndex, DistanceMetric metric, int number) throws MetricException {
        InputDatum input = getInputDatum(inputIndex);
        ArrayList<InputDistance> distances = new ArrayList<InputDistance>();
        if (distanceMatrix != null) {
            for (int i = 0; i < distanceMatrix[inputIndex].length; i++) {
                if (inputIndex != i) {
                    distances.add(new InputDistance(distanceMatrix[inputIndex][i], getInputDatum(i)));
                }
            }
        } else {
            for (int i = 0; i < numVectors(); i++) {
                if (!getInputDatum(i).equals(input)) {
                    distances.add(new InputDistance(metric.distance(input.getVector(), transformedVectors[i]), getInputDatum(i)));
                }
            }
        }
        Collections.sort(distances);
        InputDatum[] result = new InputDatum[number];
        for (int i = 0; i < number; i++) {
            result[i] = distances.get(i).getInput();
        }
        return result;
    }

    public InputDatum[] getNearestNUnsorted(int inputIndex, DistanceMetric metric, int number) throws MetricException {
        InputDatum input = getInputDatum(inputIndex);
        double longestDistance = Double.MAX_VALUE;
        if (distanceMatrix != null) {
            ArrayList<InputDatum> distances = new ArrayList<InputDatum>();
            for (int i = 0; i < 6 && i < distanceMatrix[inputIndex].length; i++) {
                distances.add(getInputDatum(i));
                if (distanceMatrix[inputIndex][i] < longestDistance) {
                    longestDistance = distanceMatrix[inputIndex][i];
                }
            }
            for (int i = 6; i < distanceMatrix[inputIndex].length; i++) {
                if (inputIndex != i) {
                    if (distanceMatrix[inputIndex][i] < longestDistance) {
                        distances.add(getInputDatum(i));
                    }
                }
            }
            return distances.toArray(new InputDatum[distances.size()]);
        } else {
            ArrayList<InputDistance> distances = new ArrayList<InputDistance>();
            for (int i = 0; i < numVectors(); i++) {
                if (!getInputDatum(i).equals(input)) {
                    distances.add(new InputDistance(metric.distance(input.getVector(), transformedVectors[i]), getInputDatum(i)));
                }
            }
            Collections.sort(distances);
            InputDatum[] result = new InputDatum[number];
            for (int i = 0; i < number; i++) {
                result[i] = distances.get(i).getInput();
            }
            return result;
        }
    }

    public double[][] getData() {
        double[][] result = new double[numVectors][dim];
        for (int i = 0; i < numVectors; i++) {
            DoubleMatrix1D v = getInputDatum(i).getVector();
            for (int j = 0; j < v.size(); j++) {
                result[i][j] = v.get(j);
            }
        }
        return result;
    }

    @Override
    public double[][] getData(String className) throws SOMToolboxException {
        if (classInfo != null) {
            String[] dataNames = classInfo.getDataNamesInClass(className);
            double[][] result = new double[dataNames.length][dim];
            for (int i = 0; i < dataNames.length; i++) {
                DoubleMatrix1D v = getInputDatum(dataNames[i]).getVector();
                for (int j = 0; j < v.size(); j++) {
                    result[i][j] = v.get(j);
                }
            }
            return result;
        } else {
            throw new SOMToolboxException("No class information file loaded!");
        }
    }

    public void setClassInfo(SOMLibClassInformation classInfo) {
        this.classInfo = classInfo;
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    public double[][] getDataIntervals() {
        if (intervals == null) {
            intervals = new double[dim()][2];
            for (int i = 0; i < intervals.length; i++) {
                double min = Integer.MAX_VALUE;
                double max = Integer.MIN_VALUE;
                for (int j = 0; j < numVectors(); j++) {
                    double value = getValue(j, i);
                    if (value > max) {
                        max = value;
                    }
                    if (value < min) {
                        min = value;
                    }
                }
                intervals[i][0] = min;
                intervals[i][1] = max;
            }
            // DEBUG info
            // System.out.println("\n\nnmin/max matrix: ");
            // for (int i = 0; i < intervals.length; i++) {
            // System.out.println(VectorTools.printVector(intervals[i]));
            // }
        }
        return intervals;
    }

    /**
     * Returns feature densities statistics of the input data, namely a mapping from the number of input objects a specific feature is not zero in, to
     * the total number of features with that density .
     */
    public Hashtable<Integer, Integer> getFeatureDensities() {
        Hashtable<Integer, Integer> densities = new Hashtable<Integer, Integer>();
        for (int i = 0; i < numVectors; i++) {
            int featureDensitiy = getInputDatum(i).getFeatureDensity();
            Integer count = densities.get(new Integer(featureDensitiy));
            if (count == null) {
                count = new Integer(1);
            } else {
                count = new Integer(count.intValue() + 1);
            }
            densities.put(new Integer(featureDensitiy), count);
        }
        return densities;
    }

    @Override
    public String[] getLabels() {
        Set<String> names = nameCache.keySet();
        return names.toArray(new String[names.size()]);
    }

    @Override
    public String getLabel(int index) {
        return dataNames[index];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractSOMLibSparseInputData) {
            AbstractSOMLibSparseInputData data = (AbstractSOMLibSparseInputData) obj;
            Object[][] assertions = { { "dim", data.dim(), dim() }, { "meanVector", data.meanVector, meanVector },
                    { "data names", data.dataNames, dataNames }, { "meanVec", data.meanVector, meanVector } };
            for (int i = 0; i < assertions.length; i++) {
                if (!assertEqual(assertions[i][0], assertions[i][1], assertions[i][2])) {
                    return false;
                }
            }
            for (int i = 0; i < numVectors(); i++) {
                if (!assertEqual("input element " + i, data.getInputDatum(i), getInputDatum(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean assertEqual(Object name, Object i1, Object i2) {
        boolean equals = false;
        if (i1 instanceof Object[] && i2 instanceof Object[]) {
            equals = Arrays.equals((Object[]) i1, (Object[]) i2);
        } else {
            equals = i1.equals(i2);
        }
        if (!equals) {
            System.out.println(name + " not equal: " + i1 + "<->" + i2);
            return false;
        } else {
            return true;
        }
    }

    public static AbstractSOMLibSparseInputData create(String vectorFileName, String templateFileName, boolean sparse, boolean norm,
            int numCacheBlocks, long seed) {
        if (vectorFileName.endsWith(".bin")) {
            try {
                return new RandomAccessFileSOMLibInputData(norm, new Random(seed), null, null, vectorFileName);
            } catch (IOException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error reading binary file: " + e.getMessage());
                System.exit(-1);
                return null;
            }
        } else if (vectorFileName.endsWith(".arff")) {
            try {
                @SuppressWarnings("unchecked")
                Class<AbstractSOMLibSparseInputData> c = (Class<AbstractSOMLibSparseInputData>) Class.forName("at.tuwien.ifs.somtoolbox.data.ARFFFormatInputData");

                Constructor<AbstractSOMLibSparseInputData> constr = c.getConstructor(String.class, boolean.class, boolean.class, int.class,
                        long.class);

                // return new ARFFFormatInputData(vectorFileName, sparse, norm, numCacheBlocks, seed);
                return constr.newInstance(new Object[] { vectorFileName, sparse, norm, numCacheBlocks, seed });
            } catch (Exception e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("ARFFFormatInputData not available. To enable add \"weka.jar\" and the connector to your classpath");
                throw new Error("Can't read an arff-file");
            }
        } else {
            return new SOMLibSparseInputData(vectorFileName, templateFileName, sparse, norm, numCacheBlocks, seed);
        }
    }

    public static AbstractSOMLibSparseInputData create(String inputVectorFileName)  {
        return create(inputVectorFileName, null, true, true, 1, 7);
    }

    public static AbstractSOMLibSparseInputData create(InputDatum[] inputData, SOMLibClassInformation classInfo) {
        return new SOMLibSparseInputData(inputData, classInfo);
    }

    @Override
    public void writeToFile(String fileName) throws IOException {
        PrintWriter writer = FileUtils.openFileForWriting("Input vector file", fileName, true);
        writeHeaderToFile(writer, numVectors(), dim());
        for (int i = 0; i < numVectors(); i++) {
            InputDatum inputDatum = getInputDatum(i);
            writeInputDatumToFile(writer, inputDatum);
        }

        writer.flush();
        writer.close();
    }

    public static void writeInputDatumToFile(PrintWriter writer, InputDatum inputDatum) throws IOException {
        writeInputDatumToFile(writer, inputDatum.getLabel(), inputDatum.getVector());
    }

    public static void writeInputDatumToFile(PrintWriter writer, String label, DoubleMatrix1D vector) throws IOException {
        for (int i = 0; i < vector.size(); i++) {
            if (!Double.isNaN(vector.get(i))) {
                writer.write(vector.get(i) + " ");
            } else {
                writer.write("? ");
            }
        }
        writer.println(label);
    }

    public static void writeHeaderToFile(PrintWriter writer, int numVectors, int dim) throws IOException {
        writer.println("$TYPE vec");
        writer.println("$XDIM " + numVectors);
        writer.println("$YDIM 1");
        writer.println("$VEC_DIM " + dim);
    }

    @Override
    public void writeAsWekaARFF(String fileName, boolean writeInstanceNames, boolean skipInputsWithoutClass) throws IOException, SOMToolboxException {
        if (classInfo == null) {
            throw new SOMToolboxException("Class Information File needed for WEKA ARFF writing");
        }
        String extension = ".arff";
        if (!fileName.endsWith(extension)) {
            fileName = fileName + extension;
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing input data as ARFF file to '" + fileName + "'.");
        PrintWriter writer = FileUtils.openFileForWriting("Weka ARFF", fileName, false);
        String relation = fileName.substring(0, fileName.length() - 4);
        StdErrProgressWriter progress = new StdErrProgressWriter(numVectors(), "Writing vector ", numVectors() / 10);
        writer.println("@RELATION " + relation + "\n");
        for (int i = 0; i < templateVector.dim(); i++) {
            writer.println("@ATTRIBUTE " + templateVector.getLabel(i) + " NUMERIC");
        }
        if (writeInstanceNames) {
            writer.println("@ATTRIBUTE instanceName STRING");
        }
        String classNamesString = "";
        for (String string : classInfo.classNames()) {
            if (classNamesString.length() > 0) {
                classNamesString += ",";
            }
            classNamesString += "'" + string + "'";
        }
        writer.println("@ATTRIBUTE class {" + classNamesString + "}");
        writer.println("@DATA");
        int skipCounter = 0;
        for (int i = 0; i < numVectors(); i++) {
            InputDatum inputDatum = getInputDatum(i);
            if (skipInputsWithoutClass && !classInfo.hasClassAssignmentForName(inputDatum.getLabel())) {
                skipCounter++;
                Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
                        "Skipping datum '" + inputDatum.getLabel() + "', as it has no class assigned; skipped " + skipCounter + " so far.");
                continue;
            }
            DoubleMatrix1D vector = inputDatum.getVector();
            for (int j = 0; j < dim(); j++) {
                writer.print(vector.get(j) + ",");
            }
            if (writeInstanceNames) {
                writer.println("'" + escapeForWeka(inputDatum.getLabel()) + "','");
            }
            writer.println(classInfo.getClassName(inputDatum.getLabel()) + "'");
            progress.progress();
        }
        writer.flush();
        writer.close();
    }

    private String escapeForWeka(String label) {
        return label.replaceAll("'", "_").replaceAll(" ", "_");
    }

    public static String getFormatName() {
        return "SOMLib";
    }
    
    public static String getFileNameSuffix() {
        return ".vec";
    }

}

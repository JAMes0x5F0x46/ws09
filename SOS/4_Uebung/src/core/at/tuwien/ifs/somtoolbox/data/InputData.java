package at.tuwien.ifs.somtoolbox.data;

import java.io.IOException;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * The <code>InputData</code> provides the input vectors to be used for the training process of a Self-Organizing Map. The data structure to read
 * construct an InputData from is normally generated by a parser or vector generator program.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: InputData.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface InputData {
    public static final double MISSING_VALUE = Double.NaN;

    String inputFileNameSuffix = ".vec";

    /**
     * Indicates whether this data set has been normalised to the unit length.
     * 
     * @return true if this data set is normalised, false otherwise.
     */
    public boolean isNormalizedToUnitLength();

    /**
     * Gets the dimension of the input data.
     * 
     * @return the dimension.
     */
    public int dim();

    /**
     * Gives the size of this input data set.
     * 
     * @return the number of vectors.
     */
    public int numVectors();

    /**
     * Gets a random input sample from the input data set.
     * 
     * @param iteration
     * @param numIterations
     * @return the random input data.
     */
    public InputDatum getRandomInputDatum(int iteration, int numIterations);

    /**
     * Get an input datum with a specified index.
     * 
     * @param d the index of the input datum.
     * @return the input datum.
     */
    public InputDatum getInputDatum(int d);

    /**
     * Get an input datum with a specified label.
     * 
     * @param label the name of the input datum.
     * @return the input datum.
     */
    public InputDatum getInputDatum(String label);

    /**
     * Returns an array of input data with the specified labels.
     * 
     * @param labels the labels of the input data.
     * @return the input data.
     */
    public InputDatum[] getInputDatum(String[] labels);

    /** Returns an array containing the labels of all the input data. */
    public String[] getLabels();

    /** Return the label of the input vector at the given index. */
    public String getLabel(int index);

    /**
     * Gets the mean vector of the input vectors.
     * 
     * @return the mean vector.
     */
    public DoubleMatrix1D getMeanVector();

    /**
     * Returns mean vector of specified vectors provided by String[] array.
     * 
     * @param labels label names of the input data.
     * @return the mean vector.
     */
    public DoubleMatrix1D getMeanVector(String[] labels);

    /**
     * Gets the template vector associated with this input data.
     * 
     * @return the template vector, or null if the template vector was not specified.
     */
    public TemplateVector templateVector();

    /**
     * Gets the class info associated with this input data.
     * 
     * @return the class info, or null if the class info file was not specified.
     */
    public SOMLibClassInformation classInformation();

    /**
     * Sets the template vector to be associated with this input data.
     * 
     * @param templateVector the new template vector.
     */
    public void setTemplateVector(TemplateVector templateVector);

    /**
     * Calculates the mean quantisation error of the top-level unit.
     * 
     * @param metric the metric to use for distance calculation.
     * @return the mqe0.
     */
    public double mqe0(DistanceMetric metric);

    /**
     * Gets a subset of this input data set. The input data in the subset are identified by the specified labels.
     * 
     * @param names the label names of the desired subset data.
     * @return a subset of the data.
     */
    public InputData subset(String[] names);

    /** Return the input data as a double array, i.e. a matrix of numVectors x dim */
    public double[][] getData();

    /** Return the min and max values for each feature, in a matrix of dim x 2 */
    public double[][] getDataIntervals();

    /** Returns the value of the y-th feature of input vector x. */
    public double getValue(int x, int y);

    /** Writes the input vectors to a file. */
    public void writeToFile(String fileName) throws IOException;

    /** Writes the data to <a href="http://www.cs.waikato.ac.nz/~ml/weka/arff.html">Weka ARFF format</a>. */
    public void writeAsWekaARFF(String fileName, boolean writeInstanceNames, boolean skipInputsWithoutClass) throws IOException, SOMToolboxException;

    /**
     * Gets the number of rows before vectorisation.
     * 
     * @return the number of rows of feature matrix before having been vectorized to input vector, or -1 if not available.
     */
    public int getFeatureMatrixRows();

    /**
     * Gets the number of columns before vectorisation.
     * 
     * @return the number of columns of feature matrix before having been vectorized to input vector, or -1 if not available.
     */
    public int getFeatureMatrixColumns();

    /**
     * Gets the content type.
     * 
     * @return the content type
     */
    public String getContentType();

    public void setClassInfo(SOMLibClassInformation classInfo);

    /**
     * Returns the vectors of all inputs associated with the given class name
     * 
     * @throws SOMToolboxException If no class information file is loaded
     */
    public double[][] getData(String className) throws SOMToolboxException;
}
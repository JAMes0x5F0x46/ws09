package at.tuwien.ifs.somtoolbox.data;

import java.io.File;

import org.math.io.files.ASCIIFile;
import org.math.io.parser.ArrayString;

import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Reads data from a simple matrix file. Rows are separated by newlines, and columns by spaces or tabs.
 * 
 * @author Rzdolf Mayer
 * @version $Id: SimpleMatrixInputData.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SimpleMatrixInputData extends AbstractSOMLibSparseInputData {
    private double[][] matrix;

    public SimpleMatrixInputData(String fileName) {
        ArrayString.defaultColumnDelimiter = StringUtils.REGEX_SPACE_OR_TAB;
        matrix = ASCIIFile.readDoubleArray(new File(fileName));
        numVectors = matrix.length;
        dim = matrix[0].length;
        dataNames = new String[numVectors];
        for (int i = 0; i < matrix.length; i++) {
            dataNames[i] = String.valueOf(i);
        }
        templateVector = new SOMLibTemplateVector(numVectors(), dim());
    }

    @Override
    public InputDatum getInputDatum(int d) {
        return new InputDatum(dataNames[d], matrix[d]);
    }

    @Override
    public double getValue(int x, int y) {
        return matrix[x][y];
    }

    @Override
    public double mqe0(DistanceMetric metric) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public InputData subset(String[] names) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static String getFormatName() {
        return "simpleMatrix";
    }
    
    public static String getFileNameSuffix() {
        return ".matrix";
    }
}

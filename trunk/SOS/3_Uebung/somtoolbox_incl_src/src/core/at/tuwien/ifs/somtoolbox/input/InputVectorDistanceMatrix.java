package at.tuwien.ifs.somtoolbox.input;

import java.io.IOException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Base for classes providing a distance matrix of the input vectors. Provides generic methods and fields
 * 
 * @author Rudolf Mayer
 * @version $Id: InputVectorDistanceMatrix.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class InputVectorDistanceMatrix {

    protected int numVectors;

    protected DistanceMetric metric;

    public DistanceMetric getMetric() {
        return metric;
    }

    public InputVectorDistanceMatrix() {
        super();
    }

    /** Return the distance between input vectors x and y. */
    public abstract double getDistance(int x, int y);

    /**
     * Return the n nearest vectors of input x. Basic implementation of the method, sub-classes might provide an optimised implementation.
     */
    public int[] getNNearest(int x, int num) {
        double[] distancesToInput = getDistances(x);
        int[] indices = new int[num];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = -1;
        }
        double[] distances = new double[num];

        for (int i = 0; i < numVectors; i++) {
            if (x == i) { // skip similarity to the vector itself..
                continue;
            }
            int element = 0;
            double distance = distancesToInput[i];
            boolean inserted = false;
            while ((inserted == false) && (element < num)) {
                if (indices[element] == -1 || distance < distances[element]) { // found place to insert unit
                    for (int m = (num - 2); m >= element; m--) { // move units with greater distance to right
                        indices[m + 1] = indices[m];
                        distances[m + 1] = distances[m];
                    }
                    indices[element] = i;
                    distances[element] = distance;
                    inserted = true;
                }
                element++;
            }
        }
        return indices;
    }

    /**
     * Return the distances to all vectors from input x. This is a basic using {@link #getDistance(int, int)}, sub-classes might provide an optimised
     * implementation.
     */
    public double[] getDistances(int x) {
        double[] d = new double[numVectors];
        for (int y = 0; y < d.length; y++) {
            d[y] = getDistance(x, y);
        }
        return d;
    }

    public int numVectors() {
        return numVectors;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Distance matrix ").append(numVectors()).append("x").append(numVectors()).append("\n");
        if (numVectors() < 20) {
            for (int i = 0; i < numVectors(); i++) {
                sb.append(StringUtils.toString(getDistances(i), 3)).append("\n");
            }
        }
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InputVectorDistanceMatrix)) {
            return false;
        } else {
            final InputVectorDistanceMatrix other = (InputVectorDistanceMatrix) obj;
            for (int i = 0; i < numVectors(); i++) {
                for (int j = 0; j < numVectors(); j++) {
                    if (getDistance(i, j) != other.getDistance(i, j)) {
                        System.out.println("not equal in " + i + "," + j + ": " + getDistance(i, j) + " <> " + other.getDistance(i, j));
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Factory method that reads and creates an {@link InputVectorDistanceMatrix} from the given file. Depending on the filename, returns either a
     * {@link RandomAccessFileInputVectorDistanceMatrix} (if the filename ends with '.bin') or a {@link MemoryInputVectorDistanceMatrix} (all other
     * cases).<br>
     * TODO: maybe more intelligent checking for file type, possibly trying to read it as binary, and checking the first bytes for a file type or so.
     */
    public static InputVectorDistanceMatrix initFromFile(String fileName) throws IOException, SOMToolboxException {
        if (fileName.endsWith(".bin") || !FileUtils.fileStartsWith(fileName, "$")) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Opening binary random access distance matrix file");
            return new RandomAccessFileInputVectorDistanceMatrix(fileName);
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Reading ASCII distance matrix into memory.");
            return new MemoryInputVectorDistanceMatrix(fileName);
        }
    }

}
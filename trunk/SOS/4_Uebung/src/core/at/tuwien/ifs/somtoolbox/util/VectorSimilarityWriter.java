package at.tuwien.ifs.somtoolbox.util;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.InputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.input.MemoryInputVectorDistanceMatrix;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Writes the nearest/most similar vectors for a given data set.
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorSimilarityWriter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VectorSimilarityWriter {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException,
            SOMToolboxException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_INPUT_SIMILARITY_COMPUTER);
        String inputVectorDistanceMatrix = config.getString("inputVectorDistanceMatrix");
        String inputVectorFileName = config.getString("inputVectorFile");
        int numNeighbours = config.getInt("numberNeighbours");
        String outputFormat = config.getString("outputFormat");

        InputVectorDistanceMatrix matrix = null;
        InputData data = new SOMLibSparseInputData(inputVectorFileName);

        if (StringUtils.isNotBlank(inputVectorDistanceMatrix)) {
            matrix = InputVectorDistanceMatrix.initFromFile(inputVectorDistanceMatrix);
        } else {
            String metricName = config.getString("metric");
            DistanceMetric metric = AbstractMetric.instantiate(metricName);
            matrix = new MemoryInputVectorDistanceMatrix(data, metric);
        }

        String outputFileName = config.getString("output");
        PrintWriter w = FileUtils.openFileForWriting("Similarity File", outputFileName);

        if (outputFormat.equals("SAT-DB")) {
            // find feature type
            String type = "";
            if (inputVectorFileName.endsWith(".rh") || inputVectorFileName.endsWith(".rp") || inputVectorFileName.endsWith(".ssd")) {
                type = "_" + inputVectorFileName.substring(inputVectorFileName.lastIndexOf(".") + 1);
            }
            w.println("INSERT INTO `sat_track_similarity_ifs" + type + "` (`TRACKID`, `SIMILARITYCOUNT`, `SIMILARITYIDS`) VALUES ");
        }

        int numVectors = matrix.numVectors();
        // numVectors = 10; // for testing
        StdErrProgressWriter progress = new StdErrProgressWriter(numVectors, "Writing similarities for vector ", 1);
        for (int i = 0; i < numVectors; i++) {
            int[] nearest = matrix.getNNearest(i, numNeighbours);
            if (outputFormat.equals("SAT-DB")) {
                w.print("  (" + i + " , NULL, '");
                for (int j = 0; j < nearest.length; j++) {
                    String label = data.getLabel(nearest[j]);
                    w.print(label.replace(".mp3", "")); // strip ending
                    if (j + 1 < nearest.length) {
                        w.print(",");
                    } else {
                        w.print("')");
                    }
                }
                if (i + 1 < numVectors) {
                    w.print(",");
                }
            } else {
                w.print(data.getLabel(i) + ",");
                for (int j = 0; j < nearest.length; j++) {
                    w.print(data.getLabel(nearest[j]));
                    if (j + 1 < nearest.length) {
                        w.print(",");
                    }
                }
            }
            w.println();
            w.flush();
            progress.progress();
        }
        if (outputFormat.equals("SAT-DB")) {
            w.print(";");
        }
        w.flush();
        w.close();
    }
}

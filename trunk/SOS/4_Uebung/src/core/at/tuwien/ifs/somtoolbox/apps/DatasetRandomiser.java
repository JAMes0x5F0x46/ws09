package at.tuwien.ifs.somtoolbox.apps;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.util.ArrayUtils;
import at.tuwien.ifs.somtoolbox.util.FileUtils;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

/**
 * Randomises a given data set (consisting of an {@link InputData} vector, and optionally a {@link TemplateVector}. The data set is randomised by
 * randomly swapping the order of columns (attributes), and/or rows (vectors).
 */
public class DatasetRandomiser implements SOMToolboxApp {
    /** @see SOMToolboxApp */
    public static final String DESCRIPTION = "Randomises data sets";

    /** @see SOMToolboxApp */
    public static final String LONG_DESCRIPTION = "Randomises data sets by swapping the order of columns (features/attributes) and/or rows (vectors)";

    /** @see SOMToolboxApp */
    public static final Parameter[] OPTIONS = { OptionFactory.getOptInputVectorFile(true), OptionFactory.getOptTemplateVectorFile(false),
            OptionFactory.getOptNumberVariants(false, 1), OptionFactory.getOptInterleave(false, 1), OptionFactory.getOptStartIndex(false, 1),
            OptionFactory.getSwitchPreserveFeatureOrder(), OptionFactory.getSwitchPreserveVectorOrder(), OptionFactory.getOptGZip(false, true),
            OptionFactory.getOptOutputFileName(true) };

    public static void main(String[] args) throws IOException {
        JSAPResult options = AbstractOptionFactory.parseResults(args, OPTIONS);
        String inputVectorFile = options.getString("inputVectorFile");
        String templateVectorFile = options.getString("templateVectorFile");
        String ouputFile = options.getString("output");
        boolean preserveFeatureOrder = options.getBoolean("preserveFeatureOrder", false);
        boolean preserveVectorOrder = options.getBoolean("preserveVectorOrder", false);
        boolean gzip = options.getBoolean("gzip", false);
        int numberVariations = options.getInt("variants");
        int interleave = options.getInt("interleave");
        int startIndex = options.getInt("startIndex");

        InputData inputData = new SOMLibSparseInputData(inputVectorFile, templateVectorFile);

        // randomly swap columns
        int dim = inputData.dim();
        int[] columnOrder = ArrayUtils.getLinearArray(dim);
        int[] rowOrder = ArrayUtils.getLinearArray(inputData.numVectors());

        for (int i = 0; i < numberVariations; i++) {
            if (!preserveFeatureOrder) {
                randomise(columnOrder);
            }
            if (!preserveVectorOrder) {
                randomise(rowOrder);
            }
            String fileName = numberVariations == 1 ? ouputFile : ouputFile + "_" + (i * interleave + startIndex);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Writing dataset '" + fileName + "'.");
            writeToFile(fileName, inputData, columnOrder, rowOrder, gzip);
        }
    }

    private static void randomise(int[] array) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").fine("Original order: " + Arrays.toString(array));
        Random random = new Random(7);
        for (int i = 0; i < array.length * 2 / 3; i++) {
            int source = random.nextInt(array.length);
            int target = random.nextInt(array.length);
            if (target == source) {
                target = random.nextInt(array.length);
            }
            Logger.getLogger("at.tuwien.ifs.somtoolbox").finer("Swapping " + (i + 1) + "/" + array.length + ": " + source + " <==> " + target);
            int temp = array[source];
            array[source] = array[target];
            array[target] = temp;
            Logger.getLogger("at.tuwien.ifs.somtoolbox").finer("Intermediate randomised order: " + Arrays.toString(array));
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Randomised order: " + ArrayUtils.toString(array, 100));
    }

    private static void writeToFile(String fileName, InputData inputData, int[] columnOrder, int[] rowOrder, boolean gzip) throws IOException {
        // write the randomised input file
        PrintWriter writer = FileUtils.openFileForWriting("Input vector file", fileName + ".vec", gzip);
        AbstractSOMLibSparseInputData.writeHeaderToFile(writer, inputData.numVectors(), inputData.dim());
        for (int i = 0; i < inputData.numVectors(); i++) {
            InputDatum inputDatum = inputData.getInputDatum(rowOrder[i]);
            writeInputDatumToFile(writer, inputDatum, columnOrder);
        }
        writer.flush();
        writer.close();

        // write the randomised template vector file
        writer = FileUtils.openFileForWriting("Template vector file", fileName + ".tv", false);
        TemplateVector tv = inputData.templateVector();
        AbstractSOMLibTemplateVector.writeHeaderToFile(writer, fileName, tv.numVectors(), tv.dim(), tv.numinfo());

        for (int i = 0; i < columnOrder.length; i++) {
            AbstractSOMLibTemplateVector.writeElementToFile(writer, i, tv.getElement(columnOrder[i]));
        }
        writer.flush();
        writer.close();

        // write the randomised template vector file, with generic names for the features
        new SOMLibTemplateVector(inputData.numVectors(), inputData.dim()).writeToFile(fileName + ".generic.tv");
    }

    private static void writeInputDatumToFile(PrintWriter writer, InputDatum inputDatum, int[] columnOrder) {
        for (int i = 0; i < columnOrder.length; i++) {
            final double v = inputDatum.getVector().get(columnOrder[i]);
            if (!Double.isNaN(v)) {
                writer.write(v + " ");
            } else {
                writer.write("? ");
            }
        }
        writer.println(inputDatum.getLabel());
    }

}

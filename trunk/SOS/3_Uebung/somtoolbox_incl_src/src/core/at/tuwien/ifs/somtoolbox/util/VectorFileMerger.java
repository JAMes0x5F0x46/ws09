package at.tuwien.ifs.somtoolbox.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Merges two vector files.
 * 
 * @author frank
 * @version $Id: VectorFileMerger.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VectorFileMerger {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO: Add option inner-, (left|right|both)outer-join, default is inner
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_VECTOR_FILE_MERGER);

        String inFile1 = config.getString("input1");
        String inFile2 = config.getString("input2");
        String outFile = config.getString("output");
        String w = config.getString("weights");

        boolean normalise = true;
        if (w.equals("NONE")) {
            normalise = false;
        }

        Logger log = Logger.getLogger(VectorFileMerger.class.getName());

        double w1 = 50, w2 = 50;
        if (normalise) {
            String[] weights = w.split(":");
            if (weights.length != 2) {
                log.severe(String.format("Invalid weight format: \"%s\". Should be sth. like \"50:50\"", w));
                System.exit(1);
            }
            try {
                w1 = Double.parseDouble(weights[0]);
                w2 = Double.parseDouble(weights[1]);
            } catch (NumberFormatException n) {
                log.severe(String.format("Invalid weight format: \"%s\". Should be sth. like \"50:50\"", w));
                System.exit(1);
            }
        }

        AbstractSOMLibSparseInputData data1 = AbstractSOMLibSparseInputData.create(inFile1);
        AbstractSOMLibSparseInputData data2 = AbstractSOMLibSparseInputData.create(inFile2);

        List<InputDatum> idList = new LinkedList<InputDatum>();
        log.info("Starting merge...");
        String[] labels = data1.getLabels();
        for (int i = 0; i < labels.length; i++) {
            String l = labels[i];
            InputDatum d1, d2;

            d1 = data1.getInputDatum(l);
            d2 = data2.getInputDatum(l);

            if (d1 == null || d2 == null) {
                continue;
            }

            if (normalise) {
                d1 = VectorTools.normaliseByLength(d1, w1);
                d2 = VectorTools.normaliseByLength(d2, w2);
            }

            DoubleMatrix1D v1 = d1.getVector();
            DoubleMatrix1D v2 = d2.getVector();

            DoubleMatrix1D res = new DenseDoubleMatrix1D(v1.size() + v2.size());
            for (int j = 0; j < v1.size(); j++) {
                res.setQuick(j, v1.get(j));
            }
            for (int j = 0; j < v2.size(); j++) {
                res.setQuick(v1.size() + j, v2.get(j));
            }
            InputDatum id = new InputDatum(l, res);
            idList.add(id);
        }
        log.info("Merge finished. Writing result.");
        SOMLibSparseInputData id = new SOMLibSparseInputData(idList.toArray(new InputDatum[] {}), null);
        try {
            id.writeToFile(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] tvAttr = new String[id.dim()];
        for (int j = 0; j < data1.dim(); j++) {
            tvAttr[j] = data1.getContentSubType() + "_" + j;
        }
        for (int j = 0; j < data2.dim(); j++) {
            tvAttr[data1.dim() + j] = data2.getContentSubType() + "_" + j;
        }
        log.info("Vector written.");
        if (config.getBoolean("writeTV")) {
            log.info("Generating TemplateVector");
            try {
                SOMLibTemplateVector tv = new SOMLibTemplateVector(id.numVectors(), tvAttr);
                tv.writeToFile(outFile + ".tv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("Done");
    }
}

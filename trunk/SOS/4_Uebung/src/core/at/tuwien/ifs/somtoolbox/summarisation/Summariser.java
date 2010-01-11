package at.tuwien.ifs.somtoolbox.summarisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.AbstractSOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.summarisation.output.ResultHandler;
import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;
import cern.colt.list.DoubleArrayList;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: Summariser.java 2890 2009-12-12 00:34:37Z mayer $
 */
public class Summariser {
    public static final Parameter[] OPTIONS = new Parameter[] { OptionFactory.getOptInputDirectory(true), OptionFactory.getOptDocument(),
            OptionFactory.getOptOutputDirectory(true), OptionFactory.getOptCompressionRate(), OptionFactory.getOptMethod(),
            OptionFactory.getOptTemplateVectorFile(true), OptionFactory.getOptInputVectorFile(true) };

    public static void main(String[] args) {
        buildResult(args);
    }

    private static void buildResult(String[] args) {
        JSAPResult config = OptionFactory.parseResults(args, OPTIONS);

        String document = config.getString("document");
        String input = config.getString("inputDir");
        String outputDirectory = config.getString("outputDirectory");
        int compression = config.getInt("compression");
        String method = config.getString("method");
        String inputvector = config.getString("input");
        String templatevector = config.getString("template");

        File output = new File(outputDirectory);
        if (!output.exists()) {
            output.mkdir();
            System.out.println("result folder created");
        }

        try {
            AbstractSOMLibSparseInputData inputvec = AbstractSOMLibSparseInputData.create(inputvector);
            SOMLibTemplateVector templatevec = new SOMLibTemplateVector(templatevector);
            Scorer scorer = new Scorer(document, inputvec, templatevec);
            scorer.setFileNamePrefix(input);
            scorer.parseDocuments();

            Object[] item = new Object[1];
            item[0] = document;
            ResultHandler resulth = new ResultHandler(item, scorer.getParsedDocuments());
            DoubleArrayList scoreArray = scorer.getScores(0, method);
            resulth.storeScore(0, scoreArray);
            resulth.createResult(0, compression);
            ArrayList<String> resultdoc = resulth.getResultDoc(0);

            File resultfile = new File(outputDirectory + File.separator + document);
            if (!resultfile.exists()) {
                resultfile.createNewFile();
            }
            BufferedWriter bufwriter = new BufferedWriter(new FileWriter(resultfile.getAbsolutePath()));
            bufwriter.write(scorer.getParsedDocument(0).get(0) + "\n");
            for (int i = 0; i < resultdoc.size(); i++) {
                bufwriter.write(resultdoc.get(i));
            }
            bufwriter.close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage() + " ");
            e.printStackTrace();
        }

    }

}

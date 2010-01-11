package at.tuwien.ifs.somtoolbox.output.labeling;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Implements the <code>Keyword selection</code> labelling method, as described in <i><b>Lagus, K. and Kaski, S.</b>:Keyword selection method for
 * characterizing text document maps. Proceedings of ICANN99, 9th International Conference on Artificial Neural Networks, volume 1, pages 371-376,
 * IEEE, London. </i><br/>
 * This implementation is based on Lucene.<br/>
 * FIXME: still incomplete, based on old/deprecated Lucene API
 * 
 * @author Rudolf Mayer
 * @version $Id: LagusKeywordLabeler.java 2915 2009-12-14 15:18:31Z mayer $
 */
public class LagusKeywordLabeler extends AbstractLabeler {
    String path;

    public static void main(String[] args) {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_LAGUS_KEYWORD_LABELER);

        int numLabels = config.getInt("numberLabels", 5);
        String inputVectorFilename = config.getString("inputVectorFile");
        boolean denseData = config.getBoolean("denseData", false);
        String templateVectorFilename = config.getString("templateVectorFile", null);
        String unitDescriptionFilename = config.getString("unitDescriptionFile", null);
        String weightVectorFilename = config.getString("weightVectorFile");
        String mapDescriptionFilename = config.getString("mapDescriptionFile", null);

        GrowingSOM gsom = null;
        try {
            gsom = new GrowingSOM(new SOMLibFormatInputReader(weightVectorFilename, unitDescriptionFilename, mapDescriptionFilename));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            e.printStackTrace();
            System.exit(-1);
        }

        InputData data = SOMLibSparseInputData.create(inputVectorFilename, templateVectorFilename, !denseData, true, 1, 7);
        Labeler labeler = new LagusKeywordLabeler(config.getString("inputDir"));
        labeler.label(gsom, data, numLabels);
    }

    public LagusKeywordLabeler(String path) {
        super();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        this.path = path;
    }

    public void label(GHSOM ghsom, InputData data, int num) {
        label(ghsom.topLayerMap(), data, num);
    }

    public void label(GrowingSOM gsom, InputData data, int num) {
        label(gsom, data, num, false);
    }

    public void label(GrowingSOM gsom, InputData data, int num, boolean ignoreLabelsWithZero) {
        Analyzer analyzer = new StandardAnalyzer();
        Unit[] units = gsom.getLayer().getAllUnits();

        try {
            IndexWriter fullIndex = new IndexWriter("index", analyzer, true, MaxFieldLength.UNLIMITED);
            IndexWriter[] unitIndices = new IndexWriter[units.length];

            for (int i = 0; i < units.length; i++) { // do labeling for each unit
                if (units[i].getNumberOfMappedInputs() != 0) {
                    InputDatum[] unitData = data.getInputDatum(units[i].getMappedInputNames());
                    String[] vectorNames = new String[unitData.length];
                    for (int j = 0; j < unitData.length; j++) {
                        vectorNames[j] = unitData[j].getLabel();
                    }
                    // Store the index in memory:
                    Directory directory = new RAMDirectory();
                    try {
                        unitIndices[i] = new IndexWriter(directory, analyzer, true, MaxFieldLength.UNLIMITED);
                        unitIndices[i].setMaxFieldLength(25000);
                        // FileIndexer indexer = new FileIndexer(unitIndices[i],
                        // (String[]) new ArrayList(FileIndexer.KNOWN_FILE_TYPES.keySet()).toArray(new String[FileIndexer.KNOWN_FILE_TYPES.size()]));
                        // indexer.indexDocs(path, vectorNames);
                        fullIndex.addIndexesNoOptimize(new Directory[] { unitIndices[i].getDirectory() });
                        IndexSearcher isearcher = new IndexSearcher(directory);

                        TermEnum terms = isearcher.getIndexReader().terms();
                        do {
                            System.out.println("Term: " + terms.term());
                        } while (terms.next());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}

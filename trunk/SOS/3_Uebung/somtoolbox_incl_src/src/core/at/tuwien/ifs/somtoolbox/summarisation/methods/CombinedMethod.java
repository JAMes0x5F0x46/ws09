package at.tuwien.ifs.somtoolbox.summarisation.methods;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;
import cern.colt.list.DoubleArrayList;

/**
 * @author Julius Penaranda
 * @version $Id: CombinedMethod.java 2937 2009-12-15 01:03:56Z mayer $
 */
public class CombinedMethod extends TFxIDF {
    LocationMethod lm = null;

    TitleMethod tm = null;

    KeywordMethod km = null;

    DoubleArrayList locarray = null;

    DoubleArrayList titlearray = null;

    DoubleArrayList tfxidfarray = null;

    DoubleArrayList kmarray = null;

    DoubleArrayList finalarray = null;

    public CombinedMethod(PartOfSpeech p) {
        super(p);
        lm = new LocationMethod(p);
        tm = new TitleMethod(p);
        km = new KeywordMethod(p);

    }

    @Override
    public void setVectors(InputData input, SOMLibTemplateVector template) {
        super.setVectors(input, template);
        lm.setVectors(input, template);
        tm.setVectors(input, template);
        km.setVectors(input, template);
    }

    @Override
    public void setDocument(String filename, ArrayList<String> doc) {
        super.setDocument(filename, doc);
        lm.setDocument(filename, doc);
        tm.setDocument(filename, doc);
        km.setDocument(filename, doc);
    }

    /**
     * adds scores of alle implemented methods
     */
    public DoubleArrayList computeScores() {
        tfxidfarray = super.computeScores(Scorer.ALL);
        lm.computeScores();
        locarray = lm.getLocationScores();
        tm.computeScores();
        titlearray = tm.getTitleScores();
        kmarray = km.computeScores(Scorer.KEYWORD_BOTH);

        finalarray = new DoubleArrayList();
        for (int i = 0; i < this.tfxidfarray.size(); i++) {
            finalarray.add(tfxidfarray.get(i) + locarray.get(i) + titlearray.get(i) + kmarray.get(i));
        }

        return finalarray;
    }

}

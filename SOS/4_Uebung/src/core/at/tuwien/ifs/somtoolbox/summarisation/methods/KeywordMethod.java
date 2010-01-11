package at.tuwien.ifs.somtoolbox.summarisation.methods;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;
import cern.colt.list.DoubleArrayList;

/**
 * @author Julius Penaranda
 * @version $Id: KeywordMethod.java 2937 2009-12-15 01:03:56Z mayer $
 */
public class KeywordMethod extends TFxIDF {

    private DoubleArrayList keyarray = null;

    public KeywordMethod(PartOfSpeech p) {
        super(p);
    }

    @Override
    public void setDocument(String filename, ArrayList<String> doc) {
        super.setDocument(filename, doc);
    }

    @Override
    public DoubleArrayList computeScores(String type) {
        keyarray = new DoubleArrayList();

        if (type == Scorer.KEYWORD_BOTH) {
            DoubleArrayList noun = new DoubleArrayList();
            DoubleArrayList verb = new DoubleArrayList();
            noun = super.computeScores(PartOfSpeech.NOUN);
            verb = super.computeScores(PartOfSpeech.VERB);
            // System.out.println("noun array: "+ noun.toString());
            // System.out.println("verb array: "+ verb.toString());
            double result = 0.0;

            for (int i = 0; i < noun.size(); i++) {
                result = noun.get(i) + verb.get(i);
                keyarray.add(result);
            }
            // System.out.println("both array: "+ keyarray.toString());
        } else if (type == Scorer.KEYWORD_NOUN) {
            keyarray = super.computeScores(PartOfSpeech.NOUN);
        } else if (type == Scorer.KEYWORD_VERB) {
            keyarray = super.computeScores(PartOfSpeech.VERB);
        }

        return keyarray;
    }

}

package at.tuwien.ifs.somtoolbox.summarisation.methods;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.summarisation.parser.Scorer;
import cern.colt.list.DoubleArrayList;

/**
 * @author Julius Penaranda
 * @version $Id: LocationMethod.java 2937 2009-12-15 01:03:56Z mayer $
 */
public class LocationMethod extends TFxIDF {
    private ArrayList<String> doc;

    private DoubleArrayList lmarray;

    private DoubleArrayList finalarray;

    public LocationMethod(PartOfSpeech p) {
        super(p);
    }

    @Override
    public void setDocument(String filename, ArrayList<String> doc) {
        this.doc = doc;
        super.setDocument(filename, doc);
    }

    /**
     * computes LocationMethod scores of all sentences
     */
    public DoubleArrayList computeScores() {
        DoubleArrayList sentscores = super.computeScores(Scorer.ALL);
        lmarray = new DoubleArrayList();
        finalarray = new DoubleArrayList();

        // for each sentence, ignore title
        for (int i = 1; i < this.doc.size(); i++) {
            if (i == 1) {
                lmarray.add(2.0);
                finalarray.add(sentscores.get(i - 1) + 2.0);
            } else if (i == this.doc.size() - 1) {
                lmarray.add(1.0);
                finalarray.add(sentscores.get(i - 1) + 1.0);
            } else {
                lmarray.add(0.0);
                finalarray.add(sentscores.get(i - 1));
            }
        }
        return finalarray;
    }

    public DoubleArrayList getLocationScores() {
        return this.lmarray;
    }

}

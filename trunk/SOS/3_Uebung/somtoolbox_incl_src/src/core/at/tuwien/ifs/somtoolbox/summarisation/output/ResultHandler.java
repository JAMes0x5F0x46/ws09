package at.tuwien.ifs.somtoolbox.summarisation.output;

import java.util.ArrayList;

import cern.colt.list.DoubleArrayList;

/**
 * @author Julius Penaranda
 * @version $Id: ResultHandler.java 2892 2009-12-12 02:18:22Z mayer $
 */
public class ResultHandler {
    private ArrayList<String>[] allresultdoc = null;

    private DoubleArrayList[] allresultscores = null;

    private ArrayList<String>[] parsedDocuments = null;

    private DoubleArrayList[] allscores = null;

    private MultiDocumentHandler mdh = null;

    private Object[] itemNames = null;

    public ResultHandler(Object[] items, ArrayList<String>[] pd) {
        this.parsedDocuments = pd;
        this.itemNames = items;

        this.allresultdoc = new ArrayList[this.itemNames.length];
        this.allscores = new DoubleArrayList[this.itemNames.length];
        this.allresultscores = new DoubleArrayList[this.itemNames.length];

        if (this.parsedDocuments.length > 1) {
            mdh = new MultiDocumentHandler(this.itemNames, this.parsedDocuments);
        }
    }

    public void storeScore(int docID, DoubleArrayList scores) {
        this.allscores[docID] = scores;
    }

    public void createResult(int docID, int compr) {
        try {
            ArrayList<String> resultdoc = new ArrayList<String>();
            DoubleArrayList resultscores = new DoubleArrayList();
            DoubleArrayList scores = new DoubleArrayList();
            scores = this.allscores[docID].copy();

            // get Threshold
            double threshold = getThreshold(scores, compr);

            // create result
            // ignore title in document
            System.out.println("size: " + allscores[docID].size());
            for (int i = 1; i < this.parsedDocuments[docID].size(); i++) {
                if (this.allscores[docID].get(i - 1) >= threshold) {
                    resultdoc.add(this.parsedDocuments[docID].get(i));
                    resultscores.add(this.allscores[docID].get(i - 1));
                }
            }
            this.allresultdoc[docID] = resultdoc;
            this.allresultscores[docID] = resultscores;
            // System.out.println("Title: "+ (String)resultdoc.get(0));
            // System.out.println((resultdoc.size()-1)+" sentences");
        } catch (Exception e) {
            System.err.println("Error in ResultHandler: computeResults: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createAllResults(DoubleArrayList scores, int compr) {
        double threshold = getThreshold(scores, compr);
        mdh.storeScores(allscores);
        mdh.createAllResults(threshold);
    }

    public double getThreshold(DoubleArrayList scores, int compr) {
        scores.quickSort();
        scores.reverse();

        double maxSent = 0;
        Integer maxInt = new Integer(scores.size());
        double comprdouble = new Integer(compr).doubleValue() * 0.01;

        maxSent = Math.ceil(maxInt.doubleValue() * comprdouble);

        int maxSent_int = new Double(maxSent).intValue();
        System.out.println("maxSent_int: " + maxSent_int);
        if (maxSent_int == 0) {
            maxSent_int = 1;
        }
        return scores.get(maxSent_int - 1);
    }

    public void find_similarities(double degree) {
        if (mdh != null) {
            mdh.storeScores(allscores);
            mdh.find_similarities(degree);
        }
    }

    public ArrayList<String> getResultDoc(int docID) {
        return this.allresultdoc[docID];
    }

    public DoubleArrayList getResultScores(int docID) {
        return this.allresultscores[docID];
    }

    public DoubleArrayList getDocumentScores(int docID) {
        return this.allscores[docID];
    }

    public DoubleArrayList[] getDocumentScores() {
        return this.allscores;
    }

    public ArrayList<String> getMultiResultDocs() {
        return this.mdh.getResultDocs();
    }

    public DoubleArrayList getMultiResultScores() {
        return this.mdh.getResultScores();
    }

    public ArrayList<String> getMultiResultFilenames() {
        return this.mdh.getResultFileNames();
    }

}

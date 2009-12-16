package at.tuwien.ifs.somtoolbox.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * This abstract implementation provides basic support for operating on a {@link TemplateVector}. Sub-classes have to implement constructors and
 * methods to read and create a template vector, e.g. from a file or a database.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: AbstractSOMLibTemplateVector.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractSOMLibTemplateVector implements TemplateVector {

    /**
     * The dimension of the template vector, i.e. the number of attributes.
     */
    protected int dim = 0;

    protected int numInfo = 0;

    protected int numVectors = 0;

    protected String templateFileName = null;

    /**
     * The attributes of the template vector.
     */
    protected TemplateVectorElement[] elements = null;

    /**
     * A mapping label --&gt; attribute to allow fast access.
     */
    protected Hashtable<String, TemplateVectorElement> elementMap = new Hashtable<String, TemplateVectorElement>();

    protected int longestStringLength = -1;

    public int dim() {
        return dim;
    }

    public int numVectors() {
        return numVectors;
    }

    @Override
    public int numinfo() {
        return numInfo;
    }

    public String getLabel(int i) {
        return elements[i].getLabel();
    }

    public String[] getLabels() {
        String[] res = new String[elements.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = elements[i].getLabel();
        }
        return res;
    }

    public ArrayList<String> getLabelsAsList() {
        ArrayList<String> res = new ArrayList<String>(elements.length);
        for (TemplateVectorElement element : elements) {
            res.add(element.getLabel());
        }
        return res;
    }

    @Override
    public int getIndexOfFeature(String label) {
        if (containsLabel(label)) {
            return elementMap.get(label).getIndex();
        } else {
            return -1;
        }
    }

    public int getIndex(String label) {
        TemplateVectorElement templateVectorElement = elementMap.get(label);
        if (templateVectorElement != null) {
            return templateVectorElement.getIndex();
        } else {
            return -1;
        }
    }

    public TemplateVectorElement getElement(String label) {
        return elementMap.get(label);
    }

    @Override
    public boolean containsLabel(String label) {
        return elementMap.containsKey(label);
    }

    /**
     * @param label the name of the term.
     * @return The document frequency of the given term
     */
    public int getDocumentFrequency(String label) {
        return elementMap.get(label).getDocumentFrequency();
    }

    /**
     * @param queryTerms A map containing <label, frequency> pairs for each term.
     * @return A vector according to the tfxidf weighting scheme
     */
    public double[] getTFxIDFVectorFromTerms(Hashtable<String, Integer> queryTerms) {
        double[] vector = new double[dim];
        for (int i = 0; i < dim; i++) {
            if (queryTerms.get(elements[i].getLabel()) != null) {
                double tf = queryTerms.get(elements[i].getLabel()).intValue();
                vector[i] = tf * Math.log((double) elements.length / (double) elements[i].getDocumentFrequency());
            } else {
                vector[i] = 0;
            }
        }
        // FIXME: normalise only when input is normalised?
        vector = VectorTools.normaliseVectorToUnitLength(vector);
        return vector;
    }

    public void writeToFile(String fileName) throws IOException {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start writing new  template vector to '" + fileName + "'.");
        PrintWriter writer = FileUtils.openFileForWriting("Template Vector", fileName, fileName.endsWith(".gz"));
        writeHeaderToFile(writer, fileName, numVectors, dim(), numInfo);
        for (int i = 0; i < this.dim; i++) {
            writeElementToFile(writer, i, elements[i]);
        }
        writer.flush();
        writer.close();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Finished.");
    }

    public static void writeHeaderToFile(PrintWriter writer, String fileName, final int numVectors, final int dim, final int numInfo)
            throws IOException {
        writer.println("$TYPE template");
        writer.println("$XDIM " + numInfo);
        writer.println("$YDIM " + numVectors);
        writer.println("$VEC_DIM " + dim);
    }

    public static void writeElementToFile(PrintWriter writer, int i, TemplateVectorElement e) {
        StringBuffer b = new StringBuffer();
        int numinfo = e.getTemplateVector().numinfo();
        if (numinfo > 2) {
            b.append(" ").append(e.getDocumentFrequency());
        }
        if (numinfo > 3) {
            b.append(" ").append(e.getCollectionTermFrequency());
        }
        if (numinfo > 4) {
            b.append(" ").append(e.getMinimumTermFrequency());
        }
        if (numinfo > 5) {
            b.append(" ").append(e.getMaximumTermFrequency());
        }
        if (numinfo > 6) {
            b.append(" ").append(e.getMeanTermFrequency());
        }
        if (e.getComment() != null) {
            b.append(" ").append(e.getComment());
        }
        writer.println(i + " " + e.getLabel() + b.toString());
    }

    @Override
    public TemplateVectorElement getElement(int index) {
        return elements[index];
    }

    @Override
    public int getLongestStringLength() {
        if (longestStringLength == -1) {
            longestStringLength = StringUtils.getLongestStringLength(elementMap.keySet());
        }
        return longestStringLength;
    }

    @Override
    public void incNumVectors(int numVectors) {
        this.numVectors += numVectors;
    }
}

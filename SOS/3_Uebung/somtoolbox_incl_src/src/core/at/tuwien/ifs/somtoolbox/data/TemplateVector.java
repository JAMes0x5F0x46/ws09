package at.tuwien.ifs.somtoolbox.data;

import java.io.IOException;
import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.util.SOMLibInputMerger;

/**
 * The template vector provides the attribute structure of the input vectors used for the training process of a Self-Organizing Map. It is usually
 * written by a parser or vector generator program creating the vector structure.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: TemplateVector.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface TemplateVector {

    String templateFileNameSuffix = ".tv";

    /**
     * Gets the dimension.
     * 
     * @return the dimension of the template vector, i.e. the number of attributes
     */
    public int dim();

    /**
     * Gets the label at the given index.
     * 
     * @return the name of the label at the given index
     */
    public String getLabel(int i);

    /** Gets all the labels defined in this template vector. */
    public String[] getLabels();

    /** Gets all the labels defined in this template vector as a list. */
    public ArrayList<String> getLabelsAsList();

    /** tests whether there is a feature/attribute with the given label */
    public boolean containsLabel(String label);

    /** Returns the numerical index of the feature with the given name. */
    public int getIndexOfFeature(String label);

    /** Return how many vectors are in the input vector file associated with this template vector */
    public int numVectors();

    /** Returns how many columns the template vector contains, i.e. the $XDIM. */
    public int numinfo();

    /** Writes the template vector to a file. */
    public void writeToFile(String fileName) throws IOException;

    /** returns the template vector element for the feature/attribute at the given position */
    public TemplateVectorElement getElement(int index);

    /** calculates the length of the longest feature/attribute label */
    public int getLongestStringLength();

    /** Increase the num-vectors counter, used e.g. when merging input files in {@link SOMLibInputMerger} . */
    public void incNumVectors(int numVectors);
}

package at.tuwien.ifs.somtoolbox.output.labeling;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * Defines basic functionality for Labelers. All classes providing labelling algorothm implementations should implement this interface.
 * 
 * @author Michael Dittenbach
 * @version $Id: Labeler.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface Labeler {

    /**
     * Determines and adds labels to the units of a GrowingSOM (should be NetworkModel in the future).
     * 
     * @param gsom The GrowingSOM to be labeled.
     * @param data The data that is already mapped onto the GrowingSOM
     * @param num The number of labels per node.
     */
    public void label(GrowingSOM gsom, InputData data, int num); // TODO: GrowingSOM -> NetworkModel or similar

    public void label(GrowingSOM gsom, InputData data, int num, boolean ignoreLabelsWithZero); // TODO: GrowingSOM -> NetworkModel or similar

    public void label(GHSOM ghsom, InputData data, int num); // TODO: GHSOM -> NetworkModel or similar

}

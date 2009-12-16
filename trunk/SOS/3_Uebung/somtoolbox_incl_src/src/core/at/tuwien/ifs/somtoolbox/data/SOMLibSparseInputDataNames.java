package at.tuwien.ifs.somtoolbox.data;

/**
 * Reads just the data names from the Input file, the rest is discarded.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMLibSparseInputDataNames.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMLibSparseInputDataNames extends SOMLibSparseInputData {

    public SOMLibSparseInputDataNames(String vectorFileName) {
        super(vectorFileName);
    }

    @Override
    protected void processLine(int index, String[] lineElements) throws Exception {
        dataNames[index] = lineElements[dim].trim();
    }
}

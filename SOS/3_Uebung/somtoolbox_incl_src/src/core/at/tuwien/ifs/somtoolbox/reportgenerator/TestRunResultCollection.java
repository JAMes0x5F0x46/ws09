package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.util.Vector;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: TestRunResultCollection.java 2874 2009-12-11 16:03:27Z frank $
 */
public class TestRunResultCollection {

    /* they all should have the same value as the ones defined in SOMLibMapDescription */
    // FIXME: use the constants from SOMLibMapDescription!
    public static final String keyType = "$TYPE";

    public static final String keyTopology = "$TOPOLOGY";

    public static final String keyXDim = "$XDIM";

    public static final String keyYDim = "$YDIM";

    public static final String keyVecDim = "$VEC_DIM";

    public static final String keyStorageDate = "$STORAGE_DATE";

    public static final String keyTrainingTime = "$TRAINING_TIME";

    public static final String keyLearnrateType = "$LEARNRATE_TYPE";

    public static final String keyLearnRateInit = "$LEARNRATE_INIT";

    public static final String keyNeighbourhoodType = "$NEIGHBORHOOD_TYPE";

    public static final String keyNeighbourhoodInit = "$NEIGHBORHOOD_INIT";

    public static final String keyRandomInit = "$RAND_INIT";

    public static final String keyTotalIterations = "$ITERATIONS_TOTAL";

    public static final String keyTotalTrainingVectors = "$NR_TRAINVEC_TOTAL";

    public static final String keyVectorsNormailised = "$VEC_NORMALIZED";

    public static final String keyQuantErrMap = "$QUANTERROR_MAP";

    public static final String keyQuantErrVector = "$QUANTERROR_VEC";

    public static final String keyUrlTrainingVector = "$URL_TRAINING_VEC";

    public static final String keyUrlTrainingVectorDescription = "$URL_TRAINING_VEC_DESCR";

    public static final String keyUrlWeightVector = "$URL_WEIGHT_VEC";

    public static final String keyUrlQuantErrMap = "$URL_QUANTERR_MAP";

    public static final String keyUrlMappedInputVector = "$URL_MAPPED_INPUT_VEC";

    public static final String keyUrlMappedInputVectorDescription = "$URL_MAPPED_INPUT_VEC_DESCR";

    public static final String keyUrlUnitDescription = "$URL_UNIT_DESCR";

    public static final String keyMetric = "$METRIC";

    public static final String keyLayerRevision = "$LAYER_REVISION";

    public static final String keyDescription = "$DESCRIPTION";

    private Vector<TestRunResult> testruns;

    public TestRunResultCollection() {
        testruns = new Vector<TestRunResult>();
    }

    public void addTestrunResult(TestRunResult newRun) {
        this.testruns.add(newRun);
    }

    public void setObjectsToCorrectType() {
        for (int i = 0; i < this.testruns.size(); i++) {
            String type = (String) this.getProperty(TestRunResultCollection.keyTopology, i);
            if (type != null) { // if type is unknown, keep normal report type
                if (type.equals("gg")) {
                    this.testruns.add(i, new GGSOMTestRunResult(this.getRun(i)));
                    this.testruns.remove(i + 1);
                } else if (type.equals("ghsom")) {
                    this.testruns.add(i, new GGSOMTestRunResult(this.getRun(i)));
                    this.testruns.remove(i + 1);
                }
            }
        }
    }

    public int getNumberOfRuns() {
        return this.testruns.size();
    }

    public Object getProperty(String property, int testrun) {
        return this.testruns.get(testrun).getMapProperty(property);
    }

    public TestRunResult getRun(int r) {
        return this.testruns.get(r);
    }
}

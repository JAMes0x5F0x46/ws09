package at.tuwien.ifs.somtoolbox.input;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Provides generic functionality to read a saved network model.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: AbstractSOMInputReader.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractSOMInputReader implements SOMInputReader {
    /**
     * Inner class holding the information about a specific unit.
     * 
     * @author Michael Dittenbach
     */
    protected class UnitInformation {
        protected String[] bestContextLabels = null;

        protected String[] contextGateLabels = null;

        protected double[] gateWeightLabels = null;

        protected String[] kaskiGateLabels = null;

        // private int kaskiWeights = 0;

        // private int gateWeights = 0;

        // private int bestContext = 0;

        protected String[] kaskiLabels = null;

        protected double[] kaskiWeightLabels = null;

        protected String[] mappedVecs = null;

        protected double[] mappedVecsDist = null;

        protected int nrbestcontext = 0;

        protected int nrContextGate = 0;

        protected int nrgateweights = 0;

        protected int nrKaski = 0;

        protected int nrKaskiGate = 0;

        protected int nrkaskiweights = 0;

        protected int nrSomsMapped = 0;

        protected int nrUnitLabels = 0;

        protected int nrVecMapped = 0;

        protected int posX = 0;

        protected int posY = 0;

        protected int posZ = 0;

        protected double quantErrorUnit = 0;

        protected double quantErrorUnitAvg = 0;

        protected String unitId = null;

        protected String[] unitLabels = null;

        protected double[] unitLabelsQe = null;

        protected double[] unitLabelsWgt = null;

        protected String[] urlMappedSoms = null;

        protected double[] vector = null;

        /**
         * Sole constructor.
         */
        protected UnitInformation() {
            vector = new double[0];
        }

        protected UnitInformation(int dim) {
            vector = new double[dim];
        }

    }

    protected boolean labelled = false;

    protected int dim = 0;

    protected String metricName = "at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric"; // default metric

    protected UnitInformation[][][] unitInfo = null;

    protected int xSize = 0;

    protected int ySize = 0;

    protected int zSize = 0;

    /** The common prefix of all input vector labels. Will be once computed in {@link #getCommonVectorLabelPrefix()}, and then cached. */
    protected String commonLabelPrefix = null;

    protected ArrayList<String> allVectorNames = new ArrayList<String>();

    public AbstractSOMInputReader() {
        super();
    }

    public String[] getBestContextUnitLabels(int x, int y) {
        return getBestContextUnitLabels(x, y, 0);
    }

    public String[] getBestContextUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].bestContextLabels;
    }

    public String[] getContextGateUnitLabels(int x, int y) {
        return getContextGateUnitLabels(x, y, 0);
    }

    public String[] getContextGateUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].contextGateLabels;
    }

    /** @see at.tuwien.ifs.somtoolbox.input.SOMInputReader#getDim() */
    public int getDim() {
        return dim;
    }

    public String[] getKaskiGateUnitLabels(int x, int y) {
        return getKaskiGateUnitLabels(x, y, 0);
    }

    public String[] getKaskiGateUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].kaskiGateLabels;
    }

    public double[] getKaskiGateUnitLabelsWgt(int x, int y) {
        return getKaskiGateUnitLabelsWgt(x, y, 0);
    }

    public double[] getKaskiGateUnitLabelsWgt(int x, int y, int z) {
        return unitInfo[x][y][z].gateWeightLabels;
    }

    public String[] getKaskiUnitLabels(int x, int y) {
        return getKaskiUnitLabels(x, y, 0);
    }

    public String[] getKaskiUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].kaskiLabels;
    }

    public double[] getKaskiUnitLabelsWgt(int x, int y) {
        return getKaskiUnitLabelsWgt(x, y, 0);
    }

    public double[] getKaskiUnitLabelsWgt(int x, int y, int z) {
        return unitInfo[x][y][z].kaskiWeightLabels;
    }

    public String[] getMappedVecs(int x, int y) {
        return getMappedVecs(x, y, 0);
    }

    public String[] getMappedVecs(int x, int y, int z) {
        return unitInfo[x][y][z].mappedVecs;
    }

    public double[] getMappedVecsDist(int x, int y) {
        return getMappedVecsDist(x, y, 0);
    }

    public double[] getMappedVecsDist(int x, int y, int z) {
        return unitInfo[x][y][z].mappedVecsDist;
    }

    public String getMetricName() {
        return metricName;
    }

    public int getNrBestContext(int x, int y) {
        return getNrBestContext(x, y, 0);
    }

    public int getNrBestContext(int x, int y, int z) {
        return unitInfo[x][y][z].nrbestcontext;
    }

    public int getNrContextGateLabels(int x, int y) {
        return getNrContextGateLabels(x, y, 0);
    }

    public int getNrContextGateLabels(int x, int y, int z) {
        return unitInfo[x][y][z].nrContextGate;
    }

    public int getNrGateWeights(int x, int y) {
        return getNrGateWeights(x, y, 0);
    }

    public int getNrGateWeights(int x, int y, int z) {
        return unitInfo[x][y][z].nrgateweights;
    }

    public int getNrKaskiGateLabels(int x, int y) {
        return getNrKaskiGateLabels(x, y, 0);
    }

    public int getNrKaskiGateLabels(int x, int y, int z) {
        return unitInfo[x][y][z].nrKaskiGate;
    }

    public int getNrKaskiLabels(int x, int y) {
        return getNrKaskiLabels(x, y, 0);
    }

    public int getNrKaskiLabels(int x, int y, int z) {
        return unitInfo[x][y][z].nrKaski;
    }

    public int getNrKaskiWeights(int x, int y) {
        return getNrKaskiWeights(x, y, 0);
    }

    public int getNrKaskiWeights(int x, int y, int z) {
        return unitInfo[x][y][z].nrkaskiweights;
    }

    // public String[] getcontextLabels(int x, int y) {
    // return unitInfo[x][y].contextLabels;
    // }

    public int getNrSomsMapped(int x, int y) {
        return getNrSomsMapped(x, y, 0);
    }

    public int getNrSomsMapped(int x, int y, int z) {
        return unitInfo[x][y][z].nrSomsMapped;
    }

    public int getNrUnitLabels(int x, int y) {
        return getNrUnitLabels(x, y, 0);
    }

    public int getNrUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].nrUnitLabels;
    }

    public int getNrVecMapped(int x, int y) {
        return getNrVecMapped(x, y, 0);
    }

    public int getNrVecMapped(int x, int y, int z) {
        return unitInfo[x][y][z].nrVecMapped;
    }

    public double getQuantErrorUnit(int x, int y) {
        return getQuantErrorUnit(x, y, 0);
    }

    public double getQuantErrorUnit(int x, int y, int z) {
        return unitInfo[x][y][z].quantErrorUnit;
    }

    public double getQuantErrorUnitAvg(int x, int y) {
        return getQuantErrorUnitAvg(x, y, 0);
    }

    public double getQuantErrorUnitAvg(int x, int y, int z) {
        return unitInfo[x][y][z].quantErrorUnitAvg;
    }

    public String[] getUnitLabels(int x, int y) {
        return getUnitLabels(x, y, 0);
    }

    public String[] getUnitLabels(int x, int y, int z) {
        return unitInfo[x][y][z].unitLabels;
    }

    public double[] getUnitLabelsQe(int x, int y) {
        return getUnitLabelsQe(x, y, 0);
    }

    public double[] getUnitLabelsQe(int x, int y, int z) {
        return unitInfo[x][y][z].unitLabelsQe;
    }

    public double[] getUnitLabelsWgt(int x, int y) {
        return getUnitLabelsWgt(x, y, 0);
    }

    public double[] getUnitLabelsWgt(int x, int y, int z) {
        return unitInfo[x][y][z].unitLabelsWgt;
    }

    public String[] getUrlMappedSoms(int x, int y) {
        return getUrlMappedSoms(x, y, 0);
    }

    public String[] getUrlMappedSoms(int x, int y, int z) {
        return unitInfo[x][y][z].urlMappedSoms;
    }

    public double[][][][] getVectors() {
        double[][][][] res = new double[xSize][ySize][zSize][];
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    res[i][j][k] = unitInfo[i][j][k].vector;
                }
            }
        }
        return res;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public int getZSize() {
        return zSize;
    }

    protected void initUnitInformation() {
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    unitInfo[i][j][k] = new UnitInformation();
                }
            }
        }
    }

    public String getCommonVectorLabelPrefix() {
        if (commonLabelPrefix == null) {
            commonLabelPrefix = StringUtils.getCommonPrefix(allVectorNames);
        }
        return commonLabelPrefix;
    }

    @Override
    public boolean isLabelled() {
        return labelled;
    }

}
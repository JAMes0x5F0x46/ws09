package at.tuwien.ifs.somtoolbox.input;

import java.io.FileNotFoundException;
import java.util.Random;

import at.tuwien.ifs.somtoolbox.models.MnemonicSOM;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * A {@link SOMInputReader} that can handle {@link MnemonicSOM}, i.e. SOMs where not all units are occupied.
 * 
 * @version $Id: MnemonicSOMLibFormatInputReader.java 2970 2009-12-16 15:39:08Z mayer $
 * @author Rudolf Mayer
 */
public class MnemonicSOMLibFormatInputReader extends SOMLibFormatInputReader {
    protected void processUnitElement(String line, int j, int i) {
        processUnitElement(line, 0, j, i);
    }

    @Override
    protected void processUnitElement(String line, int k, int j, int i) {
        if (line.startsWith("null")) {
            unitInfo[i][j][k] = null;
        } else {
            super.processUnitElement(line, k, j, i);
        }
    }

    public MnemonicSOMLibFormatInputReader(String unitDescriptionFileName, String mapDescriptionFileName, int dimension)
            throws FileNotFoundException, SOMLibFileFormatException {
        this(null, unitDescriptionFileName, unitDescriptionFileName, dimension);
    }

    public MnemonicSOMLibFormatInputReader(String weightVectorFileName, String unitDescriptionFileName, String mapDescriptionFileName)
            throws FileNotFoundException, SOMLibFileFormatException {
        super(weightVectorFileName, unitDescriptionFileName, mapDescriptionFileName);
    }

    public MnemonicSOMLibFormatInputReader(String weightVectorFileName, String unitDescriptionFileName, String mapDescriptionFileName, int dimension)
            throws FileNotFoundException, SOMLibFileFormatException {
        super(null, unitDescriptionFileName, mapDescriptionFileName);
        this.dim = dimension;
        generateWRandomWeightvectors();
    }

    /** Generates random weight vectors, but only for those units that are occupied. */
    private void generateWRandomWeightvectors() {
        Random rand = new Random();
        for (int col = 0; col < getXSize(); col++) {
            for (int row = 0; row < getYSize(); row++) {
                for (int slice = 0; slice < getZSize(); slice++) {
                    if (unitInfo[col][row][slice] != null) {
                        double[] weightVector = new double[dim];
                        for (int i = 0; i < weightVector.length; i++) {
                            weightVector[i] = rand.nextDouble();
                        }
                        unitInfo[col][row][slice].vector = VectorTools.normaliseVectorToUnitLength(weightVector);
                    }
                }
            }
        }
    }

    @Override
    public double[][][][] getVectors() {
        double[][][][] res = new double[xSize][ySize][zSize][];
        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    if (unitInfo[i][j][k] != null) {
                        res[i][j][k] = unitInfo[i][j][k].vector;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public int getNrVecMapped(int x, int y, int z) {
        if (unitInfo[x][y][z] != null) {
            return super.getNrVecMapped(x, y, z);
        } else {
            return 0;
        }
    }

    @Override
    public String[] getMappedVecs(int x, int y, int z) {
        if (unitInfo[x][y][z] != null) {
            return super.getMappedVecs(x, y, z);
        } else {
            return null;
        }
    }

    @Override
    public double[] getMappedVecsDist(int x, int y, int z) {
        if (unitInfo[x][y][z] != null) {
            return super.getMappedVecsDist(x, y, z);
        } else {
            return null;
        }
    }

}
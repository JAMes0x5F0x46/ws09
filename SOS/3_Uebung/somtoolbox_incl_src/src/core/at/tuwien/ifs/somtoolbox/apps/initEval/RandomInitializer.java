package at.tuwien.ifs.somtoolbox.apps.initEval;

import java.util.Random;

import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * Random SOM Initializer
 * 
 * @author Stefan Bischof
 * @author Leo Sklenitzka
 */
public class RandomInitializer implements LayerInitializer {

    private Layer layer;

    private int xSize;

    private int ySize;

    private int zSize;

    private int dim;

    private Random rand;

    private boolean normalized;

    /**
     * @param layer
     * @param xSize
     * @param ySize
     * @param zSize
     * @param dim
     * @param rand
     * @param normalized
     */
    public RandomInitializer(Layer layer, int xSize, int ySize, int zSize, int dim, Random rand, boolean normalized) {
        this.layer = layer;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.dim = dim;
        this.rand = rand;
        this.normalized = normalized;
    }

    /**
     * Initialize the SOM Layer randomly
     * 
     * @return initialized SOM
     */
    @Override
    public Unit[][][] initialize() {
        Unit[][][] units = new Unit[xSize][ySize][zSize];

        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    units[i][j][k] = new Unit(layer, i, j, k, dim, rand, normalized);
                }
            }
        }

        return units;
    }

}

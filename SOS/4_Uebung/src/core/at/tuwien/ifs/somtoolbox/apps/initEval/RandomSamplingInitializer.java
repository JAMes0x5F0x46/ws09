package at.tuwien.ifs.somtoolbox.apps.initEval;

import java.util.Random;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * @author Stefan Bischof
 * @author Leo Sklenitzka
 * @version $Id: RandomSamplingInitializer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class RandomSamplingInitializer implements LayerInitializer {

    private Layer layer;

    private int xSize;

    private int ySize;

    private int zSize;

    private double[][] dataarray;

    private Random rand;

    /**
     * @param layer
     * @param xSize
     * @param ySize
     * @param zSize
     * @param data
     */
    public RandomSamplingInitializer(Layer layer, int xSize, int ySize, int zSize, InputData data) {
        this.layer = layer;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.dataarray = data.getData();
        this.rand = new Random();
    }

    /**
     * Initialize the SOM Layer using Random Input Sampling
     * 
     * @return initialized SOM
     */
    @Override
    public Unit[][][] initialize() {
        Unit[][][] units = new Unit[xSize][ySize][zSize];

        for (int k = 0; k < zSize; k++) {
            for (int j = 0; j < ySize; j++) {
                for (int i = 0; i < xSize; i++) {
                    units[i][j][k] = new Unit(layer, i, j, k, VectorTools.normaliseVectorToUnitLength(dataarray[rand.nextInt(dataarray.length)]));
                }
            }
        }

        return units;
    }

}

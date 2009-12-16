package at.tuwien.ifs.somtoolbox.layers;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.InputData;

/**
 * Implementation of a toroid Self-Organizing Map layer, i.e. a "doughnut" shaped layer, whose left &amp; right and upper &amp; lower edges are
 * interconnected. This class mainly adjusts distance functions.
 * 
 * @author Karim Jafarmadar
 * @author Rudolf Mayer
 * @version $Id: ToroidLayer.java 2874 2009-12-11 16:03:27Z frank $
 */

public class ToroidLayer extends GrowingLayer {

    /** @see GrowingLayer#GrowingLayer(int, int, String, int, boolean, boolean, long, InputData) */
    public ToroidLayer(int xSize, int ySize, String metricName, int dim, boolean normalize, boolean usePCA, long seed, InputData data) {
        super(xSize, ySize, metricName, dim, normalize, usePCA, seed, data);
    }

    /** @see GrowingLayer#GrowingLayer(int, int, int, String, int, boolean, boolean, long, InputData) */
    public ToroidLayer(int xSize, int ySize, int zSize, String metricName, int dim, boolean normalize, boolean usePCA, long seed, InputData data) {
        super(xSize, ySize, zSize, metricName, dim, normalize, usePCA, seed, data);
    }

    /** @see GrowingLayer#GrowingLayer(int, Unit, int, int, String, int, boolean, boolean, long, InputData). */
    public ToroidLayer(int id, Unit su, int xSize, int ySize, String metricName, int dim, boolean normalize, boolean usePCA, long seed, InputData data) {
        super(id, su, xSize, ySize, metricName, dim, normalize, usePCA, seed, data);
    }

    /** @see GrowingLayer#GrowingLayer(int, Unit, int, int, int, String, int, boolean, boolean, long, InputData). */
    public ToroidLayer(int id, Unit su, int xSize, int ySize, int zSize, String metricName, int dim, boolean normalize, boolean usePCA, long seed,
            InputData data) {
        super(id, su, xSize, ySize, metricName, dim, normalize, usePCA, seed, data);

    }

    /** @see GrowingLayer#GrowingLayer(int, Unit, int, int, String, int, double[][][], long). */
    public ToroidLayer(int id, Unit su, int xSize, int ySize, String metricName, int dim, double[][][] vectors, long seed) throws SOMToolboxException {
        super(id, su, xSize, ySize, metricName, dim, vectors, seed);
    }

    /** @see GrowingLayer#GrowingLayer(int, Unit, int, int, int, String, int, double[][][][], long) */
    public ToroidLayer(int id, Unit su, int xSize, int ySize, int zSize, String metricName, int dim, double[][][][] vectors, long seed)
            throws SOMToolboxException {
        super(id, su, xSize, ySize, zSize, metricName, dim, vectors, seed);
    }

    /**
     * Toroid distance on the map
     */
    @Override
    public double getMapDistance(int x1, int y1, int x2, int y2) {
        int distX = Math.min(Math.abs(x1 - x2), xSize - Math.abs(x1 - x2));
        int distY = Math.min(Math.abs(y1 - y2), ySize - Math.abs(y1 - y2));
        return Math.sqrt(distX * distX + distY * distY);
    }

    @Override
    public double getMapDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        int distX = Math.min(Math.abs(x1 - x2), xSize - Math.abs(x1 - x2));
        int distY = Math.min(Math.abs(y1 - y2), ySize - Math.abs(y1 - y2));
        int distZ = Math.min(Math.abs(z1 - z2), zSize - Math.abs(z1 - z2));
        return Math.sqrt(distX * distX + distY * distY + distZ * distZ);
    }

    /**
     * On a toroid map each unit has a neighbour
     */
    @Override
    public boolean hasNeighbours(int x, int y) throws LayerAccessException {
        return true;
    }

    @Override
    protected Unit[] getNeighbouringUnits(Unit u) throws LayerAccessException {
        int x = u.getXPos();
        int y = u.getYPos();
        int z = u.getZPos();
        ArrayList<Unit> neighbourUnits = new ArrayList<Unit>();

        if (x > 0) {
            neighbourUnits.add(getUnit(x - 1, y, z));
        } else {
            neighbourUnits.add(getUnit(xSize - 1, y, z));
        }
        if (x + 1 < getXSize()) {
            neighbourUnits.add(getUnit(x + 1, y, z));
        } else {
            neighbourUnits.add(getUnit(0, y, z));
        }

        if (y > 0) {
            neighbourUnits.add(getUnit(x, y - 1, z));
        } else {
            neighbourUnits.add(getUnit(x, ySize - 1, z));
        }
        if (y + 1 < getYSize()) {
            neighbourUnits.add(getUnit(x, y + 1, z));
        } else {
            neighbourUnits.add(getUnit(x, 0, z));
        }

        if (z > 0) {
            neighbourUnits.add(getUnit(x, y, z - 1));
        } else {
            neighbourUnits.add(getUnit(x, y, zSize - 1));
        }
        if (z + 1 < getYSize()) {
            neighbourUnits.add(getUnit(x, y, z + 1));
        } else {
            neighbourUnits.add(getUnit(x, y, 0));
        }

        return neighbourUnits.toArray(new Unit[neighbourUnits.size()]);
    }

    @Override
    protected Unit[] getNeighbouringUnits(Unit u, int size) throws LayerAccessException {
        // FIXME: implement this :-)
        throw new IllegalArgumentException("Not implemented");
    }

}
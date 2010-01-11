package at.tuwien.ifs.somtoolbox.layers.metrics;

import java.awt.Point;

import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * A metric for mnemonic SOMs. The metric is basically a Manhattan/L1 Metric, but takes into consideration that not all paths between two units might
 * be possible (as the grid of the Mnemonic SOM might be sparse). For performance reasons, a distance matrix is pre-calculated.
 * 
 * @version $Id: MnemonicSOMMetric.java 2874 2009-12-11 16:03:27Z frank $
 * @author Rudolf Mayer
 */
public class MnemonicSOMMetric extends L2Metric {
    int[][][][] distanceMatrix;

    Integer[][][][] distanceMatrix_;

    /**
     * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[], double[])
     */

    public double distance(InputDatum datum1, InputDatum datum2) throws MetricException {
        Point point1 = (Point) datum1.getProperty("coordinates");
        Point point2 = (Point) datum2.getProperty("coordinates");
        if (point1 == null || point2 == null) {
            throw new IllegalArgumentException("Input data do not contain 'coordinates' property");
        }
        return distanceMatrix[point1.x][point1.y][point2.x][point2.y];
    }

    public void countDistances(int distanceFromStart, Unit startUnit, Unit currentUnit, Unit[][] units) {
        int xpos = currentUnit.getXPos();
        int ypos = currentUnit.getYPos();
        if (distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][xpos][ypos] == null
                || distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][xpos][ypos].intValue() > distanceFromStart) {
            // the unit has not been reached yet or on a longer path
            distanceMatrix_[startUnit.getXPos()][startUnit.getYPos()][xpos][ypos] = new Integer(distanceFromStart);

            // now we check all neighbours.
            if (xpos > 0 && units[xpos - 1][ypos] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos - 1][ypos], units);
            }

            if (xpos + 1 < units.length && units[xpos + 1][ypos] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos + 1][ypos], units);
            }

            if (ypos > 0 && units[xpos][ypos - 1] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos][ypos - 1], units);
            }

            if (ypos + 1 < units[0].length && units[xpos][ypos + 1] != null) {
                countDistances(distanceFromStart + 1, startUnit, units[xpos][ypos + 1], units);
            }
        }
    }

    public MnemonicSOMMetric(Unit[][] units) {
        // init the non-empty units to have a max-value distance.
        distanceMatrix_ = new Integer[units.length][units[0].length][units.length][units[0].length];

        for (int col = 0; col < units.length; col++) {
            for (int row = 0; row < units[0].length; row++) {
                if (units[col][row] != null) {
                    countDistances(0, units[col][row], units[col][row], units);
                }
            }
        }
    }

}

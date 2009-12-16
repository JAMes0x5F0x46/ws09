package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.VectorTools;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;
import cern.jet.stat.quantile.DoubleQuantileFinder;
import cern.jet.stat.quantile.QuantileFinderFactory;

/**
 * This Visualizer provides two variants of the U-Matrix.
 * <ol>
 * <li>Implementation of the classic U-Matrix as described in <i><b>Ultsch, A., and Siemon, H.P.</b> Kohonen's Self Organizing Feature Maps for
 * Exploratory Data Analysis. In Proc. Intern. Neural Networks, 1990, pp. 305-308, Kluwer Academic Press, Paris, France.</i>.</li>
 * <li>Same as 1., but D-Matrix Values only.</li>
 * </ol>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: UMatrix.java 2874 2009-12-11 16:03:27Z frank $
 */
public class UMatrix extends AbstractMatrixVisualizer implements BackgroundImageVisualizer {

    public static final String[] UMATRIX_SHORT_NAMES = new String[] { "UMatrix", "DMatrix", "PMatrix", "UStarMatrix" };

    // a cache for the computed p-matrix, which is expensive, and used also for the U*-Matrix
    private static final HashMap<GrowingSOM, DoubleMatrix2D> pMatrixCache = new HashMap<GrowingSOM, DoubleMatrix2D>();

    public UMatrix() {
        NUM_VISUALIZATIONS = 4;
        VISUALIZATION_NAMES = new String[] { "U-Matrix", "D-Matrix", "P-Matrix", "U*-Matrix" };
        VISUALIZATION_SHORT_NAMES = UMATRIX_SHORT_NAMES;
        VISUALIZATION_DESCRIPTIONS = new String[] {
                "Implementation of the classic U-Matrix as described in \"Ultsch, A., and Siemon, H.P.\n"
                        + "Kohonen's Self Organizing Feature Maps for Exploratory Data Analysis. In Proc. Intern.\n"
                        + "Neural Networks, 1990, pp. 305-308, Kluwer Academic Press, Paris, France.\"",

                "D-Matrix Values only", "P-Matrix (Pareto)", "U*-Matrix (U and P Matrix combined)" };
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) {
        if (index == 0) {
            return createOriginalUMatrix(gsom, width, height);
        } else if (index == 1) {
            return createOriginalDMatrix(gsom, width, height);
        } else if (index == 2) {
            return createOriginalPMatrix(gsom, width, height);
        } else if (index == 3) {
            return createOriginalUStarMatrix(gsom, width, height);
        } else {
            return null;
        }
    }

    /**
     * Creates an image of the D-Matrix visualisation.
     * 
     * @param gsom the GrowingSOM to generate the visualisation for
     * @param width the desired width of the image, in pixels
     * @param height the desired height of the image, in pixels.
     * @return an image for this visualisation.
     */
    private BufferedImage createOriginalDMatrix(GrowingSOM gsom, int width, int height) {
        DoubleMatrix2D dmatrix = createUMatrix(gsom).viewStrides(2, 2);
        VectorTools.normalise(dmatrix);
        return createImage(gsom, dmatrix, width, height, interpolate);
    }

    /**
     * Creates an image of the U-Matrix visualisation.
     * 
     * @param gsom the GrowingSOM to generate the visualisation for
     * @param width the desired width of the image, in pixels
     * @param height the desired height of the image, in pixels.
     * @return an image for this visualisation.
     */
    private BufferedImage createOriginalUMatrix(GrowingSOM gsom, int width, int height) {
        DoubleMatrix2D umatrix = createUMatrix(gsom);
        VectorTools.normalise(umatrix);
        return createImage(gsom, umatrix, width, height, interpolate);
    }

    private BufferedImage createOriginalPMatrix(GrowingSOM gsom, int width, int height) {
        DoubleMatrix2D pmatrix = createPMatrix(gsom);
        VectorTools.normalise(pmatrix);
        return createImage(gsom, pmatrix, width, height, interpolate);
    }

    private BufferedImage createOriginalUStarMatrix(GrowingSOM gsom, int width, int height) {
        DoubleMatrix2D ustar = createUStarMatrix(gsom);
        VectorTools.normalise(ustar);
        return createImage(gsom, ustar, width, height, interpolate);
    }

    /**
     * Creates the height matrix.
     * 
     * @param gsom the GrowingSOM to generate the visualisation for
     * @return a matrix containing heights for each coordinate.
     */
    public DoubleMatrix2D createUMatrix(GrowingSOM gsom) {
        DistanceMetric metric = gsom.getLayer().getMetric();
        int umatW = (gsom.getLayer().getXSize() * 2) - 1;
        int umatH = (gsom.getLayer().getYSize() * 2) - 1;
        DoubleMatrix2D umatrix = DoubleFactory2D.dense.make(umatH, umatW, -1);

        // calc horizontal, vertical and diagonal distances between units
        for (int j = 0; j < (umatH); j++) {
            for (int i = 0; i < (umatW); i++) {
                try {
                    if ((i % 2 != 0) && (j % 2 == 0)) { // horizontal
                        umatrix.set(j, i, metric.distance(gsom.getLayer().getUnit((i - 1) / 2, j / 2).getWeightVector(), gsom.getLayer().getUnit(
                                ((i - 1) / 2) + 1, j / 2).getWeightVector()));
                    } else if ((i % 2 == 0) && (j % 2 != 0)) { // vertical
                        umatrix.set(j, i, metric.distance(gsom.getLayer().getUnit(i / 2, (j - 1) / 2).getWeightVector(), gsom.getLayer().getUnit(
                                i / 2, ((j - 1) / 2) + 1).getWeightVector()));
                    } else if ((i % 2 != 0) && (j % 2 != 0)) { // diagonal
                        double d1 = metric.distance(gsom.getLayer().getUnit((i - 1) / 2, (j - 1) / 2).getWeightVector(), gsom.getLayer().getUnit(
                                ((i - 1) / 2) + 1, ((j - 1) / 2) + 1).getWeightVector());
                        double d2 = metric.distance(gsom.getLayer().getUnit((i - 1) / 2, ((j - 1) / 2) + 1).getWeightVector(),
                                gsom.getLayer().getUnit(((i - 1) / 2) + 1, (j - 1) / 2).getWeightVector());
                        umatrix.set(j, i, (d1 + d2) / (2 * Math.sqrt(2)));
                    }
                } catch (MetricException me) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(me.getMessage());
                    System.exit(-1);
                } catch (LayerAccessException lae) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(lae.getMessage());
                    System.exit(-1);
                }
            }
        }

        // interpolate based on surrounding distances between units (median)
        for (int i = 0; i < umatH; i += 2) {
            for (int j = 0; j < umatW; j += 2) {
                if ((i == 0) && (j == 0)) { // upper left unit
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i + 1, j), umatrix.get(i, j + 1) }));
                } else if ((j == umatW - 1) && (i == 0)) { // upper right unit
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i, j - 1), umatrix.get(i + 1, j) }));
                } else if ((j == 0) && (i == umatH - 1)) { // lower left unit
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i, j + 1), umatrix.get(i - 1, j) }));
                } else if ((j == umatW - 1) && (i == umatH - 1)) { // lower right unit
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i, j - 1), umatrix.get(i - 1, j) }));
                } else if (j == 0) { // left border
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i - 1, j), umatrix.get(i + 1, j), umatrix.get(i, j + 1),
                            umatrix.get(i, j + 1) }));
                } else if (j == umatW - 1) { // right border
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i - 1, j), umatrix.get(i + 1, j), umatrix.get(i, j - 1),
                            umatrix.get(i, j - 1) }));
                } else if (i == 0) { // top border
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i, j - 1), umatrix.get(i, j + 1), umatrix.get(i + 1, j),
                            umatrix.get(i + 1, j) }));
                } else if (i == umatH - 1) { // bottom border
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i, j - 1), umatrix.get(i, j + 1), umatrix.get(i - 1, j),
                            umatrix.get(i - 1, j) }));
                } else { // middle unit
                    umatrix.set(i, j, VectorTools.median(new double[] { umatrix.get(i, j - 1), umatrix.get(i, j + 1), umatrix.get(i - 1, j),
                            umatrix.get(i + 1, j) }));
                }
            }
        }
        return umatrix;
    }

    public DoubleMatrix2D createPMatrix(GrowingSOM gsom) {
        // we store a cache of the PMatrix, as it is computationally expensive, and is be used for both P-Matrix and U*-Matrix
        if (!pMatrixCache.containsKey(gsom)) {
            // calculate distances from each unit to each other unit
            // FIXME: move this to Layer
            DoubleMatrix2D dissimilarity = gsom.getLayer().getUnitDistanceMatrix();

            int pmatW = gsom.getLayer().getXSize();
            int pmatH = gsom.getLayer().getYSize();
            DoubleMatrix2D pmatrix = DoubleFactory2D.dense.make(pmatW, pmatH, -1);

            // calculate the percentiles
            DoubleMatrix1D percentiles = createPercentiles(dissimilarity);

            // guess the paretoRadius
            double paretoRadius = calculateParetoRadius(dissimilarity, percentiles);
            for (int i = 0; i < pmatW; i++) {
                for (int j = 0; j < pmatH; j++) {
                    pmatrix.set(i, j, countDensity(i, j, paretoRadius, dissimilarity, gsom.getLayer().getXSize()));
                }
            }
            pMatrixCache.put(gsom, pmatrix);
        }

        return pMatrixCache.get(gsom);
    }

    private DoubleMatrix1D createPercentiles(DoubleMatrix2D distances) {
        DoubleMatrix1D[] rows = new DoubleMatrix1D[distances.rows()];
        for (int r = 0; r < rows.length; r++) {
            rows[r] = DoubleFactory1D.dense.make(distances.viewRow(r).viewPart(r + 1, rows.length - (r + 1)).toArray());
        }
        DoubleMatrix1D reducedValues = DoubleFactory1D.dense.make(rows);
        DoubleArrayList nonZeroValues = new DoubleArrayList();
        reducedValues.getNonZeros(new IntArrayList(), nonZeroValues);
        // double[] allValues = DoubleFactory1D.dense.make(rows).toArray();
        // calculate distance percentiles
        DoubleQuantileFinder finder = QuantileFinderFactory.newDoubleQuantileFinder(true, nonZeroValues.size(), 0.0, 0.0, 100, null);

        finder.addAllOf(nonZeroValues);

        double[] p = new double[100];

        for (int i = 0; i < p.length; i++) {
            p[i] = (double) (i + 1) / 100;
        }

        DoubleArrayList percentages = new DoubleArrayList(p);
        return new DenseDoubleMatrix1D(finder.quantileElements(percentages).elements());
    }

    private DoubleMatrix1D getAllDensities(DoubleMatrix2D distances, double radius) {
        DoubleArrayList list = new DoubleArrayList();
        for (int row = 0; row < distances.rows(); row++) {
            int counter = 0;
            // get only the right part of the matrix (its symmetric)
            for (int col = row; col < distances.columns(); col++) {
                double distance = distances.get(row, col);
                if (distance < radius) {
                    counter++;
                }
            }
            list.add(counter);
        }
        return DoubleFactory1D.dense.make(list);
    }

    private double calculateParetoRadius(DoubleMatrix2D distances, DoubleMatrix1D percentiles) {
        final double PARETO_SIZE = 0.2013;
        int percentile = 18;
        int last_percentile = percentile;
        double diff = 0.0;
        double last_diff = 1.0;
        double median_size;
        boolean stop = false;

        Logger log = Logger.getLogger("at.tuwien.ifs.somtoolbox");
        // do not go below 2% of above 50%
        double upper_percentile = 50;
        double lower_percentile = 2;

        // to kickstart interpolation search
        double upper_size = 1.0;
        double lower_size = 0.0;

        double radius;

        while (!stop) {
            // current radius
            radius = percentiles.getQuick(percentile);

            // current densities
            DoubleMatrix1D densities = getAllDensities(distances, radius);

            // median percentage of points in spheres
            if (densities.size() != 0) {
                double median = VectorTools.median(densities.toArray());
                double mean = densities.zSum() / densities.size();
                log.info("Mean: " + mean + " median: " + median);
                median_size = Math.max(median, mean) / (double) distances.columns();
            } else
                median_size = 0;
            // log.info("spheres for " + percentile + "%-tile contain on average "
            // + Math.round(median_size * 100) + "% of the data");

            // difference to optimum
            diff = (median_size) - PARETO_SIZE;

            // stop if last step was one or border reached
            stop = (Math.abs(percentile - last_percentile) == 1) || (percentile == upper_percentile) || (percentile == lower_percentile);

            if (!stop) {
                last_percentile = percentile;
                last_diff = diff;

                // adjust percentile towards optimum with linear interpolation
                if (diff > 0) {
                    upper_percentile = percentile;
                    upper_size = median_size;
                } else {
                    lower_percentile = percentile;
                    lower_size = median_size;
                }

                // estimated position of pareto size in current interval
                double pest = ((PARETO_SIZE - lower_size) / (upper_size - lower_size) * (upper_percentile - lower_percentile)) + lower_percentile;

                // always go at least 1
                double step = pest - percentile;

                if (step > 0)
                    step = Math.max(step, 1);
                else
                    step = Math.min(step, -1);

                percentile = percentile + (int) Math.round(step);
            } else {
                if (Math.abs(diff) > Math.abs(last_diff))
                    percentile = last_percentile;

            }
        }

        log.info(percentile + "%tile chosen.");

        return percentiles.getQuick(percentile);

    }

    private int countDensity(int elementX, int elementY, double radius, DoubleMatrix2D distance, int xSize) {
        int density = 0;

        // calculate the row/col index where we need to search
        int index = calculateIndex(elementX, elementY, xSize);
        for (int i = 0; i < distance.rows(); i++) {
            double dist = distance.get(i, index);
            if (dist != 0.0 && dist < radius) {
                density++;
            }
        }

        return density;

    }

    private int calculateIndex(int x, int y, int xSize) {
        return y * xSize + x;
    }

    public DoubleMatrix2D createUStarMatrix(GrowingSOM gsom) {
        DoubleMatrix2D umatrix = createUMatrix(gsom);
        DoubleMatrix2D pmatrix = createPMatrix(gsom);

        double meanP = pmatrix.zSum() / pmatrix.size();
        double maxP = pmatrix.aggregate(Functions.max, Functions.identity);

        int pmatW = gsom.getLayer().getXSize();
        int pmatH = gsom.getLayer().getYSize();
        DoubleMatrix2D ustarmatrix = DoubleFactory2D.dense.make(pmatH, pmatW, -1);

        for (int x = 0; x < pmatH; x++) {
            for (int y = 0; y < pmatW; y++) {
                double uheight = umatrix.getQuick(x * 2, y * 2);
                double pheight = pmatrix.getQuick(x, y);
                double scaleFactor = (pheight - meanP) / (meanP - maxP) + 1;
                ustarmatrix.setQuick(x, y, uheight * scaleFactor);
            }
        }

        return ustarmatrix;
    }
}

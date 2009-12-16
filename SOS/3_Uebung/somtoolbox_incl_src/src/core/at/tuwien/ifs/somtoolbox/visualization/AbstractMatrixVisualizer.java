package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.MathUtils;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import flanagan.interpolation.BiCubicSpline;

/**
 * @author Thomas Lidy
 * @author Rudolf Mayer
 * @version $Id: AbstractMatrixVisualizer.java 2882 2009-12-11 17:57:12Z mayer $
 */
public abstract class AbstractMatrixVisualizer extends AbstractBackgroundImageVisualizer implements MatrixVisualizer {

    protected int currentPalette;

    private boolean isReversed = false;

    /** The palette currentely used to create visualisation images. */
    protected Color[] palette = null;

    protected double minimumMatrixValue = -1;

    protected double maximumMatrixValue = -1;

    public double getMinimumMatrixValue() {
        return minimumMatrixValue;
    }

    public double getMaximumMatrixValue() {
        return maximumMatrixValue;
    }

    /** overriding the method in the superclass as we have a different cache key, and to set the min & max matrix values to -1 */
    public BufferedImage getVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        if (controlPanel != null) { // we don't always have this initialised, especially when we just create the visualisation w/o the viewer
            controlPanel.updateZDim(gsom.getLayer().getZSize());
        }
        String cacheKey = getCacheKey(gsom, index, width, height);
        logImageCache(cacheKey);
        if (cache.get(cacheKey) == null) {
            minimumMatrixValue = Double.MAX_VALUE;
            maximumMatrixValue = Double.MIN_VALUE;
            cache.put(cacheKey, createVisualization(index, gsom, width, height));
        }
        return cache.get(cacheKey);
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR
                + buildCacheKey("palette:" + currentPalette, "reversed:" + isReversed, "interpolate:" + interpolate);
    }

    protected void setInterpolate(boolean interpolate) {
        this.interpolate = interpolate;
        if (controlPanel != null) { // we don't always have this initialised, especially when we just create the visualisation w/o the viewer
            this.controlPanel.interpolateCheckbox.setSelected(interpolate);
        }
    }

    /**
     * Creates an image from a matrix of heights.
     * 
     * @param gsom The GrowingSOM to generate the image for
     * @param matrix The matrix with the calucalted heights.
     * @param width the desired width of the image, in pixels
     * @param height the desired height of the image, in pixels.
     * @param interpolate indicates whether the image should be interpolated if the widht or height exceeds the matrix dimensions.
     * @return the BufferedImage for those settings
     */
    protected BufferedImage createImage(GrowingSOM gsom, DoubleMatrix2D matrix, int width, int height, boolean interpolate) {
        /** drawing stuff * */
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) res.getGraphics();

        drawBackground(width, height, g);

        int unitWidth = width / gsom.getLayer().getXSize();
        int unitHeight = height / gsom.getLayer().getYSize();

        if (interpolate) {
            DoubleMatrix2D matrixBorders = new DenseDoubleMatrix2D(matrix.rows() + 2, matrix.columns() + 2);
            matrixBorders.viewPart(1, 1, matrix.rows(), matrix.columns()).assign(matrix);
            matrixBorders.viewRow(0).assign(matrixBorders.viewRow(1));
            matrixBorders.viewRow(matrixBorders.rows() - 1).assign(matrixBorders.viewRow(matrixBorders.rows() - 2));
            matrixBorders.viewColumn(0).assign(matrixBorders.viewColumn(1));
            matrixBorders.viewColumn(matrixBorders.columns() - 1).assign(matrixBorders.viewColumn(matrixBorders.columns() - 2));

            /** start bicubic spline stuff * */
            // create support points
            double factorX = (double) (matrixBorders.columns() - 2) / (double) gsom.getLayer().getXSize();
            double factorY = (double) (matrixBorders.rows() - 2) / (double) gsom.getLayer().getYSize();
            double[] x1 = new double[matrixBorders.columns()];
            x1[0] = 0;
            for (int x = 0; x < matrixBorders.columns() - 2; x++) {
                x1[x + 1] = ((x * unitWidth / (factorX)) + (unitWidth / (2 * factorX)));
            }
            x1[matrixBorders.columns() - 1] = width;
            double[] x2 = new double[matrixBorders.rows()];
            x2[0] = 0;
            for (int y = 0; y < matrixBorders.rows() - 2; y++) {
                x2[y + 1] = ((y * unitHeight / (factorY)) + (unitHeight / (2 * factorY)));
            }
            x2[matrixBorders.rows() - 1] = height;

            BiCubicSpline bcs = new BiCubicSpline(x2, x1, matrixBorders.toArray());
            // bcs.calcDeriv();

            int ci = 0;
            int stepSize = Math.max(5000, (height * width) / 500);
            StdErrProgressWriter progress = new StdErrProgressWriter(height * width, "Creating interpolated matrix image, pixel ", stepSize);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // adapted to mnemonic (sparse) SOMs
                    try {
                        if (gsom.getLayer().getUnit((int) (x / unitWidth), (int) (y / unitHeight)) != null) {
                            ci = (int) Math.round(bcs.interpolate((double) y, (double) x) * (double) (palette.length - 1));
                            g.setPaint(palette[constrainWithinPalette(ci)]);
                            g.fill(new Rectangle(x, y, 1, 1));
                            if (ci < minimumMatrixValue) {
                                minimumMatrixValue = ci;
                            }
                            if (ci > maximumMatrixValue) {
                                maximumMatrixValue = ci;
                            }
                        } else { // we show an empty (white) unit if this unit is not part of the mnemonic map
                            g.setPaint(Color.WHITE);
                            g.fill(new Rectangle(x, y, 1, 1));
                        }
                        progress.progress();
                    } catch (LayerAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            /** end bicubic spline stuff * */
        } else {
            double factorX = (double) (matrix.columns()) / (double) gsom.getLayer().getXSize();
            double factorY = (double) (matrix.rows()) / (double) gsom.getLayer().getYSize();
            g.setColor(null);
            int ci = 0;
            int xOff = 0;
            int yOff = 0;
            if ((factorX != 1) && (factorY != 1)) {
                xOff = (int) Math.round(unitWidth / (factorX * 2));
                yOff = (int) Math.round(unitHeight / (factorY * 2));
            }
            for (int y = 0; y < matrix.rows(); y++) {
                for (int x = 0; x < matrix.columns(); x++) {
                    ci = (int) Math.round(matrix.get(y, x) * (double) (palette.length - 1));
                    g.setPaint(palette[ci]);
                    g.fill(new Rectangle(xOff + (x * (int) Math.round(unitWidth / factorX)), yOff + (y * (int) Math.round(unitHeight / factorY)),
                            (int) Math.round(unitWidth / factorX), (int) Math.round(unitHeight / factorY)));
                    if (ci < minimumMatrixValue) {
                        minimumMatrixValue = ci;
                    }
                    if (ci > maximumMatrixValue) {
                        maximumMatrixValue = ci;
                    }
                }
            }

            if ((factorX != 1) && (factorY != 1)) { // border
                ci = (int) Math.round(matrix.get(0, 0) * (double) (palette.length - 1)); // top-left
                g.fill(new Rectangle(0, 0, (int) Math.round(unitWidth / (factorX) * 2), (int) Math.round(unitHeight / (factorY * 2))));
                ci = (int) Math.round(matrix.get(0, matrix.columns() - 1) * (double) (palette.length - 1)); // top-right
                g.fill(new Rectangle(xOff + (matrix.columns() * (int) Math.round(unitWidth / factorX)), 0,
                        (int) Math.round(unitWidth / (factorX * 2)), (int) Math.round(unitHeight / (factorY * 2))));
                ci = (int) Math.round(matrix.get(matrix.rows() - 1, 0) * (double) (palette.length - 1)); // bottom-left
                g.fill(new Rectangle(0, yOff + (matrix.rows() * (int) Math.round(unitHeight / factorY)), (int) Math.round(unitWidth / (factorX * 2)),
                        (int) Math.round(unitHeight / (factorY * 2))));
                ci = (int) Math.round(matrix.get(matrix.rows() - 1, matrix.columns() - 1) * (double) (palette.length - 1)); // bottom-right
                g.fill(new Rectangle(xOff + (matrix.columns() * (int) Math.round(unitWidth / factorX)), yOff
                        + (matrix.rows() * (int) Math.round(unitHeight / factorY)), (int) Math.round(unitWidth / (factorX * 2)),
                        (int) Math.round(unitHeight / (factorY * 2))));
                for (int x = 0; x < matrix.columns(); x++) {
                    // top border
                    ci = (int) Math.round(matrix.get(0, x) * (double) (palette.length - 1));
                    g.setPaint(palette[ci]);
                    g.fill(new Rectangle(xOff + (x * (int) Math.round(unitWidth / factorX)), 0, (int) Math.round(unitWidth / factorX),
                            (int) Math.round(unitHeight / (factorY * 2))));
                    // bottom border
                    ci = (int) Math.round(matrix.get(matrix.rows() - 1, x) * (double) (palette.length - 1));
                    g.setPaint(palette[ci]);
                    g.fill(new Rectangle(xOff + (x * (int) Math.round(unitWidth / factorX)), (yOff + (matrix.rows())
                            * (int) Math.round(unitHeight / factorY)), (int) Math.round(unitWidth / factorX), (int) Math.round(unitHeight
                            / (factorY * 2))));
                }
                for (int y = 0; y < matrix.rows(); y++) {
                    // left border
                    ci = (int) Math.round(matrix.get(y, 0) * (double) (palette.length - 1));
                    g.setPaint(palette[ci]);
                    g.fill(new Rectangle(0, yOff + (y * (int) Math.round(unitHeight / factorY)), (int) Math.round(unitWidth / (factorX * 2)),
                            (int) Math.round(unitHeight / factorY)));
                    // right border
                    ci = (int) Math.round(matrix.get(y, matrix.columns() - 1) * (double) (palette.length - 1));
                    g.setPaint(palette[ci]);
                    g.fill(new Rectangle((xOff + (matrix.columns()) * (int) Math.round(unitWidth / factorX)), yOff
                            + (y * (int) Math.round(unitHeight / factorY)), (int) Math.round(unitWidth / (factorX * 2)), (int) Math.round(unitHeight
                            / factorY)));
                }
            }
        }
        return res;
    }

    protected int constrainWithinPalette(int ci) {
        return MathUtils.constrainWithin(ci, 0, palette.length - 1);
    }

    public Color[] getPalette() {
        return palette;
    }

    public void setPalette(int newPaletteInd, Palette newPalette) {
        currentPalette = newPaletteInd;
        this.palette = newPalette.getColors();
    }

    public void reversePalette() {
        if (palette != null) {
            isReversed = !isReversed;
            Color[] newPalette = new Color[palette.length];
            for (int i = 0; i < palette.length; i++) {
                newPalette[i] = palette[palette.length - 1 - i];
            }
            palette = newPalette;
        }
    }

    public boolean isReversed() {
        return isReversed;
    }

    /** Deletes all cached elements that use the {@link Palette} with the given index. */
    public void invalidateCache(int palette) {
        for (String key : cache.keySet()) {
            if (key.contains("palette:" + palette)) {
                cache.remove(key);
                System.out.println("Removed cache for: " + key);
            }
        }
    }

}

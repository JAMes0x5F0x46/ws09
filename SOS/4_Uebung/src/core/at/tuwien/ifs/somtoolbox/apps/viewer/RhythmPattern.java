package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import at.tuwien.ifs.somtoolbox.data.DataDimensionException;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Creates a visualization of Rhythm Pattern feature vectors.
 * 
 * @author Thomas Lidy
 * @version $Id: RhythmPattern.java 2874 2009-12-11 16:03:27Z frank $
 */
public class RhythmPattern {

    private int xdim = 0; // matrix dimensions

    private int ydim = 0;

    private double[][] mat; // the vector in matrix form

    private double minv = 0; // min. value of matrix

    private double maxv = 0; // max. value of matrix

    private static final int DEFAULT_bx = 5; // default graphics block size in pixel (x)

    private static final int DEFAULT_by = 5; // default graphics block size in pixel (y)

    public int bx = DEFAULT_bx; // graphics block size in pixel (x)

    public int by = DEFAULT_by; // graphics block size in pixel (y)

    private Color[] palette;

    /**
     * This constructor uses pre-defined hard-coded values; it is not recommended to use this constructor it is intended for backward compatibility
     * for RP feature vector files without the $DATA_TYPE header
     * 
     * @throws DataDimensionException
     */
    public RhythmPattern(double[] vec) throws DataDimensionException {
        this(vec, 60, 24);
    }

    /**
     * this constructor uses default graphics block size
     * 
     * @param vec feature vector given as double[] array (as used for weight vectors)
     * @param xdim the RP matrix dimension (x)
     * @param ydim the RP matrix dimension (y)
     * @throws DataDimensionException
     */
    public RhythmPattern(double[] vec, int xdim, int ydim) throws DataDimensionException {
        this(vec, xdim, ydim, DEFAULT_bx, DEFAULT_by);
    }

    /**
     * @param vec feature vector given as double[] array (as used for weight vectors)
     * @param xdim the RP matrix dimension (x)
     * @param ydim the RP matrix dimension (y)
     * @param blocksize_x the blocksize for a RP graphics data point (x)
     * @param blocksize_y the blocksize for a RP graphics data point (y)
     * @throws DataDimensionException
     */
    public RhythmPattern(double[] vec, int xdim, int ydim, int blocksize_x, int blocksize_y) throws DataDimensionException {
        this.xdim = xdim;
        this.ydim = ydim;
        this.bx = blocksize_x;
        this.by = blocksize_y;

        if ((xdim * ydim) != vec.length) {
            throw new DataDimensionException("Dimensions " + ydim + "x" + xdim + " in Rhythm Pattern do not agree with vector size " + vec.length
                    + "!");
        }

        int ind;
        minv = Double.POSITIVE_INFINITY;
        maxv = Double.NEGATIVE_INFINITY;

        mat = new double[xdim][ydim];

        for (int i = 0; i < xdim; i++) {
            for (int j = 0; j < ydim; j++) {
                ind = i * ydim + j;
                mat[i][j] = vec[ind];
                if (vec[ind] < minv) {
                    minv = vec[ind]; // determine minimum value
                }
                if (vec[ind] > maxv) {
                    maxv = vec[ind]; // determine maximum value
                }
            }
        }

        initPaint();
    }

    /**
     * This constructor uses pre-defined hard-coded values; it is not recommended to use this constructor it is intended for backward compatibility
     * for RP feature vector files without the $DATA_TYPE header
     * 
     * @throws DataDimensionException
     */
    public RhythmPattern(DoubleMatrix1D vec) throws DataDimensionException {
        this(vec, 60, 24);
    }

    /**
     * constructor with feature vector given as DoubleMatrix1D (as provided by class SOMLibSparseInputData)
     * 
     * @throws DataDimensionException
     */
    public RhythmPattern(DoubleMatrix1D vec, int xdim, int ydim) throws DataDimensionException {
        this(vec, xdim, ydim, DEFAULT_bx, DEFAULT_by);
    }

    /**
     * @param vec feature vector given as DoubleMatrix1D (as provided by class SOMLibSparseInputData)
     * @param xdim the RP matrix dimension (x)
     * @param ydim the RP matrix dimension (y)
     * @param blocksize_x the blocksize for a RP graphics data point (x)
     * @param blocksize_y the blocksize for a RP graphics data point (y)
     * @throws DataDimensionException
     */
    public RhythmPattern(DoubleMatrix1D vec, int xdim, int ydim, int blocksize_x, int blocksize_y) throws DataDimensionException {
        this.xdim = xdim;
        this.ydim = ydim;
        this.bx = blocksize_x;
        this.by = blocksize_y;

        if ((xdim * ydim) != vec.size()) {
            throw new DataDimensionException("Dimensions " + ydim + "x" + xdim + " in Rhythm Pattern do not agree with vector size " + vec.size()
                    + "!");
        }

        // vec.toArray();
        DoubleMatrix1D vecsorted = vec.viewSorted();
        minv = vecsorted.get(0);
        maxv = vecsorted.get(vec.size() - 1);
        // System.out.println("min: " + Double.toString(minv));
        // System.out.println("max: " + Double.toString(maxv));

        mat = new double[xdim][ydim];

        for (int i = 0; i < xdim; i++) {
            mat[i] = vec.viewPart(ydim * i, ydim).toArray(); // read 24 bands for 1 mod.freq en bloc
        }

        initPaint();
    }

    private void initPaint() {
        palette = initMatlabPalette();
    }

    /**
     * @return x dimension of RP matrix
     */
    public int getXdim() {
        return xdim;
    }

    /**
     * @return y dimension of RP matrix
     */
    public int getYdim() {
        return ydim;
    }

    /**
     * @return size of RhythmPattern in pixels
     */
    public Dimension getSize() {
        return new Dimension(xdim * bx, ydim * by);
    }

    /**
     * @return RP matrix as double[][] array
     */
    public double[][] getMatrix() {
        return mat;
    }

    /**
     * @return a RhythmPattern as a BufferedImage
     */
    public BufferedImage getImage() {
        BufferedImage img = new BufferedImage(xdim * bx, ydim * by, BufferedImage.TYPE_INT_RGB);
        paint(img.createGraphics());
        return img;
    }

    /**
     * paints on Graphics object provided (either by local method or by a Dialog window)
     */
    public void paint(Graphics g) {
        int xpos, ypos;
        int colind;
        int paldim = palette.length - 1;

        if (maxv == 0.0) {
            return; // would cause div/0 //TODO throw exception
        }

        xpos = 0;
        ypos = (ydim - 1) * by;

        for (int j = 0; j < ydim; j++) {
            for (int i = 0; i < xdim; i++) {

                // System.out.print(mat[i][j] + " ");
                colind = (int) (((mat[i][j] - minv) / maxv) * (double) paldim);
                g.setColor(palette[colind]);
                g.fillRect(xpos, ypos, bx, by);
                xpos += bx;
            }
            // System.out.println();
            ypos -= by;
            xpos = 0;
        }
    }

    /**
     * this palette is the MATLAB jet colormap it consists of 6 fixed color values which are interpolated through 64 values
     */
    private Color[] initMatlabPalette() {
        int i, r, g, b;
        Color[] palette = new Color[64];

        for (i = 0; i < 8; i++) {
            palette[i] = new Color(0, 0, 143 + i * 16);
        }
        g = 0;
        for (i = 8; i < 23; i++) {
            g = g + 16;
            palette[i] = new Color(0, g, 255);
        }
        r = 0;
        b = 255;
        for (i = 23; i < 39; i++) {

            palette[i] = new Color(r, 255, b);
            r = r + 16;
            b = b - 16;
        }
        g = 255;
        for (i = 39; i < 55; i++) {

            palette[i] = new Color(255, g, 0);
            g = g - 16;
        }
        r = 255;
        for (i = 55; i < 63; i++) {
            palette[i] = new Color(r, 0, 0);
            r = r - 16;
        }
        palette[63] = new Color(128, 0, 0);

        return palette;
    }

}

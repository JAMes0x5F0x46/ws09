package at.tuwien.ifs.somtoolbox.util;

import org.math.array.DoubleArray;
import org.math.array.StatisticSample;

/**
 * Principal Component Analysis, based on <a
 * href="http://jmathtools.sourceforge.net/doku.php?id=cases:pca">http://jmathtools.sourceforge.net/doku.php?id=cases:pca</a>
 * 
 * @author Yann RICHET
 * @version $Id: PCA.java 2874 2009-12-11 16:03:27Z frank $
 */
public class PCA {

    double[][] X; // initial datas : lines = events and columns = variables

    double[] meanX, stdevX;

    double[][] Z; // X centered reduced

    double[][] cov; // Z covariance matrix

    public double[][] U; // projection matrix

    public double[] info; // information matrix

    public PCA(double[][] X) {
        this.X = X;

        stdevX = StatisticSample.stddeviation(X);
        meanX = StatisticSample.mean(X);

        Z = centerReduce(X);

        cov = StatisticSample.covariance(Z);

        Jama.EigenvalueDecomposition e = org.math.array.LinearAlgebra.eigen(cov);
        U = DoubleArray.transpose(e.getV().getArray());
        info = e.getRealEigenvalues(); // covariance matrix is symetric, so only real eigenvalues...
    }

    // normalization of x relatively to X mean and standard deviation
    public double[][] centerReduce(double[][] x) {
        double[][] y = new double[x.length][x[0].length];
        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < y[i].length; j++) {
                y[i][j] = (x[i][j] - meanX[j]) / stdevX[j];
            }
        }
        return y;
    }

    // de-normalization of y relatively to X mean and standard deviation
    public double[] invCenterReduce(double[] y) {
        return invCenterReduce(new double[][] { y })[0];
    }

    // de-normalization of y relatively to X mean and standard deviation
    public double[][] invCenterReduce(double[][] y) {
        double[][] x = new double[y.length][y[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                x[i][j] = (y[i][j] * stdevX[j]) + meanX[j];
            }
        }
        return x;
    }

}
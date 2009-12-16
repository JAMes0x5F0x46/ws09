package at.tuwien.ifs.somtoolbox.visualization.clustering;

import org.apache.commons.lang.ArrayUtils;

public class Covariance {

    public static double[][] covariance(double[][] v1){
        return covariance(v1, v1);
    }
    
    /**
     * Copied from jMathtools sample code.
     * @param v1
     * @param v2
     * @return
     */
    public static double[][] covariance(double[][] v1, double[][] v2) {
        int m = v1.length;
        int n1 = v1[0].length;
        int n2 = v2[0].length;
        double[][] X = new double[n1][n2];
        int degrees = (m - 1);
        double c;
        double s1;
        double s2;
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                c = 0;
                s1 = 0;
                s2 = 0;
                for (int k = 0; k < m; k++) {
                    s1 += v1[k][i];
                    s2 += v2[k][j];
                }
                s1 = s1 / m;
                s2 = s2 / m;
                for (int k = 0; k < m; k++)
                    c += (v1[k][i] - s1) * (v2[k][j] - s2);
                X[i][j] = c / degrees;
            }
        }
        return X;
    }
    
    public static void main(String args[]){

        double[] x = {4.0000, 4.2000, 3.9000, 4.3000, 4.1000};
        double[] y = {2.0000, 2.1000, 2.0000, 2.1000, 2.2000};

        double sum = 0;
        for (int i = 0; i < y.length; i++) {
            sum += x[i] * y[i];

        }
        System.out.println("Sum: " + sum);

        double[][] o = new double[2][];
        o[0] = x;
        o[1] = y;

        double[][] covar = covariance(o);
        for (int i = 0; i < covar.length; i++) {
            System.out.println(ArrayUtils.toString(covar[i]));
        }

    }

}

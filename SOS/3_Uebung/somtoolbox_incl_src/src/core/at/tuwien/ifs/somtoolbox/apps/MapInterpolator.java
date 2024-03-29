package at.tuwien.ifs.somtoolbox.apps;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Interpolates a trained SOM to a higher resolution of units.<br>
 * TODO: the algorithm is very coarse, maybe implement <b>Interpolating self-organising map (iSOM) - Electronics Letters</b>:
 * http://ieeexplore.ieee.org/iel5/2220/17454/00807048.pdf?arnumber=807048.
 * 
 * @author Rudolf Mayer
 * @version $Id: MapInterpolator.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MapInterpolator {
    /**
     * Method for stand-alone execution of the map interpolater. Options are:<br>
     * <ul>
     * <li>-v vectorFileName (data), mand.</li>
     * <li> -d data names file, mandatory</li>
     * <li> -u unit description file, mandatory</li>
     * <li> htmlName name of output HTML file, mandatory</li>
     * </ul>
     * 
     * @param args the execution arguments as stated above.
     * @throws SOMLibFileFormatException
     * @throws IOException
     */
    public static void main(String[] args) throws SOMLibFileFormatException, IOException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_MAP_INTERPOLATOR);

        String weightVectorFileName = config.getString("weightVectorFile");
        String outputFileName = config.getString("output");
        File outputFile = new File(outputFileName);
        String outputParentDir = outputFile.getParent();
        if (outputParentDir == null) {
            outputParentDir = ".";
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Started map interpolator, writing to: " + outputParentDir + File.separator + outputFile.getName());

        int xDim = config.getInt("x-Dimension");
        int yDim = config.getInt("y-Dimension");
        int zDim = 1;
        try {
            zDim = config.getInt("z-Dimension");
        } catch (Exception e) {
            
        }

        SOMLibFormatInputReader inputreader = new SOMLibFormatInputReader(weightVectorFileName, null, null);
        double[][][][] originalVectors = inputreader.getVectors();
        int originalXdim = originalVectors.length;
        int originalYdim = originalVectors[0].length;
        int originalZdim = originalVectors[0][0].length;
        int vecDim = originalVectors[0][0][0].length;

        double relX = (double) originalXdim / (double) xDim;
        double relY = (double) originalYdim / (double) yDim;
        double relZ = (double) originalZdim / (double) zDim;

        double[][][][] vectors = new double[xDim][yDim][zDim][vecDim];
        for (int h = 0; h < xDim; h++) {
            for (int i = 0; i < yDim; i++) {
                for (int j = 0; j < zDim; j++) {
                    int startX = new BigDecimal(h * relX).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    int endX = new BigDecimal((h + 1) * relX).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    int startY = new BigDecimal(i * relY).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    int endY = new BigDecimal((i + 1) * relY).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    int startZ = new BigDecimal(j * relZ).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    int endZ = new BigDecimal((j + 1) * relZ).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    ArrayList<double[]> allVecs = new ArrayList<double[]>();
                    for (int k = startX; k < endX; k++) {
                        for (int l = startY; l < endY; l++) {
                            for (int m = startZ; m < endZ; m++) {
                                allVecs.add(originalVectors[k][l][m]);
                        }}
                    }
                    double[][] a = (double[][]) allVecs.toArray(new double[allVecs.size()][]);
                    double[] meanVector = VectorTools.meanVector(a);
                    vectors[h][i][j] = meanVector;
                }}
        }
        SOMLibMapOutputter.writeWeightVectorFile(vectors, outputParentDir, outputFile.getName(), true);
        SOMLibMapOutputter.writeUnitDescriptionFile(xDim, yDim, outputParentDir, outputFile.getName(), true);
    }

}

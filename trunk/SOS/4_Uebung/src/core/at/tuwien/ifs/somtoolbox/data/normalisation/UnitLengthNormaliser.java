package at.tuwien.ifs.somtoolbox.data.normalisation;

import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

/**
 * Unit length normalisation, normalises the length of the instance to 1.
 * 
 * @author Rudolf Mayer
 * @version $Id: UnitLengthNormaliser.java 2874 2009-12-11 16:03:27Z frank $
 */
public class UnitLengthNormaliser extends AbstractNormaliser {

    @Override
    protected void processLine(int index, String[] lineElements) throws Exception {
        double[] vector = new double[dim];
        for (int ve = 0; ve < dim; ve++) {
            double value = Double.parseDouble(lineElements[ve]);
            setMatrixValue(index, ve, value);
            vector[ve] = value;
        }
        vector = VectorTools.normaliseVectorToUnitLength(vector);
        for (int ve = 0; ve < dim; ve++) {
            if (vector[ve] == 0) {
                writer.write("0 ");
            } else {
                writer.write(StringUtils.format15FractionDigits.format(vector[ve]) + " ");
            }
        }
        writer.write(lineElements[dim]);
        writer.newLine();
    }

    @Override
    public void preReading() {
        // we will do everything during reading
    }

    @Override
    public void postReading() {
        // we did everything during reading already
    }

}

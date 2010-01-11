package at.tuwien.ifs.somtoolbox.data.normalisation;

import java.io.IOException;

import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * Min-max normalisation, normalises the attributes between 0 and 1.
 * 
 * @author Rudolf Mayer
 * @version $Id: MinMaxNormaliser.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MinMaxNormaliser extends AbstractNormaliser {

    double[] minValues;

    double[] maxValues;

    @Override
    public void preReading() {
        // in min-max data structure
        minValues = new double[dim];
        maxValues = new double[dim];
        for (int i = 0; i < minValues.length; i++) {
            minValues[i] = Double.MAX_VALUE;
            maxValues[i] = Double.MIN_VALUE;
        }
    }

    @Override
    public void postReading() throws IOException {
        // normalise and write the data
        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                double v = data.getQuick(i, j);
                v = (v - minValues[j]) / (maxValues[j] - minValues[j]);
                if (v == 0) {
                    writer.write("0 ");
                } else {
                    writer.write(StringUtils.format15FractionDigits.format(v) + " ");
                }
            }
            writer.write(dataNames[i]);
            writer.newLine();
        }
    }

    @Override
    protected void processLine(int index, String[] lineElements) throws Exception {
        for (int ve = 0; ve < dim; ve++) {
            double value = Double.parseDouble(lineElements[ve]);
            setMatrixValue(index, ve, value);
            if (value < minValues[ve]) {
                minValues[ve] = value;
            }
            if (value > maxValues[ve]) {
                maxValues[ve] = value;
            }
        }
        addInstance(index, lineElements[dim]);
    }
}

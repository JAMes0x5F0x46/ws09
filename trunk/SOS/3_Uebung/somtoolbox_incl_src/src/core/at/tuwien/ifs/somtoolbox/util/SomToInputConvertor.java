package at.tuwien.ifs.somtoolbox.util;

import java.io.File;
import java.io.IOException;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.Unit;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Converts a SOM map to an input vector file.
 * 
 * @author Rudolf Mayer
 * @version $Id: SomToInputConvertor.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SomToInputConvertor {
    public static void main(String[] args) throws SOMLibFileFormatException, IOException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptWeightVectorFile(true), OptionFactory.getOptOutputFileName(true));
        final String weightVectorFile = config.getString("weightVectorFile");
        final String outFile = config.getString("output");
        SOMLibFormatInputReader ir = new SOMLibFormatInputReader(weightVectorFile, null, null);
        GrowingLayer layer = new GrowingLayer(ir.getXSize(), ir.getYSize(), ir.getZSize(), ir.getMetricName(), ir.getDim(), false, false, 7, null);
        final Unit[] allUnits = layer.getAllUnits();
        InputDatum[] inputData = new InputDatum[allUnits.length];
        String name;
        if (weightVectorFile.contains(File.separator)) {
            name = weightVectorFile.substring(weightVectorFile.lastIndexOf(File.separator) + 1);
        } else {
            name = weightVectorFile;
        }
        name = name.replace(".wgt", "");
        for (int i = 0; i < allUnits.length; i++) {

            inputData[i] = new InputDatum(name + "_" + allUnits[i].printCoordinates(), allUnits[i].getWeightVector());
        }
        SOMLibSparseInputData data = new SOMLibSparseInputData(inputData, null);
        data.writeToFile(outFile + ".vec");
    }
}

package at.tuwien.ifs.somtoolbox.apps;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.models.NetworkModel;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Wrapper for the individual Quality Measures.
 * 
 * @author Gerd Platzgummer
 * @version $Id: QualityMeasureComputer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class QualityMeasureComputer implements NetworkModel , SOMToolboxApp {

    public static void main(String[] args) throws FileNotFoundException, SOMLibFileFormatException {
        GrowingSOM gsom = null;
        InputData data = null;

        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_QUALITY_MEASURE_COMPUTER);

        String weightFileName = config.getString("weightVectorFile", null);
        String mapDescFileName = config.getString("mapDescriptionFile", null);
        String unitDescFileName = config.getString("unitDescriptionFile", null);
        String inputVectorFileName = config.getString("inputVectorFile", null);
        String dataWinnerMappingFile = config.getString("dataWinnerMappingFile", null);
        // String templateFileName = config.getString("templateVectorFile", null);
        String qualityMeasureClass = config.getString("qualityMeasureClass", null);
        String qualityMeasureVariant = config.getString("qualityMeasureVariant", null);
        String k = config.getString("k", "5");
        String outputfile = config.getString("o", null);

        SOMLibDataWinnerMapping dataWinnerMapping = null;
        if (dataWinnerMappingFile != null) {
            dataWinnerMapping = new SOMLibDataWinnerMapping(dataWinnerMappingFile);
        }
        data = SOMLibSparseInputData.create(inputVectorFileName);

        try {
            gsom = new GrowingSOM(new SOMLibFormatInputReader(weightFileName, unitDescFileName, mapDescFileName));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
            System.exit(-1);
        }

        // BEARBEITUNG DER EINGELESENEN ARGUMENTE
        try {
            GrowingLayer layer = gsom.getLayer();
            /*
             * Quality measeures____________________________________________________________________________________
             */
            QualityMeasure inzrqm = null;
            at.tuwien.ifs.somtoolbox.layers.quality.TopographicFunction q5_tf = null;

            if (qualityMeasureClass.equals("q_te")) /* Topographic Error */
                inzrqm = new at.tuwien.ifs.somtoolbox.layers.quality.TopographicError(layer, data);

            else if (qualityMeasureClass.equals("q_qe")) /* Quantization Error */
                inzrqm = new at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError(layer, data);

            else if (qualityMeasureClass.equals("q_id")) /* Intrinsic Distance */
                inzrqm = new at.tuwien.ifs.somtoolbox.layers.quality.IntrinsicDistance(layer, data);

            else if (qualityMeasureClass.equals("q_tp")) /* Topographic Product */
                inzrqm = new at.tuwien.ifs.somtoolbox.layers.quality.TopographicProduct(layer, data);

            else if (qualityMeasureClass.equals("q_tf"))/* Topographic Function */
                q5_tf = new at.tuwien.ifs.somtoolbox.layers.quality.TopographicFunction(layer, data);

            else if (qualityMeasureClass.equals("q_tw")) /* Trustworthiness */
                inzrqm = new at.tuwien.ifs.somtoolbox.layers.quality.Trustworthiness_NeighborhoodPreservation(layer, data);

            else if (qualityMeasureClass.equals("q_np")) /* Neighborhood preservation */
                inzrqm = new at.tuwien.ifs.somtoolbox.layers.quality.Trustworthiness_NeighborhoodPreservation(layer, data);

            else if (qualityMeasureClass.equals("q_dist")) /* SOM Distortion Measure */
                inzrqm = new at.tuwien.ifs.somtoolbox.layers.quality.SOMDistortion(layer, data);

            // else if ()
            else
                throw new Exception("Quality measure class " + qualityMeasureClass + " is unknown.");
            /*
             * Variants of the measures____________________________________________________________________________________
             */
            /*
             * @ 1. Topographic Error______________________
             */
            if (qualityMeasureVariant.equals("TE_Unit")) {
                printDoubles(inzrqm.getUnitQualities(qualityMeasureVariant), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("TE8_Unit")) {
                printDoubles(inzrqm.getUnitQualities(qualityMeasureVariant), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("TE_Map")) {
                printDouble(inzrqm.getMapQuality(qualityMeasureVariant), outputfile);
            } else if (qualityMeasureVariant.equals("TE8_Map")) {
                printDouble(inzrqm.getMapQuality(qualityMeasureVariant), outputfile);
            }
            /*
             * @ 2. Quantization Error_____________________
             */
            else if (qualityMeasureVariant.equals("QE_Unit")) {
                printDoubles(inzrqm.getUnitQualities("qe"), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("MQE_Unit")) {
                printDoubles(inzrqm.getUnitQualities("mqe"), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("QE_Map")) {
                printDouble(inzrqm.getMapQuality("mqe"), outputfile);
            } else if (qualityMeasureVariant.equals("MQE_Map")) {
                printDouble(inzrqm.getMapQuality("mmqe"), outputfile);
            }

            /*
             * @ 3. Intrinsic Distance_____________________
             */
            else if (qualityMeasureVariant.equals("ID_Unit")) {
                printDoubles(inzrqm.getUnitQualities(qualityMeasureVariant), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("ID_Map")) {
                printDouble(inzrqm.getMapQuality(qualityMeasureVariant), outputfile);
            }

            /*
             * @ 4. Topographic Product_____________________
             */
            else if (qualityMeasureVariant.equals("TP_Unit")) {
                String trickydick = qualityMeasureVariant + "|" + k;
                printDoubles(inzrqm.getUnitQualities(trickydick), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("TP_Map")) {
                printDouble(inzrqm.getMapQuality(qualityMeasureVariant), outputfile);
            }

            /*
             * @ 5. Topographic Funtion_____________________
             */
            else if (qualityMeasureClass.equals("q_tf")) {
                printFunctionValues(q5_tf.getFunctionValues(Integer.parseInt(k)), Integer.parseInt(k) * 2 + 1, outputfile);
            }

            /*
             * @ 6. Trustworthiness and 7. Neighborhood Preservation_____________________
             */
            else if (qualityMeasureVariant.equals("TW_Unit")) {
                String trickydick = qualityMeasureVariant + "|" + k;
                printDoubles(inzrqm.getUnitQualities(trickydick), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("TW_Map")) {
                String trickydick = qualityMeasureVariant + "|" + k;
                printDouble(inzrqm.getMapQuality(trickydick), outputfile);
            }

            else if (qualityMeasureVariant.equals("NP_Unit")) {
                String trickydick = qualityMeasureVariant + "|" + k;
                printDoubles(inzrqm.getUnitQualities(trickydick), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("NP_Map")) {
                String trickydick = qualityMeasureVariant + "|" + k;
                printDouble(inzrqm.getMapQuality(trickydick), outputfile);
            }

            /*
             * @ 8. SOM Distortion Measure_____________________
             */
            else if (qualityMeasureVariant.equals("Dist_UnitTotal")) {
                printDoubles(inzrqm.getUnitQualities(qualityMeasureVariant), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("Dist_UnitAvg")) {
                printDoubles(inzrqm.getUnitQualities(qualityMeasureVariant), layer.getXSize(), layer.getYSize(), outputfile);
            } else if (qualityMeasureVariant.equals("Dist_Map")) {
                printDouble(inzrqm.getMapQuality(qualityMeasureVariant), outputfile);
            }

        } catch (Exception ex) {
            System.out.println("Exception:" + ex.getMessage() + "\n\n");
            ex.printStackTrace();
        }
    }

    public static void printDouble(double dvalue, String outputfile) {
        try {
            BufferedWriter bw = null;
            if (outputfile != null)
                bw = new BufferedWriter(new FileWriter(outputfile)); /* Print output in File */
            System.out.print(dvalue);

            if (outputfile != null)
                bw.write(dvalue + "");
            System.out.println();
            if (outputfile != null)
                bw.newLine();
            if (outputfile != null)
                bw.close();
        } catch (Exception ex) {
            System.out.println("Output error: " + ex.getMessage());
        }

    }

    public static void printDoubles(double[][] values, int xsize, int ysize, String outputfile) {
        try {
            BufferedWriter bw = null;
            if (outputfile != null)
                bw = new BufferedWriter(new FileWriter(outputfile)); /* Print output in File */

            for (int y = 0; y < ysize; y++) {
                for (int x = 0; x < xsize; x++) {
                    System.out.print(values[x][y]);
                    System.out.print(" ");
                    if (outputfile != null)
                        bw.write(values[x][y] + (x == xsize - 1 ? "" : " "));
                }
                System.out.println();
                if (outputfile != null)
                    bw.newLine();
            }
            if (outputfile != null)
                bw.close();
        } catch (Exception ex) {
            System.out.println("Output error: " + ex.getMessage());
        }
    }

    public static void printFunctionValues(double[] values, int length, String outputfile) {
        try {
            BufferedWriter bw = null;
            if (outputfile != null)
                bw = new BufferedWriter(new FileWriter(outputfile)); /* Print output in File */
            for (int i = 0; i < length; i++) {
                System.out.print(i - (length - 1) / 2);
                System.out.print(": ");
                System.out.print(values[i]);
                System.out.print("\n");
                if (outputfile != null)
                    bw.write(values[i] + (i == length - 1 ? "" : "\n"));
            }
            if (outputfile != null)
                bw.close();
        } catch (Exception ex) {
            System.out.println("Output error: " + ex.getMessage());
        }
    }
}
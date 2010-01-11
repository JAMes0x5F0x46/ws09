package at.tuwien.ifs.somtoolbox.util;

import java.io.IOException;
import java.util.ArrayList;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.output.SOMLibMapOutputter;
import at.tuwien.ifs.somtoolbox.properties.PropertiesException;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Reads a HTML representation of a SOM, and writes SOMLib unit and weight files for it. Tested with
 * http://www.ifs.tuwien.ac.at/~andi/somlib/data/time60/times_000_2.html, might not work for newer HTML representations.
 * 
 * @author Rudolf Mayer
 * @version $Id: HTMLMapReader.java 2874 2009-12-11 16:03:27Z frank $
 */
public class HTMLMapReader {
    public static void main(String[] args) throws ParserException, IOException, SOMLibFileFormatException, PropertiesException {
        // register and parse all options
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptInputFileName(true), OptionFactory.getOptOutputFileName(true),
                OptionFactory.getOptFileNamePrefix(false), OptionFactory.getOptFileNameSuffix(false));
        String inputFile = config.getString("inputFile");
        String outputFileName = config.getString("output");
        String prefix = config.getString("fileNamePrefix", "");
        String suffix = config.getString("fileNameSuffix", "");

        Parser parser = new Parser();
        parser.setResource(inputFile);
        final NodeList parse = parser.parse(new TagNameFilter("table"));
        ArrayList<ArrayList<ArrayList<String>>> inputs = new ArrayList<ArrayList<ArrayList<String>>>();

        Node table = parse.elementAt(0);
        int numVectors = 0;
        final NodeList trs = table.getChildren();
        for (int i = 0; i < trs.size(); i++) {
            final Node node = trs.elementAt(i);
            if (node instanceof TableRow && node.getChildren() != null) {
                final ArrayList<ArrayList<String>> row = new ArrayList<ArrayList<String>>();
                inputs.add(row);
                final NodeList tds = node.getChildren();
                for (int j = 0; tds != null && j < tds.size(); j++) {
                    final Node node2 = tds.elementAt(j);
                    if (node2 instanceof TableColumn) {
                        final ArrayList<String> cell = new ArrayList<String>();
                        row.add(cell);
                        // System.out.println(node2.getClass());
                        // System.out.println(node2);
                        final NodeList hrefs = node2.getChildren();
                        for (int k = 0; hrefs != null && k < hrefs.size(); k++) {
                            final Node node3 = hrefs.elementAt(k);
                            if (node3 instanceof LinkTag) {
                                // System.out.println(((LinkTag) node3).getLinkText());
                                cell.add(prefix + ((LinkTag) node3).getLinkText() + suffix);
                                numVectors++;
                            }
                        }
                    }
                }
            }
        }

        String[][][] labels = new String[inputs.get(0).size()][inputs.size()][];
        System.out.println("Map size: " + labels.length + "x" + labels[0].length);
        double[][][][] weights = new double[inputs.get(0).size()][inputs.size()][1][];
        for (int i = 0; i < labels.length; i++) {
            for (int j = 0; j < labels[i].length; j++) {
                ArrayList<String> l = inputs.get(j).get(i);
                labels[i][j] = l.toArray(new String[l.size()]);
                weights[i][j][0] = new double[] { 1, 2 }; // some dummy weightvector...
            }
        }
        String fDir = ".";
        SOMLibMapOutputter.writeUnitDescriptionFile(labels, fDir, outputFileName, false);
        SOMLibMapOutputter.writeWeightVectorFile(weights, fDir, outputFileName, false);
        SOMLibMapOutputter.writeMapDescriptionFile(labels.length, labels[0].length, 1, weights[0][0][0].length, numVectors, fDir, outputFileName);
    }
}

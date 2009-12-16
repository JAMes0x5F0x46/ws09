/*
 * Created on Apr 21, 2009 Version: $Id: PlaylistAnalysis.java 2874 2009-12-11 16:03:27Z frank $
 */

package at.tuwien.ifs.somtoolbox.apps.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StdErrProgressWriter;
import at.tuwien.ifs.somtoolbox.visualization.clustering.CompleteLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.SingleLinkageTreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.TreeBuilder;
import at.tuwien.ifs.somtoolbox.visualization.clustering.WardsLinkageTreeBuilder;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

/**
 * @author Jakob Frank
 * @version $Id: PlaylistAnalysis.java 2874 2009-12-11 16:03:27Z frank $
 */
public class PlaylistAnalysis {

    private InputData inputData;

    private String outBasename;

    private File outDir;

    private GrowingSOM som;

    private List<PLAnlyser> analysers;

    private String headerString;

    private MapPNode map;

    public PlaylistAnalysis() {
        analysers = new LinkedList<PLAnlyser>();
    }

    private void analyse(File file, boolean rawVal) throws IOException, MetricException {
        if (file.isDirectory()) {
            analyseDir(file, rawVal);
            return;
        }

        String plName = file.getName();
        List<String> playList = loadPlaylist(file);

        for (PLAnlyser a : analysers) {
            a.analyse(plName, playList);
        }
    }

    private void analyse(File[] listFiles, boolean rawVal) throws IOException, MetricException {
        StdErrProgressWriter progress = new StdErrProgressWriter(listFiles.length, "Analyzing playlists: ", 10);
        for (File file : listFiles) {
            analyse(file, rawVal);
            progress.progress();
        }
    }

    private void analyseDir(File file, boolean rawVal) throws IOException, MetricException {
        // FIXME: use util.ExtensionFilenameFilter
        analyse(file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".m3u");
            }
        }), rawVal);
    }

    public InputData getInputData() {
        return inputData;
    }

    public String getOutBasename() {
        return outBasename;
    }

    public File getOutDir() {
        return outDir;
    }

    public GrowingSOM getSom() {
        return som;
    }

    private void load(JSAPResult params) throws Exception {
        String HEADER_TMPL = "##config%n"
            + "# Started: %tF - %<tT%n"
            + "# UnitFile: %s%n" 
            + "# WGTFile: %s%n" 
            + "# VectorFile: %s%n"
            + "#%n%s" 
            + "# MapSize: %d x %d%n"
            + "# VectorDimensions: %d%n" 
            + "# VectorCount: %d%n" 
            + "##config-end%n";
        String clusterHeader = "";
        
        SOMLibFormatInputReader reader = new SOMLibFormatInputReader(params.getString("weightVectorFile"), params.getString("unitDescriptionFile"),
                null);
        som = new GrowingSOM(reader);

        map = new MapPNode(null, reader, new CommonSOMViewerStateData(), true);
        
        inputData = new SOMLibSparseInputData(params.getString("inputVectorFile"));

        int[] clusters = params.getIntArray("clusters");
        if (clusters.length > 0) {
            TreeBuilder clusterer = null;
            String reqBuilder = params.getString("clustertreebuilder");
            if (reqBuilder == null || reqBuilder.equalsIgnoreCase("single")) {
                clusterer = new SingleLinkageTreeBuilder();
            } else if (reqBuilder.equalsIgnoreCase("complete")) {
                clusterer = new CompleteLinkageTreeBuilder();
            } else if (reqBuilder.equalsIgnoreCase("wards")) {
                clusterer = new WardsLinkageTreeBuilder();
            } else {
                throw new Exception("Unsupported or unknown ClusterTreeBuilder: \"" + reqBuilder + "\"");
            }
            clusterHeader = String.format("# TreeBuilder: %s%n# Clusters: %s%n#%n", clusterer.getClusteringAlgName(), Arrays.toString(clusters));
            
            map.buildTree(clusterer);
        }


        outDir = params.getFile("output");
        outBasename = params.getString("basename");

        headerString = String.format(HEADER_TMPL, new Date(), params.getString("unitDescriptionFile"), params.getString("weightVectorFile"),
                params.getString("unitDescriptionFile"),clusterHeader, som.getLayer().getXSize(), som.getLayer().getYSize(), inputData.dim(),
                inputData.numVectors());

        for (PLAnlyser a : analysers) {
            a.init(this);
        }
    }

    public MapPNode getMap() {
        return map;
    }

    private void shutdown() {
        for (PLAnlyser a : analysers) {
            a.finish();
        }
        analysers.clear();
        
        inputData = null;
        som = null;
        map = null;
    }

    public void printHeader(PrintStream ps) {
        ps.println(headerString);
    }

    /**
     * @param playlist
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static List<String> loadPlaylist(File playlist) throws FileNotFoundException, IOException {
        LinkedList<String> playList = new LinkedList<String>();
        BufferedReader br = new BufferedReader(new FileReader(playlist));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#")) {
                // Blank Lines & Comments
                continue;
            }
            playList.add(line);
        }
        return playList;
    }

    /**
     * @param args
     */
    public static void main(String[] _args) {
        try {
            JSAPResult args = parseArgs(_args);

            PlaylistAnalysis pa = new PlaylistAnalysis();

            pa.registerAnalyzer(new PLInputSpaceAnalyser());
            pa.registerAnalyzer(new PLOutputSpaceAnalyser());
            int[] clusters = args.getIntArray("clusters");
            for (int c : clusters) {
                pa.registerAnalyzer(new PLClusterSpaceAnalyser(c));
            }

            pa.load(args);
            pa.analyse(args.getFile("playlist"), args.getBoolean("raw"));
            pa.shutdown();
        } catch (JSAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            System.out.println("EXIT!");
            System.exit(0);
        }
    }

    private void registerAnalyzer(PLAnlyser inputSpaceAnalyser) {
        analysers.add(inputSpaceAnalyser);
    }

    private static JSAPResult parseArgs(String[] args) throws JSAPException {
        JSAP jsap = new JSAP();

        jsap.registerParameter(OptionFactory.getOptUnitDescriptionFile(true));
        jsap.registerParameter(OptionFactory.getOptWeightVectorFile(true));
        jsap.registerParameter(OptionFactory.getOptInputVectorFile(true));

        // Defaults
        jsap.registerParameter(new Switch("help", 'h', "help", "Print this help."));

        // Print raw values
        jsap.registerParameter(new Switch("raw", JSAP.NO_SHORTFLAG, "raw", "print the raw values"));
        jsap.registerParameter(new FlaggedOption("clusters", JSAP.INTEGER_PARSER, null, false, 'c', "cluster").setAllowMultipleDeclarations(true));
        jsap.registerParameter(new FlaggedOption("clustertreebuilder", JSAP.STRING_PARSER, null, false, JSAP.NO_SHORTFLAG, "treebuilder"));

        // Output
        jsap.registerParameter(new FlaggedOption("output", FileStringParser.getParser(), ".", false, 'o', JSAP.NO_LONGFLAG));
        jsap.registerParameter(new FlaggedOption("basename", JSAP.STRING_PARSER, "analysis", false, 'B', "baseName",
                "the basename for the result files"));

        // Input
        jsap.registerParameter(new UnflaggedOption("playlist", FileStringParser.getParser(), true, "a playlist or a directory containing playlists"));

        // Preprocessing
        JSAPResult result = jsap.parse(args);
        if (result.success()) {
            if (result.getBoolean("help")) {
                System.err.println(jsap.getUsage());
                System.err.println();
                System.err.println(jsap.getHelp(80));
                System.err.println();
                System.exit(0);
            }
            return result;
        } else {
            for (Iterator<?> errs = result.getErrorMessageIterator(); errs.hasNext();) {
                System.err.println("ERROR: " + errs.next());
            }
            System.err.println();
            System.err.println(jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp(80));
            System.err.println();
            System.exit(1);
            return null;
        }
    }
}

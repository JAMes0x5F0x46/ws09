/*
 * Created on Apr 22, 2009 Version: $Id: PLInputSpaceAnalyser.java 2874 2009-12-11 16:03:27Z frank $
 */

package at.tuwien.ifs.somtoolbox.apps.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDatum;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;

/**
 * @author frank
 * @version $Id: PLInputSpaceAnalyser.java 2874 2009-12-11 16:03:27Z frank $
 */
public class PLInputSpaceAnalyser implements PLAnlyser {

    private InputData inputData;

    private PrintStream stats, histogram;
    
    private boolean initialised = false;

    public PLInputSpaceAnalyser() {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * @see at.tuwien.ifs.somtoolbox.apps.analysis.PLAnlyser#init()
     */
    @Override
    public void init(PlaylistAnalysis parent) {
        try {
            stats = new PrintStream(new File(parent.getOutDir(), parent.getOutBasename() + ".in.stats.csv"));
            parent.printHeader(stats);
            stats.println("playlist,length,max,min,mean,median,var,sigma");
            
            histogram = new PrintStream(new File(parent.getOutDir(), parent.getOutBasename() + ".in.hist.csv"));
            parent.printHeader(histogram);
            
            inputData = parent.getInputData();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        initialised = true;
    }

    /**
     * @param plName
     * @param playList
     * @throws MetricException
     */
    @Override
    public void analyse(String plName, List<String> playList) {
        if (! initialised) return;
        
        try {
            L2Metric metric = new L2Metric();
            LinkedList<Double> distList = new LinkedList<Double>();
            for (int i = 0; i < playList.size() - 1; i++) {
                InputDatum item1 = inputData.getInputDatum(playList.get(i));
                InputDatum item2 = inputData.getInputDatum(playList.get(i + 1));
                if (item1 == null || item2 == null) {
                    continue;
                }
                distList.add(metric.distance(item1.getVector(), item2.getVector()));
            }

            double[] dists = new double[distList.size()];
            for (int i = 0; i < dists.length; i++) {
                dists[i] = distList.get(i);
            }

            printHistogram(plName, dists);
            printPLStats(plName, dists);
        } catch (MetricException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void printPLStats(String plName, double[] dists) {
        if (dists.length == 0)
            return;

        double max = StatUtils.max(dists);
        double min = StatUtils.min(dists);
        double mean = StatUtils.mean(dists);
        double var = StatUtils.variance(dists, mean);

        double[] local = Arrays.copyOf(dists, dists.length);
        Arrays.sort(local);
        double median;
        if (local.length % 2 == 0) {
            median = 0.5 * (local[(local.length / 2) - 1] + local[local.length / 2]);
        } else {
            median = local[(local.length / 2)];
        }

        stats.printf("%s,%d,%f,%f,%f,%f,%f,%f%n", plName, dists.length, max, min, mean, median, var, Math.sqrt(var));
        stats.flush();
    }

    /**
     * @param dists
     */
    private void printHistogram(String plName, double[] dists) {

        for (int i = 0; i < dists.length; i++) {
            histogram.println(dists[i]);
        }
        histogram.flush();
    }

    @Override
    public void finish() {
        histogram.close();
        stats.close();
    }

}

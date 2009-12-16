package at.tuwien.ifs.somtoolbox.visualization;

import at.tuwien.ifs.somtoolbox.clustering.DistanceFunctionType;
import at.tuwien.ifs.somtoolbox.layers.metrics.AbstractMetric;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;

public class Snapper {

    protected AbstractMetric distanceFunction;
    protected DistanceFunctionType lineDistanceFunction;
    
    public Snapper(){
        distanceFunction = new L2Metric();
        lineDistanceFunction = DistanceFunctionType.Euclidean;
    }
    
    public Snapper(AbstractMetric distanceFunction, DistanceFunctionType lineDistanceFunction) {
           this.distanceFunction = distanceFunction;
           this.lineDistanceFunction = lineDistanceFunction;
       }
}

package at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric;
import at.tuwien.ifs.somtoolbox.layers.metrics.MetricException;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @version $Id: Graph.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class Graph {
    public static L2Metric metric = new L2Metric();

    // / Adjacency List
    protected TreeMap<Node, LinkedList<Edge>> adjList_;

    // A list of the edges in the graph
    protected ArrayList<Edge> edges_;

    protected GrowingSOM gsom;

    protected List<Edge> mst;

    public Graph(GrowingSOM gsom) {
        this.gsom = gsom;
        edges_ = new ArrayList<Edge>();
        adjList_ = new TreeMap<Node, LinkedList<Edge>>();
    }

    protected abstract List<Edge> calculateEdge();

    protected void connectTwoNodes(Unit unit, HashMap<Unit, Unit> hm, Unit neighbour) {

        String lu = unit.toString();
        String lv = neighbour.toString();
        Node u = getNode(lu);
        Node v = getNode(lv);

        try {
            if (!(hm.containsKey(unit) && hm.get(unit) == neighbour || hm.containsKey(neighbour) && hm.get(neighbour) == unit)) {
                insert(u, v, metric.distance(unit.getWeightVector(), neighbour.getWeightVector()));
                hm.put(unit, neighbour);
            }
        } catch (MetricException e) {
            // does not happen
            e.printStackTrace();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
        }
    }

    protected abstract void createNodes(Unit[] units);

    public abstract void drawLine(Graphics2D g, int unitWidth, int unitHeight, Unit n, Unit n1);

    public List<Edge> getMinimumSpanningTree() {
        if (mst == null) {
            this.mst = this.calculateEdge();
        }

        return mst;
    }

    protected abstract ArrayList<Unit> getNeighbours(int horIndex, int verIndex, Unit[][] units);

    protected Node getNode(String label) {
        for (Node n : adjList_.keySet()) {
            if (n.getLabel().equals(label)) {
                return n;
            }
        }
        return null;
    }

    protected void insert(Node u, Node v, double w) {

        Edge e = new Edge(u, v, w);
        adjList_.get(u).add(e);
        adjList_.get(v).add(new Edge(v, u, w));

        edges_.add(e);
    }

    /**
     * A class for spanning tree written almost completely by Prof. Linderoth. You're welcome.
     * 
     * @return
     */

    protected List<Edge> kruskalMST() {
        // Create an empty list of edges to hold the tree edges (for Kruskal)
        ArrayList<Edge> treeEdges = new ArrayList<Edge>();

        // Create a set for every node
        LinkedList<TreeSet<Node>> kruskalSets = new LinkedList<TreeSet<Node>>();
        for (Node n : adjList_.keySet()) {
            TreeSet<Node> s = new TreeSet<Node>();
            s.add(n);
            kruskalSets.add(s);
        }

        // Sort edges by weight (Fancy code...)
        Collections.sort(edges_, new Comparator<Edge>() {
            public int compare(Edge e1, Edge e2) {
                return e1.getWeight() > e2.getWeight() ? 1 : -1;
            }
        });

        /* Do Kruskal */
        for (Edge e : edges_) {
            Node u = e.getStart();
            Node v = e.getEnd();
            TreeSet<Node> uset = null;
            TreeSet<Node> vset = null;

            for (TreeSet<Node> s : kruskalSets) {
                if (s.contains(u)) {
                    uset = s;
                }
                if (s.contains(v)) {
                    vset = s;
                }
            }

            assert uset != null;
            if (!uset.equals(vset)) {
                uset.addAll(vset);
                kruskalSets.remove(vset);
                treeEdges.add(e);
            }
        }

        return treeEdges;
    }

}
package at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @version $Id: InputdataGraph.java 2874 2009-12-11 16:03:27Z frank $
 */
public class InputdataGraph extends Graph {
    InputData data;

    public InputdataGraph(GrowingSOM gsom) {
        super(gsom);
        this.data = gsom.getSharedInputObjects().getInputData();
    }

    @Override
    protected List<Edge> calculateEdge() {

        Unit[] ret = new Unit[data.numVectors()];

        int counter = 0;

        for (String dataName : data.getLabels()) {
            Unit other = gsom.getLayer().getUnitForDatum(dataName);
            ret[counter] = new Unit(gsom.getLayer(), other.getXPos(), other.getYPos(), data.getData()[counter]);

            counter++;
        }

        return connect_neighbours(ret);
    }

    @Override
    protected ArrayList<Unit> getNeighbours(int horIndex, int verIndex, Unit[][] units) {
        return null;
    }

    @Override
    protected void createNodes(Unit[] units) {
        for (Unit anUnit : units) {
            adjList_.put(new Node(anUnit.toString(), anUnit.getXPos(), anUnit.getYPos(), anUnit), new LinkedList<Edge>());
        }
    }

    @Override
    public void drawLine(Graphics2D g, int unitWidth, int unitHeight, Unit n, Unit n1) {
        g.drawLine(n.getXPos() * unitWidth, n.getYPos() * unitHeight, n1.getXPos() * unitWidth, n1.getYPos() * unitHeight);
    }

    private List<Edge> connect_neighbours(Unit[] units) {
        createNodes(units);
        HashMap<Unit, Unit> hm = new HashMap<Unit, Unit>();

        for (Unit mainunit : units) {

            // attaches each unit to mainunit
            for (Unit neighbour : units) {
                connectTwoNodes(mainunit, hm, neighbour);
            }
        }

        return kruskalMST();
    }
}

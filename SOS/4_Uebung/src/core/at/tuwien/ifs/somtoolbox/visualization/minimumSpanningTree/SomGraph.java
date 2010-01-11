package at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.tuwien.ifs.somtoolbox.layers.Unit;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @version $Id: SomGraph.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SomGraph extends Graph {
    public SomGraph(GrowingSOM gsom) {
        super(gsom);
    }

    @Override
    protected List<Edge> calculateEdge() {

        Unit[][] units = this.gsom.getLayer().get2DUnits();

        createNodes(this.gsom.getLayer().getAllUnits());

        HashMap<Unit, Unit> hm = new HashMap<Unit, Unit>();

        for (int i = 0; i < units.length; i++) {
            for (int j = 0; j < units[i].length; j++) {
                ArrayList<Unit> neighbours = getNeighbours(i, j, units);
                for (Unit neighbour : neighbours) {
                    connectTwoNodes(units[i][j], hm, neighbour);
                }
            }
        }
        return kruskalMST();
    }

    @Override
    protected ArrayList<Unit> getNeighbours(int horIndex, int verIndex, Unit[][] units) {

        ArrayList<Unit> unit = new ArrayList<Unit>();
        if (verIndex > 0) {
            unit.add(units[horIndex][verIndex - 1]);
        }
        if (verIndex + 2 <= units[horIndex].length) {
            unit.add(units[horIndex][verIndex + 1]);
        }

        if (horIndex > 0) {
            unit.add(units[horIndex - 1][verIndex]);
        }
        if (horIndex + 2 <= units.length) {
            unit.add(units[horIndex + 1][verIndex]);
        }

        return unit;
    }

    @Override
    protected void createNodes(Unit[] units) {
        for (Unit anUnit : units) {
            adjList_.put(new Node(anUnit.toString(), anUnit.getXPos(), anUnit.getYPos(), anUnit), new LinkedList<Edge>());
        }
    }

    @Override
    public void drawLine(Graphics2D g, int unitWidth, int unitHeight, Unit n, Unit n1) {
        for (int i = 0; i < Math.abs(n.getXPos() - n1.getXPos()); i++) {
            g.drawLine((n.getXPos() + i) * unitWidth, n1.getYPos() * unitHeight, (n.getXPos() + 1 + i) * unitWidth, n1.getYPos() * unitHeight);
        }

        for (int i = 0; i < Math.abs(n.getYPos() - n1.getYPos()); i++) {
            g.drawLine(n1.getXPos() * unitWidth, (n.getYPos() + i) * unitHeight, n1.getXPos() * unitWidth, (n.getYPos() + 1 + i) * unitHeight);
        }
    }
}

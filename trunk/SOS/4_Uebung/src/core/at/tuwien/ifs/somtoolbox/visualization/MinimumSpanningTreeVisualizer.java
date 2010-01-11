package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.Edge;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.Graph;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.InputdataGraph;
import at.tuwien.ifs.somtoolbox.visualization.minimumSpanningTree.SomGraph;

/**
 * @author Thomas Kern
 * @author Magdalena Widl
 * @version $Id: MinimumSpanningTreeVisualizer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MinimumSpanningTreeVisualizer extends AbstractMatrixVisualizer implements BackgroundImageVisualizer {
    private BufferedImage image;

    public MinimumSpanningTreeVisualizer() {
        NUM_VISUALIZATIONS = 3;
        VISUALIZATION_NAMES = new String[] { "Minimum Spanning Tree SOM", "Minimum Spanning Tree Input Data", "Minimum Spanning Tree Both" };
        VISUALIZATION_SHORT_NAMES = new String[] { "MSTsom", "MSTdata", "MSTboth" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Implementation of a minimum spanning tree on a SOM",
                "Implementation of a minimum spanning tree on the input data",
                "Implementation of a minimum spanning tree on the SOM and input data, ideal for comparing them" };
        neededInputObjects = new String[] { SOMVisualisationData.INPUT_VECTOR };
    }

    @Override
    public BufferedImage createVisualization(int variantIndex, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        if (variantIndex == 1 || variantIndex == 2) {
            if (gsom.getSharedInputObjects().getInputData() == null) {
                throw new SOMToolboxException("Input data is needed for this Minimum Spanning Tree!");
            }
        }
        image = null;

        switch (variantIndex) {
            case 0:
                drawMinimumSpanningTree(createImage(width, height), new SomGraph(gsom), gsom.getLayer(), Color.BLACK);
                break;
            case 1:
                drawMinimumSpanningTree(createImage(width, height), new InputdataGraph(gsom), gsom.getLayer(), Color.BLUE);
                break;
            case 2:
                drawMinimumSpanningTree(createImage(width, height), new SomGraph(gsom), gsom.getLayer(), Color.BLACK);
                drawMinimumSpanningTree(createImage(width, height), new InputdataGraph(gsom), gsom.getLayer(), Color.BLUE);
                break;
        }
        return image;
    }

    private BufferedImage createImage(int width, int height) {
        if (image == null) {
            image = new BufferedImage(width, height, Transparency.TRANSLUCENT);
        }

        return image;
    }

    private void drawMinimumSpanningTree(BufferedImage res, Graph graph, GrowingLayer layer, Color color) {

        Graphics2D g = (Graphics2D) res.getGraphics();

        int unitWidth = res.getWidth() / layer.getXSize();
        int unitHeight = res.getHeight() / layer.getYSize();

        for (Edge e : graph.getMinimumSpanningTree()) {
            g.setPaint(color);
            graph.drawLine(g, unitWidth, unitHeight, e.getStart().getUnit(), e.getEnd().getUnit());
            drawAnnotation(e, g);
        }

        drawNodePoints(graph, g, unitWidth, unitHeight);
    }

    private void drawNodePoints(Graph graph, Graphics2D g, int unitWidth, int unitHeight) {
        for (Edge e : graph.getMinimumSpanningTree()) {
            g.setPaint(Color.RED);

            g.drawOval(e.getStart().getUnit().getXPos() * unitWidth, e.getStart().getUnit().getYPos() * unitHeight, 0, 0); // startknoten
            g.drawOval(e.getEnd().getUnit().getXPos() * unitWidth, e.getEnd().getUnit().getYPos() * unitHeight, 0, 0); // endknoten
        }
    }

    private void drawAnnotation(Edge e, Graphics2D g) {

    }
}
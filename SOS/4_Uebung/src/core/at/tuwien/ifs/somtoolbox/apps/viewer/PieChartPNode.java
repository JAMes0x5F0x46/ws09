package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.PaintList;

import at.tuwien.ifs.somtoolbox.apps.viewer.controls.MapOverviewPane;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Wrapper for displaying a pie chart within the Piccolo zooming interface framework.
 * 
 * @author Michael Dittenbach
 * @version $Id: PieChartPNode.java 2874 2009-12-11 16:03:27Z frank $
 */
public class PieChartPNode extends PNode {
    private static final long serialVersionUID = 1L;

    public static double ZERO = 0.0001;

    private double[] values = null;

    private DefaultPieDataset dataset;

    private JFreeChart chart;

    private PiePlot plot;

    private Rectangle2D border = new Rectangle2D.Double();

    private double X = 0;

    private double Y = 0;

    private double width = 0;

    private double height = 0;

    /**
     * Creates a PieChartPNode at the given coordinates with the given values.
     * 
     * @param x X coordinate of the chart.
     * @param y Y coordinate of the chart.
     * @param values Array of <code>double</code> containing the values.
     */
    public PieChartPNode(double x, double y, double w, double h, double[] values) {
        super();

        X = x;
        Y = y;
        width = w;
        height = h;
        this.values = values;
        dataset = new DefaultPieDataset();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == 0) {
                dataset.setValue(String.valueOf(i), new Double(ZERO));
            } else {
                dataset.setValue(String.valueOf(i), new Double(values[i]));
            }
        }

        chart = ChartFactory.createPieChart("", dataset, false, false, false);
        plot = (PiePlot) chart.getPlot();
        plot.setShadowPaint(null);
        plot.setBackgroundPaint(null);
        plot.setLabelGenerator(new CountLabeler());
        plot.setOutlinePaint(null);
        this.setBounds(X, Y, width, height);
        // TODO: check id values == null
    }

    public boolean setBounds(double x, double y, double width, double height) {
        if (super.setBounds(x, y, width, height)) {
            border.setFrame(x, y, width, height);
            this.X = x;
            this.Y = y;
            this.width = width;
            this.height = height;
            return true;
        }
        return false;
    }

    protected void paint(PPaintContext paintContext) {
        PCamera pCam = paintContext.getCamera();
        if (!((PCanvas) pCam.getComponent()).getClass().equals(MapOverviewPane.MapOverviewCanvas.class)) { // only for main display

            Graphics2D g2d = paintContext.getGraphics();
            border.setRect(X, Y, width, height);
            plot.draw(g2d, new Rectangle2D.Double(X, Y, width, height), null, null, null);
        }
    }

    public void drawChart(Graphics2D g2d, double xCoord, double yCoord, double unitWidth, double unitHeight) {
        plot.draw(g2d, new Rectangle2D.Double(xCoord, yCoord, unitWidth, unitHeight), null, null, null);
    }

    /**
     * Draw charts on intialised X & Y coordinates
     */
    public void drawChart(Graphics2D g2d, double xCoord, double yCoord) {
        setBounds(xCoord, yCoord, width, height);
        border.setRect(xCoord, yCoord, width, height);
        plot.draw(g2d, new Rectangle2D.Double(xCoord, yCoord, width, height), null, null, null);
    }

    /**
     * Returns the colors used in the diagram. Can be used for a legend displayed in a different Swing element.
     * 
     * @return Array of <code>Color</code>.
     */
    public Color[] getLegendColors() {
        Color[] res = new Color[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = (Color) plot.getSectionPaint(i);
        }
        return res;
    }

    public PaintList getPaintList() {
        PaintList p = new PaintList();
        for (int i = 0; i < values.length; i++) {
            p.setPaint(i, plot.getSectionPaint(i));
        }
        return p;
    }

    /** Returns the colour used in the diagram at the specified index. */
    public Color getLegendColor(int index) {
        return (Color) plot.getSectionPaint(index);
    }

    public void setColor(int index, Color color) {
        plot.setSectionPaint(index, color);
    }

    public void setColors(Color[] colors) {
        for (int i = 0; i < colors.length; i++) {
            plot.setSectionPaint(i, colors[i]);
        }
    }

    public double[] getValues() {
        return values;
    }

    public void setShowLegend(boolean show) {
        if (show) {
            plot.setLabelGenerator(new CountLabeler());
        } else {
            plot.setLabelGenerator(null);
        }
    }

    @SuppressWarnings("unchecked")
    private class CountLabeler implements PieSectionLabelGenerator {
        @Override
        public String generateSectionLabel(PieDataset dataset, Comparable key) {
            int v = dataset.getValue(key).intValue();
            if (v < 1) {
                return null;
            } else {
                return String.valueOf(v);
            }
        }

        @Override
        public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
            return null;
        }
    }

}

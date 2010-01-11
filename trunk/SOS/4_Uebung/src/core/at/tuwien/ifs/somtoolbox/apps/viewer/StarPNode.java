package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Visualises the input as a star on the map.
 * 
 * @author Khalid Latif
 * @version $Id: StarPNode.java 2874 2009-12-11 16:03:27Z frank $
 */
public class StarPNode extends InputPNode {

    private static final long serialVersionUID = 1l;

    private static final Color ORANGE = new Color(250, 117, 48);

    private static final Color YELLOW = new Color(255, 244, 164);

    private Color lineColor;

    public StarPNode() {
        lineColor = YELLOW;
        addInputEventListener(new PBasicInputEventHandler() {
            public void mouseEntered(PInputEvent event) {
                lineColor = ORANGE;
                repaint();
            }

            public void mouseExited(PInputEvent event) {
                lineColor = YELLOW;
                repaint();
            }
        });
    }

    public StarPNode(double x, double y) {
        super(x, y);
    }

    /**
     * @see edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext)
     */
    protected void paint(PPaintContext paintContext) {
        Graphics2D g2 = paintContext.getGraphics();
        g2.setStroke(new BasicStroke(1f));
        // g2.setPaint(new TexturePaint());
        g2.setPaint(lineColor);
        int x1 = (int) getX();
        int y1 = (int) getY();
        g2.drawLine(x1, y1 + HEIGHT_2, x1 + WIDTH, y1 + HEIGHT_2); // hor
        g2.drawLine(x1 + WIDTH_2, y1, x1 + WIDTH_2, y1 + HEIGHT); // ver
        g2.setPaint(Color.WHITE);
        g2.fillOval(x1 + WIDTH_4, y1 + HEIGHT_4, WIDTH_2, HEIGHT_2);
    }

}

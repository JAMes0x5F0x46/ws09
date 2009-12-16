package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Displays a tool-tip window displayed on mouse-over events in the {@link SOMPane}. This class makes use of the <a
 * href="http://www.cs.umd.edu/hcil/jazz/" target="_blank">Piccolo framework</a>.
 * 
 * @author Michael Dittenbach
 * @version $Id: ToolTipPNode.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ToolTipPNode extends PText {
    private static final long serialVersionUID = 1L;

    private Rectangle2D border = new Rectangle2D.Double();

    private final Color backgroundColor = Color.decode("#feffb9");

    private final Color borderColor = Color.decode("#fcff00");

    private final int fontSize = 10;

    public ToolTipPNode() {
        super();
        setFont(new Font("Sans", Font.PLAIN, fontSize));
    }

    public ToolTipPNode(String aText) {
        super(aText);
        setFont(new Font("Sans", Font.PLAIN, fontSize));
    }

    protected void paint(PPaintContext paintContext) {
        Graphics2D g2d = (Graphics2D) paintContext.getGraphics();
        // super.paint(paintContext);

        border.setRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        g2d.setStroke(new BasicStroke(1f));
        g2d.setPaint(backgroundColor);
        g2d.fill(border);
        g2d.setColor(borderColor);
        g2d.draw(border);

        super.paint(paintContext);
    }

    public boolean getPickable() {
        return false;
    }
}

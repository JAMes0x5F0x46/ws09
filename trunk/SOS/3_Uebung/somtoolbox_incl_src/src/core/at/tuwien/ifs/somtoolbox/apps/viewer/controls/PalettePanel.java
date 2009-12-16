package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.logging.Logger;

import javax.swing.JLabel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * Implements a graphical elements displaying the currently used {@link Palette}.
 * 
 * @author Rudolf Mayer
 * @version $Id: PalettePanel.java 2874 2009-12-11 16:03:27Z frank $
 */
public class PalettePanel extends AbstractViewerControl implements ComponentListener {
    private static final long serialVersionUID = 1L;

    static final int BORDER = 10;

    private JLabel noPalette = new JLabel("No palette loaded!");

    private PaletteDisplayer drawPalettePanel;

    public PalettePanel(String title, CommonSOMViewerStateData state) {
        super(title, state, new BorderLayout());
        setPalette(null);
        setVisible(true);
        addComponentListener(this);
    }

    public void setPalette(Color[] palette) {
        setPalette(palette, -1, -1);
    }

    public void setPalette(Color[] palette, double minValue, double maxValue) {
        getContentPane().removeAll();
        if (palette != null) {
            drawPalettePanel = new PaletteDisplayer(new Palette("","","",palette));
            getContentPane().add(drawPalettePanel, BorderLayout.CENTER);
            drawPalettePanel.invalidate();
            drawPalettePanel.repaint();
        } else {
            getContentPane().add(noPalette, BorderLayout.CENTER);
        }
        getContentPane().repaint();
        repaint();
        // dirty hack to overcome repaint problems
        setVisible(!isVisible());
        setVisible(!isVisible());
    }

    public Color[] getPalette() {
        return drawPalettePanel.getPalette().getColors();
    }

    public Dimension getMinimumSize() {
        return new Dimension(state.controlElementsWidth / 2, 20 * 2 + 2 * BORDER);
    }

    public Dimension getPreferredSize() {
        return new Dimension(state.controlElementsWidth, 20 * 2 + 2 * BORDER);
    }

    public void componentResized(ComponentEvent e) {
        Logger.getLogger("at.tuwien.ifs.somtoolbox").fine("component resized called");
        if (drawPalettePanel != null) {
//            drawPalettePanel.adjustPaletteSize();
            repaint();
        }
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

}

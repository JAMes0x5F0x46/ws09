package at.tuwien.ifs.somtoolbox.summarisation.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.TogglablePanel;
import at.tuwien.ifs.somtoolbox.visualization.ColorGradientFactory;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: PalettePanel.java 2913 2009-12-14 02:38:32Z mayer $
 */
public class PalettePanel extends TogglablePanel {
    private static final long serialVersionUID = 1L;

    private static final int BORDER = 10;

    private NavigationPanel navP = null;

    private String[] colors = { "Black", "RGB", "Less_Water", "Mountain2d" };

    private Color[][] palettes = { ColorGradientFactory.GrayscaleGradient().toPalette(9), ColorGradientFactory.RGBGradient().toPalette(9),
            ColorGradientFactory.CartographyColorGradientLessWater().toPalette(9), ColorGradientFactory.CartographyMountain2dGradient().toPalette(9) };

    private DrawPalettePanel drawpalettePanel = null;

    public PalettePanel(NavigationPanel nav) {
        super(new GridBagLayout());
        this.navP = nav;
        setBorder(BorderFactory.createEtchedBorder());
        final JLabel palettelabel = new JLabel("Palette   " + TEXT_CLOSE);
        palettelabel.setForeground(Color.blue);
        palettelabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                toggleState(palettelabel);
            }
        });
        JComboBox colorCB = new JComboBox(colors);
        colorCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePalette((String) ((JComboBox) e.getSource()).getSelectedItem());
            }
        });

        Color[] palette = ColorGradientFactory.GrayscaleGradient().toPalette(9);
        drawpalettePanel = new DrawPalettePanel(palette);

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(new Insets(5, 10, 5, 10)).setGridWidth(2);

        add(palettelabel, gc);
        add(drawpalettePanel, gc.nextRow());
        add(new JLabel("high"), gc.nextRow().setGridWidth(1));
        add(new JLabel("low"), gc.nextCol().setAnchor(GridBagConstraintsIFS.NORTHEAST));
        add(colorCB, gc.nextRow().setGridWidth(2).setAnchor(GridBagConstraintsIFS.NORTHWEST));
    }

    private void updatePalette(String color) {
        int index = ArrayUtils.indexOf(colors, color);
        drawpalettePanel.setPalette(palettes[index]);
        navP.setPalette(palettes[index]);
    }

    // FIXME: reuse the code from at.tuwien.ifs.somtoolbox.apps.viewer.controls.PalettePanel
    private class DrawPalettePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        // private Color[] palette;

        private Color[] paletteForDisplay;

        public static final int PALETTE_HEIGHT = 20;

        private static final int PALETTE_WIDTH = 241;

        private int itemWidth;

        private int panelWidth;

        public DrawPalettePanel(Color[] palette) {
            this.panelWidth = PALETTE_WIDTH - 2 * BORDER;
            setPalette(palette);
        }

        private void setPalette(Color[] palette) {
            paletteForDisplay = palette;
            itemWidth = panelWidth / (palette.length - 4);
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(panelWidth, PALETTE_HEIGHT);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            int x = 0;
            for (int i = 2; i < paletteForDisplay.length - 2; i++) {
                g.setColor(paletteForDisplay[i]);
                g.fillRect(x, 0, itemWidth, PALETTE_HEIGHT);
                x = x + itemWidth;
            }
        }
    }
}

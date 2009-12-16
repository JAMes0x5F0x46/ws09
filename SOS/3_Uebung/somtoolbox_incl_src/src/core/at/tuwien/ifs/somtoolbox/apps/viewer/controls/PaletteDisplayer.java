package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * Component to display a Palette. <br>
 * Nice-to-have features:
 * <ul>
 * <li>scaling (just showing some part of the palette)</li>
 * </ul>
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: PaletteDisplayer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class PaletteDisplayer extends JComponent {
    private static final long serialVersionUID = 1L;

    private static final int MIN_PALETTE_WIDTH = 35;

    private Palette palette;

    private boolean showScale;

    private boolean autoOrientation;

    private int orientation;

    private Font font;

    /**
     * Create a new PaletteDisplayer.
     */
    public PaletteDisplayer() {
        super();
        palette = null;
        showScale = true;
        autoOrientation = true;
        font = new Font("Monospaced", Font.PLAIN, 9);

        orientation = SwingConstants.HORIZONTAL;

    }

    /**
     * Create a new PaletteDisplayer, displaying the given Palette.
     * 
     * @param palette the Palette to display.
     */
    public PaletteDisplayer(Palette palette) {
        this();
        this.palette = palette;
    }

    protected void paintComponent(Graphics g) {
        if (isAutoOrientation()) {
            if (getWidth() < getHeight())
                setOrientation(SwingConstants.VERTICAL);
            if (getWidth() > getHeight())
                setOrientation(SwingConstants.HORIZONTAL);
        }
        if (isOpaque()) { // paint background
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (palette != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            Insets insets = getInsets();
            int scaleHeight = 0;
            int scaleWidth = 0;
            if (orientation == SwingConstants.HORIZONTAL) {
                // Write the scale
                if (isShowScale()) {
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(font);
                    FontMetrics metrics = g2d.getFontMetrics(font);
                    scaleHeight = metrics.getHeight() + 2;
                    scaleWidth = metrics.stringWidth("100");
                    // System.out.println("Font: " + scaleWidth + "x" + scaleHeight);
                    int werte = (getWidth() - insets.left - insets.right) / (scaleWidth * 3);
                    float step = 100f / werte;

                    for (int i = 0; i <= werte; i++) {
                        String text = Math.round(i * step) + "";
                        int x = (insets.left + (scaleWidth / 2)) - (metrics.stringWidth(text) / 2)
                                + Math.round((getWidth() - insets.left - insets.right - scaleWidth) * ((i * step) / 100));
                        int y = getHeight() - insets.bottom - metrics.getDescent();
                        g2d.drawString(text, x, y);
                    }
                }

                int xStart = insets.left + (scaleWidth / 2);
                int xEnd = getWidth() - insets.right - (scaleWidth / 2);
                int paletteHeight = getHeight() - insets.top - insets.bottom - scaleHeight;
                Color[] colors = getPalette().getColors();
                float step = ((float) (xEnd - xStart)) / (float) colors.length;

                for (int i = 0; i < colors.length; i++) {
                    g2d.setColor(colors[i]);
                    g2d.fillRect(xStart + (Math.round(step * i)), insets.top, Math.round(step + 1), paletteHeight);
                }
            } else { // VERTICAL
                // Write the scale
                if (isShowScale()) {
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(font);
                    FontMetrics metrics = g2d.getFontMetrics(font);
                    scaleHeight = metrics.getHeight();
                    scaleWidth = metrics.stringWidth("100") + 2;

                    int werte = (getHeight() - insets.top - insets.bottom) / (scaleHeight * 4);
                    float step = 100f / werte;

                    for (int i = 0; i <= werte; i++) {
                        String text = Math.round(i * step) + "";
                        int x = (insets.left + scaleWidth) - (metrics.stringWidth(text));
                        int y = getHeight() - insets.bottom
                                - Math.round(((getHeight() - insets.top - insets.bottom - scaleHeight) * (i * step)) / 100);
                        g2d.drawString(text, x, y);
                    }
                }

                int yStart = insets.top + (scaleHeight / 2);
                int yEnd = getHeight() - insets.top - (scaleHeight / 2);
                int paletteWidth = getWidth() - insets.left - insets.right - scaleWidth;
                Color[] colors = getPalette().getColors();
                float step = ((float) (yEnd - yStart)) / (float) colors.length;

                for (int i = 0; i < colors.length; i++) {
                    g2d.setColor(colors[i]);
                    g2d.fillRect(insets.left + scaleWidth, yEnd - Math.round(step * (i + 1)), paletteWidth, Math.round(step + 1));
                }
            }
            g2d.dispose();
        }
    }

    /**
     * Returns wheter the Components orientation is automatically adjusted. Default is <c>true</c>
     * 
     * @return Returns the autoOrientation.
     */
    public boolean isAutoOrientation() {
        return autoOrientation;
    }

    /**
     * Sets wheter the components orientation should be automatically adjusted.
     * 
     * @param autoOrientation The autoOrientation to set.
     */
    public void setAutoOrientation(boolean autoOrientation) {
        this.autoOrientation = autoOrientation;
    }

    /**
     * Returns the font used for the scale.
     * 
     * @return Returns the font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the Font used for the scale.
     * 
     * @param font The font to set.
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Returns the components orientation. Default is {@link SwingConstants#HORIZONTAL}
     * 
     * @return Returns the orientation.
     * @see #getOrientation()
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Set the Orientation. This can either be {@link SwingConstants#HORIZONTAL} or {@link SwingConstants#VERTICAL}
     * 
     * @param orientation The orientation to set.
     * @see SwingConstants
     */
    public void setOrientation(int orientation) {
        if (orientation == SwingConstants.HORIZONTAL || orientation == SwingConstants.VERTICAL)
            this.orientation = orientation;
    }

    /**
     * Get the palette that is displayed.
     * 
     * @return Returns the palette.
     */
    public Palette getPalette() {
        return palette;
    }

    /**
     * Set the palette to display.
     * 
     * @param palette The palette to set.
     */
    public void setPalette(Palette palette) {
        this.palette = palette;
        revalidate();
        repaint();
    }

    /**
     * Determines whether a scale is shown or not. Default is <c>true<c>.
     * 
     * @return Returns the showScale.
     */
    public boolean isShowScale() {
        return showScale;
    }

    /**
     * Set to <c>true</c> if a scale should be shown.
     * 
     * @param showScale The showScale to set.
     */
    public void setShowScale(boolean showScale) {
        this.showScale = showScale;
    }

    public Dimension getMinimumSize() {
        Dimension s = super.getMinimumSize();
        if (s == null) s = new Dimension(0,0);
        int h = 0, w = 0;
        Insets insets = getInsets();
        h = insets.top + insets.bottom + MIN_PALETTE_WIDTH;
        w = insets.left + insets.right + MIN_PALETTE_WIDTH;

        if (h > s.height)
            s.height = h;
        if (w > s.width)
            s.width = w;
        return new Dimension((w > s.width) ? w : s.width, (h > s.height) ? h : s.height);
    }
    
    public Dimension getPreferredSize() {
        Dimension s = super.getPreferredSize();
        if (s == null) s = new Dimension(0,0);
        int h = 0, w = 0;
        Insets insets = getInsets();
        h = insets.top + insets.bottom + MIN_PALETTE_WIDTH;
        w = insets.left + insets.right + MIN_PALETTE_WIDTH;

        if (h > s.height)
            s.height = h;
        if (w > s.width)
            s.width = w;
        return new Dimension((w > s.width) ? w : s.width, (h > s.height) ? h : s.height);
    }

}

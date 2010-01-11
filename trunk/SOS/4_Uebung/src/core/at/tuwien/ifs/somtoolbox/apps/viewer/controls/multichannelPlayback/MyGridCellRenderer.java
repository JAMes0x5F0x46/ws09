package at.tuwien.ifs.somtoolbox.apps.viewer.controls.multichannelPlayback;

import java.awt.Component;
import java.text.Format;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import net.sf.jeppers.grid.CellStyle;
import net.sf.jeppers.grid.GridCellRenderer;
import net.sf.jeppers.grid.JGrid;

/**
 * Uses DefaultTableCellRenderer as superclass, so that HTML can be interpreted
 * 
 * @author Ewald Peiszer
 * @version $Id: MyGridCellRenderer.java 2874 2009-12-11 16:03:27Z frank $
 */

public class MyGridCellRenderer extends DefaultTableCellRenderer implements GridCellRenderer {
    private static final long serialVersionUID = 1L;

    /**
     * Flags that determine whether the respective speaker is currently muted and therefore should get another background colour
     * <p>
     * order of indices: [x][y]
     */
    private boolean[][] aabMuted;

    public MyGridCellRenderer(boolean[][] aabMuted) {
        this.aabMuted = aabMuted;
    }

    public Component getRendererComponent(int row, int column, Object value, CellStyle style, boolean isSelected, boolean hasFocus, JGrid grid) {
        if (value == null) {
            value = "";
        }

        // Format value
        Format formatter = style.getFormat();
        if (formatter != null) {
            try {
                value = formatter.format(value);
            } catch (Exception e) {
                /* Ignore formatting errors */
            }
        }

        if (isSelected && hasFocus) {
            setForeground(grid.getFocusForegroundColor());
            setBackground(grid.getFocusBackgroundColor());
        } else if (isSelected) {
            setForeground(grid.getSelectionForegroundColor());
            setBackground(grid.getSelectionBackgroundColor());
        } else {
            if (aabMuted[column][row]) {
                setBackground(Commons.MUTED_SPEAKER);
            } else {
                setBackground(style.getBackgroundColor());
            }
            setForeground(style.getForegroundColor());

        }
        setFont(style.getFont());
        setBorder(new EmptyBorder(style.getPadding()));
        setHorizontalAlignment(style.getHorizontalAlignment());
        setVerticalAlignment(style.getVerticalAlignment());
        setText(value.toString());
        return this;
    }

}
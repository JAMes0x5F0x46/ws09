package at.tuwien.ifs.somtoolbox.apps.viewer.controls.table;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Jakob Frank
 * @version $Id: ButtonCellRenderer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ButtonCellRenderer extends JButton implements TableCellRenderer {
    private static final long serialVersionUID = 1L;

    public ButtonCellRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object theButton, boolean isSelected, boolean hasFocus, int row, int column) {
        JButton button = (JButton) theButton;
        return button;
    }
}
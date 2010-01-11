package at.tuwien.ifs.somtoolbox.apps.viewer.controls.table;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * @author Jakob Frank
 * @version $Id: ButtonCellEditor.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final long serialVersionUID = 1L;

    JButton button;

    public ButtonCellEditor() {
        button = new JButton();
    }

    // Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue() {
        return button;
    }

    // Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        button = (JButton) value;
        return button;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }
}
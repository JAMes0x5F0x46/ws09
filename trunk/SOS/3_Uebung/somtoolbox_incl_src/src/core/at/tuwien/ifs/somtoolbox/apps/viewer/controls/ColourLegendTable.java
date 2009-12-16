package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Displays a name-colour legend, i.e. the names of different items and the colours attached to them, as a table. In a special mode, it is possible to
 * set a limit and offset to display only parts of the given data arrays (see {@link #setData(String[], Color[], int, int)}) - this can be useful
 * e.g. if the data shall be split up in several tables that are arranged horizontally
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: ColourLegendTable.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ColourLegendTable extends JTable {
    private static final long serialVersionUID = 1L;

    private String[] names = null;

    private Color[] colors = null;

    private String[] columnNames;

    private int length;

    public ColourLegendTable() { // empty constructor meant for initial init w/o names/colours
    }

    public ColourLegendTable(String[] columnNames, ListSelectionListener listener) {
        this(null, null, columnNames, listener);
    }

    public ColourLegendTable(String[] names, Color[] colors, String[] columnNames, ListSelectionListener listener) {
        super();
        setDefaultRenderer(Color.class, new ColorRenderer(true));
        setDefaultEditor(Color.class, new ColorEditor());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (listener != null) {
            ListSelectionModel listSelectionModel = getSelectionModel();
            listSelectionModel.addListSelectionListener(listener);
        }
        this.columnNames = columnNames;
        if (names != null && colors != null) {
            setData(names, colors, 0, names.length);
        }
    }

    public void setData(String[] names, Color[] colors, int offset, int limit) {
        this.names = names;
        this.colors = colors;
        this.length = limit - offset;
        Object[][] data = new Object[limit][2];
        for (int c = offset; c < offset + limit; c++) {
            data[c - offset][0] = names[c];
            data[c - offset][1] = colors[c];
        }
        setModel(new ClassColorTableModel(data, columnNames));
        initColumnSizes();
    }

    private void initColumnSizes() {
        ClassColorTableModel model = (ClassColorTableModel) getModel();
        TableCellRenderer headerRenderer = getTableHeader().getDefaultRenderer();
        TableColumn column = getColumnModel().getColumn(0);
        int headerWidth = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0).getPreferredSize().width;
        int cellWidth = getDefaultRenderer(model.getColumnClass(0)).getTableCellRendererComponent(this, model.getLongestName(), false, false, 0, 0).getPreferredSize().width;
        column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        getColumnModel().getColumn(1).setPreferredWidth(25);
    }

    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < getComponents().length; i++) {
            getComponents()[i].setEnabled(enabled);
        }
    }

    public int getMinimumHeight() {
        if (names != null) {
            // space for up to 4 classes
            return Math.min(length, 4) * 17;
        } else {
            // just space for the place-holder label
            return 0;
        }
    }

    public int getPreferredHeight() {
        if (names != null) {
            // space for all classes
            return length * getRowHeight(0);
        } else {
            return getMinimumHeight();
        }
    }

    class ClassColorTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private Object[][] data = null;

        private String[] columnNames = null;

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public ClassColorTableModel(Object[][] data, String[] columnNames) {
            this.data = data;
            this.columnNames = columnNames;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            if (col == 1) {
                return true;
            } else {
                return false;
            }
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
            colors[row] = (Color) value;
        }

        public Object getLongestName() {
            Object res = null;
            int maxLen = 0;
            for (int i = 0; i < data.length; i++) {
                if (((String) data[i][0]).length() > maxLen) {
                    maxLen = ((String) data[i][0]).length();
                    res = data[i][0];
                }
            }
            return res;
        }
    }

    public class ColorRenderer extends JLabel implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        Border unselectedBorder = null;

        Border selectedBorder = null;

        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
            Color newColor = (Color) color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }
            return this;
        }
    }

    public class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private static final long serialVersionUID = 1L;

        Color currentColor;

        JButton button;

        JColorChooser colorChooser;

        JDialog dialog;

        protected static final String EDIT = "edit";

        public ColorEditor() {
            // Set up the editor (from the table's point of view), which is a button.
            // This button brings up the color chooser dialog, which is the editor from the user's point of view.
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

            // Set up the dialog that the button brings up.
            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button, "Pick a Color", true, // modal
                    colorChooser, this, // OK button handler
                    null); // no CANCEL button handler
        }

        /**
         * Handles events from the editor button and from the dialog's OK button.
         */
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                // The user has clicked the cell, so bring up the dialog.
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setVisible(true);

                // Make the renderer reappear.
                fireEditingStopped();

            } else { // User pressed dialog's "OK" button.
                currentColor = colorChooser.getColor();
            }
        }

        // Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            return currentColor;
        }

        // Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentColor = (Color) value;
            return button;
        }
    }

}

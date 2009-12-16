package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;

/**
 * Displays the class legend, i.e. the names of the different classes and the colours attached to them. Class colours can be changed; the assignment
 * can also be save and loaded to/from a file.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @author Thomas Lidy
 * @version $Id: ClassLegendPane.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClassLegendPane extends AbstractViewerControl implements ActionListener, MouseListener {

    private static final long serialVersionUID = 1L;

    private static final String[] columnNames = new String[] { "Class Name", "Color" };

    private ColourLegendTable table = null;

    private JLabel noClassesLoaded = null;

    private final String LOAD_COLORS = "Load Colours";

    private final String SAVE_COLORS = "Save Colours";

    private SOMPane mapPane = null;

    private JFileChooser fc;

    public ClassLegendPane(SOMPane mapPane, String title, CommonSOMViewerStateData state) {
        super(title, state, new GridBagLayout());
        this.mapPane = mapPane;

        if (mapPane.getClassLegendNames() != null) {
            // this.colors = state.inputDataObjects.getClassInfo().getClassColors();
            initClassTable();
        } else {
            initNoClassInfo();
        }
        setVisible(true);
    }

    public void initNoClassInfo() {
        if (table != null) {
            remove(table);
        }
        if (noClassesLoaded != null) {
            remove(noClassesLoaded);
        } else {
            noClassesLoaded = new JLabel("No class information file was loaded!");
        }
        getContentPane().add(noClassesLoaded);
        repaint();
    }

    public void initClassTable() {
        GridBagConstraints b = new GridBagConstraints();
        b.fill = GridBagConstraints.HORIZONTAL;

        if (table != null) {
            remove(table);
        }
        if (noClassesLoaded != null) {
            remove(noClassesLoaded);
        }
        table = new ColourLegendTable(mapPane.getClassLegendNames(), mapPane.getClassLegendColors(), columnNames, new ClassSelectionHandler());
        // Jakob: to propagate color-changes
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                TableModel tm = table.getModel();
                for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
                    if (i == TableModelEvent.HEADER_ROW)
                        continue;
                    Color c = (Color) tm.getValueAt(i, 1);
                    state.inputDataObjects.getClassInfo().setClassColor(i, c);
                    state.mapPNode.setClassColor(i, c);
                }
            }
        });

        addMouseListener(this);

        b.gridx = 0;
        b.gridy = 0;
        b.gridwidth = 2;
        b.fill = GridBagConstraints.BOTH;
        b.anchor = GridBagConstraints.NORTH;
        b.weightx = 1.0;
        b.weighty = 1.0;
        JScrollPane sp = new JScrollPane(table);
        getContentPane().add(sp, b);

        // init load/save buttons
        JButton btLoadColors = new JButton(LOAD_COLORS);
        btLoadColors.setFont(smallFont);
        btLoadColors.setActionCommand(LOAD_COLORS);
        btLoadColors.addActionListener(this);

        b.gridy += 1;
        b.gridwidth = 1;
        b.weighty = 0;
        b.anchor = GridBagConstraints.SOUTH;
        getContentPane().add(btLoadColors, b);

        JButton btSaveColors = new JButton(SAVE_COLORS);
        btSaveColors.setFont(smallFont);
        btSaveColors.setActionCommand(SAVE_COLORS);
        btSaveColors.addActionListener(this);

        b.gridx += 1;
        getContentPane().add(btSaveColors, b);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == SAVE_COLORS) {
            if (table.getColors() != null) {
                initFileChooser();
                fc.showSaveDialog(this);
                File file = fc.getSelectedFile();
                if (file == null) {
                    return;
                }

                Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Saving colors to file: " + file.toString());

                try {
                    FileWriter fw = new FileWriter(file);
                    for (int i = 0; i < table.getColors().length; i++) {
                        fw.write(table.getColors()[i].getRed() + " " + table.getColors()[i].getGreen() + " " + table.getColors()[i].getBlue() + "\n");
                    }
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Could not write colors to file! " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        if (e.getActionCommand() == LOAD_COLORS) {
            initFileChooser();
            fc.showOpenDialog(this);
            File file = fc.getSelectedFile();
            if (file == null) {
                return;
            }

            if (state.inputDataObjects.getClassInfo().loadClassColours(file)) {
                // then update visualization
                updateClassColours();
                mapPane.repaint();
            }

        }
    }

    private void initFileChooser() {
        if (fc == null) {
            fc = new JFileChooser(state.fileChooser.getCurrentDirectory());
        }
    }

    public void updateClassColours() {
        table.setColors(state.inputDataObjects.getClassInfo().getClassColors());
        for (int i = 0; i < table.getRowCount(); i++) {
            table.setValueAt(table.getColors()[i], i, 1);
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        table.clearSelection();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private class ClassSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            // System.out.println("value changed");
            Vector<Integer> selIndices = new Vector<Integer>();
            if (e.getValueIsAdjusting() == false) {
                if (lsm.isSelectionEmpty()) {
                    // no selection
                    mapPane.updateClassSelection(null);
                } else {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    for (int i = minIndex; i <= maxIndex; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            selIndices.addElement(new Integer(i));
                        }
                    }
                    int[] selectedIndices = new int[selIndices.size()];
                    for (int i = 0; i < selectedIndices.length; i++) {
                        selectedIndices[i] = ((Integer) selIndices.elementAt(i)).intValue();
                    }
                    mapPane.updateClassSelection(selectedIndices);
                }
            }
        }
    }

    public void setEnabled(boolean enabled) {
        table.setEnabled(enabled);
    }

    public Color[] getColors() {
        return table.getColors();
    }

    public Dimension getMinimumSize() {
        if (table != null) {
            return new Dimension(state.controlElementsWidth, 50 + table.getMinimumHeight());
        } else {
            // just space for the place-holder label
            return new Dimension(state.controlElementsWidth, 50);
        }
    }

    public Dimension getPreferredSize() {
        if (table != null) {
            return new Dimension(state.controlElementsWidth, 50 + table.getPreferredHeight());
        } else {
            // just space for the place-holder label
            return new Dimension(state.controlElementsWidth, 50);
        }
    }

}

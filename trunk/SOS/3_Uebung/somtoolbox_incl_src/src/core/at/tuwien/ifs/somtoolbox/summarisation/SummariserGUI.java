package at.tuwien.ifs.somtoolbox.summarisation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.ui.tabbedui.VerticalLayout;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.summarisation.gui.NavigationPanel;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: SummariserGUI.java 2900 2009-12-12 03:22:29Z mayer $
 */
public class SummariserGUI extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    public JScrollPane scrollP = new JScrollPane();

    public SummariserGUI(JFrame parent, CommonSOMViewerStateData state, Object[] itemName) {
        super(parent);
        setSize(new Dimension(850, 690));
        setTitle("Summarizer");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollP, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new VerticalLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(this);

        leftPanel.add(new NavigationPanel(this, state, itemName));
        leftPanel.add(closeButton);
        getContentPane().add(leftPanel, BorderLayout.WEST);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }

}

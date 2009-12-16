package at.tuwien.ifs.somtoolbox.summarisation.gui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import at.tuwien.ifs.somtoolbox.util.GridBagConstraintsIFS;
import at.tuwien.ifs.somtoolbox.util.TogglablePanel;

/**
 * @author Julius Penaranda
 * @author Rudolf Mayer
 * @version $Id: OptionPanel.java 2913 2009-12-14 02:38:32Z mayer $
 */
public class OptionPanel extends TogglablePanel {
    private static final long serialVersionUID = 1L;

    private NavigationPanel navP = null;

    public JCheckBox scoreCB = new JCheckBox();

    public JCheckBox highlightCB = new JCheckBox();

    public JCheckBox filenameCB = new JCheckBox();

    public JCheckBox wordCB = new JCheckBox();

    public OptionPanel(NavigationPanel nav) {
        super(new GridBagLayout());
        this.navP = nav;
        setBorder(BorderFactory.createEtchedBorder());

        final JLabel optionLabel = new JLabel("Options   " + TEXT_CLOSE);
        optionLabel.setForeground(Color.blue);
        optionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                toggleState(optionLabel);
            }
        });
        scoreCB.setText("scores");
        scoreCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                navP.updateResults();
            }
        });
        highlightCB.setText("highlight");
        highlightCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (highlightCB.isSelected()) {
                    wordCB.setSelected(false);
                }
                navP.updateResults();
            }
        });
        filenameCB.setText("Filename");
        filenameCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                navP.updateResults();
            }
        });
        wordCB.setText("highl word");
        wordCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (wordCB.isSelected()) {
                    highlightCB.setSelected(false);
                }
                navP.updateResults();
            }
        });

        GridBagConstraintsIFS gc = new GridBagConstraintsIFS().setInsets(new Insets(5, 10, 5, 10));

        add(optionLabel, gc);
        add(scoreCB, gc.nextRow());
        add(highlightCB, gc.nextCol());
        if (this.navP.itemNames.length > 1) {
            add(filenameCB, gc.nextRow());
            add(wordCB, gc.nextCol());
        } else {
            add(wordCB, gc.nextRow());
        }
    }

}

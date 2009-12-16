package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.event.ChangeListener;

import at.tuwien.ifs.somtoolbox.util.CentredDialog;

/**
 * A generic color chooser dialog.
 * 
 * @author Angela Roiger
 * @author Rudolf Mayer
 * @version $Id: ColorChooser.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class ColorChooser extends CentredDialog implements ChangeListener {

    private static final long serialVersionUID = 1L;

    protected JColorChooser cc = null;

    public ColorChooser(Window parent, Color color, String title) {
        super(parent, title, true);
        setLayout(new BorderLayout());
        cc = new JColorChooser(color);
        cc.getSelectionModel().addChangeListener(this);
        setTitle(title);
        getContentPane().add(cc, BorderLayout.CENTER);
        final JButton button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ColorChooser.this.setVisible(false);
                ColorChooser.this.dispose();
            }
        });
        getContentPane().add(button, BorderLayout.SOUTH);
        setSize(500, 300);
    }

    public Color getColor() {
        return cc.getColor();
    }

}
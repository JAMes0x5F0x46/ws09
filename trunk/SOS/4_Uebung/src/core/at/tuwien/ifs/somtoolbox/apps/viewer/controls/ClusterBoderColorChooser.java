package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Color;
import java.awt.Window;

import javax.swing.event.ChangeEvent;

public class ClusterBoderColorChooser extends ColorChooser {
    private static final long serialVersionUID = 1L;

    private ClusteringControl clusteringControl;

    public ClusterBoderColorChooser(Window parent, Color color, ClusteringControl clusteringControl) {
        super(parent, color, "Select cluster border colour");
        this.clusteringControl = clusteringControl;
        setVisible(true);
    }

    public void stateChanged(ChangeEvent e) {
        clusteringControl.updateClusterColourSelection(getColor());
    }

}

package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMPane;
import at.tuwien.ifs.somtoolbox.visualization.VisualizationUpdateListener;
import at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer.VisualizationControlPanel;

/**
 * @author Rudolf Mayer
 * @version $Id: VisualizationControl.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VisualizationControl extends AbstractViewerControl implements VisualizationUpdateListener {
    private static final long serialVersionUID = 1L;

    private SOMPane mapPane;

    public VisualizationControl(String title, CommonSOMViewerStateData state, SOMPane mapPane) {
        super(title, state);
        this.mapPane = mapPane;
        getContentPane().setLayout(new GridLayout(1, 1));
        setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        setVisible(true);
    }

    public void updateVisualization() {
        mapPane.updateVisualization();
        mapPane.repaint();
        // dirty hack to enforce repaint of panel
        getPanel().setVisible(!getPanel().isVisible());
        getPanel().setVisible(!getPanel().isVisible());
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        int preferredHeight = 100;
        if (getPanel() != null) {
            preferredHeight = getPanel().getPreferredHeight();
        }
        return new Dimension(state.controlElementsWidth, preferredHeight);
    }

    public void updateVisualisationControl() {
        if (getContentPane().getComponentCount() > 0) {
            getContentPane().removeAll();
        }
        VisualizationControlPanel panel = getPanel();
        if (panel != null) {
            getContentPane().add(panel);
            mapPane.getCurrentVisualization().setVisualizationUpdateListener(this);
            panel.updateSwitchControls();
            setTitle(panel.getName());
        }
        updateVisualization();
    }

    private VisualizationControlPanel getPanel() {
        if (mapPane != null && mapPane.getCurrentVisualization() != null) {
            return mapPane.getCurrentVisualization().getControlPanel();
        } else {
            return null;
        }
    }

}

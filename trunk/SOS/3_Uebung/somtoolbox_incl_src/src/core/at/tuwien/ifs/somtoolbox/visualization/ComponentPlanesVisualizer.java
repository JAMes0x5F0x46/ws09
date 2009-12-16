package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;

/**
 * This visualiser provides a visualisation of component planes, i.e. of the template vector elements.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: ComponentPlanesVisualizer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ComponentPlanesVisualizer extends AbstractItemVisualizer {

    private SOMLibTemplateVector templateVector = null;

    public ComponentPlanesVisualizer() {
        NUM_VISUALIZATIONS = 1;
        VISUALIZATION_NAMES = new String[] { "Component Planes" };
        VISUALIZATION_SHORT_NAMES = new String[] { "ComponentPlanes" };
        VISUALIZATION_DESCRIPTIONS = new String[] { "Visualization of component planes." };
        neededInputObjects = new String[] { SOMVisualisationData.TEMPLATE_VECTOR };
        setInterpolate(false);
    }

    @Override
    protected String getCacheKey(GrowingSOM gsom, int index, int width, int height) {
        return super.getCacheKey(gsom, index, width, height) + CACHE_KEY_SECTION_SEPARATOR + "component:" + currentElement;
    }

    public BufferedImage createVisualization(int index, int plane, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        currentElement = plane;
        return createVisualization(index, gsom, width, height);
    }

    @Override
    public BufferedImage createVisualization(int index, GrowingSOM gsom, int width, int height) throws SOMToolboxException {
        if (templateVector == null) {
            templateVector = gsom.getSharedInputObjects().getTemplateVector();
            if (templateVector == null) {
                throw new SOMToolboxException("You need to specify the " + neededInputObjects[0]);
            }
        }

        if (!(controlPanel instanceof ComponentPlaneControlPanel)) {
            // create control panel once we have the template vector, and if it is a generic panel
            controlPanel = new ComponentPlaneControlPanel(this, templateVector);
        }

        if (index == 0) {
            return createComponentPlaneImage(gsom, width, height);
        } else {
            return null;
        }
    }

    private BufferedImage createComponentPlaneImage(GrowingSOM gsom, int width, int height) {
        DoubleMatrix2D plane = createNormalizedComponentPlane(gsom, currentElement);
        return createImage(gsom, plane, width, height, interpolate);
    }

    private DoubleMatrix2D createNormalizedComponentPlane(GrowingSOM gsom, int component) {
        DoubleMatrix2D plane = new DenseDoubleMatrix2D(gsom.getLayer().getComponentPlane(component, 0));
        plane = plane.viewDice();

        // normalize component plane
        final double minValue = plane.aggregate(Functions.min, Functions.identity);
        final double maxValue = plane.aggregate(Functions.max, Functions.identity);
        plane.assign(new DoubleFunction() {
            public double apply(double argument) {
                return (argument - minValue) / (maxValue - minValue);
            }
        });

        return plane;

    }

    /**
     * A control panel extending the generic {@link AbstractBackgroundImageVisualizer.VisualizationControlPanel}, adding additionally a {@link JList}
     * and a {@link JTextField} for selecting a component from the {@link TemplateVector}.
     * 
     * @author Rudolf Mayer
     */
    private class ComponentPlaneControlPanel extends AbstractSelectedItemVisualizerControlPanel implements ActionListener, ListSelectionListener {
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new component-plane control panel
         * 
         * @param vis The ComponentPlanesVisualizer listening to updates from the list box.
         * @param templateVector The {@link TemplateVector} containing the components.
         */
        private ComponentPlaneControlPanel(ComponentPlanesVisualizer vis, SOMLibTemplateVector templateVector) {
            super("Comp. Planes Control");

            JPanel compPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();

            initialiseList(templateVector.getLabels());
            JScrollPane listScroller = new JScrollPane(list);

            constr.gridwidth = GridBagConstraints.REMAINDER;
            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;
            compPanel.add(listScroller, constr);

            text.setToolTipText("Enter a (part) of a component plane name, and start the search with the <enter> key");
            text.setText(templateVector.getLabel(currentElement));
            compPanel.add(text, constr);

            add(compPanel, c);
        }
    }

}

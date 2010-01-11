package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusterNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;

/**
 * EventHandler for selecting clusters by rectangle.
 * 
 * @author Rudolf Mayer
 * @author Jakob Frank
 * @version $Id: ClusterSelectionEventHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClusterSelectionEventHandler extends OrderedPSelectionEventHandler {

    private String selectionStatusString = "Selected Units:";

    private MapPNode mapPNode;

    private ArrayList<GeneralUnitPNode> markedNodes = new ArrayList<GeneralUnitPNode>();

    public ClusterSelectionEventHandler(PNode marqueeParent, PNode selectableParents) {
        super(marqueeParent, selectableParents);
        if (selectableParents instanceof MapPNode) { // e.g. in the ComponentPlaneClusteringFrame, we do not have a MapPNode, just a PNode
            this.mapPNode = (MapPNode) selectableParents;
            addSelectableParent(mapPNode.getUnitsNode()); // we need to specifically add the sub-node of MapPNode that contains the GeneralUnitPNodes
        } else {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Not setting mapPNode in ClusterSelectionEventHandler");
        }
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
        setMarqueePaint(Color.red);
        setMarqueePaintTransparency(0.3f);
    }

    @Override
    public void decorateSelectedNode(PNode node) {
        // do nothing now
        if (GeneralUnitPNode.class.isInstance(node)) {
            GeneralUnitPNode upn = (GeneralUnitPNode) node;
            upn.setSelected(true);
            selectionStatusString += " (" + upn.getUnit().getXPos() + "/" + upn.getUnit().getYPos() + ")";
            Logger.getLogger("at.tuwien.ifs.somtoolbox").finer(selectionStatusString);
        }
    }

    @Override
    public void undecorateSelectedNode(PNode node) {
        if (node instanceof GeneralUnitPNode) {
            ((GeneralUnitPNode) node).setSelected(false);
            selectionStatusString = "Selected Units:";
        }
    }

    @Override
    protected boolean isMarqueeSelection(PInputEvent e) {
        return true;
    }

    @Override
    protected void drag(PInputEvent e) {
        markCluster(e);
        markedNodes.clear();
        super.drag(e);
    }

    @Override
    public void mouseReleased(PInputEvent e) {
        super.mouseReleased(e);
        markedNodes.clear();
    }

    public void mouseClicked(PInputEvent event) {
        markCluster(event);
        markedNodes.clear();
    }

    // Enable Multi-Select with CTRL
    public boolean isOptionSelection(PInputEvent pie) {
        return pie.isControlDown();
    }

    private void markCluster(PInputEvent event) {
        PNode selectedNode = event.getPickedNode();
        while (selectedNode != null) {
            if (GeneralUnitPNode.class.isInstance(selectedNode) && !markedNodes.contains(selectedNode)) {
                GeneralUnitPNode gupNode = (GeneralUnitPNode) selectedNode;
                boolean doSelect = !isSelected(gupNode);

                if (mapPNode.getCurrentClusteringTree() != null) {
                    // System.out.println("marked unit: " + ((GeneralUnitPNode) selectedNode).getUnit());
                    // select all the units in this cluster
                    ClusterNode findNode = mapPNode.getCurrentClusteringTree().findClusterOf(gupNode, mapPNode.getState().numClusters);
                    GeneralUnitPNode[] nodes = findNode.getNodes();
                    for (GeneralUnitPNode generalUnitPNode : nodes) {
                        // System.out.println("selecting cluster unit " + generalUnitPNode.getUnit());
                        if (doSelect) {
                            super.select(generalUnitPNode);
                        } else {
                            super.unselect(generalUnitPNode);
                            System.out.println("removing " + generalUnitPNode.getUnit());
                        }

                        markedNodes.add(generalUnitPNode);
                    }
                    // System.out.println("\n");
                } else {
                    // Without explicit clustering each unit is its own cluster...
                    if (doSelect) {
                        super.select(gupNode);
                    } else {
                        super.unselect(gupNode);
                    }
                    markedNodes.add(gupNode);
                    // unselectAll();
                }
                return;
            }
            selectedNode = selectedNode.getParent();
        }
    }
}

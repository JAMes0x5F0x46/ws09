package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;

/**
 * EventHandler for selecting by rectangle.
 * 
 * @author Robert Neumayer
 * @version $Id: MyRectangleSelectionEventHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MyRectangleSelectionEventHandler extends OrderedPSelectionEventHandler {

    private String selectionStatusString = "Selected Units:";

    public MyRectangleSelectionEventHandler(PNode marqueeParent, PNode selectableParents) {
        super(marqueeParent, selectableParents);
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
        setMarqueePaint(Color.red);
        setMarqueePaintTransparency(0.3f);
        if (selectableParents instanceof MapPNode) {
            // we need to specifically add the sub-node of MapPNode that contains the GeneralUnitPNodes
            addSelectableParent(((MapPNode) selectableParents).getUnitsNode());
        }
    }

    public void decorateSelectedNode(PNode node) {
        // do nothing now
        if (GeneralUnitPNode.class.isInstance(node)) {
            GeneralUnitPNode upn = (GeneralUnitPNode) node;
            upn.setSelected(true);
            selectionStatusString += " (" + upn.getUnit().getXPos() + "/" + upn.getUnit().getYPos() + ")";
            Logger.getLogger("at.tuwien.ifs.somtoolbox").finer(selectionStatusString);
        }
    }

    public void undecorateSelectedNode(PNode node) {
        if (GeneralUnitPNode.class.isInstance(node)) {
            ((GeneralUnitPNode) node).setSelected(false);
            selectionStatusString = "Selected Units:";
        }
    }

    protected boolean isMarqueeSelection(PInputEvent arg0) {
        return true;
    }

    protected void drag(PInputEvent arg0) {
        super.drag(arg0);
    }

    // this would also be called when selecting additional areas with SHIFT key holded
    // thus, the StatusString would be reset. hence, it is reset only when undecorateSelectedNode (above) is called
    // protected void dragActivityFinalStep(PInputEvent arg0) {
    // System.out.println("drag final");
    // super.dragActivityFinalStep(arg0);
    // selectionStatusString = "Selected Units:";
    // }

    public void mouseClicked(PInputEvent event) {
        PNode selectedNode = event.getPickedNode();
        while (selectedNode != null) {
            if (GeneralUnitPNode.class.isInstance(selectedNode)) {
                super.select(selectedNode);
                return;
            }
            selectedNode = selectedNode.getParent();
        }
    }
}

package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.geom.Point2D;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.ArrowPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.input.InputCorrections;
import at.tuwien.ifs.somtoolbox.input.InputCorrections.InputCorrection;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * Used to move an input vector to a different unit.
 * 
 * @author Rudolf Mayer
 * @version $Id: MyInputDragSequenceEventHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MyInputDragSequenceEventHandler extends AbstractDragSequenceEventHandler {

    private InputCorrections shifts;

    public MyInputDragSequenceEventHandler(InputCorrections shifts) {
        this.shifts = shifts;
        allowedNodeTypes = new String[] { "data" };
    }

    protected void endDrag(PInputEvent e) {
        super.endDrag(e);

        final PText pText = (PText) e.getPickedNode();
        final PNode pNode = pText.getParent();
        final String label = pText.getText();
        final GeneralUnitPNode sourceUnitNode = (GeneralUnitPNode) pNode.getParent();
        final MapPNode mapPNode = sourceUnitNode.getMapPNode();

        // find new unit
        final Point2D endPos = e.getPosition();
        final int newX = (int) (endPos.getX() / sourceUnitNode.getWidth());
        final int newY = (int) (endPos.getY() / sourceUnitNode.getHeight());
        final GeneralUnitPNode targetUnitNode = mapPNode.getUnit(newX, newY);

        // remove input from old unit, both graphically (GeneralUnitPNode) and logically (Unit)
        sourceUnitNode.removeChild(pNode);
        sourceUnitNode.getUnit().removeMappedInput(label);
        sourceUnitNode.updateDetailsAfterMoving();

        // and add it to the new unit
        targetUnitNode.addChild(pNode);
        targetUnitNode.getUnit().addMappedInput(label, 0.0, true); // FIXME: if input vector available, we should calculate the new distance
        targetUnitNode.updateDetailsAfterMoving();

        InputCorrection correction;
        try {
            correction = shifts.addManualInputCorrection(sourceUnitNode.getUnit(), targetUnitNode.getUnit(), label);
            System.out.println(shifts);

            // remove potentially existing arrow
            if (!correction.getSourceUnit().equals(sourceUnitNode.getUnit())) {
                for (int i = 0; i < mapPNode.getInputCorrectionsPNode().getChildrenCount(); i++) {
                    PNode node = mapPNode.getInputCorrectionsPNode().getChild(i);
                    if (StringUtils.equals(node.getAttribute(SOMVisualisationData.INPUT_CORRECTIONS), label)) {
                        mapPNode.getInputCorrectionsPNode().removeChild(node);
                        break;
                    }
                }
            }

            // draw an arrow for the mapping shift
            ArrowPNode arrow = ArrowPNode.createInputCorrectionArrow(correction, InputCorrections.CreationType.MANUAL, sourceUnitNode, targetUnitNode);
            mapPNode.getInputCorrectionsPNode().addChild(arrow);
            arrow.moveToBack();
        } catch (SOMToolboxException ex) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Error moving input: " + ex.getMessage());
        }

    }

}

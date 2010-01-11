package at.tuwien.ifs.somtoolbox.util;

import java.awt.Color;

import at.tuwien.ifs.somtoolbox.apps.viewer.handlers.EditLabelEventListener;
import at.tuwien.ifs.somtoolbox.visualization.clustering.ClusteringTree;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * Convenience class for creating new labels. (cluster and manual)
 * 
 * @author Angela Roiger
 * @version $Id: LabelPNodeGenerator.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LabelPNodeGenerator {
    private static EditLabelEventListener labelListener = new EditLabelEventListener();

    /**
     * Adds an additional line of text to the label.
     * 
     * @param labelNode The node to add the text to
     * @param text the text string
     * @param fontSize the desired font size
     * @param xOffset
     * @param yOffset
     */
    public static void addTextToLabel(PNode labelNode, String text, int fontSize, double xOffset, double yOffset) {
        PText labelText = newLabelText(text, fontSize, 0, 0);
        labelText.setOffset(labelNode.getChild(labelNode.getChildrenCount() - 1).getXOffset(),
                labelNode.getChild(labelNode.getChildrenCount() - 1).getYOffset() + labelNode.getChild(labelNode.getChildrenCount() - 1).getHeight());
        labelNode.addChild(labelText);
        // ok, his function is never used. can probably be deleted
    }

    public static void changeColor(PText label, Color c) {
        label.setPaint(c);
    }

    /**
     * Set the rotation of the PNode in radians
     * 
     * @param labelNode the node to be rotated
     * @param rotation the rotation in radians
     */
    public static void changeRotation(PNode labelNode, double rotation) {
        labelNode.setRotation(rotation);
    }

    public static PNode newLabel(PText textNode) {
        PNode labelNode = new PNode();
        labelNode.addChild(textNode);
        labelNode.addInputEventListener(labelListener);
        return labelNode;
    }

    public static PNode newLabel(String text, int fontSize) {
        return newLabel(newLabelText(text, fontSize, 0, 0));
    }

    public static PNode newLabel(String text, int fontSize, double xPos, double yPos, double xOffset, double yOffset) {
        PNode labelNode = newLabel(text, fontSize);
        labelNode.setX(xPos);
        labelNode.setY(yPos);
        labelNode.setOffset(xOffset, yOffset);
        return labelNode;
    }

    public static PNode newLabelNode(double xOffset, double yOffset, double rotation) {
        PNode n = new PNode();
        n.setOffset(xOffset, yOffset);
        n.setRotation(rotation);
        return n;
    }

    public static PText newLabelText(String text, float fontSize) {
        return newLabelText(text, fontSize, 0, 0);
    }

    public static PText newLabelText(String text, float fontSize, double xOffset, double yOffset) {
        PText labelText = new PText(text);
        labelText.setFont(ClusteringTree.defaultFont.deriveFont(fontSize));
        labelText.setOffset(xOffset, yOffset);
        labelText.addAttribute("type", "manualLabel");
        return labelText;
    }

    public static PText newLabelTextLocation(String text, float fontSize, double x, double y) {
        PText labelText = new PText(text);
        labelText.setFont(ClusteringTree.defaultFont.deriveFont(fontSize));
        labelText.setX(x);
        labelText.setY(y);
        labelText.addAttribute("type", "manualLabel");
        return labelText;
    }

}

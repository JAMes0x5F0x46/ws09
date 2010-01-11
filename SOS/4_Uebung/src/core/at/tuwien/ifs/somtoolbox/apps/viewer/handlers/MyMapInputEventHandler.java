package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.MapPNode;
import at.tuwien.ifs.somtoolbox.apps.viewer.ToolTipPNode;
import at.tuwien.ifs.somtoolbox.layers.Unit;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * @author Robert Neumayer
 * @version $Id: MyMapInputEventHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MyMapInputEventHandler extends PBasicInputEventHandler {
    private ToolTipPNode tooltipNode;

    private PNode node;

    private MapPNode mapNode;

    public MyMapInputEventHandler(ToolTipPNode tpn, PNode node) {
        super();
        tooltipNode = tpn;
        this.node = node;
        if (node instanceof MapPNode) {
            this.mapNode = (MapPNode) node;
        }
    }

    public void mouseExited(PInputEvent event) {
        tooltipNode.setText(null);
    }

    public void mouseClicked(PInputEvent event) {
        PNode n = event.getInputManager().getMouseOver().getPickedNode();
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
        if (event.getClickCount() == 2 && n instanceof PText) {
            PText pt = (PText) n;
            if (((String) pt.getAttribute("type")).equals("data")) {
                // open file in according application
                String fileLocation = null;
                try {
                    fileLocation = URLDecoder.decode((String) pt.getAttribute("location"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Error decoding file location string. Using undecoded string.");
                    fileLocation = (String) pt.getAttribute("location");
                }
                fileLocation = CommonSOMViewerStateData.fileNamePrefix + File.separator + fileLocation;
                String[] command = new String[2];
                command[0] = "firefox"; // FIXME: should be a viewer based on mime-type
                command[1] = fileLocation;
                try {
                    Runtime.getRuntime().exec(command);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    Logger.getLogger("at.tuwien.ifs.somtoolbox").warning("Error executing external application: " + command[0] + " " + command[1]);
                }
            }
        } else if (event.getClickCount() == 2 && n instanceof GeneralUnitPNode) {
            // animate to fit node on screen
            ((PLayer) node.getParent()).getCamera(0).animateViewToCenterBounds(
                    new Rectangle2D.Double(n.getX(), n.getY(), n.getWidth(), n.getHeight()), true, 2000);
        } else if (event.getClickCount() == 1 && n instanceof GeneralUnitPNode) {
            System.out.println(((GeneralUnitPNode) n).getUnit().printUnitDetails(mapNode.getInputObjects().getInputData(),
                    mapNode.getInputObjects().getTemplateVector()));
        }
    }

    public void mouseMoved(PInputEvent event) {
        updateToolTip(event);
    }

    public void updateToolTip(PInputEvent event) {
        PNode node = event.getInputManager().getMouseOver().getPickedNode();
        String tooltipString = (String) node.getAttribute("tooltip");
        if (StringUtils.isNotBlank(tooltipString) || node instanceof GeneralUnitPNode) {
            tooltipNode.setOffset(node.localToGlobal(new Point2D.Double(node.getX(), node.getY())));
            if (node instanceof PText) {
                tooltipNode.setFont(((PText) node).getFont());
            } else {
                tooltipNode.setFont(MapPNode.DEFAULT_TOOLTIP_FONT);
            }
            if (StringUtils.isBlank(tooltipString) && node instanceof GeneralUnitPNode) {
                tooltipNode.setOffset(tooltipNode.getOffset().getX() + 2, tooltipNode.getOffset().getY() + 2);
                // show full unit info if features are less than 15
                Unit unit = ((GeneralUnitPNode) node).getUnit();
                if (unit.getDim() <= 15) {
                    tooltipString = unit.printUnitDetails(mapNode.getInputObjects().getInputData(), mapNode.getInputObjects().getTemplateVector());
                } else {
                    tooltipString = "Unit " + unit.printCoordinates();
                }
                tooltipNode.setFont(MapPNode.DEFAULT_TOOLTIP_FONT_UNITINFO);

            }
            tooltipNode.setText(tooltipString);
        }
    }
}
package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.event.InternalFrameEvent;

import as.eval.EvalTool;
import as.eval.interfaces.EvalNode;
import as.eval.interfaces.EvalToolConnector;
import as.eval.interfaces.Position2D;
import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * The class EvalSOMPanel integrates EvalTool to the SOMToolbox and brings all
 * needed information to the right format.
 * 
 * @author Andreas Senfter
 * @version $Id: EvalSOMPanel.java 2874 2009-12-11 16:03:27Z frank $
 */
public class EvalSOMPanel extends AbstractViewerControl implements
	EvalToolConnector {

    private static final long serialVersionUID = 1L;

    private EvalTool tool;

    private PlaySOMPanel PlaySOM;

    /**
     * Constructor
     * 
     * @param state
     * @param playSOM
     */
    public EvalSOMPanel(CommonSOMViewerStateData state, PlaySOMPanel playSOM) {
	super("EvalSOM Control", state, new GridBagLayout());
	initGUIElements();
	setVisible(true);

	this.PlaySOM = playSOM;
    }

    /**
     * inits the GUI
     */
    protected void initGUIElements() {
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 0.1;
	constraints.weighty = 0.1;

	EvalTool.STAND_ALLONE = false;

	tool = new EvalTool();
	tool.setEvalToolConnector(this);
	getContentPane().add(tool, constraints);

    }

    /**
     * Overrides the method EvalToolConnector.getNodes()
     */
    public Collection<EvalNode> getNodes() {

	GeneralUnitPNode[] gupnA = PlaySOM.selections[PlaySOM.currentSelectionArea];

	if (gupnA != null) {

	    Unit[] units = new Unit[gupnA.length];
	    for (int i = 0; i < gupnA.length; i++)
		units[i] = gupnA[i].getUnit();

	    // sorts the Array. starts with lowest x value. line per line.
	    Arrays.sort(units);
	    int selectionHeight = (int) gupnA[0].getHeight();
	    int selectionWidth = (int) gupnA[0].getWidth();

	    // minx/miny is first entry
	    int minx = units[0].getXPos();
	    int miny = units[0].getYPos();
	    int maxx = units[units.length - 1].getXPos();
	    int maxy = units[units.length - 1].getYPos();

	    UnitMerger merger = new UnitMerger(minx, maxx, miny, maxy,
		    (int) gupnA[0].getWidth(), (int) gupnA[0].getHeight());

	    List<EvalNode> list = new ArrayList<EvalNode>();

	    // for all selected units save the nodes inculding their distance to
	    // the
	    // unit center
	    for (GeneralUnitPNode gupn : gupnA) {
		Unit curr = gupn.getUnit();
		for (int i = 0; i < gupn.getUnit().getNumberOfMappedInputs(); i++) {
		    String name = curr.getMappedInputName(i);
		    double dist = curr.getMappedInputDistance(i);
		    Point loc = gupn.getLocations()[i];
		    list.add(new Node(name, dist, loc, (curr.getXPos() - minx)
			    * selectionWidth, (curr.getYPos() - miny)
			    * selectionHeight, merger));
		}
	    }

	    return list;
	}

	// return empty List
	return new ArrayList<EvalNode>();
    }

    //@Override
    public void internalFrameClosing(InternalFrameEvent e) {
	tool.closePrompt();
    }

    /**
     * The inner class Node extends as.eval.interfaces.EvalNode. One instance
     * represents one selected track including its position in 2D.
     * 
     * @author Andreas Senfter
     */
    private class Node extends EvalNode {

	private Position2D pos;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param distance
	 * @param location
	 * @param offsetX
	 * @param offsetY
	 * @param merger
	 */
	public Node(String name, Double distance, Point location, int offsetX,
		int offsetY, UnitMerger merger) {

	    super(CommonSOMViewerStateData.fileNamePrefix + name + CommonSOMViewerStateData.fileNameSuffix);

	    pos = new Position2D(location.x + offsetX, location.y + offsetY);
	    pos.setUnitCenter(merger.getCenter(), merger.Width() / 2);
	}

	/**
	 * returns the position instance.
	 */
	public Position2D getPosition() {
	    return pos;
	}
    }

    /**
     * The inner class UnitMerger merges several units to a single unit.
     * 
     * @author Andreas Senfter
     */
    private class UnitMerger {

	private int height;
	private int width;
	private Point center;

	/**
	 * Constructor
	 * 
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @param with
	 * @param height
	 */
	public UnitMerger(int minX, int maxX, int minY, int maxY, int with,
		int height) {
	    super();

	    this.height = (maxY - minY + 1) * height;
	    this.width = (maxX - minX + 1) * with;
	    this.center = new Point(this.width / 2, this.height / 2);
	}

	/**
	 * returns the center point
	 * 
	 * @return the center point
	 */
	public Point getCenter() {
	    return center;
	}

	/**
	 * returns the width
	 * 
	 * @return the width
	 */
	public int Width() {
	    return width;
	}

	/**
	 * returns the heigth
	 * 
	 * @return the heigth
	 */
	public int Height() {
	    return height;
	}
    }
}
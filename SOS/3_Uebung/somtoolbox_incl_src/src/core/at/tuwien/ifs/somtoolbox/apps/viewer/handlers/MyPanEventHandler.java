package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.event.InputEvent;

import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PPanEventHandler;

/**
 * An EventHandler for scrolling the Pane.
 * 
 * @author Robert Neumayer
 * @version $Id: MyPanEventHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MyPanEventHandler extends PPanEventHandler {

    public MyPanEventHandler() {
        super();
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON3_MASK));
        setAutopan(true);
    }
}

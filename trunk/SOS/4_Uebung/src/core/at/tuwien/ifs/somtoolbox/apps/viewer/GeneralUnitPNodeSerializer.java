package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * replacement class for serializing and de-serializing a {@link GeneralUnitPNode}
 * 
 * @author Angela Roiger
 * @version $Id: GeneralUnitPNodeSerializer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class GeneralUnitPNodeSerializer implements Serializable {
    private static final long serialVersionUID = 1L;

    // position of the GeneralUnitPNode in MapPNode.units
    private int x;

    private int y;

    // for deserialization
    public GeneralUnitPNodeSerializer() {
    }

    public GeneralUnitPNodeSerializer(GeneralUnitPNode unit) {
        this.x = unit.getUnit().getXPos();
        this.y = unit.getUnit().getYPos();

        // small check to make sure we can restore it
        if (CommonSOMViewerStateData.getInstance().mapPNode.getUnit(x, y) != unit)
            throw new AssertionError("GeneralUnitPNode is not where it's supposed to be.");
    }

    // Called by deserialization. Find and return the "real" GeneralUnitPNode.
    private Object readResolve() throws ObjectStreamException {
        return CommonSOMViewerStateData.getInstance().mapPNode.getUnit(x, y);
    }
}

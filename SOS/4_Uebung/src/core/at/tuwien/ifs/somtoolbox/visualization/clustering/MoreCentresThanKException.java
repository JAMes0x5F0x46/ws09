package at.tuwien.ifs.somtoolbox.visualization.clustering;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * In case anyone dares to set more centres for a K-Means guy -- that's what you're gonna get.
 * @author neumayer
 * @version $Id: MoreCentresThanKException.java 2874 2009-12-11 16:03:27Z frank $
 *
 */

public class MoreCentresThanKException extends SOMToolboxException {

    private static final long serialVersionUID = 1L;

    public MoreCentresThanKException() {
        super();
    }

    public MoreCentresThanKException(String message) {
        super(message);
    }

}
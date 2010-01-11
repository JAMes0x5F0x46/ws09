package at.tuwien.ifs.somtoolbox.apps.viewer.controls;

import java.awt.BorderLayout;

import javax.swing.ListSelectionModel;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.DocViewPanel;

/**
 * This class provides the link to the {@link DocViewPanel}
 * 
 * @author Christoph Becker
 * @see at.tuwien.ifs.somtoolbox.apps.viewer.controls.AbstractSelectionPanel
 * @version $Id: DocSOMPanel.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DocSOMPanel extends AbstractSelectionPanel {
    private static final long serialVersionUID = 1L;

    /**
     * creates a new DocSOMPanel with the provided state, containing a simple list, nothing more, and inits the selection listener
     */
    public DocSOMPanel(CommonSOMViewerStateData state) {
        super(new BorderLayout(), state, "DocSOM Control", 1);
        playlists[0].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        initGui();
        setVisible(true);
    }

    private void initGui() {
        addSingleListScrollPanel(null);
    }
}

package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tuwien.ifs.somtoolbox.apps.viewer.GeneralUnitPNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

/**
 * A Selection Event Handler that stores the selected items in the selection (insertion) order.
 * 
 * @author Rudolf Mayer
 * @version $Id: OrderedPSelectionEventHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
@SuppressWarnings("unchecked")
public class OrderedPSelectionEventHandler extends PSelectionEventHandler {

    protected Set<GeneralUnitPNode> currentSelection;

    public OrderedPSelectionEventHandler(PNode marqueeParent, PNode selectableParent) {
        super(marqueeParent, selectableParent);
    }

    public OrderedPSelectionEventHandler(PNode marqueeParent, List selectableParents) {
        super(marqueeParent, selectableParents);
    }

    @Override
    protected void init() {
        currentSelection = Collections.synchronizedSet(new LinkedHashSet<GeneralUnitPNode>());
        super.init();
    }

    /**
     * Returns a copy of the currently selected nodes. Overriding the super-class method to ensure an insertion-order of the elements (the super class
     * returns the keyset of a hashmap).
     */
    @Override
    public Collection<GeneralUnitPNode> getSelection() {
        if (currentSelection != null) {
            return this.currentSelection;
        } else {
            return super.getSelection();
        }
    }

    @Override
    public void select(Collection items) {
        currentSelection.addAll(items);
        super.select(items);
    }

    @Override
    public void unselect(Collection items) {
        super.unselect(items);
        currentSelection.removeAll(items);
    }

    @Override
    protected void startDrag(PInputEvent e) {
        if (!isOptionSelection(e)) {
            this.currentSelection = Collections.synchronizedSet(new LinkedHashSet<GeneralUnitPNode>());
        }
        super.startDrag(e);
    }

    /**
     * check if an object has already been selected or not
     * 
     * @param o - object to search for in the current selection
     * @return true if the object is already selected, false otherwise.
     */
    protected boolean alreadySelected(Object o) {
        return currentSelection.contains(o);
        // for (int i = 0; i < this.currentSelection.size(); i++) {
        // if (this.currentSelection.elementAt(i).equals(o)) {
        // return true;
        // }
        // }
        // return false;
    }

    @Override
    public void select(PNode node) {
        if (node instanceof GeneralUnitPNode) {
            currentSelection.add((GeneralUnitPNode) node);
        }
        super.select(node);
    }

    @Override
    public void unselect(PNode node) {
        super.unselect(node);
        currentSelection.remove(node);
    }

    @Override
    public void unselectAll() {
        super.unselectAll();
        currentSelection = Collections.synchronizedSet(new LinkedHashSet<GeneralUnitPNode>());
    }

    @Override
    public void select(Map items) {
        for (Object object : items.keySet()) {
            if (object instanceof GeneralUnitPNode) {
                currentSelection.add((GeneralUnitPNode) object);
            }
        }
        super.select(items);
    }

}
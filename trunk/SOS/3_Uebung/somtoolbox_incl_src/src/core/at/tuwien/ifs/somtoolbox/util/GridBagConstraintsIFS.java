package at.tuwien.ifs.somtoolbox.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * A helper class around {@link GridBagConstraints}, providing convenience methods to set locations, etc..
 * 
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class GridBagConstraintsIFS extends java.awt.GridBagConstraints {

    private static final long serialVersionUID = 1L;

    /** New constraints top-left, top-alignment left. */
    public GridBagConstraintsIFS() {
        gridx = 0;
        gridy = 0;
        anchor = NORTHWEST;
    }

    public GridBagConstraintsIFS(int anchor, int fill) {
        gridx = 0;
        gridy = 0;
        this.fill = fill;
        this.anchor = anchor;
    }

    public GridBagConstraintsIFS setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public GridBagConstraintsIFS setPadding(int padx, int pady) {
        this.ipadx = padx;
        this.ipady = pady;
        return this;
    }

    public GridBagConstraintsIFS setPadding(int padx, int pady, Insets insets) {
        setPadding(padx, pady);
        return setInsets(insets);
    }

    public GridBagConstraintsIFS setInsets(int padding) {
        this.insets = new Insets(padding, padding, padding, padding);
        return this;
    }

    public GridBagConstraintsIFS setInsets(int x, int y) {
        this.insets = new Insets(y, x, y, x);
        return this;
    }

    public GridBagConstraintsIFS setInsets(Insets insets) {
        this.insets = insets;
        return this;
    }

    /** move into the next column */
    public GridBagConstraintsIFS nextCol() {
        gridx++;
        return this;
    }

    /** move into the next row */
    public GridBagConstraintsIFS nextRow() {
        gridx = 0;
        gridy++;
        return this;
    }

    /** moves to the given position */
    public GridBagConstraintsIFS moveTo(int x, int y) {
        gridx = x;
        gridy = y;
        return this;
    }

    /** set the grid width */
    public GridBagConstraintsIFS setGridWidth(int gridwidth) {
        this.gridwidth = gridwidth;
        return this;
    }

    /** set the grid height */
    public GridBagConstraintsIFS setGridHeight(int gridheight) {
        this.gridheight = gridheight;
        return this;
    }

    /** Create a new instance with horizontal weight 1 */
    public GridBagConstraintsIFS fillWidth() {
        GridBagConstraintsIFS clone = this.clone();
        clone.weightx = 1.0;
        return clone;
    }

    /** Create a new instance with vertical weight 1 */
    public GridBagConstraintsIFS fillHeight() {
        GridBagConstraintsIFS clone = this.clone();
        clone.weighty = 1.0;
        return clone;
    }

    @Override
    public GridBagConstraintsIFS clone() {
        // TODO Auto-generated method stub
        return (GridBagConstraintsIFS) super.clone();
    }

}

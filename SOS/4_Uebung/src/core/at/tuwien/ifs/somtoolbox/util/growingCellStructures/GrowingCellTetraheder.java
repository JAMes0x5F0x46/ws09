package at.tuwien.ifs.somtoolbox.util.growingCellStructures;

import at.tuwien.ifs.somtoolbox.layers.GrowingCellUnit;

/**
 * A Tetraheder of 3 Cellstructures (for efficient adding and removing of units)
 * 
 * @author Johannes Inführ
 * @author Andreas Zweng
 * @version $Id: GrowingCellTetraheder.java 2874 2009-12-11 16:03:27Z frank $
 */
public class GrowingCellTetraheder {
    /** The Units that belong to this Tetraheder */
    private GrowingCellUnit[] cellUnits;

    /**
     * Std Constructor, creates Tetraheder with Units c1,c2 and c3 (and connects them to this tetraheder)
     * 
     * @param c1
     * @param c2
     * @param c3
     */
    public GrowingCellTetraheder(GrowingCellUnit c1, GrowingCellUnit c2, GrowingCellUnit c3) {
        cellUnits = new GrowingCellUnit[3];

        cellUnits[0] = c1;
        cellUnits[1] = c2;
        cellUnits[2] = c3;

        c1.connect(this);
        c2.connect(this);
        c3.connect(this);
    }

    public GrowingCellUnit[] getCellUnits() {
        return cellUnits;
    }

    public boolean equals(Object o) {
        return this == o;
    }

    /**
     * @param unit
     * @return true if this Tetraeder connects unit
     */
    public boolean contains(GrowingCellUnit unit) {
        for (GrowingCellUnit u : cellUnits) {
            if (u.equals(unit))
                return true;
        }

        return false;
    }

    /**
     * @param u1
     * @param u2
     * @return Unit !=u1 and !=u2
     */
    public GrowingCellUnit getRemainingUnit(GrowingCellUnit u1, GrowingCellUnit u2) {
        for (GrowingCellUnit u : cellUnits) {
            if (!u.equals(u1) && !u.equals(u2))
                return u;
        }

        return null;
    }
}

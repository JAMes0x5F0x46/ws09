package at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers;

import at.tuwien.ifs.somtoolbox.layers.Unit;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: UnitQEContainer.java 2874 2009-12-11 16:03:27Z frank $
 */
public class UnitQEContainer implements QEContainer {

    private double qe;

    private Unit[] unit;

    public UnitQEContainer() {

    }

    public UnitQEContainer(Unit[] unit, double qe) {
        this.setQE(qe);
        this.setUnit(unit);
    }

    public void setUnit(Unit[] unit) {
        this.unit = unit;
    }

    public double getQE() {
        return qe;
    }

    public void setQE(double qe) {
        this.qe = qe;
    }

    public int getNumUnits() {
        return this.unit.length;
    }

    public String getUnitCoords(int index) {
        return this.unit[index].getXPos() + "," + this.unit[index].getYPos();
    }

    public int getNumberOfVectorsMapped(int index) {
        return this.unit[index].getNumberOfMappedInputs();
    }
}

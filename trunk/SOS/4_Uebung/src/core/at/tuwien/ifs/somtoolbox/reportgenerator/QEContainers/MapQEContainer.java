package at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: MapQEContainer.java 2874 2009-12-11 16:03:27Z frank $
 */

public class MapQEContainer implements QEContainer {

    protected double qe;

    public MapQEContainer() {

    }

    public MapQEContainer(double qe) {
        this.setQE(qe);
    }

    public void setQE(double qe) {
        this.qe = qe;
    }

    public double getQE() {
        return this.qe;
    }

}

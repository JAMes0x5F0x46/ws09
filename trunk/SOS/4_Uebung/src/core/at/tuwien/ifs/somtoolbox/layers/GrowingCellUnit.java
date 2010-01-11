package at.tuwien.ifs.somtoolbox.layers;

import java.util.LinkedList;
import java.util.List;

import at.tuwien.ifs.somtoolbox.util.growingCellStructures.GrowingCellTetraheder;

/**
 * Extension of Unit, needed to save additional data of growing cell structures
 * 
 * @author Johannes Inführ
 * @author Andreas Zweng
 * @version $Id: GrowingCellUnit.java 2874 2009-12-11 16:03:27Z frank $
 */
public class GrowingCellUnit extends Unit {
    /** The Tetraheders this Unit is connected to */
    private List<GrowingCellTetraheder> connectedTetraheders;

    /** The signal counter of this unit */
    private double signalCounter;

    /** The estimate of space covered by this unit */
    private double voronoiEstimate;

    // displayparameters
    private int diameter = 10;

    /** Position of Unit in Displayspace X */
    private double posX;

    /** Position of Unit in Displayspace Y */
    private double posY;

    public double getSignalCounter() {
        return signalCounter;
    }

    public void setSignalCounter(double d) {
        this.signalCounter = d;
    }

    /**
     * Std Constructor, initializes the unit wich weights
     * 
     * @param layer The layer on which this unit resides
     * @param weights The weight vector of this unit
     */
    public GrowingCellUnit(GrowingCellLayer layer, double[] weights) {
        super(layer, -1, -1, weights);

        connectedTetraheders = new LinkedList<GrowingCellTetraheder>();
        signalCounter = 0;
        voronoiEstimate = 0;
    }

    public double getVoronoiEstimate() {
        return voronoiEstimate;
    }

    public void setVoronoiEstimate(double voronoiEstimate) {
        this.voronoiEstimate = voronoiEstimate;
    }

    /**
     * Connects this unit to Tetraheder ct
     * 
     * @param ct
     */
    public void connect(GrowingCellTetraheder ct) {
        connectedTetraheders.add(ct);
    }

    /**
     * @return Tetraheders this unit is connected to
     */
    public List<GrowingCellTetraheder> getConnectedTetraheders() {
        return connectedTetraheders;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    public String toString() {
        // return "cu: no of tets: "+connectedTetraheders.size()+" sigCount: "+signalCounter+" ("+posX+","+posY+")";
        return "unit pos: (" + posX + "," + posY + ")";
    }

    /**
     * Disconnects the Unit from tetraheder t
     * 
     * @param t
     */
    public void disconnect(GrowingCellTetraheder t) {
        connectedTetraheders.remove(t);
    }

    /**
     * Puts the Unit at Position (x,y) in Display-Space
     * 
     * @param x
     * @param y
     */
    public void putAtPosition(double x, double y) {
        posX = x;
        posY = y;
    }

    /**
     * @return X-Coordinate of Unit in Display-Space
     */
    public double getX() {
        return posX;
    }

    /**
     * @return Y-Coordinate of Unit in Display-Space
     */
    public double getY() {
        return posY;
    }

    @Override
    public int getXPos() {
        return (int) getX();
    }

    @Override
    public int getYPos() {
        return (int) getY();
    }

    /**
     * @return Diameter of Unit (for physics simulation)
     */
    public int getDiameter() {
        return diameter;
    }

    /**
     * @param deltax Movement along x-axis
     * @param deltay Movement along y-axis
     */
    public void applyMovement(double deltax, double deltay) {
        posX += deltax;
        posY += deltay;
    }

    public GrowingCellUnit clone() {
        GrowingCellUnit u = new GrowingCellUnit((GrowingCellLayer) getLayer(), getWeightVector());
        u.posX = posX;
        u.posY = posY;

        return u;
    }

}

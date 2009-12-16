package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.util.ArrayList;

/**
 * @author Martin Waitzbauer (0226025)
 * @version $Id: SemanticNode.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SemanticNode {

    // private int[] Directions = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    public String WELocationCell = null;

    public String NSLocationCell = null;

    public int NordSouth = 0;

    public int WestEast = 0;

    public int Region = 0;

    public String SpecialLocation = null;

    public int[] realCoordinates = new int[2];

    public Boolean empty = null;

    private int Orientation;

    public String Description = "";

    public ArrayList<SemanticClass> Classes = null;

    public SemanticNode() {
        super();
    }

    public void setNorthSouthDegree(int d) {
        this.NordSouth = d;
    }

    public void setWestEastDegree(int d) {
        this.WestEast = d;
    }

    public void setWELocationCell(String l) {
        this.WELocationCell = l;
    }

    public void setNSLocationCell(String l) {
        this.NSLocationCell = l;
    }

    public void setRegion(int c) {
        this.Region = c;
    }

    /**
     * This method is used for improving the quality of description of the semantic node. It checks for the given String and the position of the Grid
     * the relation to the surrounding grids. Gets the corresponding String Value to the given String
     * 
     * @param Direction: in which
     * @return
     */
    public void setSpecialLocation(String Direction) {
        this.SpecialLocation = Direction;
    }

    /**
     * Sets the Nodes Coordiantes to thegiven values
     * 
     * @param x
     * @param y
     */
    public void setRealCoordinates(int x, int y) {
        this.realCoordinates[0] = x;
        this.realCoordinates[1] = y;
    }

    public void setcontainsNoClasses(boolean is) {
        this.empty = is;
    }

    public void addClass(SemanticClass sClass) {
        if(this.Classes == null)
            Classes = new ArrayList<SemanticClass>();
        if (!this.Classes.contains(sClass)) {
            this.Classes.add(sClass);
        }
    }

    /**
     * Gives a semantic Explanation of the Units location @
     */
    public void setDescription() {
        String result = "";
        result += "in the " + SemanticInterpreterGrid.getRegion(this.Region) + ",";
        String Strongness = "";
        if (this.NordSouth == this.WestEast) { // gleich st�rke, Abk�rzung m�glich
            if (this.NSLocationCell != null && this.WELocationCell != null) {
                Strongness += this.getDegree(this.NordSouth) + getShortForm(this.NSLocationCell, this.WELocationCell);
            } else {
                String a = "";
                String b = "";
                String temp = this.getDegree(this.NordSouth);
                if (this.WestEast == 3) {
                    a = "the East";
                }
                if (this.WestEast == 2) {
                    a = "central";
                }
                if (this.WestEast == 1) {
                    a = "the West";
                }
                if (this.NordSouth == 3) {
                    b = "the South";
                }
                if (this.NordSouth == 2) {
                    b = "central";
                }
                if (this.NordSouth == 1) {
                    b = "the North";
                }
                // if(!a.equals("central") && !b.equals("central"))
                temp = getShortForm(b, a);
                Strongness += this.getOrientation(this.Orientation) + this.getDegree(NordSouth) + temp;
            }
            result += Strongness;
        } else {
            if (this.NSLocationCell != null && this.WELocationCell != null) {
                result += "." + this.getDegree(this.NordSouth) + " the " + this.NSLocationCell + " and " + this.getDegree(this.WestEast) + " in the "
                        + this.WELocationCell;
            } else {
                if (this.NSLocationCell == null && WELocationCell != null) {
                    String temp = "";
                    if (this.NordSouth == 1) {
                        temp = "the North";
                    }
                    if (this.NordSouth == 3) {
                        temp = "the South";
                    }
                    result += this.getDegree(this.NordSouth) + temp + " " + this.getDegree(this.WestEast) + " "
                            + this.getOrientation(this.Orientation);
                }
                if (this.WELocationCell == null && NSLocationCell != null) {
                    String temp = "";
                    if (this.WestEast == 1) {
                        temp = "the West";
                    }
                    if (this.WestEast == 3) {
                        temp = "the East";
                    }
                    result += this.getDegree(this.WestEast) + temp + " " + this.getDegree(this.NordSouth) + " "
                            + this.getOrientation(this.Orientation);
                }
                if (this.WELocationCell == null && NSLocationCell == null) {
                    String temp = "";
                    switch (this.Orientation) {
                        case 1:
                            temp = "north";
                            break;
                        case 2:
                            temp = "south";
                            break;
                        case 3:
                            temp = "west";
                            break;
                        case 4:
                            temp = "east";
                            break;
                    }
                    result += "in the " + temp + " Middle";
                }
            }
        }
        if (this.SpecialLocation != null) {
            this.Description = result + "." + this.SpecialLocation;
        } else {
            this.Description = result;
        }
    }
    
    public Boolean containsNoClasses() {
         if(empty!=null)
             return empty;
         else
             return null;
    }

    /**
     * Sets the Orientation 1= North of Middle, 2 = South, 3 = West, 4= east of middle
     * 
     * @param i
     */
    public void setOrientation(int i) {
        this.Orientation = i;
    }

    private String getOrientation(int i) {
        switch (i) {
            case 0:
                return "directly in the Middle";
            case 1:
                return "north of the Central Point";
            case 2:
                return "south of the Central Point";
            case 3:
                return "west of the Central Point";
            case 4:
                return "east of the Central Point";
            default:
                return "Could not find Orientation";
        }
    }

    /**
     * Retunrs the String representation of a SemanticNodes Coordinates
     * 
     * @param d
     * @return
     */
    private String getDegree(int d) {
        switch (d) {
            case 8:
                return "Very strong in";
            case 7:
                return "strong in";
            case 6:
                return "average in";
            case 5:
                return "weak in";
            case 4:
                return "very weak in";
            case 3:
                return "in the direction to ";
            case 2:
                return "central ";
            case 1:
                return "in the direction to ";
            default:
                return "";
        }
    }

    /**
     * Gets a short form for directions
     * 
     * @param a First term eg Nord, S�d,
     * @param b 2ndTerm eg Westlich, S�dlich
     * @return
     */
    private String getShortForm(String a, String b) {
        String result = "";
        if (a.equals("North")) {
            result = "North-";
        }
        if (a.equals("South")) {
            result = "South-";
        }
        if (b.equals("West")) {
            result += "west";
        }
        if (b.equals("East")) {
            result += "east";
        }
        if (a.equals("center") && b.equals("center")) {
            result = "in the center";
        }
        return result;
    }

}

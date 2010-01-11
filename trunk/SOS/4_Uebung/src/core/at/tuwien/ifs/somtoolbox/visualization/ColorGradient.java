package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;
import java.util.Vector;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;

/**
 * This class implements a gradient function to generate different colors, based on an initialisation with at least two points and colors.
 * {@link #getColor(double)} can be used to get a single color for a given point along the gradient function, while {@link #toPalette(int)} can be
 * utilised to generate a palette of n numbers.
 * 
 * @author Michael Dittenbach
 * @version $Id: ColorGradient.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ColorGradient {
    private Vector<Color> gradientColors = null;

    private Vector<Double> gradientPoints = null;

    private int numGradientPoints;

    public void setGradientPoint(int index, double c, Color color) {
        if (c > 0 && c < 1) {
            gradientPoints.set(index, new Double(c));
        }
        if (color != null) {
            gradientColors.set(index, color);
        }
    }

    public ColorGradient() {
        gradientPoints = new Vector<Double>();
        gradientColors = new Vector<Color>();
        numGradientPoints = 0;
        try {
            setGradientPoint(0, Color.black);
            setGradientPoint(1, Color.white);
        } catch (SOMToolboxException e) {
            // TODO: does not happen
            e.printStackTrace();
        }
    }

    public void insertGradientPoint(int pos, Color c) {
        double point = 0.5;
        if (pos == 0) {
            setGradientPoint(0, ((Double) gradientPoints.get(1)).doubleValue() / 2, null);
            point = 0d;
        } else if (pos >= gradientPoints.size()) {
            setGradientPoint(gradientPoints.size() - 1, (1d + ((Double) gradientPoints.get(gradientPoints.size() - 2)).doubleValue()) / 2d, null);
            point = 1d;
        } else {
            point = (((Double) gradientPoints.get(pos)).doubleValue() + ((Double) gradientPoints.get(pos - 1)).doubleValue()) / 2d;
        }

        gradientColors.add(pos, c);
        gradientPoints.add(pos, new Double(point));
        numGradientPoints++;
    }

    public void deleteGradientPoing(int pos) {
        if (gradientPoints.size() < 3)
            return;
        gradientPoints.remove(pos);
        gradientColors.remove(pos);
        numGradientPoints--;
    }

    public ColorGradient(double[] points, Color[] colors) throws SOMToolboxException {
        if ((points.length > 1) && (colors.length > 1) && (points.length == colors.length)) {
            gradientPoints = new Vector<Double>();
            gradientColors = new Vector<Color>();
            numGradientPoints = 0;
            try {
                for (int i = 0; i < colors.length; i++) {
                    setGradientPoint(points[i], colors[i]);
                }
            } catch (SOMToolboxException e) {
                // TODO: does not happen
                e.printStackTrace();
            }
        } else {
            throw new SOMToolboxException("At least 2 color points have to be defined.");
        }
    }

    public Color getColor(double c) throws SOMToolboxException {
        if ((c >= 0) && c <= 1) {
            int r, g, b;

            int right = 0;
            while ((right < numGradientPoints) && (getGradientPoint(right) <= c)) {
                right++;
            }
            int left = right - 1;
            if (right == numGradientPoints) {
                left--;
                right--;
            }
            double leftPoint = getGradientPoint(left);
            double rightPoint = getGradientPoint(right);
            Color leftCol = getGradientColor(left);
            Color rightCol = getGradientColor(right);

            double frac = (c - leftPoint) / (rightPoint - leftPoint);

            r = leftCol.getRed() + (int) ((frac * (double) (rightCol.getRed() - leftCol.getRed())));
            g = leftCol.getGreen() + (int) ((frac * (double) (rightCol.getGreen() - leftCol.getGreen())));
            b = leftCol.getBlue() + (int) ((frac * (double) (rightCol.getBlue() - leftCol.getBlue())));
            
            return new Color(r, g, b);
        } else {
            throw new SOMToolboxException("Color gradient index out of range.");
        }
    }

    public Color getGradientColor(int i) {
        return (Color) gradientColors.elementAt(i);
    }

    public double getGradientPoint(int i) {
        return ((Double) gradientPoints.elementAt(i)).doubleValue();
    }

    public void setGradientPoint(double c, Color color) throws SOMToolboxException {
        // FIXME: this seems to be a simple sorting mechanism on insertion? If yes, it can be implemented using a dedicated collection method from the
        // java collections framework, which either does sorting on insertion, or sorting is manually called after insertion.
        if ((c >= 0) && c <= 1) {
            if (numGradientPoints == 0) {
                gradientPoints.addElement(new Double(c));
                gradientColors.addElement(color);
                numGradientPoints++;
            } else if (numGradientPoints == 1) {
                if (c < getGradientPoint(0)) {
                    gradientPoints.add(0, new Double(c));
                    gradientColors.add(0, color);
                    numGradientPoints++;
                } else if ((c == getGradientPoint(0))) {
                    gradientColors.setElementAt(color, 0);
                } else {
                    gradientPoints.add(new Double(c));
                    gradientColors.add(color);
                    numGradientPoints++;
                }
            } else { // if at least 2 points exist
                int i = 1;
                while ((getGradientPoint(i - 1) < c) && (i < numGradientPoints)) {
                    i++;
                }
                i--;
                if (c == getGradientPoint(i)) {
                    gradientColors.setElementAt(color, i);
                } else if (c > getGradientPoint(i)) {
                    gradientPoints.add(new Double(c));
                    gradientColors.add(color);
                    numGradientPoints++;
                } else {
                    gradientPoints.add(i, new Double(c));
                    gradientColors.add(i, color);
                    numGradientPoints++;
                }
            }
        } else {
            throw new SOMToolboxException("Color gradient index out of range.");
        }
    }

    public Color[] toPalette(int numColors) {
        Color[] palette = new Color[numColors];
        try {
            for (int i = 0; i < numColors; i++) {
                palette[i] = getColor((double) i / ((double) numColors - 1.0));
            }
        } catch (SOMToolboxException e) {
            // TODO: does not happen
            e.printStackTrace();
        }
        return palette;
    }

    public int getNumberOfPoints() {
        return gradientColors.size();
    }

}

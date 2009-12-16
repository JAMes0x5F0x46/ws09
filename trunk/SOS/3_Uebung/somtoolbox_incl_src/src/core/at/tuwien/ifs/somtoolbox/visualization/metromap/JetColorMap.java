package at.tuwien.ifs.somtoolbox.visualization.metromap;

import java.awt.Color;

/**
 * Colour map similar to the Jet colour map in MATLAB.
 * 
 * @author Rudolf Mayer
 * @version $Id: JetColorMap.java 2874 2009-12-11 16:03:27Z frank $ *
 */
public class JetColorMap {
    public Color getColor(float x) {
        float a; // alpha
        if (x < 0.f)
            return new Color(0.f, 0.f, 0.f);
        else if (x < 0.125f) {
            a = x / 0.125f;
            return new Color(0.f, 0.f, 0.5f + 0.5f * a);
        } else if (x < 0.375f) {
            a = (x - 0.125f) / 0.25f;
            return new Color(0.f, a, 1.f);
        } else if (x < 0.625f) {
            a = (x - 0.375f) / 0.25f;
            return new Color(a, 1.f, 1.f - a);
        } else if (x < 0.875f) {
            a = (x - 0.625f) / 0.25f;
            return new Color(1.f, 1.f - a, 0.f);
        } else if (x <= 1.0f) {
            a = (x - 0.875f) / 0.125f;
            return new Color(1.f - 0.5f * a, 0.f, 0.f);
        } else {
            return new Color(1.f, 1.f, 1.f);
        }
    }

    public Color[] getColors(int count) {
        Color[] res = new Color[count];
        for (int i = 0; i < res.length; i++) {
            res[i] = getColor(i / (float) (count));
        }
        return res;
    }
}

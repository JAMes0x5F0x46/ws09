package at.tuwien.ifs.somtoolbox.apps.server;

import at.tuwien.ifs.somtoolbox.visualization.Palette;

/**
 * @author Rudolf Mayer
 * @version $Id: ServerPalettes.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ServerPalettes extends at.tuwien.ifs.somtoolbox.visualization.Palettes {

    public static String getPaletteControl(Palette selected) {
        Palette[] pals = getAvailablePalettes();
        StringBuffer b = new StringBuffer(pals.length * 70);
        if (pals.length > 1 && pals.length <= 3) { // make radio buttons
            for (int i = 0; i < pals.length; i++) {
                b.append("<input type=\"radio\" name=\"palette\" onchange=\"this.form.submit()\" value=\"" + pals[i].getName() + "\"");
                if (pals[i] == selected) {
                    b.append(" selected ");
                }
                b.append(">" + pals[i].getName() + "\n");
            }
        } else { // make a select drop down
            b.append("<select name=\"palette\" onchange=\"this.form.submit()\">\n");
            for (int i = 0; i < pals.length; i++) {
                b.append("<option value=\"" + pals[i].getName() + "\"");
                if (pals[i] == selected) {
                    b.append(" selected ");
                }
                b.append(">" + pals[i].getName() + "</option>\n");
            }
            b.append("</select>\n");
        }
        return b.toString();
    }

    public static String getPaletteControl(String selected) {
        return getPaletteControl(getPaletteByName(selected));
    }

}

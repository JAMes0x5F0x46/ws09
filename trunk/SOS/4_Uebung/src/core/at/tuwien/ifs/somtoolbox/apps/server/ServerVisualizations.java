package at.tuwien.ifs.somtoolbox.apps.server;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizer;
import at.tuwien.ifs.somtoolbox.visualization.BackgroundImageVisualizerInstance;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

/**
 * @author Rudolf Mayer
 * @version $Id: ServerVisualizations.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ServerVisualizations extends Visualizations {

    public static String getVisualisationsControl(BackgroundImageVisualizerInstance selected) {
        return getVisualisationsControl(selected.getVis(), selected.getVariant());
    }

    public static String getVisualisationsControl(ArrayList<BackgroundImageVisualizerInstance> availableVis,
            BackgroundImageVisualizerInstance selected) {
        System.out.println("ready visualisations:");
        System.out.println(ArrayUtils.toString(availableVis));
        System.out.println("selected vis: " + selected);
        StringBuffer b = new StringBuffer(availableVis.size() * 150);
        if (availableVis.size() > 1 && availableVis.size() <= 3) { // make radio buttons
            for (int i = 0; i < availableVis.size(); i++) {
                BackgroundImageVisualizerInstance vis = availableVis.get(i);
                b.append("<input type=\"radio\" name=\"visualisation\" onchange=\"this.form.submit()\" value=\""
                        + vis.getVis().getVisualizationShortName(vis.getVariant()) + "\"");
                System.out.println("vis: " + vis + "[" + vis.hashCode() + "], selected: " + selected + "[" + selected.hashCode() + "]");
                if (vis.equals(selected)) {
                    b.append(" checked=\"checked\" ");
                }
                b.append(">" + vis.getDisplayName() + "\n");
            }
        } else {// make a select drop down
            b.append("<select name=\"visualisation\" onchange=\"this.form.submit()\">\n");
            for (int i = 0; i < availableVis.size(); i++) {
                BackgroundImageVisualizerInstance vis = availableVis.get(i);
                b.append("<option value=\"" + vis.getVis().getVisualizationShortName(vis.getVariant()) + "\"");
                if (vis.equals(selected)) {
                    b.append(" selected ");
                }
                b.append(">" + vis.getDisplayName() + "</option>\n");
            }
            b.append("</select>\n");
        }
        return b.toString();
    }

    public static String getVisualisationsControl(BackgroundImageVisualizer selected, int selectedVariant) {
        BackgroundImageVisualizer[] vis = getReadyVisualizations();
        System.out.println("ready visualisations:");
        System.out.println(ArrayUtils.toString(vis));
        StringBuffer b = new StringBuffer(vis.length * 150);
        if (vis.length <= 3) { // make radio buttons
            for (int i = 0; i < vis.length; i++) {
                for (int j = 0; j < vis[i].getNumberOfVisualizations(); j++) {
                    b.append("<input type=\"radio\" name=\"visualisation\" onchange=\"this.form.submit()\" value=\"" + vis[i].getVisualizationName(j)
                            + "\"");
                    if (vis[i] == selected && selectedVariant == j) {
                        b.append(" checked=\"checked\" ");
                    }
                    b.append(">" + vis[i].getVisualizationName(j) + "\n");
                }
            }
        } else {// make a select drop down
            b.append("<select name=\"visualisation\" onchange=\"this.form.submit()\">\n");
            for (int i = 0; i < vis.length; i++) {
                for (int j = 0; j < vis[i].getNumberOfVisualizations(); j++) {
                    b.append("<option value=\"" + vis[i].getVisualizationName(j) + "\"");
                    if (vis[i] == selected && selectedVariant == j) {
                        b.append(" selected ");
                    }
                    b.append(">" + vis[i].getVisualizationName(j) + "</option>\n");
                }
            }
            b.append("</select>\n");
        }
        return b.toString();
    }

    public static String getVisualisationsControl(String selected) {
        return getVisualisationsControl(getVisualizationByName(selected).getVis(), getVisualizationByName(selected).getVariant());
    }
}

package at.tuwien.ifs.somtoolbox.apps.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.tuwien.ifs.somtoolbox.data.SOMLibDataInformation;
import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: MapSectionServlet.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MapSectionServlet extends HttpServlet {
    protected static SOMMap map;

    protected static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (map == null) {
            map = SOMMap.getInstance();
        }
        System.out.println("\n\n Section Servlet: " + StringUtils.printMap(request.getParameterMap()));
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("text/html");
        out.println("<link rel=\"StyleSheet\" href=\"style.css\" type=\"text/css\" media=\"all\"/>");

        SOMPageParameters params = new SOMPageParameters(request);
        params.setShowLabelsDefault(true);
        params.parseRequest(request);

        try {
            HTMLMapInformation mapData = map.createMapSection(request, params.vis, params.palette, params.smoothingFactor, params.showNodes,
                    params.showGrid, params.fodokId, params.mapWidth, params.mapHeight);
            SOMLibDataInformation dataInfo = SOMMap.som.inputDataObjects.getDataInfo();
            out.println("<img border=\"0\" src=\"" + mapData.getImagePath() + "\" usemap=\"#som-map\" />");
            out.println(mapData.getImageMap() + "<br>");
            String[] relatedDocs = mapData.getNNearest();
            if (relatedDocs != null && relatedDocs.length > 0) {
                out.println("<b>Related documents</b><br>");
                for (int i = 0; i < relatedDocs.length; i++) {
                    String docDisplayName = relatedDocs[i];
                    if (dataInfo != null && dataInfo.getDataDisplayName(relatedDocs[i]) != null) {
                        docDisplayName = dataInfo.getDataDisplayName(relatedDocs[i]);
                    }
                    out.println("<a href=\"" + map.documentDetailLink + "?" + map.documentDetailParamName + "=" + relatedDocs[i] + "\">"
                            + docDisplayName + "<br>");
                }
            } else {
                out.println("<b>No documents</b><br>");
            }
        } catch (LayerAccessException e) {
            e.printStackTrace();
        }

    }
}

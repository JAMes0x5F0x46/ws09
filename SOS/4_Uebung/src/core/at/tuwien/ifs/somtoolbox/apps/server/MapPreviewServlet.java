package at.tuwien.ifs.somtoolbox.apps.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.tuwien.ifs.somtoolbox.layers.LayerAccessException;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: MapPreviewServlet.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MapPreviewServlet extends HttpServlet {
    protected static SOMMap map;

    protected static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        if (map == null) {
            map = SOMMap.getInstance();
        }
        System.out.println("\n\n Preview Servlet: " + StringUtils.printMap(request.getParameterMap()));
        try {
            SOMPageParameters params = new SOMPageParameters(request);
            params.setShowGridDefault(false);
            // params.setShowLabelsDefault(false);
            params.parseRequest(request);

            HTMLMapInformation mapData = map.createFullMap(request, params.vis, params.palette, params.smoothingFactor, params.showNodes,
                    params.showGrid, params.showLabels, params.fodokIds, params.mapWidth, params.mapHeight, params.fullLink);

            // Get the absolute path of the image
            ServletContext sc = getServletContext();
            String filename = sc.getRealPath(mapData.getImagePath());

            // Get the MIME type of the image
            String mimeType = sc.getMimeType(filename);
            if (mimeType == null) {
                sc.log("Could not get MIME type of " + filename);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            // Set content type
            resp.setContentType(mimeType);

            // Set content size
            File file = new File(filename);
            resp.setContentLength((int) file.length());

            // Open the file and output streams
            FileInputStream in = new FileInputStream(file);
            OutputStream out = resp.getOutputStream();

            // Copy the contents of the file to the output stream
            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }
            in.close();
            out.close();
        } catch (LayerAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

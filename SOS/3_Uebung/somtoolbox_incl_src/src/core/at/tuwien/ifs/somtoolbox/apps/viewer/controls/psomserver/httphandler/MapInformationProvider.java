package at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.httphandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;
import at.tuwien.ifs.somtoolbox.data.SOMVisualisationData;
import at.tuwien.ifs.somtoolbox.data.SharedSOMVisualisationData;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Provides useful information about the current map.
 * 
 * @author Jakob Frank
 */
public class MapInformationProvider implements HttpHandler {

    private static final String LOG_SEP = " - ";

    private final CommonSOMViewerStateData state;

    private final String contentContext;

    private final String configContext;

    private final String templatePath;

    private static final String NL = "\n";

    private Logger log;

    private static final String HTML_TEMPLATE_INDEX = "index.html";

    private static final String HTML_TEMPLATE_MAP = "map.html";

    public MapInformationProvider(CommonSOMViewerStateData state, String mapContentContext, String configurationContext) {
        this.state = state;
        this.contentContext = mapContentContext + (mapContentContext.endsWith("/") ? "" : "/");
        this.configContext = configurationContext + (configurationContext.endsWith("/") ? "" : "/");
        log = Logger.getLogger(this.getClass().getName());
        if (state != null) {
            templatePath = state.getSOMViewerProperties().getHtmlMapTemplatesDir();
        } else {
            templatePath = "bin/rsc/html/map";
        }
    }

    public void handle(HttpExchange t) throws IOException {
        // TODO: deliver arbitrary files?
        String path = t.getRequestURI().getPath();
        String file = path.substring(path.lastIndexOf('/') + 1);
        String query = t.getRequestURI().getQuery();
        // System.out.println(t.getRequestURI().toString());
        if (file.equalsIgnoreCase("") || file.equalsIgnoreCase("index.html")) {
            sendFile(t, new File(templatePath, HTML_TEMPLATE_INDEX));
        } else if (file.equalsIgnoreCase("map.html")) {
            sendFile(t, new File(templatePath, HTML_TEMPLATE_MAP));
        } else if (query != null && query.contains("download")) {
            downloadFile(t);
        } else {
            HttpErrorHandler.sendError(t, 404);
        }
    }

    private void downloadFile(HttpExchange t) throws IOException {
        String q = t.getRequestURI().getQuery();
        String[] args = q.split("&");
        String rF = "";
        // Parse the arguments
        for (int i = 0; i < args.length; i++) {
            String[] kv = args[i].split("=");
            if (kv.length != 2)
                continue;
            if (kv[0].equalsIgnoreCase("file") || kv[0].equalsIgnoreCase("f")) {
                rF = kv[1];
                break;
            }
        }

        String file = null;
        SOMViewer sv = state.getSOMViewer();
        SharedSOMVisualisationData shvd = state.inputDataObjects;

        if (sv != null) {
            if (rF.equalsIgnoreCase("vec")) {
                file = shvd.getObject(SOMVisualisationData.INPUT_VECTOR).getFileName();
            } else if (rF.equalsIgnoreCase("unit")) {
                file = sv.getUnitDescriptionFileName();
            } else if (rF.equalsIgnoreCase("wgt")) {
                file = sv.getWeightVectorFileName();
            } else if (rF.equalsIgnoreCase("dwm")) {
                file = shvd.getObject(SOMVisualisationData.DATA_WINNER_MAPPING).getFileName();
            } else if (rF.equalsIgnoreCase("tv")) {
                file = shvd.getObject(SOMVisualisationData.TEMPLATE_VECTOR).getFileName();
            } else if (rF.equalsIgnoreCase("cls")) {
                file = shvd.getObject(SOMVisualisationData.CLASS_INFO).getFileName();
            } else if (rF.equalsIgnoreCase("map")) {
                file = sv.getMapDescriptionFileName();
            } else {
                file = null;
            }
        }

        if (file != null) {
            File f = new File(file);
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);

                int size = fis.available();
                byte[] bFile = new byte[size];
                fis.read(bFile);

                log.info(200 + LOG_SEP + t.getRequestURI().toString());
                // t.getResponseHeaders().add("Accept-Ranges", "bytes");
                t.getResponseHeaders().add("Content-Type", "application/x-download; name=\"" + f.getName() + "\"");
                t.getResponseHeaders().add("Content-Disposition", "attachement; filename=\"" + f.getName() + "\"");
                t.sendResponseHeaders(200, size);

                if (t.getRequestMethod().equalsIgnoreCase("GET")) {
                    OutputStream os = t.getResponseBody();
                    os.write(bFile);
                    os.close();
                }
                t.close();
            } else {
                HttpErrorHandler.sendError(t, 500);
            }
        } else {
            HttpErrorHandler.sendError(t, 404);
        }
    }

    private void sendFile(HttpExchange t, File f) throws IOException {
        String response = null;

        try {
            if (!f.exists()) {
                URL altPath = ClassLoader.getSystemResource(f.getPath());
                if (altPath != null) {
                    f = new File(altPath.toURI());
                }
            }
            response = parseFile(t, f);
        } catch (FileNotFoundException e) {
            HttpErrorHandler.sendError(t, 404);
        } catch (IOException e) {
            HttpErrorHandler.sendError(t, 500);
        } catch (URISyntaxException e) {
            HttpErrorHandler.sendError(t, 404);
        }
        if (response == null)
            return;

        log.info(200 + LOG_SEP + t.getRequestURI().toString());
        t.getRequestBody();
        t.getResponseHeaders().add("Content-Type", "text/html");
        t.sendResponseHeaders(200, response.length());
        if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        t.close();
    }

    /**
     * @param t
     * @param f
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String parseFile(HttpExchange t, File f) throws IOException {
        if (!f.exists()) {
            throw new FileNotFoundException(f.getPath());
        }
        String response;
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader fr = new BufferedReader(new FileReader(f));
            for (String l = fr.readLine(); l != null; l = fr.readLine()) {
                sb.append(l).append(NL);
            }
            response = parseString(t, sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        return response;
    }

    private String parseString(HttpExchange t, String string) {
        // Some Defaults
        String path = t.getRequestURI().getPath();
        string = string.replaceAll("\\$PATH", path);
        String page = path.substring(path.lastIndexOf('/') + 1);
        string = string.replaceAll("\\$PAGE", page);
        string = string.replaceAll("\\$SERVER", "SOMViewer - PocketSOMConnector");

        // Mapping
        string = string.replaceAll("\\$PSOM_MAPPING", configContext + PocketSOMConfigProvider.MAPPING);
        // Current Visualisation
        string = string.replaceAll("\\$(PSOM_)?VISUALISATION", configContext + PocketSOMConfigProvider.IMG);
        // psom
        string = string.replaceAll("\\$PSOM", configContext + "psom" + PocketSOMConfigProvider.PSOM);
        // Map Data
        string = string.replaceAll("\\$DATA_PATH", contentContext);

        // Map Info
        string = string.replaceAll("\\$MAP_NAME", state.growingLayer.getIdString());
        int x = state.growingLayer.getXSize();
        int y = state.growingLayer.getYSize();
        int z = state.growingLayer.getZSize();
        string = string.replaceAll("\\$MAP_SIZE_X", x + "");
        string = string.replaceAll("\\$MAP_SIZE_Y", y + "");
        string = string.replaceAll("\\$MAP_SIZE_Z", z + "");
        string = string.replaceAll("\\$MAP_SIZE", x + "/" + y + "/" + z);
        // Data Files
        String vec = "", unit = "", wgt = "", dwm = "", tv = "", cls = "", map = "";
        SOMViewer sv = state.getSOMViewer();
        SharedSOMVisualisationData shvd = state.inputDataObjects;
        if (sv != null) {
            vec = shvd.getObject(SOMVisualisationData.INPUT_VECTOR).getFileName();
            vec = vec != null ? vec : "";
            unit = (sv.getUnitDescriptionFileName());
            unit = unit != null ? unit : "";
            wgt = (sv.getWeightVectorFileName());
            wgt = wgt != null ? wgt : "";
            dwm = shvd.getObject(SOMVisualisationData.DATA_WINNER_MAPPING).getFileName();
            dwm = dwm != null ? dwm : "";
            tv = shvd.getObject(SOMVisualisationData.TEMPLATE_VECTOR).getFileName();
            tv = tv != null ? tv : "";
            cls = shvd.getObject(SOMVisualisationData.CLASS_INFO).getFileName();
            cls = cls != null ? cls : "";
            map = (sv.getMapDescriptionFileName());
            map = map != null ? map : "";
        }
        string = string.replaceAll("\\$VEC_PATH", vec);
        string = string.replaceAll("\\$UNIT_PATH", unit);
        string = string.replaceAll("\\$WGT_PATH", wgt);
        string = string.replaceAll("\\$DWM_PATH", dwm);
        string = string.replaceAll("\\$TV_PATH", tv);
        string = string.replaceAll("\\$CLS_PATH", cls);
        string = string.replaceAll("\\$MAP_PATH", map);

        string = string.replaceAll("\\$VEC_FILE", basename(vec));
        string = string.replaceAll("\\$UNIT_FILE", basename(unit));
        string = string.replaceAll("\\$WGT_FILE", basename(wgt));
        string = string.replaceAll("\\$DWM_FILE", basename(dwm));
        string = string.replaceAll("\\$TV_FILE", basename(tv));
        string = string.replaceAll("\\$CLS_FILE", basename(cls));
        string = string.replaceAll("\\$MAP_FILE", basename(map));

        // TODO More to come

        return string;
    }

    private String basename(String path) {
        if (path == null)
            return "";
        if (path.equals(""))
            return path;
        if (path.equals("/"))
            return path;

        int lastPS = path.lastIndexOf('/');
        if (lastPS < 0)
            return path;

        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 2);

        return path.substring(path.lastIndexOf('/') + 1);

    }

}
/*
 * Created on Jun 2, 2009
 * Version: $Id: HttpErrorHandler.java 2874 2009-12-11 16:03:27Z frank $
 */

package at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.httphandler;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

public class HttpErrorHandler {

    private static final String SERVER_FOOTER = "SOMViewer - PocketSOMConnector";

    private static final String HTML_000 = "<html><head><title>%1$d - %2$s</title><body><h1>%1$d - %2$s</h1><p>%3$s</p><p>%4$s</p><hr><address>%5$s</addess></body><html>";

    public static void sendError(HttpExchange t, int errCode) throws IOException {
        sendError(t, errCode, "");
    }

    public static void sendError(HttpExchange t, int errCode, String message) throws IOException {
        t.getRequestBody();

        String title, descr;
        switch (errCode) {
            case 404:
                title = "Not found";
                descr = String.format("Requested document %s does not exist.", t.getRequestURI().getPath());
                break;
            case 500:
                title = "Internal Error";
                descr = "Internal server error occured. Sorry!";
                break;
            case 501:
                title = "Not implemented";
                descr = String.format("Method %s not implemented.", t.getRequestMethod());
                break;
            default:
                title = "Crestfallen";
                descr = "I could not satisfy your request. Sorry!";
                break;
        }
        String response = String.format(HTML_000, errCode, title, descr, message, SERVER_FOOTER);

        t.sendResponseHeaders(errCode, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
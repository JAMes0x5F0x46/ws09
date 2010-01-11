package at.tuwien.ifs.somtoolbox.apps.viewer.controls.psomserver.httphandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.viewer.CommonSOMViewerStateData;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * The SongProvider sends requested Songs via http.
 * 
 * @author Jakob Frank
 */
public class SongProvider implements HttpHandler {
    private static final String LOG_SEP = " - ";

    private Logger log;

    private final String pathOffset;

    public SongProvider(CommonSOMViewerStateData state, String context) {
        this.pathOffset = context;
        log = Logger.getLogger(this.getClass().getName());
    }

    private void sendSong(HttpExchange t, File song) throws IOException {
        log.info("Delivering song: " + song.getAbsolutePath() + " (" + song.length() + " Byte)");
        FileInputStream fis = new FileInputStream(song);

        int size = fis.available();
        byte[] bSong = new byte[size];
        fis.read(bSong);

        log.info(200 + LOG_SEP + t.getRequestURI().toString());
        t.getResponseHeaders().add("Accept-Ranges", "bytes");
        t.getResponseHeaders().add("Content-Type", "audio/mpeg; name=\"" + song.getName() + "\"");
        t.sendResponseHeaders(200, size);

        if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            OutputStream os = t.getResponseBody();
            os.write(bSong);
            os.close();
        }
        t.close();
    }

    public void handle(HttpExchange t) throws IOException {
        String path = t.getRequestURI().getPath();
        path = path.replaceFirst(pathOffset, "");
        File song = new File(CommonSOMViewerStateData.fileNamePrefix, path);
        if (song.exists()) {
            sendSong(t, song);
        } else {
            log.info(song.getAbsolutePath() + " not found");
            HttpErrorHandler.sendError(t, 404);
        }

    }

}
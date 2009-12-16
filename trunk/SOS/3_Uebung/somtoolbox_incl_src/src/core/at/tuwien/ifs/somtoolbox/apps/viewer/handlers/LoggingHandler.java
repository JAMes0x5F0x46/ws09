package at.tuwien.ifs.somtoolbox.apps.viewer.handlers;

import java.awt.Component;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JOptionPane;

import at.tuwien.ifs.somtoolbox.apps.viewer.StatusBar;

/**
 * Handles events from Logger and decides where to output it.
 * 
 * @author Thomas Lidy
 * @version $Id: LoggingHandler.java 2874 2009-12-11 16:03:27Z frank $
 */
public class LoggingHandler extends Handler {

    private StatusBar statusBar = null;

    private Component parentComp = null; // parent window to show messageBox in

    public void setStatusBar(StatusBar statusbar) {
        this.statusBar = statusbar;
    }

    public void deattachStatusBar() {
        this.statusBar = null;
    }

    public void setParentComponent(Component parentcomp) {
        this.parentComp = parentcomp;
    }

    public void deattachParentComponent() {
        this.parentComp = null;
    }

    public void publish(LogRecord record) {
        Level level = record.getLevel();
        if (statusBar != null) {
            // System.out.println("level: " + record.getLevel().toString());
            if (level == Level.SEVERE) {
                statusBar.setText("SEVERE: " + record.getMessage());
            } else if (level == Level.WARNING)
                statusBar.setText("WARNING: " + record.getMessage());
            else
                statusBar.setText(record.getMessage());
        }
        if ((parentComp != null) && (level == Level.SEVERE)) {
            JOptionPane.showMessageDialog(parentComp, record.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void flush() {
    }

    public void close() throws SecurityException {
        deattachStatusBar();
    }

    public void progress(int currentStep) {
        statusBar.progress(currentStep);
    }

    public void initProgressBar(int totalSteps) {
        statusBar.initProgressBar(totalSteps);
    }

}

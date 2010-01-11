package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import at.tuwien.ifs.somtoolbox.util.ProgressListener;

/**
 * The status bar displayed in the lower-right part of the SOMViewer application.
 * 
 * @author Thomas Lidy
 * @version $Id: StatusBar.java 2874 2009-12-11 16:03:27Z frank $
 */
public class StatusBar extends JPanel implements ProgressListener {
    private static final long serialVersionUID = 1L;

    private JLabel statusText = null;

    private JProgressBar progressBar = null;

    public StatusBar() {
        super(new BorderLayout());

        setBorder(BorderFactory.createLoweredBevelBorder());

        statusText = new JLabel("ready.", SwingConstants.LEFT);
        // statusText.setAlignmentX(JLabel.LEFT);
        statusText.setHorizontalAlignment(SwingConstants.LEFT);
        // statusText.setBorder( BorderFactory.createEtchedBorder());
        add(statusText, BorderLayout.WEST);

        // add progress bar
        progressBar = new JProgressBar();
        add(progressBar, BorderLayout.EAST);

        // TODO add methods for ProgessBar
    }

    public void setText(String text) {
        statusText.setText(text);
    }

    public void insertColumn(int columns, String message) {
        // currently not used in this implementation
    }

    public void insertRow(int rows, String message) {
        // currently not used in this implementation
    }

    public void progress(String message, int currentStep) {
        // currently not used in this implementation
    }

    public void progress(int currentStep) {
        progressBar.setValue(currentStep);
    }

    public void initProgressBar(int totalSteps) {
        progressBar.setMinimum(0);
        progressBar.setMaximum(totalSteps);
    }

    public void progress() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    public void progress(String message) {
        progressBar.setValue(progressBar.getValue() + 1);
    }
    
    public int getCurrentStep() {
        return progressBar.getValue();
    }

}

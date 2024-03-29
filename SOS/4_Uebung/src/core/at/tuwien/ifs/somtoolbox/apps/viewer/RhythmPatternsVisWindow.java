package at.tuwien.ifs.somtoolbox.apps.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import at.tuwien.ifs.somtoolbox.data.DataDimensionException;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * A dialog that displays a {@link RhythmPattern}.
 * 
 * @author Thomas Lidy
 * @version $Id: RhythmPatternsVisWindow.java 2874 2009-12-11 16:03:27Z frank $
 */
public class RhythmPatternsVisWindow extends JDialog implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;

    private JPanel drawingPane;

    private JLabel sourceName = null;

    private JLabel status = null;

    private static final int DEFAULT_bx = 5; // graphics block size in pixel (x)

    private static final int DEFAULT_by = 5; // graphics block size in pixel (y)

    private RhythmPattern rhythmPattern = null;

    private int xdim = 0; // matrix dimensions

    private int ydim = 0;

    private double[][] mat; // the vector in matrix form

    private static final float mod_freq_res = 0.168221f; // modulation frequency spacing on x_axis

    private NumberFormat numform_Hz, numform_Amp;

    private boolean display_status = true;

    /**
     * This constructor uses pre-defined hard-coded values; it is not recommended to use this constructor it is intended for backward compatibility
     * for RP feature vector files without the $DATA_TYPE header
     * 
     * @throws DataDimensionException
     */
    public RhythmPatternsVisWindow(Frame parent, double[] vec, String filelabel) throws DataDimensionException {
        this(parent, vec, 60, 24, filelabel, "Rhythm Pattern");
    }

    /**
     * constructor with feature vector given as double[] array (as used for weight vectors) creates a RhythmPatternsVisWindow
     * 
     * @throws DataDimensionException
     */
    public RhythmPatternsVisWindow(Frame parent, double[] vec, int xdim, int ydim, String filelabel) throws DataDimensionException {
        this(parent, vec, xdim, ydim, filelabel, "Rhythm Pattern");
    }

    /**
     * constructor with feature vector given as double[] array (as used for weight vectors) creates a RhythmPatternsVisWindow
     * 
     * @throws DataDimensionException
     */
    public RhythmPatternsVisWindow(Frame parent, double[] vec, int xdim, int ydim, String filelabel, String title) throws DataDimensionException {
        super(parent);
        this.xdim = xdim;
        this.ydim = ydim;
        rhythmPattern = new RhythmPattern(vec, xdim, ydim, DEFAULT_bx, DEFAULT_by);
        this.initGUI(filelabel, title);
    }

    /**
     * This constructor uses pre-defined hard-coded values; it is not recommended to use this constructor it is intended for backward compatibility
     * for RP feature vector files without the $DATA_TYPE header
     * 
     * @throws DataDimensionException
     */
    public RhythmPatternsVisWindow(Frame parent, DoubleMatrix1D vec, String filelabel, String title) throws DataDimensionException {
        this(parent, vec, 60, 24, filelabel, title);
    }

    /**
     * constructor with feature vector given as DoubleMatrix1D (as provided by class SOMLibSparseInputData) creates a RhythmPatternsVisWindow with
     * default window title
     * 
     * @throws DataDimensionException
     */
    public RhythmPatternsVisWindow(Frame parent, DoubleMatrix1D vec, int xdim, int ydim, String filelabel) throws DataDimensionException {
        this(parent, vec, xdim, ydim, filelabel, "Rhythm Pattern");
    }

    /**
     * constructor with feature vector given as DoubleMatrix1D (as provided by class SOMLibSparseInputData) creates a RhythmPatternsVisWindow with
     * provided window title
     * 
     * @throws DataDimensionException
     */
    public RhythmPatternsVisWindow(Frame parent, DoubleMatrix1D vec, int xdim, int ydim, String filelabel, String title)
            throws DataDimensionException {
        super(parent);
        this.xdim = xdim;
        this.ydim = ydim;
        rhythmPattern = new RhythmPattern(vec, xdim, ydim, DEFAULT_bx, DEFAULT_by);
        this.initGUI(filelabel, title);
    }

    private void initGUI(String filelabel, String title) {
        mat = rhythmPattern.getMatrix();

        // Handle window closing correctly.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setTitle(title);

        sourceName = new JLabel(filelabel);
        add(sourceName, BorderLayout.NORTH);

        status = new JLabel("status");
        add(status, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                // TODO what?
            }
        });

        // initialize display number formats
        numform_Hz = NumberFormat.getInstance();
        numform_Hz.setMaximumFractionDigits(2);
        numform_Amp = NumberFormat.getInstance();
        numform_Amp.setMaximumFractionDigits(6);

        // Set up the drawing area.
        drawingPane = new DrawingPane();
        drawingPane.setBackground(Color.white);
        drawingPane.setPreferredSize(rhythmPattern.getSize());
        drawingPane.addMouseListener(this);
        drawingPane.addMouseMotionListener(this);
        add(drawingPane, BorderLayout.CENTER);
    }

    private class DrawingPane extends JPanel {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DrawingPane() {

        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            rhythmPattern.paint(g);
        }
    }

    /**
     * handles mouse events: status display remains fixed after clicking (continues after 2nd clicking)
     */
    public void mouseReleased(MouseEvent e) {
        display_status = !display_status;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    /**
     * handles mouse events: status display changes when mouse is moved over Rhythm Pattern
     */
    public void mouseMoved(MouseEvent e) {
        if (!display_status) {
            return;
        }

        int cx = (e.getX() / DEFAULT_bx);
        int cy = (ydim - 1) - (e.getY() / DEFAULT_by);
        if ((cx >= xdim) || (cy >= ydim) || (cx < 0) || (cy < 0)) {
            return; // avoid out of bounds
        }

        float mod_freq = (cx + 1) * mod_freq_res;

        // System.out.println("cx " + cx + " cy " + cy + " mat "+ mat.length );

        // let index start from 1
        status.setText("band " + Integer.toString(cy + 1) + " / " + numform_Hz.format(mod_freq) + " Hz: " + numform_Amp.format(mat[cx][cy]));
    }

    public void mouseDragged(MouseEvent e) {
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        // textField.setText(null);
        setVisible(false);
    }
}
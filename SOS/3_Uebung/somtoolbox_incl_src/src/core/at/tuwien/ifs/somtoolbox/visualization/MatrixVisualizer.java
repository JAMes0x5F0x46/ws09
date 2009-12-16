package at.tuwien.ifs.somtoolbox.visualization;

import java.awt.Color;

/**
 * Interface for visualisation of matrix data of/on a SOM. If you want to provide new matrix visualisers, you have to implement this interface and
 * register your visualisation in {@link Visualizations#getAvailableVisualizations()}. If your visualisation requires user input to e.g. control parameters, extend
 * {@link at.tuwien.ifs.somtoolbox.visualization.AbstractBackgroundImageVisualizer.VisualizationControlPanel} to add your specific control panel inputs.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @author Thomas Lidy
 * @version $Id: MatrixVisualizer.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface MatrixVisualizer {

    /**
     * Gets the currently used palette.
     * 
     * @return the currently used palette
     */
    public Color[] getPalette();

    /**
     * Sets a new palette.
     * 
     * @param paletteIndex the index of this palette
     * @param palette the new palette
     */
    public void setPalette(int paletteIndex, Palette palette);

    /**
     * Reverts the currently used palette.
     */
    public void reversePalette();

    /**
     * Indicates whether the currently used palette is reversed or not.
     * 
     * @return true if the palette is reversed, false otherwise
     */
    public boolean isReversed();

}

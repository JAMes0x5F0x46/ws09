package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.util.ArrayList;

import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.models.GHSOM;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.output.labeling.LabelSOM;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;
import at.tuwien.ifs.somtoolbox.visualization.Visualizations;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @version $Id: GHSOMTestRunResult.java 2874 2009-12-11 16:03:27Z frank $
 */
public class GHSOMTestRunResult extends GGSOMTestRunResult {

    private GHSOM ghsom = null;

    /**
     * creates a new instance of this type, by handing over a TestRunResult object. All information needed are then taken from this object (all the
     * paths to the files, etc ... )
     * 
     * @param result an Object from which all the filepaths, the dataset information and the run id can be taken
     */
    public GHSOMTestRunResult(TestRunResult result) {
        super(result);
    }

    /**
     * returns a GHSOM representing the SOM trained in this run
     * 
     * @return the GHSOM rebuilt from the map file
     */
    public GHSOM getGHSOM() {
        if (this.ghsom == null) {
            this.ghsom = new GHSOM(this.getInputReader());
            this.visData = this.getVisData();
            this.ghsom.setSharedInputObjects(this.visData);

            // label the som
            LabelSOM labelsom = new LabelSOM();
            labelsom.label(this.ghsom, this.getDatasetInfo().getInputData(), this.getDatasetInfo().getVectorDim());

            SOMLibFormatInputReader somlib = this.getInputReader();
            Palette[] palettes = Palettes.getAvailablePalettes();
            Palette defaultPalette = Palettes.getDefaultPalette();
            int defaultPaletteIndex = Palettes.getPaletteIndex(defaultPalette);
            Visualizations.initVisualizations(visData, somlib, defaultPaletteIndex, defaultPalette, palettes);
        }
        return ghsom;
    }

    @Override
    public int getNumberOfMaps() {
        return this.countSubmaps(this.getGHSOM().topLayerMap(), 1);
    }

    private int countSubmaps(GrowingSOM map, int count) {
        ArrayList<GrowingSOM> submaps = map.getLayer().getAllSubMaps();
        for (int i = 0; i < submaps.size(); i++) {
            count += this.countSubmaps(submaps.get(i), 1);
        }
        return count;
    }

}

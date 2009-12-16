package at.tuwien.ifs.somtoolbox.reportgenerator;

import java.util.ArrayList;

/**
 * @author Martin Waitzbauer (0226025)
 * @author Rudolf Mayer
 * @version $Id: EditableReportProperties.java 2874 2009-12-11 16:03:27Z frank $
 */
public class EditableReportProperties {

    private boolean includeClusterReport;

    private boolean includeDistortion;

    private boolean includeFlowBorderLine;

    private boolean includeRegionReport;

    private boolean includeSDH;

    private boolean includeSemanticReport;

    private boolean includeTopographicProduct;

    private boolean includeTrustWorthiness;

    private int maxCompactness;

    private int metroMapComponents;

    private int minCompactness;

    private int sdhStep;

    private ArrayList<String> selectedQualityMeasures;

    private int topographicProductStep;

    private int trustWorthinessStep;

    public int getMAXCompactness() {
        return maxCompactness;
    }

    public int getMetroMapComponents() {
        return metroMapComponents;
    }

    public int getMINCompactness() {
        return minCompactness;
    }

    public int getSDHStep() {
        return sdhStep;
    }

    public ArrayList<String> getSelectedQualitMeasure() {
        return this.selectedQualityMeasures;
    }

    public int getTPStep() {
        return topographicProductStep;
    }

    public int getTWStep() {
        return trustWorthinessStep;
    }

    public boolean includeClusterReport() {
        return includeClusterReport;
    }

    public boolean includeDistPic() {
        return includeDistortion;
    }

    public boolean includeFlowBorderLinePic() {
        return includeFlowBorderLine;
    }

    public boolean includeRegionReport() {
        return includeRegionReport;
    }

    public boolean includeSDHVisualization() {
        return includeSDH;
    }

    public boolean includeSemanticReport() {
        return this.includeSemanticReport;
    }

    public boolean includeTPVisualization() {
        return includeTopographicProduct;
    }

    public boolean includeTWVisualization() {
        return includeTrustWorthiness;
    }

    public void setIncludeClusterReport(boolean includeClusterReport) {
        this.includeClusterReport = includeClusterReport;
    }

    public void setIncludeDistortion(boolean includeDistortion) {
        this.includeDistortion = includeDistortion;
    }

    public void setIncludeFlowBorderLine(boolean includeFlowBorderLine) {
        this.includeFlowBorderLine = includeFlowBorderLine;
    }

    public void setIncludeRegionReport(boolean includeRegionReport) {
        this.includeRegionReport = includeRegionReport;
    }

    public void setIncludeSDH(boolean includeSDH) {
        this.includeSDH = includeSDH;
    }

    public void setIncludeSemanticReport(boolean include) {
        this.includeSemanticReport = include;
    }

    public void setIncludeTopographicProduct(boolean includeTopographicProduct) {
        this.includeTopographicProduct = includeTopographicProduct;
    }

    public void setIncludeTrustWorthiness(boolean includeTrustWorthiness) {
        this.includeTrustWorthiness = includeTrustWorthiness;
    }

    public void setMaxCompactness(int maxCompactness) {
        this.maxCompactness = maxCompactness;
    }

    public void setMetroMapComponents(int metroMapComponents) {
        this.metroMapComponents = metroMapComponents;
    }

    public void setMinCompactness(int minCompactness) {
        this.minCompactness = minCompactness;
    }

    public void setSdhStep(int sdhStep) {
        this.sdhStep = sdhStep;
    }

    public void setSelectedQualitMeasure(ArrayList<String> selectedQualitMeasure) {
        this.selectedQualityMeasures = selectedQualitMeasure;
    }

    public void setSelectedQualityMeasures(ArrayList<String> selectedQualityMeasures) {
        this.selectedQualityMeasures = selectedQualityMeasures;
    }

    public void setTopographicProductStep(int topographicProductStep) {
        this.topographicProductStep = topographicProductStep;
    }

    public void setTrustWorthinessStep(int trustWorthinessStep) {
        this.trustWorthinessStep = trustWorthinessStep;
    }

}

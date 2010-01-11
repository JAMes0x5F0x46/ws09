package at.tuwien.ifs.somtoolbox.reportgenerator.output;

import at.tuwien.ifs.somtoolbox.reportgenerator.DatasetInformation;
import at.tuwien.ifs.somtoolbox.reportgenerator.TestRunResultCollection;

/**
 * @author Sebastian Skritek (0226286, Sebastian.Skritek@gmx.at)
 * @author Martin Waitzbauer (0226025)
 * @version $Id: OutputReport.java 2874 2009-12-11 16:03:27Z frank $
 */
public interface OutputReport {

    public void setDatasetInformation(DatasetInformation infoObj);

    public void setTestrunInformation(TestRunResultCollection testruns, int type);

    public void createOutput();

}

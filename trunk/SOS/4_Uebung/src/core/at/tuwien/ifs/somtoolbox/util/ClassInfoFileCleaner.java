package at.tuwien.ifs.somtoolbox.util;

import java.io.File;
import java.io.IOException;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibClassInformation;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;

import com.martiansoftware.jsap.JSAPResult;

/**
 * This class "cleans" a class info file, i.e. it removes from the class info file instances that are not present in the input vector. This can happen
 * e.g. with text data, where not all input documents are used in the final map due to sparsity reasons.
 * 
 * @author Rudolf Mayer
 * @version $Id: ClassInfoFileCleaner.java 2874 2009-12-11 16:03:27Z frank $
 */
public class ClassInfoFileCleaner {
    public static void main(String[] args) throws IOException, SOMToolboxException {
        // register and parse all options for the
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.getOptInputVectorFile(true),
                OptionFactory.getOptClassInformationFile(true), OptionFactory.getOptOutputFileName(true), OptionFactory.getOptOutputDirectory(false));

        String vectorFileName = config.getString("inputVectorFile");
        String classInfoFile = config.getString("classInformationFile");
        String outputDir = config.getString("outputDirectory", ".");
        String outputFileName = config.getString("output");

        SOMLibSparseInputData inputData = new SOMLibSparseInputData(vectorFileName);
        SOMLibClassInformation classInfo = new SOMLibClassInformation(classInfoFile);
        classInfo.removeNotPresentElements(inputData);
        classInfo.writeToFile(outputDir + File.separator + outputFileName);
    }
}

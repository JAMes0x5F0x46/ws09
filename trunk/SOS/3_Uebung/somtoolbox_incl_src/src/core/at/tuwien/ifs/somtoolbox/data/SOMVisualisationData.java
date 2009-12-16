package at.tuwien.ifs.somtoolbox.data;

import java.awt.Frame;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Observable;

import javax.swing.JFileChooser;

import at.tuwien.ifs.somtoolbox.SOMToolboxException;
import at.tuwien.ifs.somtoolbox.apps.viewer.fileutils.MySOMVisualisationDataFileFilter;

/**
 * @author Rudolf Mayer
 * @version $Id: SOMVisualisationData.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMVisualisationData extends Observable {
    public static final String CLASS_INFO = "Class Information File";

    public static final String DATA_INFO = "Data Information File";

    public static final String DATA_WINNER_MAPPING = "Data Winner Mapping File";

    public static final String INPUT_VECTOR = "Input Vector File";

    public static final String TEMPLATE_VECTOR = "Template Vector File";

    public static final String QUALITY_MEASURE_CACHE_FILE = "QM Cache File";

    public static final String LINKAGE_MAP = "Date Item Linkage Map";

    public static final String INPUT_CORRECTIONS = "Input Corrections";

    public static final String INPUT_VECTOR_DISTANCE_MATRIX = "Input Vector Distance Matrix";

    public static final String REGRESS_INFORMATION = "Regression Information File";

    private Class<?> classType;

    private Object data;

    private String[] extensions;

    private String fileName;

    private String type;

    public SOMVisualisationData(String[] extensions, Object data, Class<?> classType, String type) {
        this.extensions = extensions;
        this.data = data;
        this.classType = classType;
        this.type = type;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public Object getData() {
        return data;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public String getFileName() {
        return fileName;
    }

    private String getMessageFromException(Exception e) {
        String message = "";
        if (e.getMessage() != null) {
            message += "\n" + e.getMessage();
        }
        if (e.getCause() != null) {
            message += "\n" + e.getCause().getMessage();
        }
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean loadFromFile(JFileChooser fileChooser, Frame parentFrame) throws SOMToolboxException {
        if (fileChooser.getSelectedFile() != null) { // reusing the dialog
            fileChooser = new JFileChooser(fileChooser.getSelectedFile().getPath());
        }
        fileChooser.setFileFilter(new MySOMVisualisationDataFileFilter(this));
        fileChooser.setName(getType());
        int returnVal = fileChooser.showDialog(parentFrame, "Open " + getType());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            readFromFile(fileChooser.getSelectedFile().getAbsolutePath());
            return true;
        } else {
            return false;
        }

    }

    public void readFromFile(String fileName) throws SOMToolboxException {
        try {
            Constructor<?> constr = getClassType().getConstructor(new Class[] { String.class });
            setData(constr.newInstance(new Object[] { fileName }));
            setFileName(fileName);
        } catch (Exception e) {
            try {
                Method method = getClassType().getMethod("initFromFile", String.class);
                Object obj = method.invoke(null, fileName);
                setData(obj);
                setFileName(fileName);
            } catch (Exception e2) {
                e.printStackTrace();
                e2.printStackTrace();
                throw new SOMToolboxException("Error loading file!\n" + getMessageFromException(e), e);
            }
        }
    }

    public void setData(Object data) {
        this.data = data;
        setChanged();
        notifyObservers(data);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}

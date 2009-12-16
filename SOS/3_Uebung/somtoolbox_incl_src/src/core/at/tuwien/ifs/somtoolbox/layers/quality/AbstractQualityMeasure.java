package at.tuwien.ifs.somtoolbox.layers.quality;

import java.lang.reflect.InvocationTargetException;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.layers.Layer;

/**
 * Provides basic functionality for quality measure algorithms.
 * 
 * @author Michael Dittenbach
 * @version $Id: AbstractQualityMeasure.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class AbstractQualityMeasure implements QualityMeasure {

    protected Layer layer;

    protected InputData data;

    protected String[] mapQualityNames;

    protected String[] mapQualityDescriptions;

    protected String[] unitQualityNames;

    protected String[] unitQualityDescriptions;

    /**
     * Instantiates a certain quality measure class specified by argument <code>mqName</code>.
     * 
     * @param qmName the name of the quality measure.
     * @return a quality measure object of class <code>mqName</code>.
     * @throws ClassNotFoundException if class denoted by argument <code>mqName</code> is not found.
     * @throws InstantiationException if if this Class represents an abstract class, an interface, an array class, a primitive type, or void; or if
     *             the class has no nullary constructor; or if the instantiation fails for some other reason.
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
     */
    public static QualityMeasure instantiate(String qmName, Layer layer, InputData data) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        QualityMeasure measure = null;
        Class<?>[] argsClasses = new Class[] { Layer.class, InputData.class };
        Object[] argsValues = new Object[] { layer, data };
        measure = (QualityMeasure) Class.forName(qmName).getConstructor(argsClasses).newInstance(argsValues);
        return measure;
    }

    public AbstractQualityMeasure(Layer layer, InputData data) {
        this.layer = layer;
        this.data = data;
    }

    public final String[] getMapQualityNames() {
        return mapQualityNames;
    }

    public final String[] getUnitQualityNames() {
        return unitQualityNames;
    }

    public String[] getMapQualityDescriptions() {
        return mapQualityDescriptions;
    }

    public String[] getUnitQualityDescriptions() {
        return unitQualityDescriptions;
    }

    /**
     * @param qmName
     * @return an array containing the class name on index 0, and the method name on index 1.
     */
    public static String[] splitNameAndMethod(String qmName) {
        String qmClassName = null;
        String qmMethodName = null;

        int lastDotIndex = qmName.lastIndexOf('.');
        qmClassName = qmName.substring(0, lastDotIndex);
        qmMethodName = qmName.substring(lastDotIndex + 1);
        return new String[] { qmClassName, qmMethodName };
    }
}

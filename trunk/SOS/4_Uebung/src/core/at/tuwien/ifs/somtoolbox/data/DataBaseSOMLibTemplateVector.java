package at.tuwien.ifs.somtoolbox.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.database.MySQLConnector;

/**
 * Implements a {@link TemplateVector} by reading the vector information from a database.
 * 
 * @author liegl
 * @author Rudolf Mayer
 * @version $Id: DataBaseSOMLibTemplateVector.java 2874 2009-12-11 16:03:27Z frank $
 */
public class DataBaseSOMLibTemplateVector extends AbstractSOMLibTemplateVector {

    /**
     * Creates a new {@link TemplateVector} by reading the labels and other attribute information from the database.
     */
    public DataBaseSOMLibTemplateVector(MySQLConnector dbConnector) {
        super();
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Start reading template vector from DB.");

        try {
            String query = "SELECT label, number, documentFrequency, collectionTermFrequency, minimumTermFrequency, maximumTermFrequency, meanTermFrequency, comment FROM "
                    + dbConnector.getTermTableName() + " ORDER BY number";
            ResultSet r = dbConnector.executeSelect(query);
            ArrayList<TemplateVectorElement> elementList = new ArrayList<TemplateVectorElement>();
            while (r.next()) {
                TemplateVectorElement e = new TemplateVectorElement(this, r.getString("label"), r.getInt("number"));
                e.setDocumentFrequency(r.getInt("documentFrequency"));
                e.setCollectionTermFrequency(r.getInt("collectionTermFrequency"));
                e.setMinimumTermFrequency(r.getInt("minimumTermFrequency"));
                e.setMaximumTermFrequency(r.getInt("maximumTermFrequency"));
                e.setMeanTermFrequency(r.getDouble("meanTermFrequency"));
                e.setComment(r.getString("comment"));
                elementList.add(e);
            }
            dim = elementList.size();
            elements = elementList.toArray(new TemplateVectorElement[elementList.size()]);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("An error occured while communicating with database. Aborting.");
            System.exit(1);
        }
        Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Template vector file format seems to be correct. Riding on ...");
    }

}

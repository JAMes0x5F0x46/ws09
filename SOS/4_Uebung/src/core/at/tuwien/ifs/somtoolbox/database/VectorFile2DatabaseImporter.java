package at.tuwien.ifs.somtoolbox.database;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.data.SOMLibSparseInputData;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Imports input and template vector files to a database.
 * 
 * @author Rudolf Mayer
 * @version $Id: VectorFile2DatabaseImporter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class VectorFile2DatabaseImporter  implements SOMToolboxApp {
    /**
     * This class customises the handling of data read from the file by storing it in the DB.
     * 
     * @author Rudolf Mayer
     */
    private class InputVectorImporter extends SOMLibSparseInputData {

        private MySQLConnector dbConnector;

        public InputVectorImporter(String inputVectorFile, MySQLConnector dbConnector) {
            super();
            this.dbConnector = dbConnector;
            readVectorFile(inputVectorFile, false);
        }

        /**
         * Stores the information read in the database.
         */
        protected void processLine(int documentIndex, String[] lineElements) throws Exception {
            String label = lineElements[dim].trim();
            dbConnector.doInsert(dbConnector.getDocumentTableName(), new String[] { "number", "label" }, new Object[] { new Integer(documentIndex),
                    label });
            for (int termIndex = 0; termIndex < dim; termIndex++) {
                dbConnector.doInsert(dbConnector.getDocumentTermsTableName(), new String[] { "documentNumber", "termNumber", "weight" },
                        new Object[] { new Integer(documentIndex), new Integer(termIndex), Double.valueOf(lineElements[termIndex]) });
            }
        }
    }

    /**
     * This class customises the handling of data read from the file by storing it in the DB.
     * 
     * @author Rudolf Mayer
     */
    private class TemplateVectorImporter extends SOMLibTemplateVector {

        private MySQLConnector dbConnector;

        public TemplateVectorImporter(String templateFileName, MySQLConnector dbConnector) throws IOException {
            super();
            this.dbConnector = dbConnector;
            readTemplateVectorFile(templateFileName);
        }

        /**
         * Stores the information read in the database.
         */
        protected void processLine(int index, String[] lineElements) {
            super.processLine(index, lineElements);
            try {
                String[] fields = new String[] { "number", "label", "documentFrequency", "collectionTermFrequency", "minimumTermFrequency",
                        "maximumTermFrequency", "meanTermFrequency", "comment" };
                Object[] values = new Object[] { index, elements[index].getLabel(), elements[index].getDocumentFrequency(),
                        elements[index].getCollectionTermFrequency(), elements[index].getMinimumTermFrequency(),
                        elements[index].getMaximumTermFrequency(), elements[index].getMeanTermFrequency(), elements[index].getComment() };
                dbConnector.doInsert(dbConnector.getTermTableName(), fields, values);
            } catch (SQLException e) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
                        "Error in communicating with the database for element " + index + ": '" + e.getMessage() + "'. Aborting.");
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Erronous label name: " + elements[index].getLabel());
                e.printStackTrace();
                try {
                    System.err.println(URLDecoder.decode(elements[index].getLabel(), "UTF8"));
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                System.exit(-1);
            }
        }
    }

    /**
     * Starts the import to the database.
     * 
     * @param args Needed program arguments:
     *            <ul>
     *            <li>-v inputVectorFile, mandatory</li>
     *            <li>-t templateVectorFile, mandatory</li>
     *            <li>--dbName databaseName, mandatory</li>
     *            <li>--tablePrefix databaseTableNamePrefix, mandatory</li>
     *            <li>--server databaseServerAddress, optional</li>
     *            <li>--user databaseUser, optional</li>
     *            <li>--password databasePassword, optional</li>
     *            </ul>
     * @throws SQLException If there is a problem connecting to the database.
     * @throws IOException If the input or template vector file can't be read.
     */
    public static void main(String[] args) throws SQLException, IOException {
        JSAPResult config = OptionFactory.parseResults(args, OptionFactory.OPTIONS_VECTORFILE_2_DATABASE_IMPORTER);
        String inputVectorFile = config.getString("inputVectorFile");
        String templateVectorFile = config.getString("templateVectorFile");
        String databaseTableNamePrefix = config.getString("databaseTableNamePrefix");
        String databaseServerAddress = config.getString("databaseServerAddress");
        String databaseName = config.getString("databaseName");
        String user = config.getString("databaseUser");
        String password = config.getString("databasePassword");

        new VectorFile2DatabaseImporter(inputVectorFile, templateVectorFile, databaseServerAddress, databaseName, user, password,
                databaseTableNamePrefix);
    }

    public VectorFile2DatabaseImporter(String inputVectorFile, String templateVectorFile, String databaseServerAddress, String databaseName,
            String user, String password, String databaseTableNamePrefix) throws SQLException, IOException {
        MySQLConnector dbConnector = new MySQLConnector(databaseServerAddress, databaseName, user, password, databaseTableNamePrefix);
        dbConnector.setupTables();
        new TemplateVectorImporter(templateVectorFile, dbConnector);
        new InputVectorImporter(inputVectorFile, dbConnector);
    }

}

package at.tuwien.ifs.somtoolbox.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An abstract Database connector.
 * 
 * @author Rudolf Mayer
 * @version $Id: DBConnector.java 2874 2009-12-11 16:03:27Z frank $
 */
public abstract class DBConnector {
    public static Hashtable<String, Class<?>> fieldsDocument = new Hashtable<String, Class<?>>();

    public static Hashtable<String, Class<?>> fieldsDocumentTerms = new Hashtable<String, Class<?>>();

    public static Hashtable<String, Class<?>> fieldsTerm = new Hashtable<String, Class<?>>();

    Statement statement;
    static {
        fieldsDocument.put("number", Integer.class);
        fieldsDocument.put("label", String.class);

        fieldsTerm.put("number", Integer.class);
        fieldsTerm.put("label", String.class);
        fieldsTerm.put("documentFrequency", Integer.class);
        fieldsTerm.put("collectionTermFrequency", Integer.class);
        fieldsTerm.put("minimumTermFrequency", Integer.class);
        fieldsTerm.put("maximumTermFrequency", Integer.class);
        fieldsTerm.put("meanTermFrequency", Double.class);
        fieldsTerm.put("comment", String.class);

        fieldsDocumentTerms.put("documentNumber", Integer.class);
        fieldsDocumentTerms.put("termNumber", Integer.class);
        fieldsDocumentTerms.put("rawTermFrequency", Integer.class);
        fieldsDocumentTerms.put("weight", Double.class);
        fieldsDocumentTerms.put("weightNormalised", Double.class);
    }

    protected Connection con;

    protected String databaseName;

    protected String databaseUrl;

    protected String password;

    protected String tableNamePrefix;

    protected String user;

    protected String documentTableName = "Document";

    protected String documentTermsTableName = "DocumentTerms";

    protected String termTableName = "Term";

    protected String statementEndCharacter = "";

    public DBConnector(String databaseUrl, String databaseName, String user, String password, String tableNamePrefix) {
        this.databaseUrl = databaseUrl;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.tableNamePrefix = tableNamePrefix;
        documentTableName = tableNamePrefix + documentTableName;
        documentTermsTableName = tableNamePrefix + documentTermsTableName;
        termTableName = tableNamePrefix + termTableName;
    }

    /**
     * Checks whether the connection is open, and opens a new connection if needed.
     * 
     * @throws SQLException
     */
    protected void checkDBConnection() throws SQLException {
        if (con == null) { // no connection? --> open connection
            con = this.openConnection();
            this.statement = con.createStatement();
        }
        if (con == null) { // still no connection?
            throw new SQLException("No connection to DB.");
        }
    }

    public boolean connect() throws SQLException {
        checkDBConnection();
        return true;
    }

    /**
     * Closes the DB connection.
     * 
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.commit();
            con.close();
            con = null;
        }
    }

    public abstract Connection openConnection() throws SQLException;

    /**
     * Inserts a new row into a table.
     * 
     * @param tableName The table to insert.
     * @param columns The names of the columns.
     * @param values The values, in the same order as the column names
     * @return the row count.
     * @throws SQLException
     */
    public int doInsert(String tableName, String[] columns, Object[] values) throws SQLException {
        StringBuffer query = getInsertQuery(tableName, columns, values);
        return executeUpdate(query.toString());
    }

    public int doInsert(String tableName, String column, Object value) throws SQLException {
        StringBuffer query = getInsertQuery(tableName, column, value);
        return executeUpdate(query.toString());
    }

    public int doInsert(String tableName, String column, String value) throws SQLException {
        StringBuffer query = getInsertQuery(tableName, column, value);
        return executeUpdate(query.toString());
    }

    /**
     * Inserts multiple rows into the table.
     * 
     * @see MySQLConnector#doInsert(String, String[], Object[])
     */
    public int doInsert(String tableName, String[] columns, Object[][] values) throws SQLException {
        StringBuffer query = new StringBuffer(50 + columns.length * 7 + values.length * values[0].length * 10);
        query.append("INSERT into " + tableName + "(");
        for (int i = 0; i < columns.length; i++) {
            query.append(columns[i]);
            if (i + 1 < columns.length) {
                query.append(", ");
            }
        }
        query.append(") VALUES ");
        for (int row = 0; row < values.length; row++) {
            query.append("(");
            for (int i = 0; i < values[row].length; i++) {
                String value = String.valueOf(values[row][i]);
                if (values[row][i] instanceof String) {
                    value = "\"" + value + "\"";
                }
                query.append(value);
                if (i + 1 < values[row].length) {
                    query.append(", ");
                }
            }
            query.append(")");
            if (row + 1 < values.length) {
                query.append(", ");
            }
        }
        query.append(";");
        return executeUpdate(query.toString());
    }

    /** Creates a prepared statement for the given query */
    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        return con.prepareStatement(sql);
    }

    private StringBuffer getInsertQuery(String tableName, String[] columns, Object[] values) {
        StringBuffer query = new StringBuffer(50 + columns.length * 7 + values.length * 10);
        query.append("INSERT into ").append(tableName).append("(");
        for (int i = 0; i < columns.length; i++) {
            query.append(columns[i]);
            if (i + 1 < columns.length) {
                query.append(", ");
            }
        }
        query.append(") VALUES (");
        for (int i = 0; i < values.length; i++) {
            String value = String.valueOf(values[i]);
            if (values[i] instanceof String) {
                value = "\"" + value + "\"";
            }
            query.append(value);
            if (i + 1 < values.length) {
                query.append(", ");
            }
        }
        query.append(")").append(statementEndCharacter);
        return query;
    }

    private StringBuffer getInsertQuery(String tableName, String column, Object value) {
        StringBuffer query = new StringBuffer(50 + column.length() + value.toString().length());
        query.append("INSERT into ").append(tableName).append("(").append(column).append(") VALUES (");
        query.append(value).append(")").append(statementEndCharacter);
        return query;
    }

    private StringBuffer getInsertQuery(String tableName, String column, String value) {
        StringBuffer query = new StringBuffer(50 + column.length() + value.length());
        query.append("INSERT into ").append(tableName).append("(").append(column).append(") VALUES (");
        query.append("\"").append(value).append("\"").append(")").append(statementEndCharacter);
        return query;
    }

    /**
     * Executes a SELECT Statement and returns a ResultSet
     * 
     * @param query String
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executeSelect(String query) throws SQLException {
        checkDBConnection();
        // System.out.println("query: " + query);
        return statement.executeQuery(query);
    }

    /**
     * Executes an update statement.
     * 
     * @param query
     * @return either the row count for INSERT, UPDATE or DELETE statements, or 0 for SQL statements that return nothing.
     * @throws SQLException
     */
    public int executeUpdate(String query) throws SQLException {
        checkDBConnection();
        return statement.executeUpdate(query);
    }

    /**
     * @return the name of the table containing the document labels.
     */
    public String getDocumentTableName() {
        return documentTableName;
    }

    /**
     * @return the name of the table containing the vector elements.
     */
    public String getDocumentTermsTableName() {
        return documentTermsTableName;
    }

    /**
     * @return the name of the table containing the template vector elements.
     */
    public String getTermTableName() {
        return termTableName;
    }

    /**
     * Creates a database table.
     * 
     * @param tableName
     * @param fields
     * @param primaryKey
     * @param uniqueKey
     * @throws SQLException
     */
    protected void createTable(String tableName, Hashtable<String, Class<?>> fields, String primaryKey, String uniqueKey) throws SQLException {
        checkDBConnection();
        String dropStmt = "DROP TABLE IF EXISTS " + tableName;
        statement.execute(dropStmt);
        String stmt = "CREATE TABLE " + tableName + " (\n";
        Enumeration<String> e = fields.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            stmt += key + " ";

            if (fields.get(key) == String.class) {
                stmt += "varchar(255)";
            } else if (fields.get(key) == Integer.class) {
                stmt += "int(11)";
            } else if (fields.get(key) == Double.class) {
                stmt += "double";
            }
            // primary and unique keys must be NOT NULL
            if ((primaryKey != null && primaryKey.indexOf(key) != -1) || (uniqueKey != null && uniqueKey.indexOf(key) != -1)) {
                stmt += " NOT NULL";
            }
            if (e.hasMoreElements()) {
                stmt += ",\n";
            }
        }
        if (primaryKey != null) {
            stmt += ",\nPRIMARY KEY  (" + primaryKey + ")";
        }
        if (uniqueKey != null) {
            stmt += ",\nUNIQUE KEY  (" + uniqueKey + ")";
        }
        stmt += ") ENGINE=myISAM DEFAULT CHARSET=latin2;";
        statement.execute(stmt);
    }

    /**
     * Prepares the database by creating the needed tables.
     * 
     * @throws SQLException
     */
    public void setupTables() throws SQLException {
        createTable(getDocumentTableName(), fieldsDocument, "number", "label");
        createTable(getTermTableName(), fieldsTerm, "number", "label");
        createTable(getDocumentTermsTableName(), fieldsDocumentTerms, "documentNumber, termNumber", null);
    }
}
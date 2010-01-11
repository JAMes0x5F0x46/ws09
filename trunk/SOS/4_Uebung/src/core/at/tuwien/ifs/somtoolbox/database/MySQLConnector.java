package at.tuwien.ifs.somtoolbox.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * This class handles the communication with a MySQL database.<br/>
 * TODO: replace using this class by using e.g. Hibernate
 * 
 * @author Rudolf Mayer
 * @version $Id: MySQLConnector.java 2874 2009-12-11 16:03:27Z frank $
 */
public class MySQLConnector extends DBConnector {

    public MySQLConnector(String databaseUrl, String databaseName, String user, String password, String tableNamePrefix) {
        super(databaseUrl, databaseName, user, password, tableNamePrefix);
        statementEndCharacter = ";";
    }

    /**
     * Opens a Connection to the database
     * 
     * @return the database connection
     * @throws SQLException
     */
    public Connection openConnection() throws SQLException {
        con = null;
        // Get an instance of the database driver
        try {
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Loading JDBC driver....");
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("done!");
            // driver found - proceed
            String connectionString = "jdbc:mysql://" + databaseUrl + ":3306/" + databaseName + "?user=" + user;
            String connectionStringDBServer = "jdbc:mysql://" + databaseUrl + ":3306/" + "?user=" + user;
            if (password != null && !password.equals("")) {
                connectionString += "&password=" + password;
                connectionStringDBServer += "&password=" + password;
            }
            // create database if needed
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Opening connection to " + connectionStringDBServer);
            con = DriverManager.getConnection(connectionStringDBServer);
            con.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + databaseName);

            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Opening connection to " + connectionString);
            con = DriverManager.getConnection(connectionString);
            Logger.getLogger("at.tuwien.ifs.somtoolbox").info("Opened connection.");
            return con;
        } catch (Exception e) { // If no driver is found - terminate
            // FIXME: throw better exception
            Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Unable to find appropriate MySQL database driver 'com.mysql.jdbc.Driver'. Aborting.");
            e.printStackTrace();
            throw new SQLException(e.getMessage());
        }

    }

}

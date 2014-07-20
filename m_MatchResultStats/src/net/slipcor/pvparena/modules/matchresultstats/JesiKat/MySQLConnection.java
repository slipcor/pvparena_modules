package net.slipcor.pvparena.modules.matchresultstats.JesiKat;


import java.sql.*;

/**
 * @author Jesika(Kaitlyn) Tremaine aka JesiKat
 *         <p/>
 *         MySQLConnection.
 *         This class is a simple database connector with the basic useful methods such as getting the number of rows in a table,
 *         the names of columns in the table, etc.
 */
public class MySQLConnection {
    /*The host for the database, the username for the database, and the password*/
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;

    /*The connection object*/
    private Connection databaseConnection;

    public MySQLConnection(String table, String host, int port, String database, String username, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String dbTable = table;
        this.dbUrl = host + ":" + port + "/" + database;
        this.dbUsername = username;
        this.dbPassword = password;
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    }

    /**
     * @param printerror If this is true, this method will print an error if there is one and return false
     * @return True if the connection was made successfully, false if otherwise.
     */
    public boolean connect(boolean printerror) {
        try {
            this.databaseConnection = DriverManager.getConnection("jdbc:mysql://" + this.dbUrl + "?autoReconnect=true", this.dbUsername, this.dbPassword);
            return this.databaseConnection != null;
        } catch (SQLException e) {
            if (printerror) e.printStackTrace();
            return false;
        }
    }

    /**
     * @param query    The Query to send to the SQL server.
     * @param modifies If the Query modifies the database, set this to true. If not, set this to false
     * @return If modifies is true, returns a valid ResultSet obtained from the Query. If modifies is false, returns null.
     * @throws SQLException if the Query had an error or there was not a valid connection.
     */
    public ResultSet executeQuery(String query, boolean modifies) throws SQLException {
        Statement statement = this.databaseConnection.createStatement();
        if (modifies) {
            statement.execute(query);
            return null;
        } else {
            return statement.executeQuery(query);
        }
    }

    /**
     * @param database The database to check for the table in.
     * @param table    The table to check for existence.
     * @return true if the table exists, false if there was an error or the database doesn't exist.
     * <p/>
     * This method looks through the information schema that comes with a MySQL installation and checks
     * if a certain table exists within a database.
     */
    public boolean tableExists(String database, String table) {
        String format = "SELECT * FROM `information_schema`.`TABLES` WHERE TABLE_SCHEMA = '$DB' && TABLE_NAME = '$TABLE';";
        try {
            return this.databaseConnection.createStatement().executeQuery(format.replace("$DB", database).replace("$TABLE", table)).first();
        } catch (SQLException e) {
            return false;
        }
    }
}

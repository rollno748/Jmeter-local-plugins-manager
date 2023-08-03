package io.perfwise.local.pluginsmanager.sqlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SQLiteConnectionPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteConnectionPool.class);
    private String DB_FILE_PATH;
    private static String PLUGINS_METADATA = "CREATE TABLE metadata (id TEXT PRIMARY KEY, versions TEXT(1000000))";
    private static String PLUGINS_INFO = "CREATE TABLE plugins (id TEXT PRIMARY KEY, name TEXT, description TEXT, helpUrl TEXT, markerClass TEXT, screenshotUrl TEXT, vendor TEXT, versions_count INTEGER)";

    private final BlockingQueue<Connection> connections;

    public SQLiteConnectionPool(int poolSize) {
        connections = new ArrayBlockingQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:mydb.sqlite");
                connections.add(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public SQLiteConnectionPool(String dbPath, int poolSize){
        this.DB_FILE_PATH = dbPath;
        connections = new ArrayBlockingQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:mydb.sqlite");
                connections.add(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Connection getConnection() throws InterruptedException {
        return connections.take();
    }

    public void releaseConnection(Connection connection) {
        try {
            connections.put(connection);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    public void close() {
        for (Connection connection : connections) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    public static Connection validateDatabase(String dbPath) throws SQLException {
        Connection connection = null;
        String[] tableNames = {"metadata", "plugins"};
        String pathSeparator = System.getProperty("file.separator");
        boolean isTablePresent = false;

        if (!dbPath.endsWith(pathSeparator)) {
            dbPath += pathSeparator;
        }
        dbPath += ".metadata.db";

        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            // Create a new database file if it does not exist.
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.createStatement().execute(PLUGINS_METADATA);
            connection.createStatement().execute(PLUGINS_INFO);
        } else {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            if(connection.isValid(5)){
                try{
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet md_table = metaData.getTables(null, null, tableNames[0], null);
                    ResultSet plug_table = metaData.getTables(null, null, tableNames[1], null);
                    if (md_table.isBeforeFirst() && plug_table.isBeforeFirst()) {
                        isTablePresent = true;
                        LOGGER.info("Table Present - Skipping Tables creation");
                    }
                } catch (SQLException e) {
                    LOGGER.error("Exception occurred while fetching metadata from db file : %s", e);
                }
                if(!isTablePresent){
                    LOGGER.info("Table not Present - creating Tables");
                    connection.createStatement().execute(PLUGINS_METADATA);
                    connection.createStatement().execute(PLUGINS_INFO);
                }
            }
        }
        return connection;
    }
}

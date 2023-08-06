package io.perfwise.local.pluginsmanager.sqlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SQLiteConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteConnectionPool.class);
    private static final int DEFAULT_MIN_POOL_SIZE = 5;
    private static final int DEFAULT_MAX_POOL_SIZE = 20;
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;

    private static SQLiteConnectionPool instance;
    private static String DB_FILE_PATH;
    private static String PLUGINS_METADATA = "CREATE TABLE metadata (id TEXT PRIMARY KEY, versions TEXT(1000000))";
    private static String PLUGINS_INFO = "CREATE TABLE plugins (id TEXT PRIMARY KEY, name TEXT, description TEXT, helpUrl TEXT, markerClass TEXT, screenshotUrl TEXT, vendor TEXT, versions_count INTEGER)";
    private static int minPoolSize;
    private static int maxPoolSize;
    private static int timeoutSeconds;
    private static BlockingQueue<Connection> connections;

    private SQLiteConnectionPool(String dbPath) {
        this(dbPath, DEFAULT_MIN_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, DEFAULT_TIMEOUT_SECONDS);
    }

    private SQLiteConnectionPool(String dbPath, int minPoolSize, int maxPoolSize, int timeoutSeconds) {
        SQLiteConnectionPool.minPoolSize = minPoolSize;
        SQLiteConnectionPool.maxPoolSize = maxPoolSize;
        SQLiteConnectionPool.timeoutSeconds = timeoutSeconds;
        SQLiteConnectionPool.DB_FILE_PATH = dbPath + ".metadata.db";
        connections = new ArrayBlockingQueue<>(maxPoolSize);

        for (int i = 0; i < maxPoolSize; i++) {
            try {
                Connection connection = createConnection(SQLiteConnectionPool.getDB_FILE_PATH());
                connections.add(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Connection createConnection(String dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    public static synchronized SQLiteConnectionPool getInstance(String dbPath, int minPoolSize, int maxPoolSize, int timeoutSeconds) {
        if (instance == null) {
            instance = new SQLiteConnectionPool(dbPath, minPoolSize, maxPoolSize, timeoutSeconds);
        }
        return instance;
    }

    public static Connection getConnection() throws InterruptedException, SQLException {
        Connection connection = connections.poll(getTimeoutSeconds(), TimeUnit.SECONDS);
        if (connection == null) {
            if (connections.size() < getMaxPoolSize()) {
                connection = createConnection(getDB_FILE_PATH());
            } else {
                throw new RuntimeException("Connection pool timeout");
            }
        }
        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            if (!connections.contains(connection) && connections.size() < maxPoolSize) {
                connections.offer(connection);
            } else {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
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
        connections.clear();
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

    public static String getDB_FILE_PATH() {
        return DB_FILE_PATH;
    }

    public static void setDB_FILE_PATH(String DB_FILE_PATH) {
        SQLiteConnectionPool.DB_FILE_PATH = DB_FILE_PATH;
    }

    public static int getMinPoolSize() {
        return minPoolSize;
    }

    public static void setMinPoolSize(int minPoolSize) {
        SQLiteConnectionPool.minPoolSize = minPoolSize;
    }

    public static int getMaxPoolSize() {
        return maxPoolSize;
    }

    public static void setMaxPoolSize(int maxPoolSize) {
        SQLiteConnectionPool.maxPoolSize = maxPoolSize;
    }

    public static int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public static void setTimeoutSeconds(int timeoutSeconds) {
        SQLiteConnectionPool.timeoutSeconds = timeoutSeconds;
    }
}

package io.perfwise.local.pluginsmanager.sqlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SQLiteConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteConnectionPool.class);
    private static final int DEFAULT_MIN_POOL_SIZE = 1;
    private static final int DEFAULT_MAX_POOL_SIZE = 5;
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    private static final Object lock = new Object();
    private static SQLiteConnectionPool instance;
    private static String DB_FILE_PATH;
    private static final String DBFILENAME = "plugins.db";
    private static final String PLUGINS_METADATA = "CREATE TABLE metadata (id TEXT PRIMARY KEY, versions TEXT(1000000))";
    private static final String PLUGINS_INFO = "CREATE TABLE plugins (id TEXT PRIMARY KEY, name TEXT, type TEXT, description TEXT, helpUrl TEXT, markerClass TEXT, screenshotUrl TEXT, vendor TEXT, versions_count INTEGER)";
    private static int minPoolSize;
    private static int maxPoolSize;
    private static int timeoutSeconds;
    private static BlockingQueue<Connection> connections;

    private SQLiteConnectionPool(String dbPath) {
        this(dbPath, DEFAULT_MIN_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, DEFAULT_TIMEOUT_SECONDS);
    }

    private SQLiteConnectionPool(String dbPath, int minPoolSize, int maxPoolSize, int timeoutSeconds) {
        setMinPoolSize(minPoolSize);
        setMaxPoolSize(maxPoolSize);
        setTimeoutSeconds(timeoutSeconds);
        setDB_FILE_PATH(dbPath + DBFILENAME);
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

    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            synchronized (lock) { // Synchronize on a separate lock object
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean createLocalDatabase(String dbFilePath) {
        String pathSeparator = System.getProperty("file.separator");
        boolean result = false;
        if (!dbFilePath.endsWith(pathSeparator)) {
            dbFilePath += pathSeparator;
        }else{
            dbFilePath += DBFILENAME;
        }
        String dbUrl = "jdbc:sqlite:" + dbFilePath;
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            if (conn != null) {
                validateDatabase(conn);
            }
            result = true;
        } catch (SQLException e) {
            LOGGER.error("Exception occurred while creating database: {}", e.getMessage());
        }
        return result;
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

    private static void validateDatabase(Connection conn) {
        String[] tableNames = {"metadata", "plugins"};
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet metadata_table = metaData.getTables(null, null, tableNames[0], null);
            ResultSet plugin_table = metaData.getTables(null, null, tableNames[1], null);

            if (metadata_table.isBeforeFirst() && plugin_table.isBeforeFirst()) {
                LOGGER.info("Table Present - Skipping Tables creation");
            } else {
                LOGGER.info("Table not Present - creating Tables");
                conn.createStatement().execute(PLUGINS_METADATA);
                conn.createStatement().execute(PLUGINS_INFO);
            }
        } catch (SQLException e) {
            LOGGER.error("Exception occurred while fetching metadata from db file", e);
        } finally {
            releaseConnection(conn);
        }
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

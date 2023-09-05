package io.perfwise.local.pluginsmanager.utils;

import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

public class PreCheckValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreCheckValidation.class);
    private final Path basePath;
    private final DirectoryOps directoryOps;

    public PreCheckValidation(Properties props) {
        this.basePath = Paths.get(props.getProperty("local.repo.path"));
        this.directoryOps = new DirectoryOps();
    }

    public boolean validateDirectoryPresence() {
        if(basePath == null || basePath.toString().isEmpty()) {
            LOGGER.error("Plugins directory path is invalid: {}", basePath);
            return false;
        }

        String pluginsPath = this.basePath.resolve("plugins").toString();
        String dependenciesPath = this.basePath.resolve("libs").toString();
        String customPluginsPath = this.basePath.resolve("custom").toString();

        if (directoryOps.createDirectory(pluginsPath) && directoryOps.createDirectory(dependenciesPath) && directoryOps.createDirectory(customPluginsPath)){
            LOGGER.info("Created local plugins directory successfully !");
            return true;
        }else{
            LOGGER.error("Failed to create local plugins directory");
            return false;
        }
    }

    public boolean validate() throws SQLException {
        boolean result = false;
        if (validateDirectoryPresence()){
            result = createLocalDatabaseIfNotPresent();
        }else {
            result = createLocalDatabaseIfNotPresent();
        }
        return result;
    }

    private boolean createLocalDatabaseIfNotPresent() {
        return SQLiteConnectionPool.createLocalDatabase(this.basePath.toString());
    }

}

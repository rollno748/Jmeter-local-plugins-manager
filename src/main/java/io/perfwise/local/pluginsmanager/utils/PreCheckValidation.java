package io.perfwise.local.pluginsmanager.utils;

import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Properties;

public class PreCheckValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreCheckValidation.class.getName());
    private final Properties props;
    private final DirectoryOps directoryOps;

    public PreCheckValidation(Properties props) {
        this.props = props;
        this.directoryOps = new DirectoryOps();
    }

    public boolean validateDirectoryPresence() {
        String pluginsPath = props.getProperty("local.repo.plugins.dir.path");
        if(pluginsPath == null || pluginsPath.isEmpty()) {
            LOGGER.error("Plugins directory path is invalid: {}", pluginsPath);
            return false;
        }

        String dependenciesPath = props.getProperty("local.repo.dependencies.dir.path");
        if(dependenciesPath == null || dependenciesPath.isEmpty()) {
            LOGGER.error("Dependencies directory path is invalid: {}", dependenciesPath);
            return false;
        }

        String customPluginsPath = props.getProperty("local.custom.plugins.dir.path");
        if(customPluginsPath == null || customPluginsPath.isEmpty()) {
            LOGGER.error("Custom Plugins directory path is invalid: {}", customPluginsPath);
            return false;
        }

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
        return SQLiteConnectionPool.createLocalDatabase(props.getProperty("local.sqlite.db.path"));
    }

}

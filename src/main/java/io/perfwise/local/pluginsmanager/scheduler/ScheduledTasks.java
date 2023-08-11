package io.perfwise.local.pluginsmanager.scheduler;

import io.perfwise.local.pluginsmanager.scheduler.http.HttpRequest;
import io.perfwise.local.pluginsmanager.scheduler.parser.Parse;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;

public class ScheduledTasks extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
    private final Properties props;
    private Parse parser;
    private int localDBPluginsCount;

    public ScheduledTasks(Properties props) {
        this.props = props;
        this.parser = new Parse(props);
    }

    @Override
    public void run() {
        try {
            JSONArray pluginsArray = HttpRequest.get(props.getProperty("jmeter.plugins.url"));
            if(isNewPluginsAvailable(pluginsArray)){
                if(getLocalDBPluginsCount() == 0){
                    parser.downloadAllPlugins(pluginsArray);
                }else{
                    List<String> missingPluginsList = parser.getMissingPluginsNames(pluginsArray);
                    if(pluginsArray.length() > missingPluginsList.size()){
                        parser.downloadMissingPlugins(missingPluginsList, pluginsArray);
                    }
                }
            }else{
                LOGGER.info("Skipping Downloader - No new plugins available");
            }
        } catch (IOException | SQLException | InterruptedException e) {
            LOGGER.error("Exception occurred while checking with plugins manager");
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        LOGGER.debug("Checking for plugin updates from Plugins manager");
    }

    private Boolean isNewPluginsAvailable(JSONArray pluginsArray) throws SQLException, InterruptedException {
        int publicCount = pluginsArray.length();
        int localStoreCount = Parse.getLocalPluginCount(pluginsArray);
        setLocalDBPluginsCount(localStoreCount);
        return publicCount > localStoreCount;
    }

    public int getLocalDBPluginsCount() {
        return localDBPluginsCount;
    }

    public void setLocalDBPluginsCount(int localDBPluginsCount) {
        this.localDBPluginsCount = localDBPluginsCount;
    }
}

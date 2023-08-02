package io.perfwise.local.pluginsmanager.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.perfwise.local.pluginsmanager.scheduler.http.HttpRequest;
import io.perfwise.local.pluginsmanager.scheduler.model.PluginModel;
import io.perfwise.local.pluginsmanager.scheduler.parser.Parse;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimerTask;
public class ScheduledTasks extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
    private final Properties props;

    public ScheduledTasks(Properties props) {
        this.props = props;
    }

    @Override
    public void run() {
        try {
            JSONArray js = HttpRequest.get(props.getProperty("jmeter.plugins.url"));

            ObjectMapper objectMapper = new ObjectMapper();
            PluginModel pluginModel = objectMapper.readValue(js.get(0).toString(), PluginModel.class);

            System.out.println("Plugin ID: " + pluginModel.getId());
            System.out.println("Plugin Name: " + pluginModel.getName());
            System.out.println("Plugin Description: " + pluginModel.getDescription());
            System.out.println("Total available versions: "+ pluginModel.getVersions().size());
            // Accessing the "versions" object
            for (Map.Entry<String, PluginModel.Version> entry : pluginModel.getVersions().entrySet()) {
                String versionName = entry.getKey();
                PluginModel.Version version = entry.getValue();
                System.out.println("Version: " + versionName);
                System.out.println("Depends: " + String.join(", ", version.getDepends()));
                System.out.println("Download URL: " + version.getDownloadUrl());
                System.out.println("Changes: " + version.getChanges());
            }

            List<String> ls = Parse.getAllPluginsNames(js);
            System.out.println(ls);
        } catch (IOException e) {
            LOGGER.error("Exception occurred while checking with plugins manager");
            throw new RuntimeException(e);
        }
        LOGGER.debug("Checking for plugin updates from Plugins manager");
    }
}

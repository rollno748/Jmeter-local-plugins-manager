package io.perfwise.local.pluginsmanager;

import io.perfwise.local.pluginsmanager.controller.RestController;
import io.perfwise.local.pluginsmanager.scheduler.ScheduledTasks;
import io.perfwise.local.pluginsmanager.utils.PreCheckValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.Properties;

public class LocalPluginsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalPluginsManager.class);
    private static final Properties props = new Properties();

    public static void main(String[] args) throws IOException{
        Timer timer = new Timer();
        RestController restController;
        try {
            InputStream inputStream = LocalPluginsManager.class.getClassLoader().getResourceAsStream("config.properties");
            props.load(inputStream);
            LOGGER.info("Properties load :: Success");

            if (!props.isEmpty()) {
                if(new PreCheckValidation(props).validateDirectoryPresence()){
                    timer.schedule(new ScheduledTasks(props), 0, Long.parseLong(props.getProperty("scheduler.interval")));
                    new RestController(props).startRestServer();
                }else{
                    throw new IOException("Failed to create directories, verify the location permissions in config file");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            RestController.stopRestServer();
        }
    }
}

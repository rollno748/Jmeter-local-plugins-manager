package io.perfwise.local.pluginsmanager;

import io.perfwise.local.pluginsmanager.controller.RestController;
import io.perfwise.local.pluginsmanager.scheduler.ScheduledTasks;
import io.perfwise.local.pluginsmanager.utils.PreCheckValidation;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Timer;
import java.util.Properties;

public class LocalPluginsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalPluginsManager.class);
    private static final Properties props = new Properties();

    public static void main(String[] args) throws IOException{
        Timer timer = new Timer();
        RestController restController;

        Options options = new Options();
        options.addOption("config", true, "Path to the properties file");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command-line arguments: " + e.getMessage());
            System.exit(1);
            return;
        }

        try {
            InputStream inputStream = LocalPluginsManager.class.getClassLoader().getResourceAsStream("config.properties");
            props.load(inputStream);
            LOGGER.info("Properties load :: Success");

            if (!props.isEmpty()) {
                if(new PreCheckValidation(props).validate()){
                    new RestController(props).startRestServer();
                    timer.schedule(new ScheduledTasks(props), 0, Long.parseLong(props.getProperty("scheduler.interval")));
                }else{
                    throw new IOException("Failed to create directories, verify the location permissions in config file");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            RestController.stopRestServer();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

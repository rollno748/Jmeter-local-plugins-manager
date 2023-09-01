package io.perfwise.local.pluginsmanager;

import io.perfwise.local.pluginsmanager.controller.RestController;
import io.perfwise.local.pluginsmanager.scheduler.ScheduledTasks;
import io.perfwise.local.pluginsmanager.utils.PreCheckValidation;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Timer;

public class LocalPluginsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalPluginsManager.class);
    private static final Properties props = new Properties();

    public static void main(String[] args) throws IOException, SQLException {
        Timer timer = new Timer();

        Options options = new Options();

        Option helpOpt = Option.builder("h")
                .longOpt("help")
                .desc("Usage Help")
                .build();

        Option configOpt = Option.builder("c")
                .longOpt("config")
                .desc("Config file path")
                .hasArg()
                .argName("config")
                .required()
                .build();

        options.addOption(helpOpt);
        options.addOption(configOpt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        if(args.length != 0){
            try {
                cmd = parser.parse(options, args);
                if (cmd.hasOption("h")) {
                    System.out.println("h");
                    System.exit(1);
                }
                if (cmd.hasOption("c")) {
                    String configFile = cmd.getOptionValue("config");
                    try (InputStream inputStream = Files.newInputStream(Paths.get(configFile))) {
                        props.load(inputStream);
                        LOGGER.info("Properties load :: Success");
                    } catch (IOException e) {
                        LOGGER.error("Error loading properties from file: " + e.getMessage());
                        System.exit(1);
                    }
                } else {
                    LOGGER.error("Missing command-line arguments");
                    System.exit(1);
                }
            } catch (ParseException e) {
                LOGGER.error("Error parsing command-line arguments: " + e.getMessage());
                System.exit(1);
                return;
            }
        }else {
            LOGGER.info("Missing command-line arguments - Loading properties from resources");
            try(InputStream inputStream = LocalPluginsManager.class.getClassLoader().getResourceAsStream("config.properties")){
                if (inputStream != null) {
                    props.load(inputStream);
                    LOGGER.info("Properties load :: Success");
                } else {
                    LOGGER.error("Resource 'config.properties' not found");
                    System.exit(1);
                }
            }
        }

        if (!props.isEmpty()) {
            if(new PreCheckValidation(props).validate()){
                new RestController(props).startRestServer();
                timer.schedule(new ScheduledTasks(props), 0, Long.parseLong(props.getProperty("scheduler.interval")));
            }else{
                throw new IOException("Failed to create directories, verify the location permissions in config file");
            }
        }
    }
}


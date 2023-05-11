package io.perfwise.local.pluginsmanager;

import io.perfwise.local.pluginsmanager.controller.RestController;
import io.perfwise.local.pluginsmanager.utils.PreCheckValidation;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.Properties;




public class LocalPluginsManager {

    private static final Logger LOGGER = Logger.getLogger(LocalPluginsManager.class.getName());
    private static final Properties props = new Properties();

    public static void main(String[] args) throws IOException{
        RestController restController;
        try {
            InputStream inputStream = LocalPluginsManager.class.getClassLoader().getResourceAsStream("config.properties");
            props.load(inputStream);
            LOGGER.info("Properties load :: Success");

            if (!props.isEmpty()) {
                new PreCheckValidation(props).validateDirectoryPresence();
                new RestController(props).startRestServer();
            }
        } catch (IOException e) {
            e.printStackTrace();
            RestController.stopRestServer();
        }
    }


}

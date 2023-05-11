package io.perfwise.local.pluginsmanager.controller;

import spark.Spark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Properties;

import static spark.Spark.init;
import static spark.Spark.port;
import static spark.Spark.get;

public class RestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestController.class);
    private Properties props;
    public RestController() {
    }
    public RestController(Properties props) {
        this.props = props;
    }

    public void startRestServer() {

        int serverPort = Integer.parseInt(props.getProperty("server.port"));
        String uriPath = props.getProperty("server.uri.path");
        try {
            port(serverPort);
            init();
            LOGGER.info(String.format("Local Plugins manager - REST services started :: http://%s:%s%s/",
                    InetAddress.getLocalHost().getHostAddress(), serverPort, uriPath));
        } catch (Exception e) {
            LOGGER.error("Local Plugins manager - REST services failed to start", e);
        }
    }

    public static void stopRestServer() {
        Spark.stop();
    }

    public static void loadRestApiServices() {
        LOGGER.info("Loading REST Services");
        get("/", (request, response) ->  "Hello World");
    }

}
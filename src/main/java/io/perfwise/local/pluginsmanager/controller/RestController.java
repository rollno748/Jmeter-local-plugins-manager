package io.perfwise.local.pluginsmanager.controller;

import spark.Spark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Properties;

import static spark.Spark.*;
import static spark.Spark.path;

public class RestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestController.class);
    private static String uriPath;
    private static int serverPort;
    public RestController() {
    }
    public RestController(Properties props) {
        RestController.serverPort = Integer.parseInt(props.getProperty("server.port"));
        RestController.uriPath = props.getProperty("server.uri.path");
    }

    public void startRestServer() {
        try {
            port(serverPort);
            init();
            awaitInitialization();
            loadRestApiServices();
            LOGGER.debug(String.format("Local Plugins manager - REST services started :: http://%s:%s%s/",
                    InetAddress.getLocalHost().getHostAddress(), serverPort, uriPath));
        } catch (Exception e) {
            LOGGER.error("Local Plugins manager - REST services failed to start", e);
        }
    }

    public static void stopRestServer() {
        Spark.stop();
    }

    public static void loadRestApiServices() {
        path(uriPath, () -> {
            before("/*", (q, a) -> LOGGER.debug("Received api call"));

            get("/greet", (req, res) -> {
                return "Hello Work !";
            });
        });
    }

}
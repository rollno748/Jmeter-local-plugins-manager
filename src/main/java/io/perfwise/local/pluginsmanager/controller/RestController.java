package io.perfwise.local.pluginsmanager.controller;

import com.google.gson.Gson;
import spark.Spark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.util.Properties;

import static spark.Spark.*;
import static spark.Spark.path;

public class RestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestController.class);
    private static String uriPath;
    private int serverPort;
    private String fileServerLocation;

    public RestController() {
    }
    public RestController(Properties props) {
        this.serverPort = Integer.parseInt(props.getProperty("server.port"));
        RestController.uriPath = props.getProperty("server.uri.path");
        this.fileServerLocation = props.getProperty("local.sqlite.db.path");
    }

    public void startRestServer() {
        try {
            port(serverPort);
            staticFiles.externalLocation(this.fileServerLocation);
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
            before("/*", (req, res) -> {
                res.header("Access-Control-Allow-Origin", "*");
                LOGGER.debug("Received api call");

            });

            get("/", (req, res) -> {
                res.type("application/json");
                File[] files = File.listRoots(); //get array of files
                return new Gson().toJson(files);
            });

            get("/greet", (req, res) -> {
                return "Hello Work !";
            });
        });
    }

}
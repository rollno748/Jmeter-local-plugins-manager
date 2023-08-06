package io.perfwise.local.pluginsmanager.controller;

import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import spark.staticfiles.StaticFilesConfiguration;
import spark.utils.ClassUtils;

import java.net.InetAddress;
import java.util.Properties;

import static spark.Spark.*;

public class RestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestController.class);
    private static String uriPath;
    private int serverPort;
    private int MIN_THREADS = 2;
    private int MAX_THREADS = 10;
    private int TIMEOUT = 30000;
    private String fileServerLocation;
    private SQLiteConnectionPool connectionPool;

    public RestController() {
    }
    public RestController(Properties props) {
        this.serverPort = Integer.parseInt(props.getProperty("server.port"));
        uriPath = props.getProperty("server.uri.path");
        this.fileServerLocation = props.getProperty("local.sqlite.db.path");
    }

    public void startRestServer() {
        try {
            port(serverPort);
            staticFiles.location("public");
            connectionPool = SQLiteConnectionPool.getInstance(fileServerLocation, MIN_THREADS, MAX_THREADS, TIMEOUT);
            staticFiles.location("public");
//            staticFiles.externalLocation(this.fileServerLocation);
            init();
            awaitInitialization();
            loadRestApiServices();
            LOGGER.debug(String.format("Local Plugins manager - REST services started :: http://%s:%s%s/",
                    InetAddress.getLocalHost().getHostAddress(), serverPort, uriPath));
        } catch (Exception e) {
            LOGGER.error("Local Plugins manager - REST services failed to start", e);
        }
    }

    public static void loadRestApiServices() {
        path(RestController.uriPath, () -> {
            before("/*", (req, res) -> {
                res.header("Access-Control-Allow-Origin", "*");
                res.type("text/html");
                LOGGER.debug("Received api call");

            });

            get("/greet", (req, res) -> {
                return "Hello Work !";
            });

            StaticFilesConfiguration staticFilesConfig = new StaticFilesConfiguration();
            staticFilesConfig.configure("/public");

//            before("/upload", (request, response) -> {
//                response.type("text/html");
//                staticFilesConfig.consume(request.raw(), response.raw());
//            });

            get("/upload", (req, res) -> {
                res.type("text/html");
                res.redirect("/public");
                halt();
                return ClassUtils.getDefaultClassLoader().getResourceAsStream("public/upload.html");
            });

        });
    }

    public static void stopRestServer() {
        Spark.stop();
    }

}


//        Service service = Service.ignite();
//            service.staticFiles.location("/public");
//            service.port(serverPort);
//            service.threadPool(MAX_THREADS, MIN_THREADS, TIMEOUT);
//            loadRestApiServices(service);
//            service.init();
//            service.awaitInitialization();
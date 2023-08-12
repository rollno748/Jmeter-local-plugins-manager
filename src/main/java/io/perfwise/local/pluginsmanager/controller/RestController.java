package io.perfwise.local.pluginsmanager.controller;

import io.perfwise.local.pluginsmanager.service.UploadService;
import io.perfwise.local.pluginsmanager.service.UploadServiceImpl;
import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import spark.ModelAndView;
import spark.Spark;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static spark.Spark.*;

public class RestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestController.class);
    private static String uriPath;
    private int serverPort;
    private int MIN_THREADS;
    private int MAX_THREADS;
    private int TIMEOUT;
    private String fileServerLocation;
    private SQLiteConnectionPool connectionPool;

    public RestController() {
    }
    public RestController(Properties props) {
        this.serverPort = Integer.parseInt(props.getProperty("server.port"));
        uriPath = props.getProperty("server.uri.path");
        this.fileServerLocation = props.getProperty("local.sqlite.db.path");
        this.MIN_THREADS = Integer.parseInt(props.getProperty("db.min.threads"));
        this.MAX_THREADS = Integer.parseInt(props.getProperty("db.max.threads"));
        this.TIMEOUT = Integer.parseInt(props.getProperty("db.timeout.secs"));
    }

    /*
     * This method initialises the RestServer which exposes
     * 1. HealthCheck
     * 2. Upload Custom plugin API
     * 3. FileServer to download plugins
     * 4. Initialises connection pool manager for SQLite DB
     */
    public void startRestServer() {
        try {
            staticFiles.location("/public");
            staticFiles.externalLocation(this.fileServerLocation);
            port(serverPort);
            connectionPool = SQLiteConnectionPool.getInstance(fileServerLocation, MIN_THREADS, MAX_THREADS, TIMEOUT);
            init();
            awaitInitialization();
            loadRestApiServices();
            LOGGER.debug(String.format("Local Plugins manager - REST services started :: http://%s:%s%s/",
                    InetAddress.getLocalHost().getHostAddress(), serverPort, uriPath));
        } catch (Exception e) {
            LOGGER.error("Local Plugins manager - REST services failed to start", e);
        }
    }

    /*
     * This method creates URI Path for the Rest services
     */
    public void loadRestApiServices() {

        UploadService uploadService = new UploadServiceImpl();
        path(RestController.uriPath, () -> {
            before("/*", (req, res) -> LOGGER.info("Received api call"));

            get("/greet", (req, res) -> {
                return "Hello Work !";
            });

//            get("/upload", (req, res) -> {
//                JSONObject jsonObject = new JSONObject(req.body());
//                uploadService.uploadCustomPlugin(jsonObject);
//                return null;
//            });
            after((req, res) -> res.header("Content-Encoding", "gzip"));

        });
    }

    public static void stopRestServer() {
        Spark.stop();
    }

}

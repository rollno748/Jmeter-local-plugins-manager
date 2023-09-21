package io.perfwise.local.pluginsmanager.controller;

import io.perfwise.local.pluginsmanager.service.PluginService;
import io.perfwise.local.pluginsmanager.service.PluginServiceImpl;
import io.perfwise.local.pluginsmanager.service.UploadService;
import io.perfwise.local.pluginsmanager.service.UploadServiceImpl;
import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static spark.Spark.*;

public class RestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestController.class);
    private static String uriPath;
    private Path basePath;
    private String pluginsPath;
    private String customPluginsPath;
    private String libPath;
    private int serverPort;
    private int MIN_THREADS;
    private int MAX_THREADS;
    private int TIMEOUT;
    private static long startTime;
    private SQLiteConnectionPool connectionPool;

    public RestController() {
    }

    public enum Plugins {
        PUBLIC,
        CUSTOM
    }

    public RestController(Properties props) {
        this.serverPort = Integer.parseInt(props.getProperty("server.port"));
        uriPath = props.getProperty("server.uri.path");
        this.basePath = Paths.get(props.getProperty("local.repo.path"));
        this.pluginsPath = this.basePath.resolve("plugins").toString();
        this.libPath = this.basePath.resolve("libs").toString();
        this.customPluginsPath = this.basePath.resolve("custom").toString();
        this.MIN_THREADS = Integer.parseInt(props.getProperty("db.min.threads"));
        this.MAX_THREADS = Integer.parseInt(props.getProperty("db.max.threads"));
        this.TIMEOUT = Integer.parseInt(props.getProperty("db.timeout.secs"));
        RestController.startTime = System.currentTimeMillis();
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
            staticFiles.externalLocation(this.basePath.toString());
            port(serverPort);
            connectionPool = SQLiteConnectionPool.getInstance(this.basePath.toString(), MIN_THREADS, MAX_THREADS, TIMEOUT);
            init();
            awaitInitialization();
            loadRestApiServices();
            LOGGER.info("##############################################################################");
            LOGGER.info(String.format("Local Plugins manager - REST services started :: http://%s:%s%s/status",
                    InetAddress.getLocalHost().getHostAddress(), serverPort, uriPath));
            LOGGER.info("##############################################################################");
        } catch (Exception e) {
            LOGGER.error("Local Plugins manager - REST services failed to start", e);
        }
    }

    /*
     * This method creates URI Path for the Rest services
     */
    public void loadRestApiServices() {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(new File("uploads"));
        ServletFileUpload upload = new ServletFileUpload(factory);

        path(RestController.uriPath, () -> {
            before("/*", (req, res) -> LOGGER.info("Received api call"));

            get("/status", (req, res) -> {
                long uptimeMillis = System.currentTimeMillis() - startTime;
                long uptimeSeconds = uptimeMillis / 1000;
                long hours = uptimeSeconds / 3600;
                long minutes = (uptimeSeconds % 3600) / 60;
                long seconds = uptimeSeconds % 60;
                String uptimeMessage = String.format("Uptime: %d hours, %d minutes, %d seconds", hours, minutes, seconds);

                // Create an HTML response with the uptime information in a box
                return "<html><body><div style='border: 1px solid #ccc; padding: 50px; width: 1750px; text-align: center;'>" +
                        "<h1>Application is up and running</h1>" +
                        "<p>" + uptimeMessage + "</p>" +
                        "</div></body></html>";
            });

            get("/dashboard", (req, res) -> {
                res.type("text/html");
                res.redirect("/dashboard.html");
                return null;
            });

            get("/upload", (req, res) -> {
                res.type("text/html");
                res.redirect("/");
                return null;
            });

            get("/plugins", (req, res) -> {
                res.type("application/json");
                String type = req.queryParams("type");
                PluginService pluginService = new PluginServiceImpl();

                if (type == null || type.isEmpty()) {
                    return pluginService.getAllPlugins();
                } else if (type.equalsIgnoreCase(Plugins.PUBLIC.toString())) {
                    return pluginService.getPublicPlugins();
                } else if (type.equalsIgnoreCase(Plugins.CUSTOM.toString())) {
                    return pluginService.getCustomPlugins();
                } else {
                    return "Invalid plugin type.";
                }
            });

            get("/plugins-table", (req, res) -> {
                res.type("application/json");
                PluginService pluginService = new PluginServiceImpl();
                return pluginService.getPluginTable();
            });

            post("/upload", (req, res) -> {
                req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
                UploadService uploadService = new UploadServiceImpl(this.customPluginsPath, this.libPath);
                String resp = uploadService.customPluginUpload(req, upload);
                if(Integer.parseInt(resp) >= 500){
                    return "Something went wrong !";
                }else{
                    return "File Uploaded Successfully !";
                }
            });

            after((req, res) -> res.header("Content-Encoding", "gzip"));

        });
    }

    public static void stopRestServer() {
        Spark.stop();
    }

}

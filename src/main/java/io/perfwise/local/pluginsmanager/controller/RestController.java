package io.perfwise.local.pluginsmanager.controller;

import io.perfwise.local.pluginsmanager.service.UploadService;
import io.perfwise.local.pluginsmanager.service.UploadServiceImpl;
import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Properties;

import static spark.Spark.*;

public class RestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestController.class);
    private static String uriPath;
    private static String customPluginPath;
    private static String libPath;
    private int serverPort;
    private int MIN_THREADS;
    private int MAX_THREADS;
    private int TIMEOUT;
    private String fileServerLocation;
    private static long startTime;
    private SQLiteConnectionPool connectionPool;

    public RestController() {
    }
    public RestController(Properties props) {
        this.serverPort = Integer.parseInt(props.getProperty("server.port"));
        uriPath = props.getProperty("server.uri.path");
        RestController.customPluginPath = props.getProperty("local.custom.plugins.dir.path");
        RestController.libPath = props.getProperty("local.repo.dependencies.dir.path");
        this.fileServerLocation = props.getProperty("local.sqlite.db.path");
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
            staticFiles.externalLocation(this.fileServerLocation);
            port(serverPort);
            connectionPool = SQLiteConnectionPool.getInstance(fileServerLocation, MIN_THREADS, MAX_THREADS, TIMEOUT);
            init();
            awaitInitialization();
            loadRestApiServices();
            LOGGER.info(String.format("Local Plugins manager - REST services started :: http://%s:%s%s/",
                    InetAddress.getLocalHost().getHostAddress(), serverPort, uriPath));
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

            get("/", (req, res) -> {
                long uptimeMillis = System.currentTimeMillis() - startTime;
                long uptimeSeconds = uptimeMillis / 1000;
                long hours = uptimeSeconds / 3600;
                long minutes = (uptimeSeconds % 3600) / 60;
                long seconds = uptimeSeconds % 60;
                return String.format("Application is up and running<br>Uptime: %d hours, %d minutes, %d seconds", hours, minutes, seconds);
            });

            get("/upload", (req, res) -> {
                res.type("text/html");
                res.redirect("/");
                return null;
            });

            post("/upload", (req, res) -> {
                req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
                UploadService uploadService = new UploadServiceImpl(RestController.customPluginPath, RestController.libPath);
                List<FileItem> items = upload.parseRequest(req.raw());
                String resp = uploadService.handleFileUpload(items);
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

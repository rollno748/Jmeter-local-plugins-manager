package io.perfwise.local.pluginsmanager.scheduler.http;

import com.google.gson.Gson;
import io.perfwise.local.pluginsmanager.model.MetadataModel;
import io.perfwise.local.pluginsmanager.model.PluginModel;
import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class HttpRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);
    private static final HttpClient HTTP_CLIENT = HttpClients.createDefault();
    private Properties props;
    private Connection conn;
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_INTERVAL_MS = 60000;
    private static String INSERT_METADATA_INFO = "INSERT INTO metadata (id, version, downloadUrl, libs) VALUES (?, ?, ?, ?)";
    private static String INSERT_PLUGIN_INFO = "INSERT INTO plugins (id, name, type, description, helpUrl, markerClass, screenshotUrl, vendor, versions_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public HttpRequest(Properties props){
        this.props = props;
    }

    public static JSONArray get(String url) throws IOException {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = HTTP_CLIENT.execute(httpGet);
                HttpEntity entity = response.getEntity();
                JSONArray jsonArray = new JSONArray(EntityUtils.toString(entity));
                EntityUtils.consume(entity);
                return jsonArray;
            } catch (IOException e) {
                LOGGER.error("Attempt " + (retries + 1) + " failed. Retrying in " + (RETRY_INTERVAL_MS / 1000) + " seconds...");
                retries++;
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException interruptedException) {
                    // Ignore interruption during sleep
                }
            }
        }
        throw new IOException("Failed to execute HTTP GET request after " + MAX_RETRIES + " retries.");
    }

    public static void fileDownloader(String filePath, URL url) throws IOException {
        int respCode = getResponseCode(url);
        if (respCode == 200) {
            File file = new File(filePath + url.toString().substring(url.toString().lastIndexOf("/")));

            try (InputStream input = url.openStream();
                 FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = input.read(buffer)) != -1) {
                    output.write(buffer, 0, n);
                }
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }

            LOGGER.info("{} downloaded successfully!", file);
        }
    }

    public static int getResponseCode(URL url) throws IOException {
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestMethod("GET");
        huc.connect();
        return huc.getResponseCode();
    }

    public static void fileUploader(String pluginJar, List<String> dependencyJars, String customPluginPath) {


    }

    public static void fileUploader(String pluginJar, String customPluginPath) {
        fileUploader(pluginJar, null, customPluginPath);
    }

    public void downloadPlugins(JSONObject pluginObject) throws URISyntaxException, IOException {
        JSONObject metaDataObj = new JSONObject();
        metaDataObj.put("id", pluginObject.getString("id"));
        JSONObject versionObj = pluginObject.getJSONObject("versions");
        for (String version : versionObj.keySet()) {
            metaDataObj.put("version", version);
            JSONObject verObj = versionObj.getJSONObject(version);
            if(!verObj.isNull("downloadUrl")){
                String downloadUrl = verObj.getString("downloadUrl");
                metaDataObj.put("downloadUrl",  downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1));
                fileDownloader(this.getProps().getProperty("local.repo.plugins.dir.path"), new URI(downloadUrl).toURL());

               if(verObj.has("libs")){
                    JSONObject libsObject = verObj.getJSONObject("libs");
                    metaDataObj.put("libs", libsObject);
                    for (String lib : libsObject.keySet()) {
                        String libUrl = libsObject.getString(lib);
                        fileDownloader(this.getProps().getProperty("local.repo.dependencies.dir.path"), new URI(libUrl).toURL());
                    }
                }
            }
            this.updatePluginMetadataInfo(metaDataObj);
            LOGGER.info("Downloaded {} plugin - version {}", pluginObject.getString("id"), version);
        }
        this.updatePluginInfoInDB(pluginObject, "public");
    }

    private void updatePluginMetadataInfo(JSONObject metaDataObj) {
        MetadataModel metadataModel = new Gson().fromJson(String.valueOf(metaDataObj), MetadataModel.class);
        try{
            if(conn == null){
                conn = SQLiteConnectionPool.getConnection();
            }

            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_METADATA_INFO);
            preparedStatement.setString(1, metadataModel.getId());
            preparedStatement.setString(2, metadataModel.getVersion());
            preparedStatement.setString(3, metadataModel.getDownloadUrl());
            preparedStatement.setString(4, metadataModel.getLibs());

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Data inserted successfully!");
            } else {
                System.out.println("Failed to insert data.");
            }
            preparedStatement.close();
        }catch (SQLException | InterruptedException e){
            e.printStackTrace();
        }

    }

    private void updatePluginInfoInDB(JSONObject pluginObject, String type) {
        int versionsCount = pluginObject.getJSONObject("versions").length();
        pluginObject.put("versions_count", versionsCount);
        PluginModel pluginModel = new Gson().fromJson(String.valueOf(pluginObject), PluginModel.class);

        try{
            if(conn == null){
                conn = SQLiteConnectionPool.getConnection();
            }

            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_PLUGIN_INFO);
            preparedStatement.setString(1, pluginModel.getId());
            preparedStatement.setString(2, pluginModel.getName());
            preparedStatement.setString(3, type);
            preparedStatement.setString(4, pluginModel.getDescription());
            preparedStatement.setString(5, pluginModel.getHelpUrl());
            preparedStatement.setString(6, pluginModel.getMarkerClass());
            preparedStatement.setString(7, pluginModel.getScreenshotUrl());
            preparedStatement.setString(8, pluginModel.getVendor());
            preparedStatement.setInt(9, pluginModel.getVersions_count());

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Data inserted successfully!");
            } else {
                System.out.println("Failed to insert data.");
            }
            preparedStatement.close();
        }catch (SQLException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }
}

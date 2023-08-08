package io.perfwise.local.pluginsmanager.scheduler.http;

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
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class HttpRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);
    private static final HttpClient HTTP_CLIENT = HttpClients.createDefault();
    private Properties props;
    private Connection conn;
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_INTERVAL_MS = 60000;

    public HttpRequest(Properties props){
        this.props = props;
        try {
            this.conn = SQLiteConnectionPool.getConnection();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
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

    public void downloadPlugins(JSONObject pluginObject) throws URISyntaxException, IOException {
        JSONObject versionObj = pluginObject.getJSONObject("versions");
        for (String version : versionObj.keySet()) {
            JSONObject versionDetails = pluginObject.getJSONObject("versions").getJSONObject(version);
            if(!versionDetails.isNull("downloadUrl")){
                String downloadUrl = versionDetails.getString("downloadUrl");
                fileDownloader(this.getProps().getProperty("local.repo.plugins.dir.path"), new URI(downloadUrl).toURL());
                if(versionDetails.has("libs")){
                    JSONObject libsObject = versionDetails.getJSONObject("libs");
                    for (String lib : libsObject.keySet()) {
                        String libUrl = libsObject.getString(lib);
                        fileDownloader(this.getProps().getProperty("local.repo.dependencies.dir.path"), new URI(libUrl).toURL());
                    }
                }
            }
            
            this.updatePluginInfoInDB(pluginObject);

            LOGGER.info("Downloaded {} plugin - version {}", pluginObject.getString("id"), version);
        }
    }

    private void updatePluginInfoInDB(JSONObject pluginObject) throws URISyntaxException {
        JSONObject versionObj = pluginObject.getJSONObject("versions");
        for (String version : versionObj.keySet()) {
            JSONObject versionDetails = pluginObject.getJSONObject("versions").getJSONObject(version);
            if(!versionDetails.isNull("downloadUrl")){
                String downloadUrl = versionDetails.getString("downloadUrl");
                try {
                    fileDownloader(this.getProps().getProperty("local.repo.plugins.dir.path"), new URI(downloadUrl).toURL());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(versionDetails.has("libs")){
                    JSONObject libsObject = versionDetails.getJSONObject("libs");
                    for (String lib : libsObject.keySet()) {
                        String libUrl = libsObject.getString(lib);
                        try {
                            fileDownloader(this.getProps().getProperty("local.repo.dependencies.dir.path"), new URI(libUrl).toURL());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            this.updatePluginInfoInDB(pluginObject);

            LOGGER.info("Downloaded {} plugin - version {}", pluginObject.getString("id"), version);
        }

    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }
}

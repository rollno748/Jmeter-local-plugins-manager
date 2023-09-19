package io.perfwise.local.pluginsmanager.scheduler.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);
    private Properties props;
    private final Path basePath;
    private final String pluginsPath;
    private final String dependenciesPath;
    private final String customPluginsPath;
    private Connection conn;
    private static final HttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_INTERVAL_MS = 60000;
    private static final String INSERT_METADATA_INFO = "INSERT INTO metadata (id, version, changes, depends, downloadUrl, libs) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_PLUGIN_INFO = "INSERT INTO plugins (id, name, type, description, helpUrl, markerClass, screenshotUrl, vendor, componentClasses, versions_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_PLUGIN_VERSION_METADATA = "SELECT COUNT(*) AS COUNT FROM METADATA WHERE ID = ? AND VERSION = ?";
    private static final String SELECT_PLUGINS = "SELECT ID, NAME, DESCRIPTION, HELPURL, MARKERCLASS, SCREENSHOTURL, VENDOR, COMPONENTCLASSES FROM plugins";
    private static final String SELECT_PLUGINS_TABLE_DATA = "SELECT ID, NAME, TYPE, DESCRIPTION, HELPURL, MARKERCLASS, SCREENSHOTURL, VENDOR, COMPONENTCLASSES, VERSIONS_COUNT FROM plugins";
    private static final String SELECT_PLUGINS_WITH_FILTER = "SELECT ID, NAME, DESCRIPTION, HELPURL, MARKERCLASS, SCREENSHOTURL, VENDOR, COMPONENTCLASSES FROM plugins WHERE type = ?";
    private static final String SELECT_METADATA_BY_ID = "SELECT ID, VERSION, CHANGES, DEPENDS, DOWNLOADURL, LIBS FROM metadata WHERE ID = ?";


    public HttpRequest(Properties props){
        this.props = props;
        this.basePath = Paths.get(props.getProperty("local.repo.path"));
        this.pluginsPath = this.basePath.resolve("plugins").toString();
        this.dependenciesPath = this.basePath.resolve("libs").toString();
        this.customPluginsPath = this.basePath.resolve("custom").toString();
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
        try{
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

                LOGGER.debug("{} downloaded successfully!", file);
            }
        }catch(UnknownHostException uhe){
            LOGGER.info(String.format("Unable to resolve hostname %s \nException trace %s", url, uhe));
        }
    }

    public static int getResponseCode(URL url) throws IOException {
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestMethod("GET");
        huc.connect();
        return huc.getResponseCode();
    }

    public void downloadMissingPlugins(JSONObject pluginObject) throws URISyntaxException, IOException {
        JSONObject metaDataObj = new JSONObject();
        metaDataObj.put("id", pluginObject.getString("id"));
        JSONObject versionObj = pluginObject.getJSONObject("versions");
        for (String version : versionObj.keySet()) {
            if(!isPluginVersionExist(pluginObject.getString("id"), version)){
                metaDataObj.put("version", version);
                JSONObject verObj = versionObj.getJSONObject(version);
                if(!verObj.isNull("downloadUrl")){
                    String downloadUrl = verObj.getString("downloadUrl");
                    if(!downloadUrl.contains("%1$s.jar")){
                        metaDataObj.put("downloadUrl",  downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1));
                        fileDownloader(this.pluginsPath, new URI(downloadUrl).toURL());
                    }else{
                        List<String> availableVersions = this.getAvailableLibraryVersions(downloadUrl);
                        for (String ver : availableVersions){
                            String url = downloadUrl.replace("%1$s", ver);
                            metaDataObj.put("downloadUrl",  url.substring(url.lastIndexOf('/') + 1));
                            fileDownloader(this.pluginsPath, new URI(url).toURL());
                        }
                    }
                    if(verObj.has("libs")){
                        JSONObject libsObject = verObj.getJSONObject("libs");
                        metaDataObj.put("libs", libsObject.toString());
                        for (String lib : libsObject.keySet()) {
                            String libUrl = libsObject.getString(lib);
                            fileDownloader(this.dependenciesPath, new URI(libUrl).toURL());
                        }
                    }

                    metaDataObj.put("changes", verObj.has("changes") ? verObj.get("changes") : null);
                    metaDataObj.put("depends", verObj.has("depends") ? verObj.get("depends").toString() : null);
                }
                this.updatePluginMetadataInfo(metaDataObj);
                LOGGER.info("Downloaded {} plugin - version {}", pluginObject.getString("id"), version);
            }
        }
        this.updatePluginInfoInDB(pluginObject, "public");
    }

    private List<String> getAvailableLibraryVersions(String downloadUrl) {
        String libName = null;
        Pattern pattern = Pattern.compile("jmeter/(?<libName>[^/]+)/%\\d[^/]*");
        Matcher matcher = pattern.matcher(downloadUrl);
        if(matcher.find()) {
            libName = matcher.group("libName");
        }
        return fetchAvailableVersions(libName);
    }

    public JSONArray fetchPluginsFromLocalDB(String query, String type){
        JSONArray jsonArray = new JSONArray();
        try{
            if(conn == null || conn.isClosed()){
                conn = SQLiteConnectionPool.getConnection();
            }
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            if(type != null){
                preparedStatement.setString(1, type);
            }
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                JSONObject pluginObject = new JSONObject();
                JSONObject libraryObj;

                pluginObject.put("id", rs.getString("id"));
                pluginObject.put("name", rs.getString("name"));
                pluginObject.put("description", rs.getString("description"));
                pluginObject.put("helpUrl", rs.getString("helpUrl"));
                pluginObject.put("markerClass", rs.getString("markerClass"));
                pluginObject.put("screenshotUrl", rs.getString("screenshotUrl"));
                pluginObject.put("vendor", rs.getString("vendor"));
                libraryObj = this.getDependentLibraryObj(rs.getString("id"), type);
                pluginObject.put("versions", libraryObj);
                String componentClasses = rs.getString("componentClasses");
                if(componentClasses != null){
                    pluginObject.put("componentClasses", new JSONArray(componentClasses));
                }
                jsonArray.put(pluginObject);
            }
            preparedStatement.close();
        }catch(SQLException | InterruptedException e){
            LOGGER.error("Exception occurred while fetching plugins information");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } finally {
            SQLiteConnectionPool.releaseConnection(conn);
        }
        return jsonArray;
    }

    public JSONArray getAllPlugins() {
        JSONArray publicPluginArray = getPublicPlugins();
        JSONArray customPluginArray = getCustomPlugins();

        for(Object obj: customPluginArray){
            publicPluginArray.put((JSONObject) obj);
        }
        return publicPluginArray;
    }

    private JSONArray getCombinedPlugins() {
        return fetchPluginsFromLocalDB(SELECT_PLUGINS, "all");
    }

    public JSONArray getPublicPlugins() {
        return fetchPluginsFromLocalDB(SELECT_PLUGINS_WITH_FILTER, "public");
    }

    public JSONArray getCustomPlugins() {
        return fetchPluginsFromLocalDB(SELECT_PLUGINS_WITH_FILTER, "custom");
    }

    public JSONArray getAllPluginsTableData() {
        return fetchAllPluginsTableData();
    }

    private JSONArray fetchAllPluginsTableData() {
        JSONArray jsonArray = new JSONArray();
        try{
            if(conn == null || conn.isClosed()){
                conn = SQLiteConnectionPool.getConnection();
            }
            PreparedStatement preparedStatement = conn.prepareStatement(HttpRequest.SELECT_PLUGINS_TABLE_DATA);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                JSONObject pluginObject = new JSONObject();
                JSONObject libraryObj;

                pluginObject.put("id", rs.getString("id"));
                pluginObject.put("name", rs.getString("name"));
                pluginObject.put("type", rs.getString("type"));
                pluginObject.put("description", rs.getString("description"));
                pluginObject.put("helpUrl", rs.getString("helpUrl"));
                pluginObject.put("markerClass", rs.getString("markerClass"));
                pluginObject.put("screenshotUrl", rs.getString("screenshotUrl"));
                pluginObject.put("vendor", rs.getString("vendor"));
                pluginObject.put("componentClasses", rs.getString("componentClasses"));
                pluginObject.put("versions_count", rs.getString("versions_count"));
                jsonArray.put(pluginObject);
            }
            preparedStatement.close();
        }catch(SQLException | InterruptedException e){
            LOGGER.error("Exception occurred while fetching plugins information");
        } finally {
            SQLiteConnectionPool.releaseConnection(conn);
        }
        return jsonArray;
    }

    private JSONObject getDependentLibraryObj(String id, String type) throws UnknownHostException {

        String host = InetAddress.getLocalHost().getHostAddress();
        String libUrl = String.format("http://%s:%s/%s/", host, props.getProperty("server.port"), "libs");
        String pluginUrl = null;
        if(type.equals("public")){
            pluginUrl = String.format("http://%s:%s/%s/", host, props.getProperty("server.port"), "plugins");
        }else{
            pluginUrl = String.format("http://%s:%s/%s/", host, props.getProperty("server.port"), "custom");
        }

        JSONObject libraryObj = new JSONObject();
        try{
            if(conn == null || conn.isClosed()){
                conn = SQLiteConnectionPool.getConnection();
            }
            PreparedStatement preparedStatement = conn.prepareStatement(SELECT_METADATA_BY_ID);
            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                JSONObject versionsObj = new JSONObject();
                String depends = rs.getString("depends");
                versionsObj.put("changes", rs.getString("changes"));
                if(depends != null){
                    versionsObj.put("depends", new JSONArray(depends));
                }
                versionsObj.put("downloadUrl", pluginUrl + rs.getString("downloadUrl"));
                if (rs.getString("libs") != null){
                    versionsObj.put("libs", processLibs(rs.getString("libs"), libUrl));
                }
                libraryObj.put(rs.getString("version"), versionsObj);
            }
            preparedStatement.close();
        }catch(SQLException | InterruptedException e){
            LOGGER.error("Exception occurred while fetching plugins information");
        }
        return libraryObj;
    }

    private JSONObject processLibs(String libs, String libUrl) {
        JSONObject libraryObject = new JSONObject();
        JSONObject jsonObject = new JSONObject(libs);
        for (String key : jsonObject.keySet()) {
            String jVal = (String) jsonObject.get(key);
            String val = libUrl + jVal.substring(jVal.lastIndexOf('/') + 1);
            libraryObject.put(key, val);
        }
        return libraryObject;
    }

    private List<String> fetchAvailableVersions(String libName) {
        Document doc = null;
        List<String> versions = new ArrayList<>();
        try{
            doc = Jsoup.connect("https://repo1.maven.org/maven2/org/apache/jmeter/" + libName).get();
            Elements links = doc.select("a[href]");
            for(Element link : links) {
                String href = link.attr("href");
                if(href.contains(libName + "-")) {
                    String version = href.substring(href.lastIndexOf("-") + 1, href.lastIndexOf(".jar"));
                    versions.add(version);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return versions;
    }

    public void updatePluginMetadataInfo(JSONObject metaDataObj) {
        MetadataModel metadataModel = new Gson().fromJson(String.valueOf(metaDataObj), MetadataModel.class);
        try{
            if(conn == null || conn.isClosed()){
                conn = SQLiteConnectionPool.getConnection();
            }
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_METADATA_INFO);
            preparedStatement.setString(1, metadataModel.getId());
            preparedStatement.setString(2, metadataModel.getVersion());
            preparedStatement.setString(3, metadataModel.getChanges() != null ? metadataModel.getChanges() : null);
            preparedStatement.setString(4, metadataModel.getDepends() != null ? metadataModel.getDepends() : null);
            preparedStatement.setString(5, metadataModel.getDownloadUrl());
            preparedStatement.setString(6, metadataModel.getLibs());

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                LOGGER.debug("Data inserted for {} - successfully", metadataModel.getId());
            } else {
                LOGGER.debug("Failed to insert data for {}", metadataModel.getId());
            }
            preparedStatement.close();
        }catch (SQLException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public boolean isPluginVersionExist(String id, String version){
        int result = 0;
        try{
           if(conn == null || conn.isClosed()){
                conn = SQLiteConnectionPool.getConnection();
            }
            PreparedStatement preparedStatement = conn.prepareStatement(SELECT_PLUGIN_VERSION_METADATA);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, version);
            ResultSet rs = preparedStatement.executeQuery();
            result = rs.getInt("COUNT");
            preparedStatement.close();
        }catch (SQLException | InterruptedException e){
            e.printStackTrace();
        }

        return result > 0;
    }

    public void updateCustomPluginInfoInDB(JSONObject pluginModel) {
        try{
           if(conn == null || conn.isClosed()){
                conn = SQLiteConnectionPool.getConnection();
            }
            //"CREATE TABLE plugins (id TEXT PRIMARY KEY, name TEXT, type TEXT, description TEXT, helpUrl TEXT, markerClass TEXT, screenshotUrl TEXT, vendor TEXT, componentClasses TEXT, versions_count INTEGER)";
           //id, name, type, description, helpUrl, markerClass, screenshotUrl, vendor, componentClasses, versions_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_PLUGIN_INFO);
            preparedStatement.setString(1, pluginModel.getString("id"));
            preparedStatement.setString(2, pluginModel.getString("name"));
            preparedStatement.setString(3, "custom");
            preparedStatement.setString(4, pluginModel.getString("description"));
            preparedStatement.setString(5, pluginModel.getString("helpUrl"));
            preparedStatement.setString(6, pluginModel.getString("markerClass"));
            preparedStatement.setString(7, pluginModel.getString("screenshotUrl"));
            preparedStatement.setString(8, pluginModel.getString("vendor"));
            preparedStatement.setString(9, pluginModel.getString("componentClasses"));
            preparedStatement.setDouble(10, pluginModel.getDouble("version_count"));

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                LOGGER.debug("Data inserted for {} - successfully", pluginModel.getString("id"));
            } else {
                LOGGER.debug("Failed to insert data for {}", pluginModel.getString("id"));
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
            if(conn == null || conn.isClosed()){
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
            preparedStatement.setString(9, pluginModel.getComponentClasses() != null ? pluginModel.getComponentClasses().toString() : null);
            preparedStatement.setDouble(10, pluginModel.getVersions_count());

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                LOGGER.debug("Data inserted for {} - successfully", pluginModel.getId());
            } else {
                LOGGER.debug("Failed to insert data for {}", pluginModel.getId());
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

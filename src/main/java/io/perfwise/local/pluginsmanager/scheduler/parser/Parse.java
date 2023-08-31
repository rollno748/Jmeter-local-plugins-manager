package io.perfwise.local.pluginsmanager.scheduler.parser;

import io.perfwise.local.pluginsmanager.scheduler.http.HttpRequest;
import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Parse {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parse.class);
    private static Connection conn;
    private static HttpRequest httpRequest = null;
    private static final String PLUGINS_INFO = "SELECT ID, VERSIONS_COUNT FROM PLUGINS WHERE TYPE = 'public'";
    private static final String METADATA_INFO = "SELECT ID, VERSION FROM METADATA";
    private static final String PLUGINS_COUNT = "SELECT COUNT(ID) AS COUNT FROM PLUGINS";
    private static final String PLUGINS_METADATA_BY_ID = "SELECT COUNT(*) AS COUNT FROM METADATA WHERE ID = ?";

    public Parse() {
        try {
            Parse.conn = SQLiteConnectionPool.getConnection();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Parse(Properties props) {
        httpRequest = new HttpRequest(props);
        try {
            Parse.conn = SQLiteConnectionPool.getConnection();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONArray getMissingPluginsNames(JSONArray jmeterRepoJson) {
//        List<String> missingPluginsInfo = new ArrayList<>();
        JSONArray missingPluginArray = new JSONArray();
        for (Object plugin : jmeterRepoJson) {
            JSONObject pluginObj = (JSONObject) plugin;
            String id = pluginObj.getString("id") ;
            JSONObject versions= pluginObj.getJSONObject("versions");

            for (String key : versions.keySet()) {
                if(!httpRequest.isPluginVersionExist(id, key)){
//                    missingPluginsInfo.add(id);
                    missingPluginArray.put(pluginObj);
                }
            }
        }
        return missingPluginArray;
    }

    public static void downloadAllPlugins(JSONArray jmeterRepoJson) throws URISyntaxException, IOException {
        for (int i = 0; i < jmeterRepoJson.length(); i++) {
            httpRequest.downloadPlugins(jmeterRepoJson.getJSONObject(i));
        }
    }

    public static int getLocalPluginCount(JSONArray pluginsArray) throws SQLException, InterruptedException {
        int localStoreCount = 0;
        conn = SQLiteConnectionPool.getConnection();

        if(!conn.isClosed()){
            try{
                ResultSet rs = conn.createStatement().executeQuery(PLUGINS_COUNT);
                localStoreCount = rs.getInt("COUNT");
            } catch (SQLException e) {
                LOGGER.error("Exception occurred while executing SQL statement");
                throw new RuntimeException(e);
            }finally{
                SQLiteConnectionPool.releaseConnection(conn);
            }
        }
        return localStoreCount;
    }

    public static int availablePluginsCount(String[] ids) throws SQLException, InterruptedException{
        int availableCount = 0;
        conn = SQLiteConnectionPool.getConnection();

        if(!conn.isClosed()){
            try{
                ResultSet rs = conn.createStatement().executeQuery(PLUGINS_METADATA_BY_ID);
                availableCount = rs.getInt("COUNT");
            } catch (SQLException e) {
                LOGGER.error("Exception occurred while executing SQL statement");
                throw new RuntimeException(e);
            }finally{
                SQLiteConnectionPool.releaseConnection(conn);
            }
        }
        return availableCount;
    }

    public static JSONArray getAllPlugins() {
        return httpRequest.getAllPlugins();
    }

    public static JSONArray getPublicPlugins() {
        return null;
    }

    public static JSONArray getCustomPlugins() {
        return null;
    }

    public void downloadMissingPlugins(JSONArray missingPluginsList) throws URISyntaxException, IOException {
        for (int i = 0; i < missingPluginsList.length(); i++) {
            httpRequest.downloadMissingPlugins(missingPluginsList.getJSONObject(i));
        }
    }

    public static HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public static void setHttpRequest(HttpRequest httpRequest) {
        Parse.httpRequest = httpRequest;
    }
}

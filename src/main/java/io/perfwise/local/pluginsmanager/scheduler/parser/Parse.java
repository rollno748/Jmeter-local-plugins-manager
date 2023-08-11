package io.perfwise.local.pluginsmanager.scheduler.parser;

import io.perfwise.local.pluginsmanager.scheduler.http.HttpRequest;
import io.perfwise.local.pluginsmanager.sqlite.SQLiteConnectionPool;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class Parse {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parse.class);
    private static Connection conn;
    private static HttpRequest httpRequest = null;
    private static final String PLUGINS_METADATA_INFO = "SELECT ID, VERSIONS FROM METADATA";
    private static final String PLUGINS_INFO = "SELECT ID, VERSIONS_COUNT FROM PLUGINS";

    public Parse() {
    }

    public Parse(Properties props) {
        httpRequest = new HttpRequest(props);
        try {
            Parse.conn = SQLiteConnectionPool.getConnection();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAllPluginsNames(JSONArray jmeterRepoJson) {
        List<String> jmeterRepoList = new ArrayList<>(jmeterRepoJson.length());
        for (int i = 0; i < jmeterRepoJson.length(); i++) {
            jmeterRepoList.add(jmeterRepoJson.getJSONObject(i).getString("id"));
        }
        return jmeterRepoList;
    }

    public static List<String> getMissingPluginsNames(JSONArray jmeterRepoJson) throws SQLException, URISyntaxException, IOException {
        HashMap<String, Integer> pluginInfoFromDB = new HashMap<String, Integer>();
        if(!conn.isClosed()){
            try {
                ResultSet rs = conn.createStatement().executeQuery(PLUGINS_INFO);
                if(rs.next()){
                    while(rs.next()){
                        String id = rs.getString("id");
                        int version = rs.getInt("versions_count");
                        if(!pluginInfoFromDB.containsKey(id)){
                            pluginInfoFromDB.put(id, version);
                        }
                    }
                }else{
                    getAllPluginsNames(jmeterRepoJson);
                }
            } catch (SQLException sqle) {
                LOGGER.error("Exception occurred while executing query : %s", sqle);
            }
        }
        return getMissingPluginsList(jmeterRepoJson, pluginInfoFromDB);
    }

    public static void downloadAllPlugins(JSONArray jmeterRepoJson) throws URISyntaxException, IOException {
        for (int i = 0; i < jmeterRepoJson.length(); i++) {
            httpRequest.downloadPlugins(jmeterRepoJson.getJSONObject(i));
        }
    }

    private static List<String> getMissingPluginsList(JSONArray jmeterRepoJson, HashMap<String, Integer> pluginInfoFromDB) {
        List<String> missingPluginsList = new ArrayList<>(jmeterRepoJson.length());

        for (int i = 0; i < jmeterRepoJson.length(); i++) {
            if(pluginInfoFromDB.containsKey(jmeterRepoJson.getJSONObject(i).getString("id"))){
                missingPluginsList.add(jmeterRepoJson.getJSONObject(i).getString("id"));
            }
        }
        return missingPluginsList;
    }

    public static int getLocalPluginCount(JSONArray pluginsArray) throws SQLException, InterruptedException {
        int localStoreCount = 0;
        conn = SQLiteConnectionPool.getConnection();

        if(!conn.isClosed()){
            try{
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(ID) AS COUNT FROM PLUGINS");
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

    public void downloadMissingPlugins(List<String> missingPluginsList, JSONArray pluginsArray) throws URISyntaxException, IOException {
        for (int i = 0; i < pluginsArray.length(); i++) {
            if(missingPluginsList.contains(pluginsArray.getJSONObject(i).getString("id"))){
                httpRequest.downloadPlugins(pluginsArray.getJSONObject(i));
            }
        }
    }
}

package io.perfwise.local.pluginsmanager.scheduler.parser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class Parse {

    public static List<String> getAllPluginsNames(JSONArray jmeterRepoJson) {
        List<String> jmeterRepoList = new ArrayList<>(jmeterRepoJson.length());
        for (int i = 0; i < jmeterRepoJson.length(); i++) {
            jmeterRepoList.add(jmeterRepoJson.getJSONObject(i).getString("id"));
        }
        return jmeterRepoList;
    }

}

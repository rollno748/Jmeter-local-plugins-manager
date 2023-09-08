package io.perfwise.local.pluginsmanager.service;

import io.perfwise.local.pluginsmanager.scheduler.parser.Parse;
import org.json.JSONArray;

public class PluginServiceImpl implements PluginService {

    @Override
    public JSONArray getAllPlugins() {
        return Parse.getAllPlugins();
    }

    @Override
    public JSONArray getPublicPlugins() {
        return Parse.getPublicPlugins();
    }

    @Override
    public JSONArray getCustomPlugins() {
        return Parse.getCustomPlugins();
    }

    @Override
    public JSONArray getPluginTable() {
        return Parse.getPluginsTableData();
    }


}

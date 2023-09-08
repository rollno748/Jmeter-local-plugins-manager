package io.perfwise.local.pluginsmanager.service;

import org.json.JSONArray;

public interface PluginService {
    JSONArray getAllPlugins();
    JSONArray getPublicPlugins();
    JSONArray getCustomPlugins();
    JSONArray getPluginTable();
}

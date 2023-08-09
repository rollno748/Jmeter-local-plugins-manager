package io.perfwise.local.pluginsmanager.service;

import org.json.JSONObject;

public interface UploadService {

    public void uploadCustomPlugin(JSONObject jsonObject);
    public void uploadLibsForCustomPlugin();
}

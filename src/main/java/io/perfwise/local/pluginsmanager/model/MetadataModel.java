package io.perfwise.local.pluginsmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataModel {
    @JsonProperty("id")
    private String id;
    @JsonProperty("version")
    private String version;
    @JsonProperty("changes")
    private String changes;
    @JsonProperty("depends")
    private String depends;
    @JsonProperty("downloadUrl")
    private String downloadUrl;
    @JsonProperty("libs")
    private String libs;

//    Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getLibs() {
        return libs;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public String getDepends() {
        return depends;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }

    public void setLibs(String libs) {
        this.libs = libs;
    }
}

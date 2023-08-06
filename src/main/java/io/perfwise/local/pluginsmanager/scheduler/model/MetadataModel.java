package io.perfwise.local.pluginsmanager.scheduler.model;


import com.fasterxml.jackson.annotation.JsonProperty;
public class MetadataModel {

    @JsonProperty("id")
    private String id;

    @JsonProperty("versions")
    private String versions;


//    Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }
}

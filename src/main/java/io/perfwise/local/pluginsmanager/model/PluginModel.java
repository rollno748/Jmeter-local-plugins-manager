package io.perfwise.local.pluginsmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginModel {

    @JsonProperty("description")
    private String description;

    @JsonProperty("helpUrl")
    private String helpUrl;

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("markerClass")
    private String markerClass;

    @JsonProperty("name")
    private String name;

    @JsonProperty("screenshotUrl")
    private String screenshotUrl;

    @JsonProperty("vendor")
    private String vendor;

    @JsonProperty("versions_count")
    private int versions_count;


    // Getter and Setter methods
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMarkerClass() {
        return markerClass;
    }

    public void setMarkerClass(String markerClass) {
        this.markerClass = markerClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }
    public String getVendor() {
        return vendor;
    }
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getVersions_count() {
        return versions_count;
    }

    public void setVersions_count(int versions_count) {
        this.versions_count = versions_count;
    }
}
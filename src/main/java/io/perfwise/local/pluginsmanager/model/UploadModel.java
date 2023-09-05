package io.perfwise.local.pluginsmanager.model;

import java.util.List;

public class UploadModel {
    private String id;
    private String name;
    private String description;
    private String helpUrl;
    private String markerClass;
    private String screenshotUrl;
    private String vendor;
    private String version;
    private String plugin;
    private List<String> dependency;

    //Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getMarkerClass() {
        return markerClass;
    }

    public void setMarkerClass(String markerClass) {
        this.markerClass = markerClass;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public List<String> getDependency() {
        return dependency;
    }

    public void setDependency(List<String> dependency) {
        this.dependency = dependency;
    }
}

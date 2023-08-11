package io.perfwise.local.pluginsmanager.model;

public class Plugin {
    private String id;
    private String name;
    private String description;
    private String helpUrl;
    private String screenshotUlr;
    private String vendor ;
    private String version;
    private String pluginPath;
    private String libPath;

    public Plugin(String id, String name, String description, String helpUrl, String screenshotUlr, String vendor, String version, String pluginPath, String libPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.helpUrl = helpUrl;
        this.screenshotUlr = screenshotUlr;
        this.vendor = vendor;
        this.version = version;
        this.pluginPath = pluginPath;
        this.libPath = libPath;
    }

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

    public String getScreenshotUlr() {
        return screenshotUlr;
    }

    public void setScreenshotUlr(String screenshotUlr) {
        this.screenshotUlr = screenshotUlr;
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

    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
    }
}

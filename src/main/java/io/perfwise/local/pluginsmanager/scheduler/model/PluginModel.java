package io.perfwise.local.pluginsmanager.scheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginModel {

    @JsonProperty("description")
    private String description;

    @JsonProperty("helpUrl")
    private String helpUrl;

    @JsonProperty("id")
    private String id;

    @JsonProperty("markerClass")
    private String markerClass;

    @JsonProperty("name")
    private String name;

    @JsonProperty("screenshotUrl")
    private String screenshotUrl;

    @JsonProperty("vendor")
    private String vendor;

    @JsonProperty("versions_count")
    private int versions;

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

    public int getVersions() {
        return versions;
    }

    public void setVersions(int versions) {
        this.versions = versions;
    }
}



//    public Map<String, Version> getVersions() {
//        return versions;
//    }
//
//    public void setVersions(Map<String, Version> versions) {
//        this.versions = versions;
//    }
//
//    public static class Version {
//        @JsonProperty("depends")
//        private String[] depends;
//
//        @JsonProperty("downloadUrl")
//        private String downloadUrl;
//
//        @JsonProperty("changes")
//        private String changes;
//
//        // Getter and Setter methods
//
//        public String[] getDepends() {
//            return depends;
//        }
//
//        public void setDepends(String[] depends) {
//            this.depends = depends;
//        }
//
//        public String getDownloadUrl() {
//            return downloadUrl;
//        }
//
//        public void setDownloadUrl(String downloadUrl) {
//            this.downloadUrl = downloadUrl;
//        }
//
//        public String getChanges() {
//            return changes;
//        }
//
//        public void setChanges(String changes) {
//            this.changes = changes;
//        }
//    }
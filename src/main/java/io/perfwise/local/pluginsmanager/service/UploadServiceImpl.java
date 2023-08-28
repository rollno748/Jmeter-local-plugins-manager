package io.perfwise.local.pluginsmanager.service;

import io.perfwise.local.pluginsmanager.scheduler.http.HttpRequest;
import io.perfwise.local.pluginsmanager.scheduler.parser.Parse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class UploadServiceImpl implements UploadService{
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);
    private String customPluginPath;
    private String libPath;

    public UploadServiceImpl(String customPluginPath, String libPath) {
        this.customPluginPath = customPluginPath;
        this.libPath = libPath;
    }

    @Override
    public String customPluginUpload(ServletFileUpload upload, Request req) throws FileUploadException, SQLException, InterruptedException {
        JSONObject pluginObj = new JSONObject();
        JSONObject metaDataObj = new JSONObject();
        HttpRequest httpRequest = Parse.getHttpRequest();

        int versionCount = Parse.availablePluginsCount(req.queryMap("id").values());

        pluginObj.put("id", req.queryMap("id").values());
        pluginObj.put("name", req.queryMap("name").values());
        pluginObj.put("type", "custom");
        pluginObj.put("description", req.queryMap("description").values());
        pluginObj.put("helpUrl", req.queryMap("helpUrl").values());
        pluginObj.put("markerClass", req.queryMap("markerClass").values());
        pluginObj.put("screenshotUrl", req.queryMap("screenshotUrl").values());
        pluginObj.put("vendor", req.queryMap("vendor").values());

        if(httpRequest.isPluginVersionExist(Arrays.toString(req.queryMap("id").values()), Arrays.toString(req.queryMap("version").values()))){
            pluginObj.put("versionCount", 0);
        }else{
            pluginObj.put("version_count", versionCount + 1);
        }

        metaDataObj.put("id", req.queryMap("id").values());
        metaDataObj.put("version", req.queryMap("version").values());
        metaDataObj.put("downloadUrl", req.queryMap("version").values());
        metaDataObj.put("libs", req.queryMap("version").values());

        List<FileItem> items = upload.parseRequest(req.raw());
        return this.handleFileUpload(metaDataObj, items);
    }

    @Override
    public String handleFileUpload(JSONObject metaDataObj, List<FileItem> items) {
        try{
            for (FileItem item : items) {
                if (!item.isFormField() && item.getFieldName().equals("pluginJar")) {
                    String path = customPluginPath;
                    copyFileItemToDirectory(item, path);
                }else if(!item.isFormField() && item.getFieldName().equals("dependencyJars")){
                    String path = libPath;
                    copyFileItemToDirectory(item, path);
                }
            }
            return "200";
        }catch(Exception e){
            e.printStackTrace();
            return "500";
        }
    }

    private void copyFileItemToDirectory(FileItem item, String path) {
        String fileName = item.getName();
        File file = new File(path + fileName);
        try (InputStream inputStream = item.getInputStream()) {
            Files.copy(inputStream, file.toPath());
        } catch (IOException e) {
            System.out.println("Error " + e);
        }
    }

}

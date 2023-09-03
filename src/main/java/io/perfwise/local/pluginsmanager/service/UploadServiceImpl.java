package io.perfwise.local.pluginsmanager.service;

import io.perfwise.local.pluginsmanager.scheduler.http.HttpRequest;
import io.perfwise.local.pluginsmanager.scheduler.parser.Parse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Collection;

public class UploadServiceImpl implements UploadService{
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);
    private String customPluginPath;
    private String libPath;

    public UploadServiceImpl(String customPluginPath, String libPath) {
        this.customPluginPath = customPluginPath;
        this.libPath = libPath;
    }

    @Override
    public String customPluginUpload(Request req, ServletFileUpload servletFileUpload) throws SQLException, InterruptedException, IOException {
        String result = null;
        Collection<Part> dependencyJars = null;
        JSONObject pluginObj = new JSONObject();
        JSONObject metaDataObj = new JSONObject();
        HttpRequest httpRequest = Parse.getHttpRequest();

        String id = req.queryMap("id").values()[0];
        String version = req.queryMap("version").values()[0];

        int versionCount = Parse.availablePluginsCount(id);

        pluginObj.put("id", id);
        pluginObj.put("name", req.queryMap("name").values()[0]);
        pluginObj.put("type", "custom");
        pluginObj.put("description", req.queryMap("description").values()[0]);
        pluginObj.put("helpUrl", req.queryMap("helpUrl").values()[0]);
        pluginObj.put("markerClass", req.queryMap("markerClass").values()[0]);
        pluginObj.put("screenshotUrl", req.queryMap("screenshotUrl").values()[0]);
        pluginObj.put("vendor", req.queryMap("vendor").values()[0]);

        if(httpRequest.isPluginVersionExist(id, version)){
            pluginObj.put("version_count", versionCount);
        }else{
            pluginObj.put("version_count", versionCount + 1);
        }
        Parse.addPluginDataToDB(pluginObj);

        metaDataObj.put("id", id);
        metaDataObj.put("version", version);

        try{
            dependencyJars = req.raw().getParts();
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }

        boolean isUploadSuccess = this.handleFileUpload(dependencyJars, metaDataObj);
        if(isUploadSuccess){
            result = "200";
        }else{
            result = "500";
        }
        return result;
    }
    @Override
    public boolean handleFileUpload(Collection<Part> uploadedFiles, JSONObject metaDataObj) throws IOException {
        String url = "http://dummy.com/";
        JSONObject depsObj = new JSONObject();
        try{
            for (Part uploadedFile : uploadedFiles) {
                String fieldName = uploadedFile.getName();
                if(fieldName != null && fieldName.equals("pluginJar")){
                    copyFileItemToDirectory(uploadedFile, this.customPluginPath);
                    metaDataObj.put("downloadUrl", uploadedFile.getSubmittedFileName());
                } else if (fieldName != null && fieldName.equals("dependencyJars")) {
                    String fileName = uploadedFile.getSubmittedFileName();
                    String strippedName = fileName.substring(0, fileName.length() - 4);
                    copyFileItemToDirectory(uploadedFile, this.libPath);
                    depsObj.put(strippedName, url + fileName);
                }
            }
            if(depsObj.length()>0)
                metaDataObj.put("libs", depsObj.toString());

            Parse.addMetaDataToDB(metaDataObj);
            return true;
        }catch(Exception e){
            LOGGER.error("Exception occurred while Uploading custom plugins: ");
            e.printStackTrace();
            return false;
        }
    }

    private void copyFileItemToDirectory(Part uploadedItem, String path) {
        String filename = uploadedItem.getSubmittedFileName();
        String destination = path + File.separator + filename;
        try (InputStream fileInputStream = uploadedItem.getInputStream()) {
            Files.copy(fileInputStream, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

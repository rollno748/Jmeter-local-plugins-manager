package io.perfwise.local.pluginsmanager.service;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import spark.Request;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface UploadService {

    String customPluginUpload(ServletFileUpload upload, Request req) throws FileUploadException, SQLException, InterruptedException;
    String handleFileUpload(JSONObject metaDataObj, List<FileItem> items) throws IOException;
}

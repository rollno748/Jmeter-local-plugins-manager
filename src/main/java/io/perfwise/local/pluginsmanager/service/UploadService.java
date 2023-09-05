package io.perfwise.local.pluginsmanager.service;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import spark.Request;

import javax.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface UploadService {
    String customPluginUpload(Request req, ServletFileUpload servletFileUpload) throws FileUploadException, SQLException, InterruptedException, IOException;
    boolean handleFileUpload(Collection<Part> uploadedFiles, JSONObject metadataObject) throws IOException;

}

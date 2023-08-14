package io.perfwise.local.pluginsmanager.service;

import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class UploadServiceImpl implements UploadService{
    private String customPluginPath;
    private String libPath;

    public UploadServiceImpl(String customPluginPath, String libPath) {
        this.customPluginPath = customPluginPath;
        this.libPath = libPath;
    }

    @Override
    public void handleFileUpload(List<FileItem> items) {
        for (FileItem item : items) {
            if (!item.isFormField() && "pluginJar".equals(item.getFieldName())) {
                // Handle the uploaded file
                String fileName = item.getName();
                File file = new File(customPluginPath + fileName);
                try (InputStream inputStream = item.getInputStream()) {
                    Files.copy(inputStream, file.toPath());
                } catch (IOException e) {
                    System.out.println("Error " + e);
//                    res.status(500);
//                    return "Error saving file: " + e.getMessage();
                }
            }
        }
    }

}

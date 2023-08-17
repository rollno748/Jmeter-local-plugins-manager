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
    public String handleFileUpload(List<FileItem> items) {
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

package io.perfwise.local.pluginsmanager.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class DirectoryOps {
    private static final Logger LOGGER = Logger.getLogger(DirectoryOps.class.getName());

    public boolean createDirectory(String dirPath){
        boolean status = false;
        File file = new File(dirPath);

        if(!file.exists()) {
            try {
                status = file.mkdirs();
                if(file.exists()) {
                    LOGGER.info("Local Plugin directory not found, Created new directory " + file.toString());
                    status=true;
                }
            }catch(Exception e){
                LOGGER.info("Failed to create a directory for LocalRepo, Permission denied !!");
                e.printStackTrace();
            }
        }else {
            if(hasWritePermission(file)) {
                status=true;
            }else {
                LOGGER.info("Permission issue.. Try with sudo ");
            }
        }
        return status;
    }

    private static boolean hasWritePermission(File dirPath) {
        File sample = new File(dirPath, "permission.txt");
        boolean status;
        try {
            status = sample.createNewFile();
            status = sample.delete();
        } catch (IOException e) {
            LOGGER.info("Write permission denied for path :" + dirPath);
            status = false;
        }
        return status;
    }
}

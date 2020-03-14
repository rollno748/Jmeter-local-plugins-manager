package com.perftalks.jmeter.repo.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.perftalks.jmeter.app.PluginsManager;

public class DirOps {

	private static final Logger LOGGER = Logger.getLogger(PluginsManager.class.getName());	

	public static boolean createDir(String dirPath) {
		
		Boolean status = false;
		File file = new File(dirPath);
		
		if(!file.exists()) {
			try {
				file.mkdirs();
				if(file.exists()) {
					LOGGER.info("Local Plugin directory not found, Created new direcoty " + file.toString());
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
				status=false;
				}
		}
		return status;
	}

	private static boolean hasWritePermission(File dirPath) {
		File sample = new File(dirPath, "empty.txt");
		try {
			sample.createNewFile();
			sample.delete();
			return true;
		} catch (IOException e) {
			LOGGER.info("Write permission denied for path :" + dirPath);
			return false;
		}
	}

}

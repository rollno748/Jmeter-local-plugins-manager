package com.perftalks.jmeter.repo.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.perftalks.jmeter.repo.rest.HTTPRequests;

public class Plugins {

	private static final Logger LOGGER = Logger.getLogger(Plugins.class.getName());
	private static ArrayList<String> missingPluginsList = new ArrayList<String>();
	private static File dir ;
	

	@SuppressWarnings("rawtypes")
	public static void getMissingPlugins(HashMap<String, JSONObject> hmap, Properties props) throws MalformedURLException, IOException {
		
		LOGGER.info("Retrieving Missing Plugins Information");
		
		for (Map.Entry element : hmap.entrySet()) { 
            String key = element.getKey().toString();
            dir = new File(props.get("LOCAL_REPO_PATH").toString()+"/"+key);            
            if(!dir.exists()) {
            	missingPluginsList.add(key);            	
            }
        } 
		
		LOGGER.info("Found "+ missingPluginsList.size() +" new plugins which is missing from local repository" );
		downloadMissingPlugins(hmap,props,missingPluginsList);
		
	}
	
	
	public static void downloadMissingPlugins(HashMap<String, JSONObject> hmap, Properties props, ArrayList<String> missingPluginsList) {
		
		for (String key : missingPluginsList) {
			JSONObject jObj = hmap.get(key);
			jObj = jObj.getJSONObject("versions");
			try {
				HTTPRequests.DownloadPlugins(jObj);	
			}catch(Exception ex) {
				LOGGER.info("Download Failed :"+ex);
			}
		}	
	}
	
	
	
	public static void updateAllPlugins(HashMap<String, JSONObject> hmapp) {
		
		LOGGER.info("Checking plugin updates");
		

	}
	
	
	

}

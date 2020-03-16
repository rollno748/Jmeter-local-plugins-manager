package com.perftalks.jmeter.repo.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.perftalks.jmeter.repo.rest.HTTPRequests;

public class Plugins {

	private static final Logger LOGGER = Logger.getLogger(Plugins.class.getName());
	private static ArrayList<String> missingPluginsList = new ArrayList<String>();
	private static File dir;
	private static String filePath;
	private static Properties props;
	private static URL url;

	@SuppressWarnings("static-access")
	public Plugins(Properties props) {
		this.props = props;
	}

	@SuppressWarnings("rawtypes")
	public void downloadMissingPlugins(HashMap<String, JSONObject> hmap) throws MalformedURLException, IOException {

		LOGGER.info("Retrieving Missing Plugins Information");

		for (Map.Entry element : hmap.entrySet()) {
			String key = element.getKey().toString();
			dir = new File(props.getProperty("local.repo.plugins.path") + "/" + key);

			if (!dir.exists()) {
				missingPluginsList.add(key);
			}
		}

		if (missingPluginsList.size() > 0) {
			LOGGER.info(String.format("Found %s Missing Plugins from the Market", missingPluginsList.size()));
			getMissingPlugins(hmap, missingPluginsList);

		} else {
			LOGGER.info("No new plugins available in the market !!");
		}

		updateAllPlugins(hmap);

	}

	public static void getMissingPlugins(HashMap<String, JSONObject> hmap, ArrayList<String> missingPluginsList) {

		for (String key : missingPluginsList) {
			JSONObject jObj = hmap.get(key);
			LOGGER.info("The Sampler Name is " + key + " and it has " + jObj.getJSONObject("versions").length()
					+ " version");
			jObj = jObj.getJSONObject("versions");
			try {
				DownloadPlugins(key, jObj);
			} catch (Exception ex) {
				LOGGER.info("Download Failed :" + ex);
			}
		}
	}

	public static void updateAllPlugins(HashMap<String, JSONObject> hmap) throws MalformedURLException, IOException {

		LOGGER.info("Checking plugin updates");

		for (@SuppressWarnings("rawtypes")
		Map.Entry element : hmap.entrySet()) {

			JSONObject jObj = hmap.get(element.getKey().toString());

			if (jObj.getJSONObject("versions").length() > 1) {

				jObj = jObj.getJSONObject("versions");

				for (Object key : jObj.keySet()) {
					// based on key types
					String keyStr = (String) key;
					dir = new File(props.getProperty("local.repo.plugins.path") + "/" + element.getKey().toString()
							+ "/" + keyStr);

					if (!dir.exists()) {
						DirOps.createDir(dir.toString());
						JSONObject pluginObj = (JSONObject) jObj.get(keyStr);
						getPlugins(dir.toString(), pluginObj);
					}
				}
			}
		}
		
		LOGGER.info("Plugins update completed");
	}


	public static void DownloadPlugins(String pluginName, JSONObject jObj) throws MalformedURLException, IOException {

		//LOGGER.info(jObj.toString());
		for (Object key : jObj.keySet()) {
			// based on key types
			String keyStr = (String) key;
			JSONObject pluginObj = (JSONObject) jObj.get(keyStr);

			filePath = props.getProperty("local.repo.plugins.path") + pluginName + "/" + keyStr;
			if (!new File(filePath).exists()) {
				DirOps.createDir(filePath);
			}
			
			getPlugins(filePath, pluginObj);

		}

	}

	private static void getPlugins(String fileLocation, JSONObject pluginObj)
			throws MalformedURLException, IOException {

		url = new URL(pluginObj.getString("downloadUrl"));
		
		try {
			HTTPRequests.Downloader(fileLocation, url);
		}catch(Exception e) {
			LOGGER.info("ERROR"+ e);
		}
		
		if(pluginObj.has("libs")) {
			if (pluginObj.getJSONObject("libs").length() >= 1) {

				JSONObject libObj = pluginObj.getJSONObject("libs");

				for (String libKey : libObj.keySet()) {
					url = new URL(libObj.getString(libKey));
					try {
						HTTPRequests.Downloader(props.getProperty("local.repo.lib.path") + libKey, url);
					}catch(Exception e) {
						LOGGER.info("ERROR"+ e);
					}
					url = null;
				}
			}
		}

	}

	public void UpdateJsonPluginsInfo(JSONArray jmeterJson, Properties props) throws IOException {
		JSONArray modifiedJsonArray = new JSONArray();
		
		modifiedJsonArray = PluginsFileWriter.getModifiedJson(jmeterJson, props);
		PluginsFileWriter.createJSON(modifiedJsonArray, props);
		LOGGER.info("The Plugins JSON can be viewed here : "+ "http://" + props.getProperty("repo.hostname") + "/prjoects/di-repo" + "/plugins.json");
		
	}

}

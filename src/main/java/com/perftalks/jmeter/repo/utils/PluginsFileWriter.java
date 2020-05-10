package com.perftalks.jmeter.repo.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class PluginsFileWriter {

	private static final Logger LOGGER = Logger.getLogger(PluginsFileWriter.class.getName());

	private static void createJSONBackup(String repoPath) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
		LocalDateTime ldt = LocalDateTime.now();

		String targetFile = repoPath + "backup/" + "plugin_" + dtf.format(ldt) + ".json";
		String backupPath = repoPath + "backup/";
		Path source = Paths.get(repoPath + "/plugins.json");
		Path target = Paths.get(targetFile);

		if (!new File(backupPath).exists()) {
			DirOps.createDir(backupPath);
		}

		try {
			Files.copy(source, target);
			Files.delete(source);
		} catch (IOException excep) {
			excep.printStackTrace();
		}

	}

	public static void createJSON(JSONArray modifiedJSON, Properties props) throws IOException {

		String JSONFile = props.getProperty("local.repo.path") + "/plugins.json";

		if (new File(JSONFile).exists()) {
			LOGGER.info("Creating backup of JSON");
			createJSONBackup(props.getProperty("local.repo.path"));
		}

		try {
			FileWriter file = new FileWriter(JSONFile, false);
			modifiedJSON.write(file);
			file.close();
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public static JSONArray getModifiedJson(JSONArray jmeterJson, Properties props)
			throws UnsupportedEncodingException {

		Boolean flag;
		String pluginsPath = "http://" + props.getProperty("repo.hostname") + "/projects/di-repo/plugins";
		String libraryPath = "http://" + props.getProperty("repo.hostname") + "/projects/di-repo/libs";

		JSONObject jsonObj1 = new JSONObject();
		JSONObject jsonObj2 = new JSONObject();
		String temp1 = null;
		String temp2 = null;

		for (int i = 0; i < jmeterJson.length(); i++) {
			flag = true;

			if (i <= jmeterJson.length()) {
				jsonObj1 = jmeterJson.getJSONObject(i).getJSONObject("versions");
				Iterator<String> keys = jsonObj1.keys();

				while (keys.hasNext()) {
					String key = keys.next();
					if (flag) {
						temp1 = jsonObj1.getJSONObject(key).get("downloadUrl").toString();
						temp2 = pluginsPath + "/" + key + "/" + Parser.getJarName(temp1);
						jsonObj1.getJSONObject(key).put("downloadUrl", temp2);
					}

					boolean avail = jsonObj1.getJSONObject(key).has("libs");

					if (avail) {
						jsonObj2 = jsonObj1.getJSONObject(key).getJSONObject("libs");

						for (int j = 0; j < jsonObj2.length(); j++) {
							Iterator<String> k = jsonObj2.keys();

							while (k.hasNext()) {
								String keey = k.next();
								temp1 = jsonObj2.getString(keey);
								temp2 = libraryPath + "/" + Parser.encodeUrl(keey) + "/" + Parser.getJarName(temp1);
								jsonObj2.put(keey, temp2);
							}

						}
					}
				}
			}
		}

		return jmeterJson;
	}

}

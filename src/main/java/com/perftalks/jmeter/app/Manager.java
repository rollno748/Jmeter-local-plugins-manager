package com.perftalks.jmeter.app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.JSONArray;

import com.perftalks.jmeter.repo.rest.HTTPRequests;
import com.perftalks.jmeter.repo.utils.DirOps;
import com.perftalks.jmeter.repo.utils.LoadMap;
import com.perftalks.jmeter.repo.utils.Plugins;

public class App {

	private static final Logger LOGGER = Logger.getLogger(App.class.getName());
	private static LoadMap load = new LoadMap();
	private final Properties props;
	

	public App(Properties props) {
		this.props = props;
	}
	
	public void execute() throws MalformedURLException, IOException {
		
		Plugins plugins = new Plugins(props);
		File repoPath = new File(props.getProperty("local.repo.plugins.path"));
		
		if(!repoPath.exists()) {
			DirOps.createDir(props.getProperty("local.repo.plugins.path"));
		}
		
		JSONArray jmeterJson = HTTPRequests.get(props.getProperty("jmeter.repo.url"));
		if (!jmeterJson.isEmpty()) {
			LOGGER.info("Plugins sync started");
			plugins.downloadMissingPlugins(load.ConvertToMap(jmeterJson));		
			LOGGER.info("Updating Plugins Json for DI-Internal");
			plugins.UpdateJsonPluginsInfo(jmeterJson, props);
			LOGGER.info("Plugins sync completed");
		} else {
			LOGGER.info("Failed to connect to Internet, Check your Internet settings..");
			System.exit(1);
		}
			
	}
}

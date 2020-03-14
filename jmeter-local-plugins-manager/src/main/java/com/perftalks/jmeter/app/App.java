package com.perftalks.jmeter.app;

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

		if (DirOps.createDir(props.getProperty("local.repo.plugins.path"))) {
			JSONArray jmeterJson = HTTPRequests.get(props.getProperty("jmeter.repo.url"));
			if (!jmeterJson.isEmpty()) {
				LOGGER.info("Checking available plugins");
				plugins.downloadMissingPlugins(load.ConvertToMap(jmeterJson));
				
			} else {
				LOGGER.info("Failed to connect to Internet, Check your Internet settings..");
				System.exit(1);
			}
		}

	}
}


/*
for (Object libKey : libObject.keySet()) {

libPath = props.getProperty("local.repo.lib.path") + libKey.toString();

if (! new File("libPath").exists()) {
	DirOps.createDir(libPath);
	getDependencies(libPath, libObject);
}
}

*/
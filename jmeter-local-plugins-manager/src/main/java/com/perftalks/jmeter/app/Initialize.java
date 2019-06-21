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

public class Initialize {

	private static final Logger LOGGER = Logger.getLogger(Initialize.class.getName());
	private static LoadMap load = new LoadMap();
	

	public static void setParams(Properties props) throws MalformedURLException, IOException {
		
		if(DirOps.createDir(props.getProperty("LOCAL_REPO_PATH"))) {
			JSONArray jmeterJson = HTTPRequests.get(props.getProperty("JMETER_REPO_URL"));
			if(!jmeterJson.isEmpty()) {
				LOGGER.info("Checking available plugins");
				Plugins.getMissingPlugins(load.ConvertToMap(jmeterJson), props);
				
			}else {
				System.exit(1);
			}
		}
	
	}

}

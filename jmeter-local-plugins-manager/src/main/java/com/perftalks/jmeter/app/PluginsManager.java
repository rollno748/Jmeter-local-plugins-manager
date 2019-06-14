package com.perftalks.jmeter.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class PluginsManager {
	
	private static Properties props = new Properties();
	
	private static final Logger LOGGER = Logger.getLogger(PluginsManager.class.getName());

	public static void main(String[] args) throws IOException {
		
		LOGGER.info("Starting Local Plugins Manager");
		InputStream inputStream = PluginsManager.class.getClassLoader().getResourceAsStream("config.properties");
		props.load(inputStream);
		
		Initialize.setParams(props);
		

	}

}

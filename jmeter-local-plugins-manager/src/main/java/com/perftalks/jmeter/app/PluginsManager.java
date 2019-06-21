package com.perftalks.jmeter.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class PluginsManager {

	private static final Logger LOGGER = Logger.getLogger(PluginsManager.class.getName());

	private static Properties props = new Properties();

	public static void main(String[] args) throws IOException {

		try {
			InputStream inputStream = PluginsManager.class.getClassLoader().getResourceAsStream("config.properties");
			props.load(inputStream);
			LOGGER.info("Loading Properties completed");
		}catch(IOException e) {
			e.printStackTrace();
		}


		if(!props.isEmpty()) {
			LOGGER.info("Starting Local Plugins Manager");
			Initialize.setParams(props);
		}

	}

}

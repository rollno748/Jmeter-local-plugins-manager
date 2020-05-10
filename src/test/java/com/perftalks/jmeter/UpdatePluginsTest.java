package com.perftalks.jmeter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

//import org.junit.Test;
import com.perftalks.jmeter.app.App;
import com.perftalks.jmeter.app.PluginsManager;

public final class UpdatePluginsTest {
	
	private static Properties props = new Properties();
	
	//@Test
	public void executePluginsManager() throws MalformedURLException, IOException {

		try {
			InputStream inputStream = PluginsManager.class.getClassLoader().getResourceAsStream("config.properties");
			props.load(inputStream);
			System.out.println("Loading Properties completed");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!props.isEmpty()) {
			System.out.println("Starting Local Plugins Manager");
			new App(props).execute();
			
		}
	}
}

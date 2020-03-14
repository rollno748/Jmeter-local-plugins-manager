package com.perftalks.jmeter.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

public class DemoMain {
	
	private static Properties props = new Properties();

	public static void main(String[] args) throws MalformedURLException, IOException {
		try {
			InputStream inputStream = PluginsManager.class.getClassLoader().getResourceAsStream("config.properties");
			props.load(inputStream);
			System.out.println("Properties load :: Success");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!props.isEmpty()) {
			System.out.println("Starting Local Plugins Manager");
			new App(props).execute();
			
		}

	}

}

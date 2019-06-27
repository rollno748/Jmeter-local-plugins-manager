package com.perftalks.jmeter.repo.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPRequests {
	
	private static final Logger LOGGER = Logger.getLogger(HTTPRequests.class.getName());
	
	public static JSONArray get(String url) throws MalformedURLException, IOException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONArray jsonArr = new JSONArray(jsonText);
			return jsonArr;
		} finally {
			is.close();
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}


	public static void DownloadPlugins(URL url, File file) throws MalformedURLException, IOException {

		int respCode = getResponseCode(url);

		if(respCode == 200) {

			try {
				InputStream input = url.openStream();
				if (file.exists()) {
					if (file.isDirectory())
						throw new IOException("File '" + file + "' is a directory");

					if (!file.canWrite())
						throw new IOException("File '" + file + "' cannot be written");
				} else {
					File parent = file.getParentFile();
					if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
						throw new IOException("File '" + file + "' could not be created");
					}
				}

				FileOutputStream output = new FileOutputStream(file);

				byte[] buffer = new byte[4096];
				int n = 0;
				while (-1 != (n = input.read(buffer))) {
					output.write(buffer, 0, n);
				}

				input.close();
				output.close();

				LOGGER.info( file + " downloaded successfully!");
			}
			catch(IOException ioEx) {
				ioEx.printStackTrace();
			}			
		}			
	}

	public static int getResponseCode(URL url) throws MalformedURLException, IOException {

		HttpURLConnection huc =  (HttpURLConnection)  url.openConnection(); 
		huc.setRequestMethod("GET"); 
		huc.connect(); 
		return huc.getResponseCode();
	}

	public static void DownloadPlugins(JSONObject jObj) throws MalformedURLException, IOException {
		
	
		
		
		
	}



}
package com.perftalks.jmeter.repo.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Parser {

	static String result;

	public static String getJarName(String temp) {		

		if(temp.contains(".jar")) {
			result = temp.substring(temp.lastIndexOf("/")+1, temp.length());
		}			
		return result;
	}


	public static ArrayList<String> getAllPluginsNames(JSONArray jmeterRepoJson) {
		ArrayList<String> jmeterRepoList = new ArrayList<String>();
		JSONArray jsArr = new JSONArray();
		String temp = new String();
		jsArr = jmeterRepoJson;

		for (int i=0; i<jsArr.length(); i++) {			
			JSONObject jb = jsArr.getJSONObject(i);
			temp = jb.getString("id");	
			jmeterRepoList.add(temp);			
		}

		return jmeterRepoList;
	}


	public static String encodeUrl(String keey) throws UnsupportedEncodingException {
		return URLEncoder.encode(keey, StandardCharsets.UTF_8.toString());
	}

}

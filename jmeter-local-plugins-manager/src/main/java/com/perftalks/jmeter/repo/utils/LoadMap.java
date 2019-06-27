package com.perftalks.jmeter.repo.utils;

import java.util.HashMap;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoadMap {
	
	private static final Logger LOGGER = Logger.getLogger(LoadMap.class.getName());
	private static HashMap<String, JSONObject> hmap = new HashMap<String, JSONObject>();

	public HashMap<String, JSONObject> ConvertToMap(JSONArray jmeterJson) {
		
		LOGGER.info("Found "+jmeterJson.length()+" Jmeter plugins in the market");
		
		for (Object obj : jmeterJson) {
			//System.out.println("JSON OBJ ::" + obj.toString());
			JSONObject jObj = new JSONObject(obj.toString());
			String id = jObj.getString("id");
			hmap.put(id, jObj);
		}
		return hmap;
	}
	
	

}

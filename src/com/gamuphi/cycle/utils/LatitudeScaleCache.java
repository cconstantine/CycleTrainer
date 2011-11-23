package com.gamuphi.cycle.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.android.maps.GeoPoint;

public class LatitudeScaleCache {
    private static Map<Integer, Double> cache = new HashMap<Integer, Double>();
    
	public static double getScale(GeoPoint geopoint) {
		Integer lat = (int)(geopoint.getLatitudeE6()/1E6);
		
		if(cache.containsKey(lat))
			return cache.get(lat);
		
		Logger.debug("cache miss");
		double result = (1/ Math.cos(Math.toRadians(lat)));
		cache.put(lat,  result);
		return result;
	}
}

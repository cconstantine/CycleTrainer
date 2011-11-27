package com.gamuphi.cycle.models;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class LocationFix extends OverlayItem {
	float accuracy;
	public LocationFix(GeoPoint point, float accuracy) {
		super(point, "", "");
		this.accuracy = accuracy;
	}
	
	public float getAccuracy() {
		return accuracy;
	}

}

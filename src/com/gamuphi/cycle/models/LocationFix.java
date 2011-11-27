package com.gamuphi.cycle.models;

import android.text.format.Time;

import com.google.android.maps.GeoPoint;

public class LocationFix {
	private GeoPoint point;
	private float accuracy;
	private Time created_at;
	
	public LocationFix(GeoPoint point, float accuracy, Time created_at) {
		this.point = point;
		this.accuracy = accuracy;
		this.created_at = created_at;
		if(created_at == null) {
			this.created_at = new Time();
			this.created_at.setToNow();
		}
			
	}

	public GeoPoint getPoint() {
		return point;
	}
	public float getAccuracy() {
		return accuracy;
	}

	public Time getCreatedAt() {
		// TODO Auto-generated method stub
		return created_at;
	}

}

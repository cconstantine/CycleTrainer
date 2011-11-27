package com.gamuphi.cycle.overlays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.gamuphi.cycle.models.Trip;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class StatsOverlay extends Overlay {
	private Paint paint = null;
	private Trip trip = null;
	
	public StatsOverlay() {
		super();
		
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		paint.setTextSize(20);
	}
	
	public void setTrip(Trip t) {
		this.trip = t;
	}
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if(this.trip != null) {
			String t = String.format("Distance: %.1f", trip.getDistanceInMiles());
			canvas.drawText(t, 10, 25, paint);
		}
	}
}

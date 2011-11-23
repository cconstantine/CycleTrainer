package com.gamuphi.cycle.overlays;

import java.util.ArrayList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.gamuphi.cycle.LocationFix;
import com.gamuphi.cycle.utils.LatitudeScaleCache;
import com.gamuphi.cycle.utils.Logger;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class PointOverlay extends Overlay {
	private ArrayList<LocationFix> mOverlays = new ArrayList<LocationFix>();

    Paint circlePaint;
    Paint linePaint;
    
    Path route;
    Path points;
    MapView mapView;
    
    
	public PointOverlay(MapView mv) {
		super();
		mapView = mv;
		
		circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.GREEN);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAlpha(79);
        
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3);
        linePaint.setAlpha(79);
        
	} 
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Logger.debug("draw: " + size());
		Long then = System.currentTimeMillis();

        route = new Path();
        points = new Path();
	    Projection projection = mapView.getProjection();

	    Point prevP = null;
	    for(int i = 0;i < size();i++) {
	    	LocationFix p = (LocationFix) createItem(i);
	    	GeoPoint geopoint = p.getPoint();
		    Point point = new Point();
		    projection.toPixels(geopoint, point);
		    
	        if(prevP != null) {
	        	route.lineTo(point.x, point.y);
	        } else {
	        	route.moveTo(point.x, point.y);
	        }

		    float acc = p.getAccuracy();
		    if (acc == 0)
		    	acc = 50;
		    
		    int radiusPixel = (int) (projection.metersToEquatorPixels(acc) * LatitudeScaleCache.getScale(geopoint));

        	points.moveTo(point.x, point.y);
	        points.addCircle(point.x, point.y, radiusPixel, Path.Direction.CW);
	        prevP = point;
	    }
	    Long now = System.currentTimeMillis();
	    Logger.debug("Time to draw: " + (now - then) + "ms");
	    canvas.drawPath(points, circlePaint);
	    canvas.drawPath(route, linePaint);

	    super.draw(canvas, mapView, false);
	}

	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	public int size() {
		return mOverlays.size();
	}
	
	public void addLocation(LocationFix overlay) {
	    mOverlays.add(overlay);
	}
}

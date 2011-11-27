package com.gamuphi.cycle.views;

import android.content.Context;
import android.util.AttributeSet;

import com.gamuphi.cycle.models.LocationFix;
import com.gamuphi.cycle.models.Trip;
import com.gamuphi.cycle.models.Trip.FixListener;
import com.gamuphi.cycle.overlays.PointOverlay;
import com.gamuphi.cycle.overlays.StatsOverlay;
import com.gamuphi.cycle.utils.Logger;
import com.google.android.maps.MapView;

public class CycleView extends MapView {

	private Trip trip = null;
    private PointOverlay itemizedOverlay;
    private StatsOverlay stats;
	private FixListener listener;
    
	private void init() {
		
        this.setBuiltInZoomControls(true);
        
        itemizedOverlay = new PointOverlay(this);    
        this.getOverlays().add(itemizedOverlay);
        
        this.listener = new FixListener() {
        	public void newLocation(LocationFix l) {
				CycleView.this.itemizedOverlay.addLocation(l);
			}        	
        	public void latestEmitted() {
    			CycleView.this.invalidate();
    		}
        };
        stats = new StatsOverlay();
        this.getOverlays().add(stats);
	}
	
	public CycleView(Context context, String apiKey) {
		super(context, apiKey);
		init();
	}

	public CycleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CycleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setTrip(Trip t) {
		Logger.debug("CycleView.setTrip: " + (t != null ? "present" : "null"));
		if(this.trip != null) {
			trip.removeListener(listener);
		}
		trip = t;
		itemizedOverlay.clear();
		
		trip.addListener(listener);
		stats.setTrip(t);
	}


}

package com.gamuphi.cycle.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.AttributeSet;

import com.gamuphi.cycle.LocationFix;
import com.gamuphi.cycle.R;
import com.gamuphi.cycle.overlays.PointOverlay;
import com.gamuphi.cycle.providers.TripStore;
import com.gamuphi.cycle.utils.Logger;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class CycleView extends MapView {

	private int trip_id = 0;
	private int latest_location_id = 0;

    private List<Overlay> mapOverlays = new ArrayList<Overlay>();
    private PointOverlay itemizedOverlay;
    
	private void init() {
		
        this.setBuiltInZoomControls(true);
        mapOverlays = this.getOverlays();
        itemizedOverlay = new PointOverlay(this);     
        mapOverlays.add(itemizedOverlay);
        
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
	
	public void setTrip(int trip_id) {

		ContentObserver observer = new ContentObserver(getHandler()) {
			@Override 
			public void onChange(boolean selfChange) {
				CycleView.this.load_points();
			}
		};
		this.getContext().getContentResolver().registerContentObserver(TripStore.LOCATION_CONTENT_URI, true, observer );
		
		this.latest_location_id = 0;
		this.trip_id = trip_id;
	}

	protected void load_points() {
        Logger.debug("Adding item");
        AsyncTask<Void, Void, Void> t = new AsyncTask<Void, Void, Void>() {

            List<LocationFix> locations = new LinkedList<LocationFix>();
			@Override
			protected Void doInBackground(Void... params) {
				 String selectors = "trip_id = " + trip_id + " AND _id > " + CycleView.this.latest_location_id;
			        Cursor c = CycleView.this.getContext().getContentResolver().query(TripStore.LOCATION_CONTENT_URI, 
			        									  					null, 
			        									  					selectors, 
			        									  					null, "_id ASC");
			        Logger.debug("has " + c.getCount() + "records");
			        if(c.moveToFirst()) {
			        	int id_idx = c.getColumnIndex("_id");
			        	int created_at_idx = c.getColumnIndex("created_at");
			        	int lat_idx = c.getColumnIndex("lat");
			        	int long_idx = c.getColumnIndex("long");
			        	int speed_idx = c.getColumnIndex("speed");
			        	do {
			        		double lat = c.getDouble(lat_idx);
			        		double lon = c.getDouble(long_idx);
			        		float  speed = c.getFloat(speed_idx);
			        		CycleView.this.latest_location_id = c.getInt(id_idx);

			                GeoPoint point = new GeoPoint((int)(lat*1000000f), (int)(lon*1000000f));
			                LocationFix overlayitem = new LocationFix(point, 50);
			                locations.add(overlayitem);

			        	} while(c.moveToNext());
			        }
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				for(LocationFix overlayitem : locations) {
					itemizedOverlay.addLocation(overlayitem);
				}

	            invalidate();
			}
        };
		t.execute(null, null, null);
       
		/*
        GeoPoint point = new GeoPoint((int)(location.getLatitude()*1000000f), (int)(location.getLongitude()*1000000f));
        LocationFix overlayitem = new LocationFix(point, location.getAccuracy());
        itemizedOverlay.addLocation(overlayitem);

        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.invalidate();
        */
	}
}

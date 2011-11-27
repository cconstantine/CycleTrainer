package com.gamuphi.cycle.models;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.format.Time;

import com.gamuphi.cycle.models.LocationFix;
import com.gamuphi.cycle.providers.TripStore;
import com.google.android.maps.GeoPoint;

public class Trip {

	public interface FixListener {
		void newLocation(LocationFix l);
		void latestEmitted();
	}
	private class ListenerEntry {
		private int latest_location_id;
		private FixListener listeners;

		public ListenerEntry(FixListener l) {
			this.listeners = l;
			this.latest_location_id = 0;
		}
		
		public void emit() {
	       AsyncTask<Void, Void, Void> t = new AsyncTask<Void, Void, Void>() {
	            List<LocationFix> locations = new LinkedList<LocationFix>();
				@Override
				protected Void doInBackground(Void... params) {
					 String selectors = "trip_id = " + trip_id + " AND _id > " + ListenerEntry.this.latest_location_id;
				        Cursor c = context.getContentResolver().query(TripStore.LOCATION_CONTENT_URI, 
				        		null, 
				        		selectors, 
				        		null, "_id ASC");

				        if(c.moveToFirst()) {
				        	int id_idx = c.getColumnIndex("_id");
				        	//int created_at_idx = c.getColumnIndex("created_at");
				        	int lat_idx = c.getColumnIndex("lat");
				        	int long_idx = c.getColumnIndex("long");
				        	//int speed_idx = c.getColumnIndex("speed");
				        	do {
				        		double lat = c.getDouble(lat_idx);
				        		double lon = c.getDouble(long_idx);
				        		//float  speed = c.getFloat(speed_idx);
				        		ListenerEntry.this.latest_location_id = c.getInt(id_idx);
	
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
						ListenerEntry.this.listeners.newLocation(overlayitem);
					}
					ListenerEntry.this.listeners.latestEmitted();
				}
	        };
			t.execute(null, null, null);
		}
	}
	private long trip_id;
	private Context context;
	private float distance  = 0;
	
	private Map<FixListener, ListenerEntry> listeners = new HashMap<FixListener, ListenerEntry>();
	
	private void init(Context c) {
		this.context = c;
		
		ContentObserver observer = new ContentObserver(new Handler()) {
			@Override 
			public void onChange(boolean selfChange) {
				emit();
			}
		};
		context.getContentResolver().registerContentObserver(TripStore.LOCATION_CONTENT_URI, true, observer );
		
		this.addListener(new FixListener() {
			GeoPoint prev = null;
			public void newLocation(LocationFix l) {
				if(prev == null) {
					prev = l.getPoint();
					return;
				}
				float[] results = new float[1];
				GeoPoint cur = l.getPoint();
				Location.distanceBetween(
						prev.getLatitudeE6()/1E6, prev.getLatitudeE6()/1E6,
						cur.getLatitudeE6()/1E6, cur.getLatitudeE6()/1E6, results);
				distance += results[0];
			}
			public void latestEmitted() { }
		});
	}
	public Trip(Context c) {
		
        Time tripStart = new Time();
        tripStart.setToNow();
        
		ContentValues values = new ContentValues();
		values.put("created_at", tripStart.format3339(false));

    	Uri trip_uri;
		trip_uri = c.getContentResolver().insert(TripStore.TRIP_CONTENT_URI, values);
    	trip_id = Long.parseLong(trip_uri.getLastPathSegment());

		init(c);
	}
	
	public Trip(Context c, long trip_id) {
		this.trip_id = trip_id;
		init(c);
	}
	
	public long getTripId() {
		return trip_id;
	}
	public float getDistanceInMeters() {
		return distance;
	}
	
	public float getDistanceInMiles() {
		return (getDistanceInMeters() * 0.6214f / 1000.0f );
	}
	public void emit() {
		for(ListenerEntry l : Trip.this.listeners.values()) {
			l.emit();
		}
	}
	public void addListener(FixListener l) {
		listeners.put(l, new ListenerEntry(l));
		emit();
	}
	public void removeListener(FixListener l) {
		listeners.remove(l);;
	}
	
}

package com.gamuphi.cycle.services;


import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.List;

import com.gamuphi.cycle.providers.TripStore;
import com.gamuphi.cycle.utils.Logger;

public class CycleLocationService extends Service {
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds

    public static List<Location> locations = new ArrayList<Location>();
    
	protected LocationManager locationManager;
	protected LocationListener listener;
	protected boolean started = false;

    public class LocalBinder extends Binder {
    	public CycleLocationService getService() {
            return CycleLocationService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();
    final Messenger mMessenger = new Messenger(new Handler());
	
    @Override
    public void onCreate() { 
    	Logger.debug("CycleLocationService::onCreate");
        super.onCreate();
    }
    @Override
    public void onStart(Intent intent, int startId) {
    	Logger.debug("CycleLocationService::onStart");
        super.onStart(intent, startId);
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    
    @Override
    public void onDestroy() {
    	Logger.debug("CycleLocationService::onDestroy");
    	this.pause();
    	
    }
    
    synchronized public void start(final int trip_id) {
        listener = new LocationListener() {
            public void onProviderDisabled(String s) {
            	Logger.debug("Provider disabled by the user. GPS turned off");
            }

            public void onProviderEnabled(String s) {
            	Logger.debug("Provider enabled by the user. GPS turned on");
            }

    		public void onLocationChanged(Location location) {
                String message = String.format(
                        "Longitude: %1$s,  Latitude: %2$sm Speed: %2$s",
                        location.getLongitude(), location.getLatitude(), location.getSpeed()
                );
                Logger.debug(message);

                Time t = new Time();
                t.setToNow();
        		ContentValues values = new ContentValues();
        		values.put("trip_id", trip_id);
        		values.put("lat", location.getLatitude());
        		values.put("long", location.getLongitude());
        		values.put("created_at", t.format3339(false));

        		getContentResolver().insert(TripStore.LOCATION_CONTENT_URI, values);
    		}

    		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    			Logger.debug("onStatusChanged: " + arg0);
    			
    		}
        };
        
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 
                MINIMUM_TIME_BETWEEN_UPDATES, 
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                listener);
    }
    
    synchronized public void pause() {
    	if(started) {
    		locationManager.removeUpdates(listener);
    		started = false;
    	}
    }
    synchronized public static void report() {
    	Logger.debug("REPORTING");
    	for(Location location : locations) {
            String message = String.format(
                    "Longitude: %1$s,  Latitude: %2$sm Speed: %2$s",
                    location.getLongitude(), location.getLatitude(), location.getSpeed()
            );
            Logger.debug(message);
    	}
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}

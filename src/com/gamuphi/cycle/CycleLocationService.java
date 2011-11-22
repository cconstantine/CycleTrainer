package com.gamuphi.cycle;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import java.util.ArrayList;
import java.util.List;

public class CycleLocationService extends Service {
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds

    public static List<Location> locations = new ArrayList<Location>();
    
	protected LocationManager locationManager;
	protected LocationListener listener;

    public class LocalBinder extends Binder {
    	CycleLocationService getService() {
            return CycleLocationService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();
    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        }
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
	public List<LocationListener> listeners = new ArrayList<LocationListener>();
	
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

				synchronized(CycleLocationService.locations) { 
					CycleLocationService.locations.add(location);
				}
				
				synchronized(CycleLocationService.this.listeners) {
					for (LocationListener l : CycleLocationService.this.listeners) {
						l.onLocationChanged(location);
					}
				}
    			
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
    
    @Override
    public void onDestroy() {
    	Logger.debug("CycleLocationService::onDestroy");
    	this.pause();
    	
    }
    
    public void pause() {
    	if(listener != null) {
    		locationManager.removeUpdates(listener);
    		listener = null;
    	}
    }
    public static void report() {
    	Logger.debug("REPORTING");
    	for(Location location : locations) {
            String message = String.format(
                    "Longitude: %1$s,  Latitude: %2$sm Speed: %2$s",
                    location.getLongitude(), location.getLatitude(), location.getSpeed()
            );
            Logger.debug(message);
    		
    	}
    }
    
    public void addLocationListener(LocationListener l) {
    	synchronized(this.listeners){
    		this.listeners.add(l);
    	}
    }
    
    public void removeLocationListener(LocationListener l) {
    	synchronized(this.listeners){
    		this.listeners.remove(l);
    	}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}

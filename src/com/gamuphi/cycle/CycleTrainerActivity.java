package com.gamuphi.cycle;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

public class CycleTrainerActivity extends MapActivity {

	Intent service;

    List<Overlay> mapOverlays;
    PointOverlay itemizedOverlay;
    private CycleLocationService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((CycleLocationService.LocalBinder)service).getService();
            mBoundService.addLocationListener(new LocationListener() {

				public void onLocationChanged(Location location) {
		            GeoPoint point = new GeoPoint((int)(location.getLatitude()*1000000f), (int)(location.getLongitude()*1000000f));
		            LocationFix overlayitem = new LocationFix(point, location.getAccuracy());
		            itemizedOverlay.addLocation(overlayitem);
		            Logger.debug("Adding item");

		            MapView mapView = (MapView) findViewById(R.id.mapview);
		            mapView.invalidate();
		            
				}

				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub
					
				}

				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub
					
				}

				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					// TODO Auto-generated method stub
					
				}
            	
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Logger.debug("CycletrainerActivity::onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapOverlays = mapView.getOverlays();
        itemizedOverlay = new PointOverlay(mapView);     
        mapOverlays.add(itemizedOverlay);
        
        service = new Intent();
        service.setAction(CycleLocationService.class.getName());

        bindService(new Intent(this, 
        		CycleLocationService.class), mConnection, Context.BIND_AUTO_CREATE);
       
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Logger.debug("onResume");
    }
    public void onReport(View v) {
        CycleLocationService.report();
    }

    public void onStart(View v) {
        startService(service);
    }
    
    public void onStop(View v) {
    	if (mBoundService != null)
    		mBoundService.pause();
    	
    }
    public void onExit(View v) {
    	this.stopService(service);
    	this.finish();
    	
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
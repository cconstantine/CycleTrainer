package com.gamuphi.cycle.activities;

import java.util.List;


import com.gamuphi.cycle.services.CycleLocationService;
import com.gamuphi.cycle.LocationFix;
import com.gamuphi.cycle.utils.Logger;
import com.gamuphi.cycle.overlays.PointOverlay;
import com.gamuphi.cycle.R;
import com.gamuphi.cycle.providers.TripStore;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Time;
import android.view.View;

public class CycleTrainerActivity extends MapActivity {

	private Intent service;

    private List<Overlay> mapOverlays;
    private PointOverlay itemizedOverlay;
    private CycleLocationService mBoundService;
    
    private Time tripStart;
    

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

        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Logger.debug("onResume");
    }
	@Override
	public void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}
    public void onReport(View v) {
        CycleLocationService.report();

        
        Cursor c = getContentResolver().query(TripStore.CONTENT_URI, null, null, null, null);
        Logger.debug("has " + c.getCount() + "records");
        if(c.moveToFirst()) {
        	int created_at_idx = c.getColumnIndex("created_at");
        	Time t = new Time();
        	do {
        		t.parse3339(c.getString(created_at_idx));
            	Logger.debug(t.format2445());
        	} while(c.moveToNext());
        }
    }
	public void onHistory(View v) {
		Intent i = new Intent(this, HistoryActivity.class);
		startActivity(i);
	}
    public void onStart(View v) {
        startService(service);
        
        tripStart = new Time();
        tripStart.setToNow();
    }
    
    public void onStop(View v) {
    	if (mBoundService != null) 
    		mBoundService.pause();
    	
    	if(tripStart != null) {
    		ContentValues values = new ContentValues();

    		values.put("created_at", tripStart.format3339(false));

    		getContentResolver().insert(TripStore.CONTENT_URI, values);

    	}
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
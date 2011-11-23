package com.gamuphi.cycle.activities;

import java.util.List;


import com.gamuphi.cycle.services.CycleLocationService;
import com.gamuphi.cycle.utils.Logger;
import com.gamuphi.cycle.views.CycleView;
import com.gamuphi.cycle.R;
import com.gamuphi.cycle.providers.TripStore;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Time;
import android.view.View;

public class CycleTrainerActivity extends MapActivity {

	private Intent service;

    private CycleLocationService mBoundService;
    
    private CycleView cycleView;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((CycleLocationService.LocalBinder)service).getService();
            /*mBoundService.addLocationListener(new LocationListener() {

				public void onLocationChanged(Location location) {
		            
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
            	
            });*/
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
        
        cycleView = (CycleView) findViewById(R.id.mapview);
        
        service = new Intent();
        service.setAction(CycleLocationService.class.getName());

        startService(service);
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

        Cursor c = getContentResolver().query(TripStore.TRIP_CONTENT_URI, null, null, null, null);
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
    	Uri trip_uri;
    	int trip_id;

        Time tripStart = new Time();
        tripStart.setToNow();
        
		ContentValues values = new ContentValues();
		values.put("created_at", tripStart.format3339(false));

		trip_uri = getContentResolver().insert(TripStore.TRIP_CONTENT_URI, values);
    	trip_id = Integer.parseInt(trip_uri.getLastPathSegment());
    	
    	mBoundService.start(trip_id);
    	cycleView.setTrip(trip_id);
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
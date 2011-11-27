package com.gamuphi.cycle.activities;

import com.gamuphi.cycle.services.CycleLocationService;
import com.gamuphi.cycle.utils.Logger;
import com.gamuphi.cycle.views.CycleView;
import com.gamuphi.cycle.R;
import com.gamuphi.cycle.models.Trip;
import com.google.android.maps.MapActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;

public class CycleTrainerActivity extends MapActivity {

	private Intent service;
    private CycleLocationService mBoundService;
    private CycleView cycleView;
    
    private static int HISTORY_REQUEST = 1;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((CycleLocationService.LocalBinder)service).getService();
            if(mBoundService.isActive()) {
            	Trip t = new Trip(CycleTrainerActivity.this, mBoundService.getActiveTrip());
            	CycleTrainerActivity.this.setTrip(t);
            }
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
	
	public void onHistory(View v) {
		Intent i = new Intent(this, HistoryActivity.class);
		this.startActivityForResult(i,  HISTORY_REQUEST);
	}
	
    public void onStart(View v) {
    	Trip t = new Trip(this);
    	
    	mBoundService.start(t.getTripId());
    	setTrip(t);
    }
    
    public void setTrip(Trip t) {
    	cycleView.setTrip(t);
    	
    	ImageView si = (ImageView) findViewById(R.id.traffic_light);
    	si.setBackgroundResource(R.drawable.green_light);
    	
    	View b = findViewById(R.id.start);
    	b.setVisibility(View.GONE);
    	
    	b = findViewById(R.id.stop);
    	b.setVisibility(View.VISIBLE);
    }
    
    public void onStop(View v) {
    	if (mBoundService != null) 
    		mBoundService.pause();

    	ImageView si = (ImageView) findViewById(R.id.traffic_light);
    	si.setBackgroundResource(R.drawable.red_light);
    	
    	View b = findViewById(R.id.start);
    	b.setVisibility(View.VISIBLE);
    	
    	b = findViewById(R.id.stop);
    	b.setVisibility(View.GONE);
    	
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
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HISTORY_REQUEST) {
            if (resultCode == RESULT_OK) {
            	cycleView.setTrip(new Trip(this, data.getExtras().getLong("trip_id")));	
            }
        }
    }
}
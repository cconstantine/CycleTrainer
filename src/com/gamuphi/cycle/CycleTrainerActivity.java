package com.gamuphi.cycle;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.view.View;

public class CycleTrainerActivity extends Activity {

	Intent service;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Logger.debug("CycletrainerActivity::onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Logger.debug("Service: " + CycleLocationService.class.getName());
        service = new Intent();
        service.setAction(CycleLocationService.class.getName());
        ComponentName c = startService(service);
        if(c != null) {
        	Logger.debug(c.toString());
        } else
        	Logger.debug("c is null");
        
        	
    }
    public void onReport(View v) {
        CycleLocationService.report();
    }
    
    public void onExit(View v) {
    	this.stopService(service);
    	this.finish();
    	
    }
}
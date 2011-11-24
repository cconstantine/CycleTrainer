package com.gamuphi.cycle.activities;

import com.gamuphi.cycle.providers.TripStore;
import com.gamuphi.cycle.utils.Logger;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class HistoryActivity extends ListActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        

        Cursor mCursor = this.getContentResolver().query(TripStore.TRIP_CONTENT_URI, null, null, null, null);
        startManagingCursor(mCursor);
        
        ListAdapter adapter = new SimpleCursorAdapter(
                this, // Context.
                android.R.layout.two_line_list_item,  // Specify the row template to use (here, two columns bound to the two retrieved cursor
                mCursor,                                              // Pass in the cursor to bind to.
                new String[] {"_id", "created_at"},           // Array of cursor columns to bind to.
                new int[] {android.R.id.text1, android.R.id.text2});  // Parallel array of which template objects to bind to those columns.

        // Bind to our new adapter.
        setListAdapter(adapter);
    }
    
    protected void onListItemClick (ListView l, View v, int position, long id) {
    	Intent i = new Intent();
    	Logger.debug("Sending: " + id);
    	i.putExtra("trip_id", id);
    	this.setResult(RESULT_OK, i);
    	finish();
    }
}

package com.gamuphi.cycle.providers;

import java.util.HashMap;

import com.gamuphi.cycle.utils.Logger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TripStore extends ContentProvider {
	public static final String AUTHORITY = "com.gamuphi.cycle.providers.TripStore";

	public static final Uri TRIP_CONTENT_URI = 
            Uri.parse("content://" + AUTHORITY + "/trips");
	
	public static final Uri LOCATION_CONTENT_URI = 
            Uri.parse("content://" + AUTHORITY + "/locations");
	
    private static final String DATABASE_NAME = "TripStore.db";
    private static final int DATABASE_VERSION = 8;
    
    private static final String TRIP_TABLE_NAME = "trips";
    private static final String LOCATION_TABLE_NAME = "locations";
    
    private static final UriMatcher sUriMatcher;

    private static final int TRIP = 1;
    private static final int LOCATION = 2;
    
    private static HashMap<String, String> tripsProjectionMap;
    private static HashMap<String, String> locationsProjectionMap;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
        	Logger.debug("TripStore.onCreate");
            db.execSQL("CREATE TABLE " + TRIP_TABLE_NAME + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, created_at date);");
            String c = "CREATE TABLE " + LOCATION_TABLE_NAME + " ( " + 
					"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"trip_id INTEGER, " +
					"created_at date, " + 
					"long DOUBLE, " +
					"lat DOUBLE, " + 
					"speed FLOAT, " + 
					"accuracy FLOAT, " + 
 					"FOREIGN KEY(trip_id) REFERENCES " + TRIP_TABLE_NAME + "(_id) " +
					");";
            Logger.debug(c);
            db.execSQL(c);
            db.execSQL("CREATE UNIQUE INDEX trip_location ON " + LOCATION_TABLE_NAME + " (trip_id, _id)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TRIP_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
            onCreate(db);
        }
    }
    private DatabaseHelper dbHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count;
        switch (sUriMatcher.match(uri)) {
            case TRIP:
                count = db.delete(TRIP_TABLE_NAME, where, whereArgs);
                break;
            case LOCATION:
                count = db.delete(LOCATION_TABLE_NAME, where, whereArgs);
                break;
           default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
            case TRIP:
                return "vnd.android.cursor.dir/vnd.gamuphi.trips";
 
        }

        throw new IllegalArgumentException("Unknown URI " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		String table = null;
		Uri content_uri = null;
        switch (sUriMatcher.match(uri)) {
	        case TRIP:
	            table = TRIP_TABLE_NAME;
	            content_uri = TRIP_CONTENT_URI;
	            break;
	        case LOCATION:
	            table = LOCATION_TABLE_NAME;
	            content_uri = LOCATION_CONTENT_URI;
	            break;
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(table, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(content_uri, rowId);
        	Logger.debug("Inserted: " + noteUri.toString());

            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
	}

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }
 

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case TRIP:
                qb.setTables(TRIP_TABLE_NAME);
                qb.setProjectionMap(tripsProjectionMap);
                break;
            case LOCATION:
                qb.setTables(LOCATION_TABLE_NAME);
                qb.setProjectionMap(locationsProjectionMap);
                break;
 
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
 
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case TRIP:
                count = db.update(TRIP_TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
 
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, TRIP_TABLE_NAME, TRIP);
		sUriMatcher.addURI(AUTHORITY, LOCATION_TABLE_NAME, LOCATION);
		
		tripsProjectionMap = new HashMap<String, String>();
		tripsProjectionMap.put("_id", "_id");
        tripsProjectionMap.put("created_at", "created_at");
        
		locationsProjectionMap = new HashMap<String, String>();
		locationsProjectionMap.put("_id", "_id");
		locationsProjectionMap.put("trip_id", "trip_id");
		locationsProjectionMap.put("long", "long");
		locationsProjectionMap.put("lat", "lat");
		locationsProjectionMap.put("speed", "speed");
		locationsProjectionMap.put("created_at", "created_at");

	}
}

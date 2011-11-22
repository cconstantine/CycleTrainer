package com.gamuphi.cycle.providers;

import java.util.HashMap;

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

	public static final Uri CONTENT_URI = 
            Uri.parse("content://" + AUTHORITY + "/trips");
	
    private static final String DATABASE_NAME = "TripStore.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "trips";
    private static final UriMatcher sUriMatcher;

    private static final int TRIP = 1;
    private static HashMap<String, String> tripsProjectionMap;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, created_at date);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
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
                count = db.delete(TABLE_NAME, where, whereArgs);
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
		if (sUriMatcher.match(uri) != TRIP) 
		{ throw new IllegalArgumentException("Unknown URI " + uri); }
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
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
                qb.setTables(TABLE_NAME);
                qb.setProjectionMap(tripsProjectionMap);
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
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
 
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, TABLE_NAME, TRIP);
		
		tripsProjectionMap = new HashMap<String, String>();
		tripsProjectionMap.put("_id", "_id");
        tripsProjectionMap.put("created_at", "created_at");

	}
}

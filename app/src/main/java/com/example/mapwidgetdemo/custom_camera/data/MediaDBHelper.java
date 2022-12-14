package com.example.mapwidgetdemo.custom_camera.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by koushick on 24-Mar-18.
 */

public class MediaDBHelper extends SQLiteOpenHelper {

    public static final String TAG = "MediaDBHelper";
    public static String DATABASE_NAME = "media.db";
    private static int DATABASE_VERSION = 1;
    public static String DB_PATH = "/data/data/com.example.mapwidgetdemo.custom_camera/databases/";
    private final Context myContext;
    boolean VERBOSE = false;

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        if(VERBOSE)Log.d(TAG, "Upgrading database from version " + oldVersion + " to " +
                newVersion + ". OLD DATA WILL BE DESTROYED");

        // Drop the table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MediaTableConstants.MEDIA_TABLE);

        // re-create database
        onCreate(sqLiteDatabase);
    }

    public MediaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE " + MediaTableConstants.MEDIA_TABLE + " ( "+
                MediaTableConstants.ID + " INTEGER NOT NULL PRIMARY KEY , "+
                MediaTableConstants.FILE_NAME + " TEXT , "+
                MediaTableConstants.MEMORY_STORAGE + " INTEGER "+
                " ); ";
        if(VERBOSE)Log.d(TAG, CREATE_TABLE);
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }
}

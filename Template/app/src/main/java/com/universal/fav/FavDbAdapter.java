package com.universal.fav;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 *  This adapter is used to manage the database where all the users favorite items are kept, 
 *  It contains methods to check for duplicates, create new database, remove values, etc.
 */
public class FavDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATE = "date";
    public static final String KEY_URL = "url";
    public static final String KEY_RN = "rn";
    public static final String KEY_UN = "un";
    public static final String KEY_CAT = "cat";
    
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    //create new database
    private static final String DATABASE_CREATE =
        "create table notes (_id integer primary key autoincrement, "
        + "title text not null, "
        + "body text not null, "
        + "date text not null, "
        + "url text not null, "
        + "rn text not null, "
        + "un text not null, "
        + "cat text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database " + oldVersion + " to "
                    + newVersion + ", all data will be destroyed");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public FavDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    //Open the database
    public FavDbAdapter open() throws SQLException {
        try {
    	mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        } catch (Exception e){
        	Log.w(TAG, "Exception");
        }
        return this;
    }

    //close the database
    public void close() {
        mDbHelper.close();
    }


    //Create a new favorite
    public long addFavorite(String title, String body, String date, String url, String rn, String un, String cat) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_URL, url);
        initialValues.put(KEY_RN, rn);
        initialValues.put(KEY_UN, un);
        initialValues.put(KEY_CAT, cat);
        
        //This adds respectively the folowing values
        //-title
        //-description or tweet
        //-date
        //-url or id
        //-realname
        //-username
        //-catagory (youtube, twitter, rss, web)      
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a favorite
    public boolean deleteFav(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    // Delete all favorites
    public void emptyDatabase(){
    	mDb.delete(DATABASE_TABLE, null, null);
    }

    
    //Get all favorites
    public Cursor getFavorites() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_DATE, KEY_URL, KEY_RN, KEY_UN, KEY_CAT}, null, null, null, null, null);
    }

    //Return item with given rowid
    public Cursor getFavorite(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_BODY, KEY_DATE, KEY_URL, KEY_RN, KEY_UN, KEY_CAT}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    //used to update a favorite, is not being used at the moment, but might for feature updates
    public boolean updateFavorite(long rowId, String title, String body, String date, String url, String rn, String un, String cat) {
    	
    	
    	ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_DATE, date);
        args.put(KEY_URL, url);
        args.put(KEY_RN, rn);
        args.put(KEY_UN, un);
        args.put(KEY_CAT, cat);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    //check for duplicates
    public boolean checkEvent(String title, String body, String date, String url, String rn, String un, String cat) 
    {

        Cursor cursor = mDb.query(DATABASE_TABLE,
                new String[] {KEY_TITLE, KEY_BODY, KEY_DATE, KEY_URL, KEY_RN, KEY_UN, KEY_CAT}, 
                KEY_TITLE + " = ? and "+ 
                KEY_BODY + " = ? and " + 
                KEY_DATE + " = ? and "+ 
                KEY_URL + " = ? and " + 
                KEY_RN + " = ? and "+ 
                KEY_UN + " = ? and " + 
                KEY_CAT + " = ?" , 
                new String[] {title, body, date, url, rn, un, cat}, null, null, null);

        if(cursor.moveToFirst()) {
         Log.w(TAG, "Row Exists");
         return false; //row exists
         
        } else {
         Log.w(TAG, "Row does not exist");
         return true;
        }
    }
}

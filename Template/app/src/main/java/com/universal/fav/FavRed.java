package com.universal.fav;

import com.universal.rss.ui.RssDetailActivity;
import com.universal.twi.ui.TweetDetailActivity;
import com.universal.web.WebviewActivity;
import com.universal.wordpress.ui.WordpressDetailActivity;
import com.universal.yt.ui.VideoDetailActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

/**
 *  This activity redirects the user to the correct activity for viewing the
 *  saved favorite item
 */

public class FavRed extends Activity {

    private Long mRowId;
    private FavDbAdapter mDbHelper;
    
    String title;
    String date;
    String body;
    String url;
    String rn;
    String un;
    String cat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new FavDbAdapter(this);
        mDbHelper.open();

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(FavDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(FavDbAdapter.KEY_ROWID)
									: null;
		}

		getData();
		
		openActivity();
    }

    @SuppressWarnings("deprecation")
	private void getData() {
        if (mRowId != null) {
            Cursor note = mDbHelper.getFavorite(mRowId);
            startManagingCursor(note);
            title = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_TITLE));
            date  = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_DATE));
            body  = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_BODY));
            url  = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_URL));
            rn  = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_RN));
            un  = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_UN));
            cat  = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_CAT));
        }
    }
    
    private void openActivity() {
    	if ("youtube".equals(cat)) {
    		Intent intent = new Intent(this,VideoDetailActivity.class);
   		      Bundle bundle = new Bundle();
   		      bundle.putString("keyTitle", title);
   		      bundle.putString("keyDescription", body);
   		      bundle.putString("keyId", url);
   		      bundle.putString("keyDate", date);
   		      bundle.putString("keyFavorites", "true");
   		      intent.putExtras(bundle);
   		      startActivity(intent);  
    	} else if ("rss".equals(cat)) {
    		Intent intent = new Intent(this,RssDetailActivity.class);
   		      Bundle bundle = new Bundle();
   		      bundle.putString("keyTitle", title);
   		      bundle.putString("keyDescription", body);
   		      bundle.putString("keyLink", url);
   		      bundle.putString("keyPubdate", date);
   		      bundle.putString("keyFavorites", "true");
   		      intent.putExtras(bundle);
   		      startActivity(intent);
    	} else if ("twitter".equals(cat)) {
    		  Intent intent = new Intent(this,TweetDetailActivity.class);
   		      Bundle bundle = new Bundle();
   		      bundle.putString("keyUsername", un);
   		      bundle.putString("keyRealName", rn); //
   		      bundle.putString("keyId", url);
   		      bundle.putString("keyPubdate", date); //
   		 	  bundle.putString("keyTweet", body); //
   		 	  bundle.putString("keyFavorites", "true");
   		 	  intent.putExtras(bundle);
   		      startActivity(intent);
    	} else if ("web".equals(cat)) {
    		  Intent mIntent = new Intent(FavRed.this, WebviewActivity.class);
    		  mIntent.putExtra(WebviewActivity.URL, url);
    		  startActivity(mIntent);
    	} else if ("wordpress".equals(cat)) {
  		      Intent intent = new Intent(this, WordpressDetailActivity.class);
		      Bundle bundle = new Bundle();
		      bundle.putString("fav", "true");
		 	  bundle.putString("favTitle", title);
		 	  bundle.putString("favId", body);
		 	  bundle.putString("favDate", date);
		 	  bundle.putString("favUrl", url);
		 	  intent.putExtras(bundle);
		      startActivity(intent);
	    }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    
    public void setData(){
  }
}
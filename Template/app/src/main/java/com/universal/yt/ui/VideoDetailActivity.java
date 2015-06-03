package com.universal.yt.ui;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.universal.Helper;
import com.universal.R;
import com.universal.fav.FavDbAdapter;
import com.universal.util.TrackingScrollView;
import com.universal.util.WebHelper;
import com.universal.yt.player.YouTubePlayerActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  This activity is used to display the details of a video
 */

public class VideoDetailActivity extends ActionBarActivity {
	
	private FavDbAdapter mDbHelper;
    private Toolbar mToolbar;

	String date;
	String id;
	String title;
	String description;
	String favorite;
	String image;
	
	ImageLoader imageLoader;
	private TextView mPresentation;
	int mImageHeight;
	int latestAlpha;
	ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.activity_youtube_detail);
	  
	  mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
      setSupportActionBar(mToolbar);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
	  //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
	  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	  mToolbar.getBackground().setAlpha(0);
	  getSupportActionBar().setDisplayShowTitleEnabled(false);
		
	  mPresentation = (TextView)findViewById(R.id.youtubetitle);
	  TextView detailsDescription = (TextView)findViewById(R.id.youtubedescription);
	  TextView detailsPubdate = (TextView)findViewById(R.id.youtubeurl);
	  
	  imageLoader = Helper.initializeImageLoader(VideoDetailActivity.this);
      detailsDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getWebViewFontSize(this));
	  Bundle bundle = this.getIntent().getExtras();
    
	  mPresentation.setText(bundle.getString("keyTitle"));
      detailsDescription.setText(bundle.getString("keyDescription"));
      detailsPubdate.setText(bundle.getString("keyDate"));
      		
      title = (bundle.getString("keyTitle"));
      id = (bundle.getString("keyId"));
      date = (bundle.getString("keyDate"));
      description = (bundle.getString("keyDescription"));
      favorite = (bundle.getString("keyFavorites"));
      image = (bundle.getString("keyImage"));
      		
      if ((getResources().getString(R.string.ad_visibility).equals("0"))){
	        	// Look up the AdView as a resource and load a request.
	        	AdView adView = (AdView) findViewById(R.id.adView);
	        	AdRequest adRequest = new AdRequest.Builder().build();
	        	adView.loadAd(adRequest);
	  }
      
       mImage = (ImageView) findViewById(R.id.image);
       imageLoader.displayImage(image, mImage);
	   mImageHeight = mImage.getLayoutParams().height;

		((TrackingScrollView) findViewById(R.id.scroller)).setOnScrollChangedListener(
				new TrackingScrollView.OnScrollChangedListener() {
					@Override
					public void onScrollChanged(TrackingScrollView source, int l, int t, int oldl, int oldt) {
						handleScroll(source, t);
					}
				}
		);
      
      ImageButton btnPlay= (ImageButton) findViewById(R.id.playbutton);
      btnPlay.bringToFront();
      //Listening to button event
      btnPlay.setOnClickListener(new View.OnClickListener() {

    	  public void onClick(View arg0) {
    		  Intent intent = new Intent(VideoDetailActivity.this, YouTubePlayerActivity.class);
    		  intent.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, id);
    		  intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    		  startActivity(intent);
    	  }
     });
      
      Button btnOpen = (Button) findViewById(R.id.openbutton);

      //Listening to button event
      btnOpen.setOnClickListener(new View.OnClickListener() {

    	  public void onClick(View arg0) {
    		       try {  
    		         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
    	             startActivity(intent);                 
    	           }catch (ActivityNotFoundException ex){
    	             Intent intent=new Intent(Intent.ACTION_VIEW, 
    	             Uri.parse("http://www.youtube.com/watch?v="+id));
    	             startActivity(intent);
    	         }
    	  }
     });
      
      Button btnFav = (Button) findViewById(R.id.favoritebutton);
      
    //Listening to button event
      btnFav.setOnClickListener(new View.OnClickListener() {

    	  public void onClick(View arg0) {
    		  mDbHelper = new FavDbAdapter(VideoDetailActivity.this);
              mDbHelper.open();
              
              if(mDbHelper.checkEvent(title, description, date, id, "", "", "youtube")) {
                  // Item is new
           	   mDbHelper.addFavorite(title, description, date, id, "", "", "youtube");
           	    Toast toast = Toast.makeText(VideoDetailActivity.this, getResources().getString(R.string.favorite_success), Toast.LENGTH_LONG);
                  toast.show();
              } else {
                  Toast toast = Toast.makeText(VideoDetailActivity.this, getResources().getString(R.string.favorite_duplicate), Toast.LENGTH_LONG);
                  toast.show();
              }
    	  }
     });
   }
    
    private void handleScroll(TrackingScrollView source, int top) {
		int scrolledImageHeight = Math.min(mImageHeight, Math.max(0, top));

		ViewGroup.MarginLayoutParams imageParams = (ViewGroup.MarginLayoutParams) mImage.getLayoutParams();
		int newImageHeight = mImageHeight - scrolledImageHeight;
		if (imageParams.height != newImageHeight) {
			// Transfer image height to margin top
			imageParams.height = newImageHeight;
			imageParams.topMargin = scrolledImageHeight;

			// Invalidate view
			mImage.setLayoutParams(imageParams);
		}
		
		final int imageheaderHeight = mImage.getHeight() - getSupportActionBar().getHeight();
    	//t=how far you scrolled
    	//ratio is from 0,0.1,0.2,...1
    	final float ratio = (float) Math.min(Math.max(top, 0), imageheaderHeight) / imageheaderHeight;
    	//setting the new alpha value from 0-255 or transparent to opaque
    	final int newAlpha = (int) (ratio * 255);
    	
    	if (newAlpha != latestAlpha){
    		mToolbar.getBackground().setAlpha(newAlpha);
    	}
    	
    	latestAlpha = newAlpha;
	}

	
	@Override
	public void onPause(){
		super.onPause();
		mToolbar.getBackground().setAlpha(255);
	}
	
	@Override
	public void onResume(){
		super.onPause();
		mToolbar.getBackground().setAlpha(latestAlpha);
	}
    
   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	 	case android.R.id.home:
            finish();
            return true;
        case R.id.share:
        	String applicationName = getResources().getString(R.string.app_name);
  		    Intent sendIntent = new Intent();
  		    sendIntent.setAction(Intent.ACTION_SEND);
  		    
  		    String urlvalue = getResources().getString(R.string.video_share_begin);
  		    String seenvalue = getResources().getString(R.string.video_share_middle);
  		    String appvalue = getResources().getString(R.string.video_share_end);
  		                                           //this is the text that will be shared
  		    sendIntent.putExtra(Intent.EXTRA_TEXT, (urlvalue+"http://youtube.com/watch?v="+id+seenvalue+applicationName+appvalue));
  		    sendIntent.putExtra(Intent.EXTRA_SUBJECT, title); //you can replace title with a string of your choice
  		    sendIntent.setType("text/plain");
  		    startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share_header)));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.youtube_detail_menu, menu);
 	    return true;
 	}
 	
}

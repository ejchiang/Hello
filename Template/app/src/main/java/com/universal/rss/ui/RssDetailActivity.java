package com.universal.rss.ui;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.universal.R;
import com.universal.fav.FavDbAdapter;
import com.universal.util.WebHelper;
import com.universal.web.WebviewActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  This activity is used to display details of a rss item
 */

public class RssDetailActivity extends ActionBarActivity {

	private WebView wb;
	private FavDbAdapter mDbHelper;
    private Toolbar mToolbar;

	String date;
	String link;
	String title;
	String description;
	String favorite;

	@SuppressLint("SetJavaScriptEnabled")@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rss_details);
		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
		TextView detailsTitle = (TextView) findViewById(R.id.detailstitle);
		TextView detailsPubdate = (TextView) findViewById(R.id.detailspubdate);

		Bundle bundle = this.getIntent().getExtras();

		detailsTitle.setText(bundle.getString("keyTitle"));
		detailsPubdate.setText(bundle.getString("keyPubdate"));
		date = (bundle.getString("keyPubdate"));
		link = (bundle.getString("keyLink"));
		title = (bundle.getString("keyTitle"));
		description = (bundle.getString("keyDescription"));
		favorite = (bundle.getString("keyFavorites"));

		wb = (WebView) findViewById(R.id.descriptionwebview);

		//parse the html and apply some styles
		Document doc = Jsoup.parse(description);
		String html = WebHelper.docToBetterHTML(doc, this);;

		wb.getSettings().setJavaScriptEnabled(true);
		wb.loadDataWithBaseURL(link, html , "text/html", "UTF-8", "");
		Log.v("INFO", "Wordpress HTML: " + html);
		wb.setBackgroundColor(Color.argb(1, 0, 0, 0));
		wb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		wb.getSettings().setDefaultFontSize(WebHelper.getWebViewFontSize(this));
		wb.setWebViewClient(new WebViewClient(){
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	if (url != null && (url.startsWith("http://") || url.startsWith("http://"))) {
		        	Intent mIntent = new Intent(RssDetailActivity.this, WebviewActivity.class);
		        	mIntent.putExtra(WebviewActivity.URL, url);
		        	startActivity(mIntent);
		            return true;
		        } else {
		        	Uri uri = Uri.parse(url);
		        	Intent ViewIntent = new Intent(Intent.ACTION_VIEW, uri);

		        	// Verify it resolves
		        	PackageManager packageManager = getPackageManager();
		        	List<ResolveInfo> activities = packageManager.queryIntentActivities(ViewIntent, 0);
		        	boolean isIntentSafe = activities.size() > 0;

		        	// Start an activity if it's safe
		        	if (isIntentSafe) {
		        	    startActivity(ViewIntent);
		        	}
		        	return true;
		        }
		    }
		});

		if ((getResources().getString(R.string.ad_visibility).equals("0"))) {
			// Look up the AdView as a resource and load a request.
			AdView adView = (AdView) findViewById(R.id.adView);
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
		}

		Button btnOpen = (Button) findViewById(R.id.openbutton);

		//Listening to button event
		btnOpen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(link));
				startActivity(intent);

			}
		});

		Button btnFav = (Button) findViewById(R.id.favoritebutton);

		//Listening to button event
		btnFav.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				mDbHelper = new FavDbAdapter(RssDetailActivity.this);
				mDbHelper.open();

				if (mDbHelper.checkEvent(title, description, date, link, "", "", "rss")) {
					// Item is new
					mDbHelper.addFavorite(title, description, date, link, "", "", "rss");
					Toast toast = Toast.makeText(RssDetailActivity.this, getResources().getString(R.string.favorite_success), Toast.LENGTH_LONG);
					toast.show();
				} else {
					Toast toast = Toast.makeText(RssDetailActivity.this, getResources().getString(R.string.favorite_duplicate), Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});


	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.share:
				String html = description;
				html = html.replaceAll("<(.*?)\\>", ""); //Removes all items in brackets
				html = html.replaceAll("<(.*?)\\\n", ""); //Must be undeneath
				html = html.replaceFirst("(.*?)\\>", ""); //Removes any connected item to the last bracket
				html = html.replaceAll("&nbsp;", "");
				html = html.replaceAll("&amp;", "");

				String linkvalue = getResources().getString(R.string.item_share_begin);
				String seenvalue = getResources().getString(R.string.item_share_middle);
				String appvalue = getResources().getString(R.string.item_share_end);
				String applicationName = getResources().getString(R.string.app_name);
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				//this is the text that will be shared
				sendIntent.putExtra(Intent.EXTRA_TEXT, (html + linkvalue + link + seenvalue + applicationName + appvalue));
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
		inflater.inflate(R.menu.rss_detail_menu, menu);
		return true;
	}

}
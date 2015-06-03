package com.universal.twi.ui;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.universal.R;
import com.universal.fav.FavDbAdapter;
import com.universal.util.WebHelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity is used to display the details of a click tweet
 */

public class TweetDetailActivity extends ActionBarActivity {

	String name;
	String tweetId;
	String username;
	String tweet;
	String date;
	String favorite;

	private FavDbAdapter mDbHelper;
    private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_twitter_details);

		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView detailsTitle = (TextView) findViewById(R.id.detailstitle);
		TextView detailsDescription = (TextView) findViewById(R.id.detailsdescription);
		TextView detailsPubdate = (TextView) findViewById(R.id.detailspubdate);

		Bundle bundle = this.getIntent().getExtras();

		detailsTitle.setText(bundle.getString("keyRealName"));
		detailsDescription.setText(Html.fromHtml(bundle.getString("keyTweet")));
		detailsPubdate.setText(bundle.getString("keyPubdate"));
		date = (bundle.getString("keyPubdate"));
		name = (bundle.getString("keyRealName"));
		tweetId = (bundle.getString("keyId"));
		username = (bundle.getString("keyUsername"));
		tweet = (bundle.getString("keyTweet"));
		favorite = (bundle.getString("keyFavorites"));
		
		detailsDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getWebViewFontSize(this));

		Button btnOpenWeb = (Button) findViewById(R.id.openbuttonweb);

		// Listening to button event
		btnOpenWeb.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://twitter.com/" + username + "/status/"
								+ tweetId));
				startActivity(intent);

			}
		});

		Button btnFav = (Button) findViewById(R.id.favoritebutton);

		// Listening to button event
		btnFav.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				mDbHelper = new FavDbAdapter(TweetDetailActivity.this);
				mDbHelper.open();

				if (mDbHelper.checkEvent("@" + username + " " + tweet, tweet,
						date, tweetId, name, username, "twitter")) {
					// Item is new
					mDbHelper.addFavorite("@" + username + " " + tweet, tweet,
							date, tweetId, name, username, "twitter");
					Toast toast = Toast
							.makeText(TweetDetailActivity.this, getResources()
									.getString(R.string.favorite_success),
									Toast.LENGTH_LONG);
					toast.show();
				} else {
					Toast toast = Toast.makeText(
							TweetDetailActivity.this,
							getResources().getString(
									R.string.favorite_duplicate),
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

		if ((getResources().getString(R.string.ad_visibility).equals("0"))) {
			// Look up the AdView as a resource and load a request.
			AdView adView = (AdView) findViewById(R.id.adView);
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.share:
			String applicationName = getResources()
					.getString(R.string.app_name);
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			String link = ("http://twitter.com/" + username + "/status/" + tweetId);
			// this is the text that will be shared
			sendIntent.putExtra(
					Intent.EXTRA_TEXT,
					(tweet
							+ "\n@"
							+ username
							+ " \n\n"
							+ getResources()
									.getString(R.string.tweet_share_url)
							+ link
							+ getResources().getString(
									R.string.tweet_share_text_begin)
							+ applicationName + getResources().getString(
							R.string.tweet_share_text_end)));
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, name
					+ getResources().getString(R.string.tweet_share_header)); // you
																				// can
																				// replace
																				// title
																				// with
																				// a
																				// string
																				// of
																				// your
																				// choice
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, getResources()
					.getString(R.string.share_header)));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tweet_detail_menu, menu);
		return true;
	}

}
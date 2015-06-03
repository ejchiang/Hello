package com.universal.web;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.universal.Helper;
import com.universal.MainActivity;
import com.universal.R;
import com.universal.fav.FavDbAdapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * This activity is used to display webpages
 */

public class WebviewFragment extends Fragment {
	private WebView browser;
	Activity mAct;
	private ProgressBar progressBar;

	int stateForward = 1;
	int stateBack = 1;

	ImageButton webBackButton;
	ImageButton webForwButton;

	private LinearLayout ll;

	private FavDbAdapter mDbHelper;
	
	//HTML5 video
	 private View mCustomView;
     private int mOriginalSystemUiVisibility;
     private int mOriginalOrientation;
     private WebChromeClient.CustomViewCallback mCustomViewCallback;
     protected FrameLayout mFullscreenContainer;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (LinearLayout) inflater.inflate(R.layout.fragment_webview,
				container, false);

		setHasOptionsMenu(true);

		// actionbar
		if ((getResources().getString(R.string.ad_visibility).equals("0"))) {
			// Look up the AdView as a resource and load a request.
			AdView adView = (AdView) ll.findViewById(R.id.adView);
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
		}

		browser = (WebView) ll.findViewById(R.id.webView);

		// settings some settings like zooming etc in seperate method for
		// suppresslint
		browserSettings();

		browser.setWebViewClient(new WebViewClient() {
			// Make sure any url clicked is opened in webview
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if ((url.contains("market://") || url.contains("mailto:")
						|| url.contains("play.google") || url.contains("tel:") || url
						.contains("vid:")) == true) {
					// Load new URL Don't override URL Link
					view.getContext().startActivity(
							new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

					return true;
				}
				// Return true to override url loading (In this case do
				// nothing).
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(browser, url);
				adjustControls();
			}

			// handeling errors
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
				builder.setMessage(description)
						.setPositiveButton(
								getResources().getString(R.string.ok), null)
						.setTitle(
								getResources().getString(
										R.string.error_received));
				builder.show();
			}

			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				adjustControls();

			}

		});

		progressBar = (ProgressBar) ll.findViewById(R.id.progressbar);

		// has all to do with progress bar
		browser.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int progress) {
				progressBar.setProgress(0);
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress(progress);

				//progressBar.incrementProgressBy(progress);

				if (progress == 100) {	
					progressBar.setVisibility(View.GONE);
				}
			}
			
			@SuppressLint("InlinedApi")
			@Override
	            public void onShowCustomView(View view,
	                                         WebChromeClient.CustomViewCallback callback) {
	                // if a view already exists then immediately terminate the new one
	                if (mCustomView != null) {
	                    onHideCustomView();
	                    return;
	                }

	                // 1. Stash the current state
	                mCustomView = view;
	                mOriginalSystemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
	                mOriginalOrientation = getActivity().getRequestedOrientation();

	                // 2. Stash the custom view callback
	                mCustomViewCallback = callback;

	                // 3. Add the custom view to the view hierarchy
	                FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
	                decor.addView(mCustomView, new FrameLayout.LayoutParams(
	                        ViewGroup.LayoutParams.MATCH_PARENT,
	                        ViewGroup.LayoutParams.MATCH_PARENT));


	                // 4. Change the state of the window
	                getActivity().getWindow().getDecorView().setSystemUiVisibility(
	                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
	                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
	                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
	                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
	                                View.SYSTEM_UI_FLAG_FULLSCREEN |
	                                View.SYSTEM_UI_FLAG_IMMERSIVE);
	                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	            }

	            @Override
	            public void onHideCustomView() {
	                // 1. Remove the custom view
	                FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
	                decor.removeView(mCustomView);
	                mCustomView = null;

	                // 2. Restore the state to it's original form
	                getActivity().getWindow().getDecorView()
	                        .setSystemUiVisibility(mOriginalSystemUiVisibility);
	                getActivity().setRequestedOrientation(mOriginalOrientation);

	                // 3. Call the custom view callback
	                mCustomViewCallback.onCustomViewHidden();
	                mCustomViewCallback = null;

	            }

		});

		browser.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		// setting an on touch listener
		browser.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					break;
				}
				return false;
			}
		});
		return ll;
	}// of oncreateview

	@SuppressLint("InflateParams")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		ActionBar actionBar = ((ActionBarActivity) activity)
				.getSupportActionBar();
		
		if (activity instanceof WebviewActivity){
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
		} else {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		}
		
		View view = activity.getLayoutInflater().inflate(R.layout.fragment_webview_actionbar, null);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		actionBar.setCustomView(view, lp);

		webBackButton = (ImageButton) activity.findViewById(R.id.goBack);
		webForwButton = (ImageButton) activity.findViewById(R.id.goForward);

		webBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (browser.canGoBack())
					browser.goBack();
			}
		});
		webForwButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (browser.canGoForward())
					browser.goForward();
			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		if (checkConnectivity()) {
			String weburl = this.getArguments().getString(MainActivity.DATA);
			browser.loadUrl(weburl);
		}

	}
	
	@Override
	public void onPause(){
		super.onPause();
		browser.onPause();
	}

	@Override
	public void onResume(){
		super.onResume();
		browser.onResume();
	}
	
    public boolean canGoBack(){
    	if (browser.canGoBack()){
	        browser.goBack();
	        return true;
    	} else {
    		return false;
    	}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
			shareURL();
			return true;
		case R.id.favorite:
			mDbHelper = new FavDbAdapter(mAct);
			mDbHelper.open();

			String title = browser.getTitle();
			String url = browser.getUrl();

			if (mDbHelper.checkEvent(title, "", "", url, "", "", "web")) {
				// This item is new
				mDbHelper.addFavorite(title, "", "", url, "", "", "web");
				Toast toast = Toast.makeText(mAct,
						getResources().getString(R.string.favorite_success),
						Toast.LENGTH_LONG);
				toast.show();
			} else {
				Toast toast = Toast.makeText(mAct,
						getResources().getString(R.string.favorite_duplicate),
						Toast.LENGTH_LONG);
				toast.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.webview_menu, menu);
	}

	// Checking for an internet connection
	private boolean checkConnectivity() {
		boolean enabled = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) mAct
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		if ((info == null || !info.isConnected() || !info.isAvailable())) {
			enabled = false;
			
			Helper.noConnection(mAct, true);
		}
		
		return enabled;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void adjustControls() {
		if (browser.canGoBack()) {
			webBackButton.setColorFilter(Color.argb(255, 255, 255, 255));
		} else {
			webBackButton.setColorFilter(Color.argb(255, 0, 0, 0));
		}
		if (browser.canGoForward()) {
			webForwButton.setColorFilter(Color.argb(255, 255, 255, 255));
		} else {
			webForwButton.setColorFilter(Color.argb(255, 0, 0, 0));
		}
	}

	// sharing
	private void shareURL() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		String appname = getString(R.string.app_name);
		shareIntent.putExtra(Intent.EXTRA_TEXT,
				(getResources().getString(R.string.web_share_begin)) + appname
						+ getResources().getString(R.string.web_share_end)
						+ browser.getUrl());
		startActivity(Intent.createChooser(shareIntent, getResources()
				.getString(R.string.share)));
	}

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	private void browserSettings() {
		// set javascript and zoom and some other settings
		browser.getSettings().setJavaScriptEnabled(true);
		//browser.getSettings().setBuiltInZoomControls(true);
		browser.getSettings().setDisplayZoomControls(false);
		browser.getSettings().setAppCacheEnabled(true);
		browser.getSettings().setDatabaseEnabled(true);
		browser.getSettings().setDomStorageEnabled(true);
		browser.getSettings().setUseWideViewPort(true);
		browser.getSettings().setLoadWithOverviewMode(true);

		// enable all plugins (flash)
		browser.getSettings().setPluginState(PluginState.ON);
	}

}

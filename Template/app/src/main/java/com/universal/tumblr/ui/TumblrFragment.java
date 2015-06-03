package com.universal.tumblr.ui;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.universal.Helper;
import com.universal.MainActivity;
import com.universal.R;
import com.universal.tumblr.ImageAdapter;
import com.universal.tumblr.JSONParser;
import com.universal.tumblr.Constants.Extra;
import com.universal.tumblr.TumblrItem;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 *  This activity is used to display a list of tumblr imagess
 */

public class TumblrFragment extends Fragment {
	
	ArrayList<TumblrItem> tumblrItems;
	private ImageAdapter imageAdapter = null;
	
	Activity mAct;
	
	private GridView listView;
	private LinearLayout footerView;
	private LinearLayout ll;
	
	RelativeLayout pDialog;

	public static DisplayImageOptions options;
	
	String perpage = "25";
	Integer curpage = 0;
	Integer total_posts;
	
	String baseurl;
	
	Boolean initialload = true;
	Boolean isLoading = true;
	
	protected ImageLoader imageLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ll = (LinearLayout) inflater.inflate(R.layout.fragment_tumblr, container, false);
		setHasOptionsMenu(true);
		imageLoader = Helper.initializeImageLoader(mAct);
		
		String username = this.getArguments().getString(MainActivity.DATA);;
		baseurl = "http://"+username+".tumblr.com/api/read/json?type=photo&num=" + perpage + "&start=";
			    
		if ((getResources().getString(R.string.ad_visibility).equals("0"))){
		      // Look up the AdView as a resource and load a request.
		      AdView adView = (AdView) ll.findViewById(R.id.adView);
		      AdRequest adRequest = new AdRequest.Builder().build();
		      adView.loadAd(adRequest);
		}
		
		footerView = (LinearLayout) ll.findViewById(R.id.loading);

		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.ic_menu_gallery)
			.showImageOnFail(R.drawable.ic_error)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

		listView = (GridView) ll.findViewById(R.id.gridview);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startImagePagerActivity(position);
			}
		});
		
		listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
			        int visibleItemCount, int totalItemCount) {

			    if (imageAdapter == null)
			        return ;

			    if (imageAdapter.getCount() == 0)
			        return ;

			    int l = visibleItemCount + firstVisibleItem;
			    if (l >= totalItemCount && !isLoading && (curpage * Integer.parseInt(perpage)) <= total_posts) {
			        // It is time to add new data. We call the listener
			        isLoading = true;
			        new InitialLoadGridView().execute(baseurl);
			    }
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
		});
		return ll;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		
        new InitialLoadGridView().execute(baseurl);
	}

	private void startImagePagerActivity(int position) {
		Intent intent = new Intent(mAct, TumblrPagerActivity.class);
		
		ArrayList<TumblrItem> underlying =  new ArrayList<TumblrItem>();
		for (int i = 0; i < imageAdapter.getCount(); i++)
		    underlying.add(imageAdapter.getItem(i));
		
		Bundle b = new Bundle();
		b.putParcelableArrayList(Extra.IMAGES, underlying);
		intent.putExtras(b);
		intent.putExtra(Extra.IMAGE_POSITION, position);
		startActivity(intent);
	}
	
	public void updateList() {	
		if (initialload){
		    imageAdapter = new ImageAdapter(mAct, 0, tumblrItems);
		    listView.setAdapter(imageAdapter);
		    initialload = false;
		} else {
		    imageAdapter.addAll(tumblrItems);
		    imageAdapter.notifyDataSetChanged();
		}
		isLoading = false;
	}
	
	private class InitialLoadGridView extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			// Showing progress dialog before sending http request
			if (initialload){
				pDialog = (RelativeLayout) ll.findViewById(R.id.progressBarHolder);
			} else {
				footerView.setVisibility(View.VISIBLE);
			}
		}

		protected Void doInBackground(String... params) {
			String geturl = params[0];
			geturl = geturl + Integer.toString((curpage) *  Integer.parseInt(perpage));
            curpage = curpage + 1;
            
			JSONParser jParser = new JSONParser();
			// getting JSON string from URL
			Log.v("INFO", geturl);
			JSONObject json = jParser.makeHttpRequest(geturl, "GET");
			
			ArrayList<TumblrItem> images = new ArrayList<TumblrItem>();

			try {
				// Checking for SUCCESS TAG
				String success = json.getString("posts-total");
				total_posts = Integer.parseInt(success);

				if (0 < Integer.parseInt(success)) {
					// products found
					// Getting Array of Products
					JSONArray products;
					
					products = json.getJSONArray("posts");

					// looping through All Products
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString("id");
						String link = c.getString("url");
						String url;
						try {
						   url = c.getString("photo-url-1280");					
						} catch (JSONException e){
						   try {
								url = c.getString("photo-url-500");					
						   } catch (JSONException r){
							   try {
									url = c.getString("photo-url-250");					
							   } catch (JSONException l){
										url = null;
							   }
						   }
						}

						// creating new HashMap
						//HashMap<String, String> map = new HashMap<String, String>();

						// adding items to arraylist
						if (url != null){
							TumblrItem item = new TumblrItem(id, link, url);
							images.add(item);
						}
					}
					
					tumblrItems = images;
				} else {
					Log.v("INFO", "No items found");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
			return (null);
		}
		
		
		protected void onPostExecute(Void unused) {
			if (null != tumblrItems) {
				updateList();
			} else {
				Helper.noConnection(mAct, true);
			}
			if (pDialog.getVisibility() == View.VISIBLE) {
				pDialog.setVisibility(View.GONE);
				Helper.revealView(listView,ll);
			} else {
				footerView.setVisibility(View.GONE);
			}
		}
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.refresh_menu, menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case R.id.refresh:
        	if (!isLoading){
        		initialload = true;
	    		isLoading = true;
	    		curpage = 1;
	    		tumblrItems.clear();
	    		listView.setAdapter(null);
	    		new InitialLoadGridView().execute(baseurl);
	    	} else {
	    		Toast.makeText(mAct, getString(R.string.already_loading), Toast.LENGTH_LONG).show();
	    	}
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
}
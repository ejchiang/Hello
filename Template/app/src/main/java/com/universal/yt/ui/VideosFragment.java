package com.universal.yt.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnAttachStateChangeListener;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.support.v7.widget.SearchView;
import android.widget.Toast;
import android.support.v7.widget.SearchView.OnQueryTextListener;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


import com.universal.Helper;
import com.universal.MainActivity;
import com.universal.R;
import com.universal.yt.RetrieveVideos;
import com.universal.yt.ReturnItem;
import com.universal.yt.Video;
import com.universal.yt.VideosAdapter;

/**
 * This activity is used to display a list of vidoes
 */
public class VideosFragment extends Fragment {
    // A reference to our list that will hold the video details
	private ListView listView;
	private View footerView;
	
	private LinearLayout ll;
	public RelativeLayout pDialog;
	private static Context context;
	private Activity mAct;
    ArrayList<Video> videoList;
    VideosAdapter adapter;
	
	String perpage = "15";
	String pagetoken;
	
	String baseurl;
	String searchurl;
	
	Boolean initialload = true;
	Boolean isLoading = true;
	
	public String GOOGLE_API_KEY;
	
    @SuppressLint("InflateParams")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ll = (LinearLayout) inflater.inflate(R.layout.fragment_youtube, container, false);
		setHasOptionsMenu(true);
		
        GOOGLE_API_KEY = getResources().getString(R.string.google_server_key);

        //checking if the user has just opened the app
		footerView = inflater.inflate(R.layout.listview_footer, null);
        listView = (ListView) ll.findViewById(R.id.videosListView);
        listView.setOnItemClickListener(new OnItemClickListener(){
        	@Override
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
        		Object o = listView.getItemAtPosition(position);
        		Video video = (Video) o;
        		Intent intent = new Intent(mAct, VideoDetailActivity.class);
        		Bundle bundle = new Bundle();
        		bundle.putString("keyTitle", video.getTitle());
        		bundle.putString("keyDescription", video.getDescription());
        		bundle.putString("keyImage", video.getImage());
        		bundle.putString("keyId", video.getId());
        		bundle.putString("keyDate", video.getUpdated());
        		intent.putExtras(bundle);
    		    startActivity(intent);
        	}
    	});  
        listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
			        int visibleItemCount, int totalItemCount) {

			    if (listView == null)
			        return ;

			    if (listView.getCount() == 0)
			        return ;

			    int l = visibleItemCount + firstVisibleItem;
			    if (l >= totalItemCount && !isLoading) {
			        // It is time to add new data. We call the listener
			    	if (null != pagetoken){
			    		isLoading = true;
			    		Log.v("INFO", "Getting more items");
			    		new DownloadFilesTask().execute(baseurl);
			    	}
			    } 
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
		});
                
        if ((getResources().getString(R.string.ad_visibility).equals("0"))){
    	        	// Look up the AdView as a resource and load a request.
    	        	AdView adView = (AdView) ll.findViewById(R.id.adView);
    	        	AdRequest adRequest = new AdRequest.Builder().build();
    	        	adView.loadAd(adRequest);
    	}    
        return ll;

    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
        mAct = getActivity();

        //Load the youtube videos
        getUserYouTubeFeed();
        
        //Declaring a globally accesible context (toasting from error)
        VideosFragment.context = mAct.getApplicationContext();
    }
      
    public void getUserYouTubeFeed(){ 	
    	String username = getPassedData()[0];
    			
    	//start video retrieval process     
    	baseurl = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" +username+ "&maxResults=" + perpage + "&key=" + GOOGLE_API_KEY;
    	new DownloadFilesTask().execute(baseurl);
    }
    
    public void getSearchYouTubeFeed(String query){ 	
    	String username = getPassedData()[1];
    	
    	//start video retrieval process        
    	try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	baseurl = ("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&channelId="+username+"&q="+query+"&maxResults="+perpage+ "&key=" + GOOGLE_API_KEY);
    	new DownloadFilesTask().execute(baseurl);
    }
    
    private String[] getPassedData(){
    	String username = this.getArguments().getString(MainActivity.DATA);
    	
    	String[] parts = username.split(",");
    	return parts;
    }
    
	public void updateList() {	
		if (initialload){
			 adapter = new VideosAdapter(mAct.getApplicationContext(), 0, videoList, listView);
		     listView.setAdapter(adapter);
		     initialload = false;
		} else {
			 adapter.addAll(videoList);
			 adapter.notifyDataSetChanged();
		}
		isLoading = false;

	}
    
    private class DownloadFilesTask extends AsyncTask<String, Integer, Void> {

		@Override
		protected void onPreExecute() {
			if (initialload){
				pDialog = (RelativeLayout) ll.findViewById(R.id.progressBarHolder);
				if (pDialog.getVisibility() == View.GONE) {
					pDialog.setVisibility(View.VISIBLE);
					listView.setVisibility(View.GONE);
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					listView.addFooterView(footerView);
				}
			} else {
				listView.addFooterView(footerView);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
			if (null != videoList && !videoList.isEmpty()) {
				updateList();
			} else {
				Helper.noConnection(mAct, true);
			}
			if (pDialog.getVisibility() == View.VISIBLE) {
				pDialog.setVisibility(View.GONE);
				//feedListView.setVisibility(View.VISIBLE);
				Helper.revealView(listView,ll);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					listView.removeFooterView(footerView);
				}
			} else {
				listView.removeFooterView(footerView);
			}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			String url = params[0];
			if (null != pagetoken){
				url = url + "&pageToken=" + pagetoken;
			}
            
            Log.v("Info", url);
            ReturnItem item = RetrieveVideos.getVideos(url);
            videoList = item.getList();
            pagetoken = item.getPageToken();
			return null;
		}
	}
    
	public static Context getAppContext() {
        return VideosFragment.context;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.refresh_menu, menu);
    	
	    //set & get the search button in the actionbar 
		final SearchView searchView = new SearchView(mAct);

	    searchView.setQueryHint(getResources().getString(R.string.video_search_hint));
	    searchView.setOnQueryTextListener(new OnQueryTextListener() {
	        	
	    	@Override
	    	public boolean onQueryTextSubmit(String query) {
	    		isLoading = true;
	    		initialload = true;
	        	pagetoken = null;
	        	listView.setAdapter(null);
	        	adapter.clear();
	        	getSearchYouTubeFeed(query);
	        	searchView.clearFocus();
	            return true;
	       }

	       @Override
	       public boolean onQueryTextChange(String newText) {
	        	return false;
	       }

	    });
	        
    	String[] parts = getPassedData();
    	
    	searchView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View arg0) {
            	if (!isLoading){
    	    		initialload = true;
    	    		isLoading = true;
    	    		pagetoken = null;
    	    		videoList.clear();
    	    		listView.setAdapter(null);
    	    		getUserYouTubeFeed();
    	    	} 
            }

            @Override
            public void onViewAttachedToWindow(View arg0) {
                // search was opened
            }
        });
    	

	    if (parts.length == 2){
	         menu.add("search")
	         	.setIcon(R.drawable.ic_action_search)
	         	.setActionView(searchView)
	         	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	    }
	    //TODO make menu an xml item
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case R.id.refresh:
	    	if (!isLoading){
	    		initialload = true;
	    		isLoading = true;
	    		pagetoken = null;
	    		videoList.clear();
	    		listView.setAdapter(null);
	    		getUserYouTubeFeed();
	    	} else {
	    		Toast.makeText(mAct, getString(R.string.already_loading), Toast.LENGTH_LONG).show();
	    	}
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
}
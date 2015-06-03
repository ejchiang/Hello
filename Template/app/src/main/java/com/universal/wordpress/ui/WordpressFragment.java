package com.universal.wordpress.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.universal.Helper;
import com.universal.MainActivity;
import com.universal.R;
import com.universal.wordpress.CustomListAdapter;
import com.universal.wordpress.FeedItem;

/**
 *  This activity is used to display a list of wordpress articles
 */

public class WordpressFragment extends Fragment {

	private ArrayList<FeedItem> feedList = null;
	private ListView feedListView = null;
	private View footerView;
	private Activity mAct;
	private CustomListAdapter feedListAdapter = null;
	
	private LinearLayout ll;
	RelativeLayout pDialog;
	
	Integer pages;
	String perpage = "15";
	Integer curpage = 1;
	
	String apiurl;
	String baseurl;
	String searchurl;
	String searchurlend;
	String pageurl;
	
	Boolean isLoading = false;
	
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (LinearLayout) inflater.inflate(R.layout.fragment_wordpress_list, container, false);
		setHasOptionsMenu(true);
		
		apiurl = this.getArguments().getString(MainActivity.DATA);
		constructUrls();
					    
		if ((getResources().getString(R.string.ad_visibility).equals("0"))){
				      // Look up the AdView as a resource and load a request.
				      AdView adView = (AdView) ll.findViewById(R.id.adView);
				      AdRequest adRequest = new AdRequest.Builder().build();
				      adView.loadAd(adRequest);
		}
		
        footerView = inflater.inflate(R.layout.listview_footer, null);
		feedListView= (ListView) ll.findViewById(R.id.custom_list);
		feedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,	long id) {
				Object o = feedListView.getItemAtPosition(position);
				FeedItem newsData = (FeedItem) o;
				
				Intent intent = new Intent(mAct, WordpressDetailActivity.class);
				intent.putExtra("feed", newsData);
				intent.putExtra("apiurl", apiurl);
				startActivity(intent);
			}
		});
		feedListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
			        int visibleItemCount, int totalItemCount) {

			    if (feedListAdapter == null)
			        return ;

			    if (feedListAdapter.getCount() == 0)
			        return ;

			    int l = visibleItemCount + firstVisibleItem;
			    if (l >= totalItemCount && !isLoading && curpage <= pages) {
			        new DownloadFilesTask(baseurl, false).execute();
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

		new DownloadFilesTask(baseurl, true).execute();
	}
	
	public void constructUrls() {
		String[] parts = apiurl.split(",");

	    if (parts.length == 2){
			pageurl = parts[0] + "get_category_posts?category_slug="+ parts[1] +"&count=" + perpage + "&page=";
			apiurl = parts[0];
	    }else {	
	    	pageurl = apiurl + "get_recent_posts?exclude=comments,tags,categories,custom_fields&count=" + perpage + "&page=";
	    }
		baseurl = pageurl;
		
		searchurl = apiurl + "get_search_results?count=" + perpage + "&search=";
        searchurlend = "&page=";
	}

	public void updateList(boolean initialload) {	
		if (initialload){
			feedListAdapter = new CustomListAdapter(mAct, 0, feedList);
			feedListView.setAdapter(feedListAdapter);
		} else {
			feedListAdapter.addAll(feedList);
			feedListAdapter.notifyDataSetChanged();
		}
	}

	private class DownloadFilesTask extends AsyncTask<String, Integer, Void> {
		
		String url;
		boolean initialload;
		
		DownloadFilesTask(String url, boolean firstload){
			this.url = url;
			this.initialload = firstload;
		}
				
		@Override
		protected void onPreExecute() {
			if (isLoading){
				this.cancel(true);
			} else {
				isLoading = true;
			}
			if (initialload){
				pDialog = (RelativeLayout) ll.findViewById(R.id.progressBarHolder);
				
				if (pDialog.getVisibility() == View.GONE) {
					pDialog.setVisibility(View.VISIBLE);
					feedListView.setVisibility(View.GONE);
				}
				
				curpage = 1;
				
				if (null != feedList){
					feedList.clear();
				} 
				if (null != feedListView){
					feedListView.setAdapter(null);
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					feedListView.addFooterView(footerView);
				}
			} else {
				feedListView.addFooterView(footerView);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			if (null != feedList) {
				updateList(initialload);
			} else {
				Helper.noConnection(mAct, true);
			}
			if (pDialog.getVisibility() == View.VISIBLE) {
				pDialog.setVisibility(View.GONE);
				//feedListView.setVisibility(View.VISIBLE);
				Helper.revealView(feedListView,ll);
				
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					feedListView.removeFooterView(footerView);
				}
			} else {
				feedListView.removeFooterView(footerView);
			}
			isLoading = false;
		}

		@Override
		protected Void doInBackground(String... params) {
			//String url = params[0];
			url = url + Integer.toString(curpage);
            curpage = curpage + 1;
            
            Log.v("INFO", "Step 0, started");       
			// getting JSON string from URL
			JSONObject json = getJSONFromUrl(url);
			Log.v("INFO", "Step 2, got JsonObjoct");
			//parsing json data
			parseJson(json);
			return null;
		}
	}

	
	public JSONObject getJSONFromUrl(String url) {
		InputStream is = null;
		JSONObject jObj = null;
		String json = null;

		// Making HTTP request
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpPost = new HttpGet(url);
			httpPost.setHeader("Accept", "application/json"); // or application/jsonrequest
			httpPost.setHeader("Content-Type", "application/json");
			//httpPost.addHeader("accept", "application/json");
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
			Log.v("INFO", "Step 1, got Respons");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			jObj = new JSONObject(json);
		} catch (Exception e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;

	}

	public void parseJson(JSONObject json) {
		try {
			pages = json.getInt("pages");
			// parsing json object
			if (json.getString("status").equalsIgnoreCase("ok")) {
				JSONArray posts = json.getJSONArray("posts");

				feedList = new ArrayList<FeedItem>();

				for (int i = 0; i < posts.length(); i++) {
					Log.v("INFO", "Step 3: item " + i + " of " + posts.length());
					try {
						JSONObject post = (JSONObject) posts.getJSONObject(i);
						FeedItem item = new FeedItem();
						item.setTitle(Html.fromHtml(post.getString("title"))
								.toString());
						item.setDate(post.getString("date"));
						item.setId(post.getString("id"));
						item.setUrl(post.getString("url"));
						item.setContent(post.getString("content"));
						if (post.has("author")){
							Object author = post.get("author");
							if (author instanceof JSONArray && ((JSONArray) author).length() > 0){
								author = ((JSONArray) author).getJSONObject(0);
							}
							
							if (author instanceof JSONObject && ((JSONObject) author).has("name")) {
								item.setAuthor(((JSONObject) author).getString("name"));
							}
						}
						
						//TODO do we dear to remove catch clause?
						try {
							boolean thumbnailfound = false;

							if (post.has("thumbnail")) {
								String thumbnail = post.getString("thumbnail");
								if (thumbnail != "") {
									item.setThumbnailUrl(thumbnail);
									thumbnailfound = true;
								}
							}
							
							if (post.has("attachments")){

								JSONArray attachments = post
										.getJSONArray("attachments");

								//checking  how many attachments post has and grabbing the first one
								if (attachments.length() > 0) {
									JSONObject attachment = attachments
											.getJSONObject(0);
									
									item.setAttachmentUrl(attachment
										.getString("url"));
									
									//if we do not have a thumbnail yet, get one now
									if (attachment.has("images") && !thumbnailfound){
								
										JSONObject thumbnail;
										if (attachment.getJSONObject("images").has("post-thumbnail")){
											thumbnail = attachment.getJSONObject("images")
												.getJSONObject("post-thumbnail"); 
											
											item.setThumbnailUrl(thumbnail.getString("url"));
										} else if (attachment.getJSONObject("images").has("thumbnail")){
											thumbnail = attachment.getJSONObject("images")
												.getJSONObject("thumbnail");
											
											item.setThumbnailUrl(thumbnail.getString("url"));
										}
									
									}
								}
							}
							
						} catch (Exception e){
							Log.v("INFO", "Item " + i + " of " + posts.length() + " will have no thumbnail or image because of exception!");
							e.printStackTrace();
						}

						feedList.add(item);
					} catch (Exception e) {
						Log.v("INFO", "Item " + i + " of " + posts.length() + " has been skipped due to exception!");
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    	inflater.inflate(R.menu.refresh_menu, menu);
	        
	    	//set & get the search button in the actionbar 
	        final SearchView searchView = new SearchView(mAct);

	        searchView.setQueryHint(getResources().getString(R.string.video_search_hint));
	        searchView.setOnQueryTextListener(new OnQueryTextListener() {
	        //	
	        	@Override
	        	public boolean onQueryTextSubmit(String query) {
	        		try {
						query = URLEncoder.encode(query, "UTF-8");
				    } catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
	        		searchView.clearFocus();

	        		baseurl = searchurl + query + searchurlend;
	        		new DownloadFilesTask(baseurl, true).execute();
	                return true;
	        	}

	        	@Override
	        	public boolean onQueryTextChange(String newText) {
	        		return false;
	        	}

	        });
	
	        
	        searchView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

	            @Override
	            public void onViewDetachedFromWindow(View arg0) {
	            	if (!isLoading){
	    	    		baseurl = pageurl;
	    	    		new DownloadFilesTask(baseurl, true).execute();
	            	}
	            }

	            @Override
	            public void onViewAttachedToWindow(View arg0) {
	                // search was opened
	            }
	        });
	        
	        //TODO make menu an xml item
	        menu.add("search")
	         	.setIcon(R.drawable.ic_action_search)
	         	.setActionView(searchView)
	         	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	  
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case R.id.refresh:
	    	if (!isLoading){
	    		baseurl = pageurl;
	    		new DownloadFilesTask(baseurl, true).execute();
	    	} else {
	    		Toast.makeText(mAct, getString(R.string.already_loading), Toast.LENGTH_LONG).show();
	    	}
        default:
            return super.onOptionsItemSelected(item);
        }
    }

}

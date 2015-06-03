package com.universal.rss.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.graphics.Bitmap;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.universal.Helper;
import com.universal.MainActivity;
import com.universal.R;
import com.universal.rss.RSSFeed;
import com.universal.rss.RSSHandler;
import com.universal.rss.RSSItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 *  This activity is used to display a list of rss items
 */

public class RssFragment extends Fragment {
	
	private RSSFeed myRssFeed = null;
	private Activity mAct;
	private LinearLayout ll;
	private RelativeLayout pDialog;	
	
	public class MyCustomAdapter extends ArrayAdapter<RSSItem> {

		 public MyCustomAdapter(Context context, int textViewResourceId,
				 List<RSSItem> list) {
			 	super(context, textViewResourceId, list);
		 }

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) { 
			View row = convertView;
		  
			final ViewHolder holder;
		 
			if(row==null){
		      
				LayoutInflater inflater=mAct.getLayoutInflater();
				row=inflater.inflate(R.layout.fragment_rss_row, null);
		   
				holder = new ViewHolder();
		      
				holder.listTitle=(TextView)row.findViewById(R.id.listtitle);
				holder.listPubdate=(TextView)row.findViewById(R.id.listpubdate);
				holder.listDescription=(TextView)row.findViewById(R.id.shortdescription);
				holder.listThumb =(ImageView)row.findViewById(R.id.listthumb);
			  	
				row.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
		 
			holder.listTitle.setText(myRssFeed.getList().get(position).getTitle());	  
			holder.listPubdate.setText(myRssFeed.getList().get(position).getPubdate());
		  
			String html = myRssFeed.getList().get(position).getRowDescription();
		  
			holder.listDescription.setText(html);
			
			holder.listThumb.setImageDrawable(null);
		 
			//get Imageloader
			ImageLoader imageLoader = Helper.initializeImageLoader(mAct);
		 
			//LayoutInflater inflater = (LayoutInflater) mAct
			//	    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//View v = inflater.inflate(R.layout.fragment_rss, null);
		
		    //ListView listView = (ListView) v.findViewById(R.id.rsslist);
		 
		    //pausing on scrolling the listview
			//boolean pauseOnScroll = true; // or true
			//boolean pauseOnFling = true; // or false
			//PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling);
			//listView.setOnScrollListener(listener);
					
			String thumburl = myRssFeed.getList().get(position).getThumburl();
			if (thumburl != "" && thumburl != null){
				//setting the image
				imageLoader.displayImage(myRssFeed.getList().get(position).getThumburl(), holder.listThumb, new SimpleImageLoadingListener() {
			        @Override
			        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			        }

			        @Override
			        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

			            if (10 > loadedImage.getWidth() || 10 > loadedImage.getHeight()) {
			                // handle scaling
				            holder.listThumb.setVisibility(View.GONE);
			            } else {
			            	holder.listThumb.setVisibility(View.VISIBLE);
			            }

			        }
			    });
			} else {
				holder.listThumb.setVisibility(View.GONE);
			}
		 
			return row;
		 }
		 
	}
	
	static class ViewHolder {
		  TextView listTitle;
		  TextView listPubdate;
		  TextView listDescription;
		  ImageView listThumb;
		  int position;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ll = (LinearLayout) inflater.inflate(R.layout.fragment_rss, container, false);
		setHasOptionsMenu(true);
        
	    if ((getResources().getString(R.string.ad_visibility).equals("0"))){
        	// Look up the AdView as a resource and load a request.
        	AdView adView = (AdView) ll.findViewById(R.id.adView);
        	AdRequest adRequest = new AdRequest.Builder().build();
        	adView.loadAd(adRequest);
        }
	    return ll;
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		Log.v("INFO", "onAttach() called");
		new MyTask().execute();
	}
	
	
	private class MyTask extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onPreExecute(){
			pDialog = (RelativeLayout) ll.findViewById(R.id.progressBarHolder);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				String weburl = RssFragment.this.getArguments().getString(MainActivity.DATA);
				URL rssUrl = new URL(weburl);
				SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
				SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
				XMLReader myXMLReader = mySAXParser.getXMLReader();
				RSSHandler myRSSHandler = new RSSHandler();
				myXMLReader.setContentHandler(myRSSHandler);
				InputSource myInputSource = new InputSource(rssUrl.openStream());
				myXMLReader.parse(myInputSource);
				
				myRssFeed = myRSSHandler.getFeed();

			} catch (MalformedURLException e) {
				e.printStackTrace();				
			} catch (ParserConfigurationException e) {
				e.printStackTrace();	
			} catch (SAXException e) {
				e.printStackTrace();			
			} catch (IOException e) {
				e.printStackTrace();	
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ListView listview = (ListView) ll.findViewById(R.id.rsslist);
			
			if (myRssFeed != null) {
				MyCustomAdapter adapter = new MyCustomAdapter(mAct,
						R.layout.fragment_rss_row, myRssFeed.getList());
				listview.setAdapter(adapter);

				listview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View v,
							int position, long id) {
						Intent intent = new Intent(mAct,
								RssDetailActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("keyTitle", myRssFeed
								.getItem(position).getTitle());
						bundle.putString("keyDescription",
								myRssFeed.getItem(position).getDescription());
						bundle.putString("keyLink", myRssFeed.getItem(position)
								.getLink());
						bundle.putString("keyPubdate",
								myRssFeed.getItem(position).getPubdate());
						intent.putExtras(bundle);
						startActivity(intent);

					}
				});

			} else {
				Helper.noConnection(mAct, true);
			}

			if (pDialog.getVisibility() == View.VISIBLE) {
				pDialog.setVisibility(View.GONE);
				//feedListView.setVisibility(View.VISIBLE);
				Helper.revealView(listview,ll);
			}
			super.onPostExecute(result);
		}

	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.rss_menu, menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case R.id.refresh_rss:
	    	new MyTask().execute();
	    	return true;
        case R.id.info:    
        	//show information about the feed in general in a dialog 
            if (myRssFeed!=null)
			{
				String FeedTitle = (myRssFeed.getTitle());
				String FeedDescription = (myRssFeed.getDescription());
				//String FeedPubdate = (myRssFeed.getPubdate()); most times not present
				String FeedLink = (myRssFeed.getLink());
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
				
				String titlevalue = getResources().getString(R.string.feed_title_value);
				String descriptionvalue = getResources().getString(R.string.feed_description_value);
				String linkvalue = getResources().getString(R.string.feed_link_value);
				
				if (FeedLink.equals("")){
	                 builder.setMessage(titlevalue+": \n"+FeedTitle+
      		               "\n\n"+descriptionvalue+": \n"+FeedDescription);
				} else {
					 builder.setMessage(titlevalue+": \n"+FeedTitle+
          		           "\n\n"+descriptionvalue+": \n"+FeedDescription +
          		           "\n\n"+linkvalue+": \n"+FeedLink);
				};
				
	                 builder.setNegativeButton(getResources().getString(R.string.ok),null)
	                 .setCancelable(true);
	            builder.create();
	            builder.show();
				
			}else{
				
			}     
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
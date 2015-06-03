package com.universal.tumblr.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.universal.Helper;
import com.universal.R;
import com.universal.tumblr.Constants.Extra;
import com.universal.tumblr.TumblrItem;

/**
 *  This activity is used to show a swipable viewpager of the selected tumblr items
 *
 *  Contains code from: Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */

public class TumblrPagerActivity extends Activity {

	private static final String STATE_POSITION = "STATE_POSITION";

	DisplayImageOptions options;

	ViewPager imagePager;
	
	protected ImageLoader imageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tumblr_pager);

		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		ArrayList<TumblrItem> tumblrItems = bundle.getParcelableArrayList(Extra.IMAGES);
		int pagerPosition = bundle.getInt(Extra.IMAGE_POSITION, 0);

		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}
		
		imageLoader = Helper.initializeImageLoader(TumblrPagerActivity.this);

		options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_menu_gallery)
			.showImageOnFail(R.drawable.ic_error)
			.resetViewBeforeLoading(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();

		imagePager = (ViewPager) findViewById(R.id.pager);
		if (null != tumblrItems){
			imagePager.setAdapter(new ImagePagerAdapter(tumblrItems));
			imagePager.setCurrentItem(pagerPosition);
		}
		
	    if ((getResources().getString(R.string.ad_visibility).equals("0"))){
	        	// Look up the AdView as a resource and load a request.
	        	AdView adView = (AdView) findViewById(R.id.adView);
	        	AdRequest adRequest = new AdRequest.Builder().build();
	        	adView.loadAd(adRequest);
	    }
    
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, imagePager.getCurrentItem());
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private ArrayList<TumblrItem> images;
		private LayoutInflater inflater;

		ImagePagerAdapter(ArrayList<TumblrItem> images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			View imageLayout = inflater.inflate(R.layout.activity_tumblr_pager_image, view, false);
			assert imageLayout != null;
			final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			
			final Button btnShare = (Button) imageLayout.findViewById(R.id.btnShare);
			Button btnSet = (Button) imageLayout.findViewById(R.id.btnSet);
			final Button btnSave = (Button) imageLayout.findViewById(R.id.btnSave);
			
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

			imageLoader.displayImage(images.get(position).getUrl(), imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					Toast.makeText(TumblrPagerActivity.this, "Something went wrong: " + failReason.getType() + " with: " + imageUri, Toast.LENGTH_SHORT).show();
					Log.e("INFO", failReason.getType() + " with: " + imageUri);
					spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) {
					spinner.setVisibility(View.GONE);
					// close button click event
			        btnSave.setOnClickListener(new View.OnClickListener() {			
						@Override
						public void onClick(View v) {
							String path = Environment.getExternalStorageDirectory().toString();
							OutputStream fOut = null;
							File file = new File(path, "tumblr_"+images.get(position).getId()+".jpg");
							try {
								fOut = new FileOutputStream(file);
							Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
							bitmap.compress(Bitmap.CompressFormat.JPEG, 99, fOut);
							fOut.flush();
							fOut.close();

							MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
							
							String saved = getResources().getString(R.string.saved);
							Toast.makeText(getBaseContext(), saved + " " + file.toString(), Toast.LENGTH_LONG).show();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}); 
			        
			        // close button click event
			        btnShare.setOnClickListener(new View.OnClickListener() {			
						@Override
						public void onClick(View v) {
					    	String appvalue = getResources().getString(R.string.tumblr_share_begin);
					    	String applicationName = getResources().getString(R.string.app_name);
					    	   
							Intent shareIntent = new Intent();
							shareIntent.setType("text/plain");
							shareIntent.setAction(Intent.ACTION_SEND);
							shareIntent.putExtra(Intent.EXTRA_TEXT, appvalue + " " + applicationName + ": " + images.get(position).getLink());
							TumblrPagerActivity.this.startActivity(Intent.createChooser(shareIntent, "Share"));
						}
					}); 
				}
			});
			
	        
	        btnSet.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(TumblrPagerActivity.this);
		    		builder.setMessage("Are you sure you would like to set this image as wallpaper")
		    		   .setCancelable(true)
		    		   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		       public void onClick(DialogInterface dialog, int id) {
		    		    	   try {
		    		    	           Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
										WallpaperManager myWallpaperManager = WallpaperManager.getInstance(TumblrPagerActivity.this);
										myWallpaperManager.setBitmap(bitmap);
										Toast.makeText(TumblrPagerActivity.this, "You've set a new Wallpaper!", Toast.LENGTH_SHORT).show();
								} catch (IOException e) {
										e.printStackTrace();
										Log.v("ERROR", "Wallpaper not set");
								}  
									
		    		       }
		    		   });
		    		AlertDialog alert = builder.create();   
		    		alert.show();
				}
			}); 

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
}
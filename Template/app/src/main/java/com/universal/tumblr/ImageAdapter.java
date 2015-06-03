package com.universal.tumblr;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.universal.Helper;
import com.universal.R;
import com.universal.tumblr.ui.TumblrFragment;

public class ImageAdapter extends ArrayAdapter<TumblrItem> {

	private ArrayList<TumblrItem> listData;
	private LayoutInflater layoutInflater;
	private Context mContext;
	
	public ImageAdapter(Context context, Integer something, ArrayList<TumblrItem> listData) {
		super(context, something, listData);
		this.listData = listData;
		layoutInflater = LayoutInflater.from(context);
		mContext = context;
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public TumblrItem getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View view = convertView;
		if (view == null) {
			view = layoutInflater.inflate(R.layout.fragment_tumblr_row, parent, false);
			holder = new ViewHolder();
			assert view != null;
			holder.imageView = (ImageView) view.findViewById(R.id.image);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		ImageLoader imageLoader = Helper.initializeImageLoader(mContext);

		imageLoader.displayImage(listData.get(position).getUrl(), holder.imageView, TumblrFragment.options, new SimpleImageLoadingListener() {
									 @Override
									 public void onLoadingStarted(String imageUri, View view) {
										 holder.progressBar.setProgress(0);
										 holder.progressBar.setVisibility(View.VISIBLE);
									 }

									 @Override
									 public void onLoadingFailed(String imageUri, View view,
											 FailReason failReason) {
										 holder.progressBar.setVisibility(View.GONE);
									 }

									 @Override
									 public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
										 holder.progressBar.setVisibility(View.GONE);
									 }
								 }, new ImageLoadingProgressListener() {
									 @Override
									 public void onProgressUpdate(String imageUri, View view, int current,
											 int total) {
										 holder.progressBar.setProgress(Math.round(100.0f * current / total));
									 }
								 }
		);

		return view;
	}

	static class ViewHolder {
		ImageView imageView;
		ProgressBar progressBar;
	}
}

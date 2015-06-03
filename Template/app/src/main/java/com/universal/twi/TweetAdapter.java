package com.universal.twi;

/**
 *  This class is used to create an adapter of the tweets, and fill the listview
 */

import java.util.ArrayList;

import com.universal.R;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class TweetAdapter  extends ArrayAdapter<Tweet> {

	private Context context;
	private ArrayList<Tweet> tweets;

	public TweetAdapter(Context context, int viewResourceId, ArrayList<Tweet> tweets) {
		super(context, viewResourceId, tweets);
		this.context = context;
		this.tweets = tweets;
	}
	
	@Override
	public View getView(int posicao, View view, ViewGroup parent){

		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.fragment_tweets_row, parent, false);
		}

		Tweet tweet = tweets.get(posicao);

		if (tweet != null) {

			TextView name = (TextView) view.findViewById(R.id.name);
			TextView username = (TextView) view.findViewById(R.id.username);
			ImageView imagem = (ImageView) view.findViewById(R.id.profile_image);
			TextView message = (TextView) view.findViewById(R.id.message);
			TextView data = (TextView) view.findViewById(R.id.data);

			name.setText(tweet.getname());
			username.setText("@" + tweet.getusername());
			username.setTag(tweet.getusername());
			BitmapManager.getInstance().loadBitmap(tweet.geturlProfileImage(), imagem);
			message.setText(Html.fromHtml(tweet.getmessage()));
			message.setTag(tweet.getTweetId());
			data.setText(tweet.getData());
		}

		return view;
	}
}


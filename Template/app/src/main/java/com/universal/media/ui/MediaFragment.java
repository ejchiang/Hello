package com.universal.media.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.StrictMode;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.universal.Helper;
import com.universal.MainActivity;
import com.universal.R;
import com.universal.media.MediaService;
import com.universal.media.UrlParser;
import com.universal.media.LineRenderer;
import com.universal.media.VisualizerView;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 *  This activity is used to listen to a radio station
 */

public class MediaFragment extends Fragment implements OnClickListener, MediaPlayer.OnPreparedListener {

    private ProgressBar playSeekBar;
    private Button buttonPlay;
    private Button buttonStopPlay;
    private VisualizerView mVisualizerView;
    
    Activity mAct;
    
    private LinearLayout ll;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	ll = (LinearLayout) inflater.inflate(R.layout.fragment_media, container, false);
        
        initializeUIElements();
        
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
		
		Helper.isOnline(mAct, true, true);
		
		mAct.startService(new Intent(mAct, MediaService.class));
		
		//TODO Move it nicely to a background thread (turn off to see where on main thread network connection is made).
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 		
		//
		
    }

    private void initializeUIElements() {
        playSeekBar = (ProgressBar) ll.findViewById(R.id.seekBar);
        playSeekBar.setMax(100);
        playSeekBar.setVisibility(View.INVISIBLE);

        buttonPlay = (Button) ll.findViewById(R.id.btn_play);
        buttonPlay.setOnClickListener(this);

        buttonStopPlay = (Button) ll.findViewById(R.id.btn_pause);
        buttonStopPlay.setOnClickListener(this);
        
        if (null != MediaService.get() && MediaService.get().isPlaying()){
        	buttonPlay.setEnabled(false);
        	buttonStopPlay.setEnabled(true);
        } else {
            buttonPlay.setEnabled(true);
            buttonStopPlay.setEnabled(false);
        }
        
        mVisualizerView = (VisualizerView) ll.findViewById(R.id.visualizerView);

    }

    public void onClick(View v) {
        if (v == buttonPlay) {
        		if (null == mVisualizerView.getVisualizer()){
        			mVisualizerView.link(MediaService.get());
        			addLineRenderer();
        			// set up listener
        			MediaService.get().setOnPreparedListener(this);
        		}
            
        		startPlaying();
        		AudioManager am = (AudioManager) mAct.getSystemService(Context.AUDIO_SERVICE);
        		int volume_level= am.getStreamVolume(AudioManager.STREAM_MUSIC);
            	if (volume_level < 2){
            		Toast.makeText(mAct, getResources().getString(R.string.volume_low),Toast.LENGTH_SHORT).show();
            	}
        } else if (v == buttonStopPlay) {
            stopPlaying();
        }
    }

    private void startPlaying() {
    	Log.v("INFO", "Start playing");
        buttonStopPlay.setEnabled(true);
        buttonPlay.setEnabled(false);

        playSeekBar.setVisibility(View.VISIBLE);
        
        String radio = MediaFragment.this.getArguments().getString(MainActivity.DATA);

        try {
			MediaService.get().setDataSource((UrlParser.getUrl(radio)));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        MediaService.get().prepareAsync();

    }

    private void stopPlaying() {
        MediaService.get().reset();
        mVisualizerView.setEnabled(false);

        buttonPlay.setEnabled(true);
        buttonStopPlay.setEnabled(false);
        playSeekBar.setVisibility(View.INVISIBLE);
    }
    
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        MediaService.get().start();
        playSeekBar.setVisibility(View.INVISIBLE);
    }

 
    private void addLineRenderer()
    {
      Paint linePaint = new Paint();
      linePaint.setStrokeWidth(1f);
      linePaint.setAntiAlias(true);
      linePaint.setColor(Color.argb(88, 0, 128, 255));

      Paint lineFlashPaint = new Paint();
      lineFlashPaint.setStrokeWidth(5f);
      lineFlashPaint.setAntiAlias(true);
      lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
      LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
      mVisualizerView.addRenderer(lineRenderer);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
                
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    
}
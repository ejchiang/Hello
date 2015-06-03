package com.universal.media;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MediaService extends Service implements MediaPlayer.OnPreparedListener {

    private static MediaPlayer player;
    private static int currentSong = -1;
    private static int currentRadio = 0;
    private static String currentAlbum = null;

    public MediaService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // if the mediaPlayer object is null, create it
        if(player == null)
            player = new MediaPlayer();
        // acquire partial wake lock to allow background playback
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        
        Log.v("INFO", "MediaService Started");
    }

    public static MediaPlayer get() {
        return player;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();
    }

    public static int getCurrentSong() {
        return currentSong;
    }

    public static void setCurrentSong(int s) {
        currentSong = s;
    }

    public static int getCurrentRadio() {
        return currentRadio;
    }

    public static void setCurrentRadio(int r) {
        currentRadio = r;
    }

    public static String getCurrentAlbum() {
        return currentAlbum;
    }

    public static void setCurrentAlbum(String currentAlbum) {
        MediaService.currentAlbum = currentAlbum;
    }

    // release mediaPlayer object on service destruction
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player != null){
            player.release();
            player = null;
        }
    }
}

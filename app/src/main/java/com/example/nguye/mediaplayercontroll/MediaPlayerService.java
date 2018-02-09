package com.example.nguye.mediaplayercontroll;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nguye on 21.01.2018.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener {
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    public static final String ANDROID_RESOURCE = "android.resource://";
    public static final String FORESLASH = "/";
    MediaPlayer mMediaPlayer = null;
    IBinder mBinder;
    int currentListPosition;
    int currentResId;
    ArrayList<String> songList;

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        //if (intent.getAction().equals(ACTION_PLAY)) {
            //mMediaPlayer.setOnPreparedListener(this);
            //mMediaPlayer.prepareAsync(); // prepare async to not block main thread
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.example.nguye.mediaplayercontroll.SONG");
            filter.addAction("com.example.nguye.mediaplayercontroll.PAUSE");
            filter.addAction("com.example.nguye.mediaplayercontroll.CONTINUE");
            filter.addAction("com.example.nguye.mediaplayercontroll.NEXT");
            filter.addAction("com.example.nguye.mediaplayercontroll.PREV");
            LocalBroadcastManager.getInstance(this).registerReceiver(new Receiver(), filter);
        //}

        return START_STICKY;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            if(arg1.getAction().equals("com.example.nguye.mediaplayercontroll.SONG")) {
                int resID = arg1.getIntExtra("resID", -1);
                songList = arg1.getStringArrayListExtra("songlist");
                currentListPosition = arg1.getIntExtra("position", 0);
                currentResId = resID;
                mMediaPlayer = MediaPlayer.create(MediaPlayerService.this, resID);
                mMediaPlayer.start();// initialize it here
            }

            if(arg1.getAction().equals("com.example.nguye.mediaplayercontroll.PAUSE")) {
                if(arg1.getStringExtra("action").equals("pause")) {
                    mMediaPlayer.pause();
                }
            }

            if(arg1.getAction().equals("com.example.nguye.mediaplayercontroll.CONTINUE")) {
                if(arg1.getStringExtra("action").equals("continue")) {
                    mMediaPlayer.start();
                }
            }

            if(arg1.getAction().equals("com.example.nguye.mediaplayercontroll.NEXT")) {
                if(arg1.getStringExtra("action").equals("next")) {
                    Log.i("position", Integer.toString(currentListPosition));
                    Log.i("songList", Integer.toString(songList.size()));
                    if(currentListPosition == songList.size()) {
                        currentListPosition = 0;
                    }
                    if(currentListPosition < songList.size()) {
                        mMediaPlayer.stop();
                        mMediaPlayer = null;
                        int resID = getResources().getIdentifier(songList.get(currentListPosition), "raw", getPackageName());
                        currentResId = resID;
                        currentListPosition++;
                        mMediaPlayer = MediaPlayer.create(MediaPlayerService.this, resID);
                        mMediaPlayer.start();
                    }
                }
            }

            if(arg1.getAction().equals("com.example.nguye.mediaplayercontroll.PREV")) {
                if(arg1.getStringExtra("action").equals("prev")) {
                    if(currentListPosition > 0) {
                        mMediaPlayer.stop();
                        mMediaPlayer = null;
                        int resID = getResources().getIdentifier(songList.get(currentListPosition), "raw", getPackageName());
                        currentResId = resID;
                        currentListPosition--;
                        mMediaPlayer = MediaPlayer.create(MediaPlayerService.this, resID);
                        mMediaPlayer.start();
                    }
                }
            }

            Intent intent = new Intent();
            intent.setAction("com.example.nguye.mediaplayercontroll.ABC");
            intent.putExtra("duration", mMediaPlayer.getDuration());
            LocalBroadcastManager.getInstance(MediaPlayerService.this).sendBroadcast(intent);
        }
    }
}

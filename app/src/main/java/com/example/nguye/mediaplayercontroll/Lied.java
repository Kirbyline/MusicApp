package com.example.nguye.mediaplayercontroll;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class Lied extends AppCompatActivity {

    Button playBtn;
    Button prevBtn;
    Button nextBtn;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    MediaPlayer mp;
    int totalTime;

    private String TAG = "MainActivity";

    private int REQUEST_ENABLE_BT = 3000;

    private BluetoothService bluetoothService = null;

    private boolean mIsBound;
    private boolean pause = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lied);

        Intent intent = new Intent(getApplicationContext(), BluetoothService.class);
        startService(intent);
        doBindService();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.nguye.mediaplayercontroll.ABC");
        filter.addAction("com.example.nguye.mediaplayercontroll.BLUETOOTH_NEXT");
        LocalBroadcastManager.getInstance(this).registerReceiver(new TimeReceiver(), filter);

        playBtn = (Button) findViewById(R.id.playBtn);
        prevBtn = (Button) findViewById(R.id.prevBtn);
        nextBtn = (Button) findViewById(R.id.nextBtn);

        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);

        // Media Player
        mp = MediaPlayer.create(this, R.raw.conan);
        mp.setLooping(true);
        mp.seekTo(0);
        mp.setVolume(0.5f, 0.5f);
        //totalTime = mp.getDuration();

        // Position Bar
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );


        // Volume Bar
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        mp.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        // Thread (Update positionBar & timeLabel)
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        timeHandler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            bluetoothService = ((BluetoothService.LocalBinder)iBinder).getInstance();
            bluetoothService.setHandler(controlHandler);
            Log.d("Lied.class", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            bluetoothService = null;
            Log.d("Lied.class", "onServiceDisconnected");
        }
    };

    private void doBindService()
    {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this,
                BluetoothService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService()
    {
        if (mIsBound)
        {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        doUnbindService();
        stopService(new Intent(this, BluetoothService.class));
    }

    private Handler controlHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String action = (String) msg.obj;
            Log.i("output", action);
            switch(action) {
                case "n":
                    nextSong();
                    break;
                case "p":
                    prevSong();
                    break;
                case "s":
                    if(pause) {
                        pauseSong();
                        pause = false;
                        playBtn.setBackgroundResource(R.drawable.play);
                    } else {
                        continueSong();
                        pause = true;
                        playBtn.setBackgroundResource(R.drawable.stop);
                    }
                    break;
                default:
                    break;


            }
        }
    };

    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            positionBar.setProgress(currentPosition);

            // Update Labels.
            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTime-currentPosition);
            remainingTimeLabel.setText("- " + remainingTime);
        }
    };

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    public void stopBtnClick(View view) {

        Button button = (Button) findViewById(R.id.playBtn);

        if(button.getBackground().getConstantState() == this.getDrawable(R.drawable.stop).getConstantState()) {
            pauseSong();
            playBtn.setBackgroundResource(R.drawable.play);
        } else {
            continueSong();
            playBtn.setBackgroundResource(R.drawable.stop);
        }
    }

    public void nextBtnClick(View view) {
        nextSong();
    }

    public void prevBtnClick(View view) {
        prevSong();
    }

    public void nextSong() {
        Intent intent = new Intent();
        intent.setAction("com.example.nguye.mediaplayercontroll.NEXT");
        intent.putExtra("action", "next");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void prevSong() {
        Intent intent = new Intent();
        intent.setAction("com.example.nguye.mediaplayercontroll.PREV");
        intent.putExtra("action", "prev");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void pauseSong() {
        Intent intent = new Intent();
        intent.setAction("com.example.nguye.mediaplayercontroll.PAUSE");
        intent.putExtra("action", "pause");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void continueSong() {
        Intent intent = new Intent();
        intent.setAction("com.example.nguye.mediaplayercontroll.CONTINUE");
        intent.putExtra("action", "continue");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            totalTime = arg1.getIntExtra("duration", -1);
            Log.d("test", Integer.toString(totalTime));

            if(arg1.getAction().equals("com.example.nguye.mediaplayercontroll.BLUETOOTH_NEXT")) {
                if(arg1.getStringExtra("action").equals("next")) {
                    Log.d("Lied.class", "next");
                }
            }
        }
    }

    /*public void nextBtn.setOnItemClickListener(new View.OnClickListener() {

            int  currentSongIndex= 0;
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                if(currentSongIndex < (ListView.size() - 1)){
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                }else{
                    // play first song
                    mp.start(i);
                }

            }
        });*/

    }

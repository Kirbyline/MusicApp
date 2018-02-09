package com.example.nguye.mediaplayercontroll;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> list;
    ListAdapter adapter;
    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        list = new ArrayList<>();

        Field[] fields = R.raw.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            list.add(fields[i].getName());
        }

        list.remove(0);
        list.remove(fields.length-2);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        startService(intent);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                int resID = getResources().getIdentifier(list.get(i), "raw", getPackageName());
                //Log.d("test", Integer.toString(resID));
                //mp = MediaPlayer.create(MainActivity.this, resID);

                Intent intent = new Intent();
                intent.setAction("com.example.nguye.mediaplayercontroll.SONG");
                intent.putExtra("resID", resID);
                intent.putStringArrayListExtra("songlist", list);
                intent.putExtra("position", i);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);

                Intent myIntent = new Intent(view.getContext(), Lied.class);
                startActivity(myIntent);

                /*if (mp != null){
                    mp.release();
                }

                int resID = getResources().getIdentifier(list.get(i), "raw", getPackageName());
                mp = MediaPlayer.create(MainActivity.this, resID);
                mp.start();
            }*/
                    //mp.start();

                ;
                /*if (mp==R.raw.haruharu){
                    Intent myIntent = new Intent(view.getContext(), Lied.class);
                    startActivityForResult(myIntent, 0);
                }
                if (mp==R.raw.byul){
                    Intent myIntent = new Intent(view.getContext(), Lied.class);
                    startActivityForResult(myIntent, 0);
                }*/
            }

        });
    }
}
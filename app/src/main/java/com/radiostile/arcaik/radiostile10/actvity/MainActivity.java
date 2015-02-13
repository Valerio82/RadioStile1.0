package com.radiostile.arcaik.radiostile10.actvity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.radiostile.arcaik.radiostile10.R;
import com.radiostile.arcaik.radiostile10.service.MediaPlayerService;
import com.radiostile.arcaik.radiostile10.utility.IcyStreamMeta;
import com.radiostile.arcaik.radiostile10.utility.Singleton;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    private ImageButton buttonPlayPause;
    private TextView textViewSongTitle;
    private String datiCanzone = "";
    private boolean statusBottone = false;
    private boolean metaData = false;
    private Timer timer;
    private String RADIOSTILE_URL="http://178.32.138.88:8046/stream";
    private Intent intentStartMediaplayerService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface customFont=Typeface.createFromAsset(getAssets(),"font/lenka.ttf");
        textViewSongTitle = (TextView) findViewById(R.id.textViewSongTitle);
        textViewSongTitle.setTypeface(customFont);
        buttonPlayPause = (ImageButton) findViewById(R.id.imageButtonPlayPause);
        textViewSongTitle.setText(datiCanzone);
        intentStartMediaplayerService = new Intent(this, MediaPlayerService.class);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        Singleton.getInstance().setBoolean(statusBottone);
        if(metaData==true){
            timer.cancel();
            timer.purge();
        }
    }
    public void onResume() {
        super.onResume();
        statusBottone = Singleton.getInstance().getBoolean();
        if (!statusBottone) {
            buttonPlayPause.setImageResource(R.drawable.play);
        } else {
            buttonPlayPause.setImageResource(R.drawable.pause);
            getMeta();
        }
        final IntentFilter broadcastIntentFilter=new IntentFilter(MediaPlayerService.NOTIFICATION);
        registerReceiver(receiver, broadcastIntentFilter);
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        boolean serverOffline = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            serverOffline = bundle.getBoolean(MediaPlayerService.STATO_SERVER);
            if (serverOffline == true) {
                statusBottone = false;
                buttonPlayPause.setImageResource(R.drawable.play);
                Toast.makeText(getApplicationContext(), "SERVER OFFLINE", Toast.LENGTH_LONG).show();

            } else {
                statusBottone = true;
                buttonPlayPause.setImageResource(R.drawable.pause);
            }
        }
    };

    public void startService(View v) {
        if (!statusBottone) {
            metaData = true;
            getMeta();
            startService(intentStartMediaplayerService);
            statusBottone = true;
            buttonPlayPause.setImageResource(R.drawable.pause);
        } else {
            statusBottone = false;
            metaData = false;
            stopService(intentStartMediaplayerService);
            buttonPlayPause.setImageResource(R.drawable.play);
        }
    }
    private void getMeta() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    IcyStreamMeta icy = new IcyStreamMeta(new URL(RADIOSTILE_URL));
                    datiCanzone = icy.getStreamTitle();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (metaData == true) {
                                textViewSongTitle.setText(datiCanzone);
                                textViewSongTitle.refreshDrawableState();
                            } else {
                                textViewSongTitle.setText("");

                            }
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 7000);
    }


}

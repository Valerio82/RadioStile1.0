package com.radiostile.arcaik.radiostile10.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import java.io.IOException;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,AudioManager.OnAudioFocusChangeListener {
    public static final String NOTIFICATION="com.example.radiostile";
    public static final String STATO_SERVER="Stato server";
    private MediaPlayer lettoreRadio=null;
    private String RADIOSTILE_URL="http://178.32.138.88:8046/stream";
    private boolean serverOffline=false;
    private AudioManager audioManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        inizializzaLettoreRadio();
        return START_STICKY;

    }
    public void onPrepared(MediaPlayer mediaPlayer){
        audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        lettoreRadio.start();
        serverOffline=false;
    }

    @Override
    public void onDestroy(){
        lettoreRadio.release();
        audioManager.abandonAudioFocus(this);


    }
    public void lettoreRadioStop(){
        lettoreRadio.stop();
        lettoreRadio.release();
        stopSelf();

    }
    public void inizializzaLettoreRadio(){
        lettoreRadio=new MediaPlayer();
        lettoreRadio.setOnPreparedListener(this);
        lettoreRadio.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            lettoreRadio.setDataSource(getApplicationContext(), Uri.parse(RADIOSTILE_URL));
        } catch (IOException e) {
            e.printStackTrace();
        }
        lettoreRadio.prepareAsync();
        lettoreRadio.setOnErrorListener(this);
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_ERROR_IO:
                serverOffline=true;
                inviaRisultato();
                lettoreRadioStop();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                serverOffline=true;
                inviaRisultato();
                lettoreRadioStop();

                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                serverOffline=true;
                inviaRisultato();
                lettoreRadio.stop();
                lettoreRadio.release();
                stopSelf();
                break;
        }
        return true;
    }
    public void inviaRisultato(){
        Intent mIntent=new Intent(NOTIFICATION);
        mIntent.putExtra(STATO_SERVER,serverOffline);
        sendBroadcast(mIntent);
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                if(lettoreRadio==null){
                    inizializzaLettoreRadio();
                }else
                if(!lettoreRadio.isPlaying())
                    lettoreRadio.start();
                lettoreRadio.setVolume(1.0f,1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if(lettoreRadio.isPlaying()){
                    lettoreRadio.stop();
                }
                lettoreRadio.release();
                lettoreRadio=null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if(lettoreRadio.isPlaying()) {
                    lettoreRadio.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(lettoreRadio.isPlaying())
                    lettoreRadio.setVolume(0.1f,0.1f);
                break;
        }
    }
}

package com.classiqo.nativeandroid_32bitz;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private static final String ACTION_PLAY = "com.classiqo.nativeandroid_32bitz.PLAY";
    private String musicURL = null;
    public MediaPlayer mMediaPlayer = null;
    WifiManager.WifiLock wifiLock;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
//        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
        Toast.makeText(this, "Music Service가 중지되었습니다.", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy()");
        mMediaPlayer.stop();
        mMediaPlayer = null;
    }

//    public MusicService() {
//        String songName;
//
//        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
//                new Intent(getApplicationContext(), MainActivity.class),
//                PendingIntent.FLAG_UPDATE_CURRENT;
//
//        Notification.Builder builder = new Notification.Builder(context);
//        Notification notification = new Notification();
////        notification.tickerText = text;
////        notification.icon = R.drawable.play0;
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;
//        notification.setLatestEventInfo(getApplicationContext(), "MusicPlayerSample",
//                "Playing: " + songName, pi);
//        startForeground(NOTIFICATION_ID, notification);
//    }
//
//    public void initMediaPlayer() {
//        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                wifiLock.release();
//
//                return false;
//            }
//        });
//    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        String musicName = intent.getStringExtra("PUT_MUSICNAME");
        Log.d(TAG, musicName);

//        if (intent.getAction().equals(ACTION_PLAY)) {
//
//        }

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = new MediaPlayer();
        }

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        selectMusic(musicName);
        mMediaPlayer.prepareAsync();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared()");

        mp.start();

        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError()");
        return false;
    }

    private void selectMusic(String musicName) {
        musicURL = "https://s3.ap-northeast-2.amazonaws.com/project32bitz/" + musicName + ".mp3";
        Log.d(TAG, "selectMusic(), musicURL : " + musicURL);
        try {
            mMediaPlayer.setDataSource(musicURL);
        } catch (Exception e) {
            Log.e("StreamAudioDemo", e.getMessage());
        }
    }
}

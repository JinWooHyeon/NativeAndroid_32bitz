package com.classiqo.nativeandroid_32bitz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    static final String[] LIST_MENU = {
            "01 Magic",
            "01 - Don't Know Why",
            "01 - Riot",
            "10 - Well You Needn't",
            "04 John Legend  - All Of Me",
            "07 Lorde - Team",
            "09 OneRepublic - Counting Stars"
    };
    private static final int SNACKBAR_PLAY = 0;
    private static final int SNACKBAR_PAUSE = 1;

    private ListView lvPlayList;
    private SeekBar sbMusicProgress;
    private Button btRewind;
    private Button btPlay;
    private Button btStop;
    private Button btFastForward;
    private FloatingActionButton fab;
    private TextView tvCurrentTime;
    private TextView tvDurationTime;
    private ProgressDialog pd;
    private MediaPlayer mediaPlayer = null;
    private Intent intent = null;

    private String musicName = null;
    private String musicURL = null;
    private Boolean isPlaying = null;
    private Boolean isProgressing = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, LIST_MENU);

        lvPlayList = (ListView) findViewById(R.id.lvPlayList);
        lvPlayList.setAdapter(adapter);
        lvPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicName = (String) parent.getItemAtPosition(position);

                playMusic(view);
            }
        });

        tvCurrentTime = (TextView) findViewById(R.id.tvCurrentTime);
        tvDurationTime = (TextView) findViewById(R.id.tvDurationTime);

        sbMusicProgress = (SeekBar) findViewById(R.id.sbMusicProgress);
        sbMusicProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null) {
                    tvCurrentTime.setText(time.format(sbMusicProgress.getProgress()));
                    if (isPlaying == true) {
                        sbMusicProgress.getProgress();
                        mediaPlayer.start();
                    } else {
                        sbMusicProgress.getProgress();
                        mediaPlayer.pause();
                    }
                } else sbMusicProgress.setProgress(0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    isProgressing = true;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    isProgressing = false;
                    mediaPlayer.seekTo(sbMusicProgress.getProgress());
                    tvCurrentTime.setText(time.format(mediaPlayer.getCurrentPosition()));
                }
            }
        });

        btRewind = (Button) findViewById(R.id.btRewind);
        btRewind.setOnClickListener(this);

        btPlay = (Button) findViewById(R.id.btPlay);
        btPlay.setOnClickListener(this);

        btStop = (Button) findViewById(R.id.btStop);
        btStop.setOnClickListener(this);

        btFastForward = (Button) findViewById(R.id.btFastForward);
        btFastForward.setOnClickListener(this);
    }

    private Runnable timeThread = new Runnable() {
        @Override
        public void run() {
            try {
                if (mediaPlayer != null && isProgressing == false) {
                    tvCurrentTime.setText(time.format(mediaPlayer.getCurrentPosition()));
                    sbMusicProgress.setProgress(mediaPlayer.getCurrentPosition());
                    tvCurrentTime.postDelayed(timeThread, 500);
                }
            } catch (Exception e) {
                Log.e("timeThread", e.getMessage());
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btRewind:
                if (mediaPlayer != null && mediaPlayer.getCurrentPosition() >= 5000)
                    mediaPlayer.seekTo(0);
                break;
            case R.id.fab:
            case R.id.btPlay:
                playPause(v);
                break;
            case R.id.btStop:
//                stopService(intent);
                if (mediaPlayer != null) {
                    Handler mHandler = new Handler();
                    mHandler.removeCallbacks(timeThread);

                    isPlaying = false;
                    btPlay.setText("▶");
                    tvCurrentTime.setText("00:00");
                    tvDurationTime.setText("00:00");
                    sbMusicProgress.setProgress(0);

                    mediaPlayer.stop();
                    mediaPlayer = null;

                    Snackbar.make(v, "■ " + musicName, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    Snackbar.make(v, "Null", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                break;
            case R.id.btFastForward:
                break;
        }
    }

    private void setMusicURL(String musicName) {
        if (musicName.equals("01 Magic")) musicURL = getText(R.string.rootURL) + "leebuyeong/" + musicName + ".wav";
        else musicURL = getText(R.string.rootURL) + musicName + ".mp3";

        try {
            mediaPlayer.setDataSource(musicURL);
        } catch (Exception e) {
            Log.e("StreamAudioDemo", e.getMessage());
        }
    }

    private void playMusic(final View v) {
        pd = new ProgressDialog(v.getContext());
        pd.setMessage("Buffering " + musicName);
        pd.show();

//        intent = new Intent(MainActivity.this, MusicService.class);
//        intent.putExtra("PUT_MUSICNAME", musicName);
//        startService(intent);

        if (mediaPlayer != null) {
            Handler mHandler = new Handler();
            mHandler.removeCallbacks(timeThread);

            isPlaying = false;
            btPlay.setText("▶");
            tvCurrentTime.setText("00:00");
            tvDurationTime.setText("00:00");
            sbMusicProgress.setProgress(0);

            mediaPlayer.stop();
            mediaPlayer = new MediaPlayer();
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        setMusicURL(musicName);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPlaying = true;
                isProgressing = false;
                pd.dismiss();

                btPlay.setText("||");
                tvCurrentTime.postDelayed(timeThread, 500);
                tvDurationTime.setText(time.format(mediaPlayer.getDuration()));
                sbMusicProgress.setProgress(0);
                sbMusicProgress.setMax(mediaPlayer.getDuration());

                mp.start();
                loadSnackbar(SNACKBAR_PLAY, v);
            }
        });
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPlaying = false;
                pd.dismiss();

                mediaPlayer.release();
                mediaPlayer = null;

                return false;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlaying = false;

                mediaPlayer.release();
                mediaPlayer = null;

                Snackbar.make(v, "Complete", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    private void playPause(View v) {
        if (mediaPlayer == null)
            if (musicURL == null) Snackbar.make(v, "Null", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            else playMusic(v);
        else
            if (isPlaying == false) {
                isPlaying = true;
                btPlay.setText("||");
                tvCurrentTime.postDelayed(timeThread, 500);

                mediaPlayer.start();
                loadSnackbar(SNACKBAR_PLAY, v);
            } else {
                isPlaying = false;
                btPlay.setText("▶");

                mediaPlayer.pause();
                loadSnackbar(SNACKBAR_PAUSE, v);
            }
    }

    private void loadSnackbar(int snackbarId, View v) {
        Snackbar snackbar;
        View snackView;
        switch (snackbarId) {
            case SNACKBAR_PLAY:
                snackbar = Snackbar.make(v, musicName, Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(Color.parseColor("#FFFFFFFF"))
                        .setAction("||", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                playPause(v);
                            }
                        });
                snackView = snackbar.getView();
                snackView.setBackgroundColor(Color.parseColor("#715700"));
                snackbar.show();
                break;
            case SNACKBAR_PAUSE:
                snackbar = Snackbar.make(v, musicName, Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(Color.parseColor("#FFFFFFFF"))
                        .setAction("▶", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                playPause(v);
                            }
                        });
                snackView = snackbar.getView();
                snackView.setBackgroundColor(Color.parseColor("#715700"));
                snackbar.show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            isPlaying = false;

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
package com.example.pikanshu.cokestudio;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MusicPlayer extends AppCompatActivity implements View.OnTouchListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener {

    private final Handler handler = new Handler();
    ImageView button_stop, button_play;
    // seek bar for player
    private SeekBar seekBarProgress;
    private TextView songNameTextView;
    private TextView artistNameTextView;
    private Context mContext;
    private String AudioURL = "";
    private Song mSong;
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private boolean init = true;
    // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
    private int mediaFileLengthInMilliseconds;
    private Runnable notification; // notification for seekbar update
    private WifiManager.WifiLock wifiLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);

        mContext = this;

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        mSong = (Song) extras.getSerializable("song");
        AudioURL = mSong.getSongURL();


        // initializing views
        button_play = (ImageView) findViewById(R.id.play);
        button_stop = (ImageView) findViewById(R.id.stop);
        songNameTextView = (TextView) findViewById(R.id.songname);
        songNameTextView.setText(mSong.getSongname());
        artistNameTextView = (TextView) findViewById(R.id.artistname);
        artistNameTextView.setText(mSong.getArtistName());

        seekBarProgress = (SeekBar) findViewById(R.id.seekbar);
        seekBarProgress.setMax(99); // It means 100% .0-99
        seekBarProgress.setOnTouchListener(this);

        // setting up mediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);

        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.FULL_WAKE_LOCK);
        mediaPlayer.setScreenOnWhilePlaying(true);

        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();

        try {
            mediaPlayer.setDataSource(AudioURL); // setup song from https://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
            mediaPlayer.prepareAsync(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
        } catch (Exception e) {
            e.printStackTrace();
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC); // to control stream volume using vol. buttons


        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** ImageButton onClick event handler. Method which start/pause mediaplayer playing */
//                Log.d("MusicPlayer","play button clicked");

                if (isPrepared) {
                    mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL

                    if (!mediaPlayer.isPlaying()) {
                        button_play.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                        seekBarProgress.setEnabled(true);
                        mediaPlayer.start();

                    } else {
                        button_play.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                        mediaPlayer.pause();
                        seekBarProgress.setEnabled(false);

                    }

                    primarySeekBarProgressUpdater();
                } else {
                    Toast.makeText(mContext, " Bufferring ...", Toast.LENGTH_SHORT).show();
                }

            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//                Log.d("MusicPlayer","Stop button clicked");
                mediaPlayer.seekTo(0);
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100));
                seekBarProgress.setEnabled(false);
                /** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
                button_play.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                button_play.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                mediaPlayer.pause();
                seekBarProgress.setEnabled(false);
                handler.removeCallbacks(notification);
            }
        }
        super.onPause();
    }

    /**
     * Method which updates the SeekBar primary progress by current song playing position
     */
    private void primarySeekBarProgressUpdater() {
        seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
        if (mediaPlayer.isPlaying()) {
            notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater();
                }
            };
            handler.postDelayed(notification, 1000);
        }
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        /** Method which updates the SeekBar secondary progress by current song loading from URL position*/
        seekBarProgress.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        /** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
        button_play.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v.getId() == R.id.seekbar) {
            /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/

            if (mediaPlayer.isPlaying()) {
//                Log.d("MusicPlayer","is playing : true");
                SeekBar sb = (SeekBar) v;
                int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMillisecconds);
            } else {
//                Log.d("MusicPlayer","is playing : false");
                seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100));
            }
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        if (init) {
            button_play.callOnClick();
            init = false;
        }
    }

    @Override
    public void onDestroy() {
//        Log.d("MusicPlayer","OnDestroyed Called");
        handler.removeCallbacks(notification);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        wifiLock.release();
        super.onDestroy();
    }
}

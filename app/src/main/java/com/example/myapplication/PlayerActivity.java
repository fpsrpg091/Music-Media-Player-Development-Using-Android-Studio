package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private MusicService musicService;
    private boolean isBound = false;
    private TextView textViewTitle;
    private ImageButton btnPlayPause;
    private SeekBar seekBarProgress;
    private TextView textViewCurrentTime;
    private TextView textViewTotalTime;
    private Handler handler;
    private ImageButton btnToggleLoopShuffle;
    private ImageButton btnReplay;
    private Button btnLyrics;
    private TextView textViewArtist;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;

            ArrayList<String> songPaths = getIntent().getStringArrayListExtra("SONG_PATHS");
            int currentSongPosition = getIntent().getIntExtra("CURRENT_SONG_POSITION", 0);
            musicService.setSongData(songPaths, currentSongPosition);

            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private final BroadcastReceiver playbackStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.ACTION_PLAYBACK_STATE_CHANGED.equals(intent.getAction())) {
                boolean isPlaying = intent.getBooleanExtra("IS_PLAYING", false);
                btnPlayPause.setImageResource(isPlaying ? R.drawable.player_pause : R.drawable.player_play);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        textViewTitle = findViewById(R.id.textViewSongTitle);
        textViewArtist = findViewById(R.id.textViewSongArtist);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBarProgress = findViewById(R.id.seekBarProgress);
        textViewCurrentTime = findViewById(R.id.textViewCurrentTime);
        textViewTotalTime = findViewById(R.id.textViewTotalTime);
        btnToggleLoopShuffle = findViewById(R.id.btnToggleLoopShuffle);
        btnReplay = findViewById(R.id.btnReplay);
        btnLyrics = findViewById(R.id.btnlyrics);

        handler = new Handler();
        seekBarProgress.setOnSeekBarChangeListener(this);
        btnPlayPause.setImageResource(R.drawable.player_pause);
        btnPlayPause.setOnClickListener(v -> {
            if (isBound) {
                if (musicService.isPlaying()) {
                    musicService.pauseMusic();
                    btnPlayPause.setImageResource(R.drawable.player_play);
                } else {
                    musicService.playMusic();
                    btnPlayPause.setImageResource(R.drawable.player_pause);
                }
            }
        });

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (isBound) {
                musicService.nextSong();
                if (musicService.isPlaying()) {
                    btnPlayPause.setImageResource(R.drawable.player_pause);
                }
            }
        });

        findViewById(R.id.btnPrevious).setOnClickListener(v -> {
            if (isBound) {
                musicService.previousSong();
                if (musicService.isPlaying()) {
                    btnPlayPause.setImageResource(R.drawable.player_pause);
                }
            }
        });

        btnToggleLoopShuffle.setOnClickListener(v -> {
            if (isBound) {
                musicService.toggleShuffle();
                updateToggleLoopShuffleButton();
            }
        });

        btnReplay.setOnClickListener(v -> {
            if (isBound) {
                musicService.toggleReplay();
                updateReplayButton();
            }
        });

        btnLyrics.setOnClickListener(v -> {
            if (isBound) {
                String currentSongTitle = new File(musicService.songPaths.get(musicService.getCurrentSongPosition())).getName();
                String artistName = musicService.getCurrentSongArtist(); // You should get the actual artist name from your data
                Intent intent = new Intent(PlayerActivity.this, LyricsActivity.class);
                intent.putExtra("SONG_TITLE", currentSongTitle);
                intent.putExtra("ARTIST_NAME", artistName);
                intent.putExtra("SONG_PATHS", getIntent().getStringArrayListExtra("SONG_PATHS"));
                intent.putExtra("CURRENT_SONG_POSITION", musicService.getCurrentSongPosition());
                startActivity(intent);
            }
        });

        bindService(new Intent(this, MusicService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        Runnable updateUIRunnable = new Runnable() {
            @Override
            public void run() {
                if (isBound) {
                    updateUI();
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateUIRunnable);

        // Register BroadcastReceiver to handle notifications
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.ACTION_PLAY);
        filter.addAction(MusicService.ACTION_PAUSE);
        filter.addAction(MusicService.ACTION_NEXT);
        filter.addAction(MusicService.ACTION_PREVIOUS);
        registerReceiver(notificationReceiver, filter);

        IntentFilter playbackStateFilter = new IntentFilter();
        playbackStateFilter.addAction(MusicService.ACTION_PLAYBACK_STATE_CHANGED);
        registerReceiver(playbackStateReceiver, playbackStateFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacksAndMessages(null);

        // Unregister BroadcastReceiver
        unregisterReceiver(notificationReceiver);
    }

    private void updateUI() {
        if (isBound && musicService != null) {
            textViewTitle.setText(new File(musicService.songPaths.get(musicService.getCurrentSongPosition())).getName());
            textViewArtist.setText(musicService.getCurrentSongArtist());
            seekBarProgress.setMax(musicService.getDuration());
            seekBarProgress.setProgress(musicService.getCurrentPosition());

            textViewCurrentTime.setText(formatTime(musicService.getCurrentPosition()));
            textViewTotalTime.setText(formatTime(musicService.getDuration()));

            updateToggleLoopShuffleButton();
            updateReplayButton();
            btnPlayPause.setImageResource(musicService.isPlaying() ? R.drawable.player_pause : R.drawable.player_play);

            // Send broadcast to update PlayerBottom song name
            Intent intent = new Intent(MusicService.ACTION_SONG_CHANGED);
            intent.putExtra("CURRENT_SONG_TITLE", new File(musicService.songPaths.get(musicService.getCurrentSongPosition())).getName());
            intent.putExtra("CURRENT_SONG_ARTIST", musicService.getCurrentSongArtist());
            sendBroadcast(intent);
        }
    }

    private void updateToggleLoopShuffleButton() {
        if (isBound) {
            btnToggleLoopShuffle.setImageResource(musicService.isShuffleOn() ? R.drawable.player_shuffle : R.drawable.player_loop);
        }
    }

    private void updateReplayButton() {
        if (isBound) {
            btnReplay.setImageResource(musicService.isReplayOn() ? R.drawable.player_replay : R.drawable.player_no_replay);
        }
    }

    private String formatTime(int millis) {
        int minutes = millis / 60000;
        int seconds = (millis % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && isBound) {
            musicService.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isBound) {
                    updateUI();
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };
}

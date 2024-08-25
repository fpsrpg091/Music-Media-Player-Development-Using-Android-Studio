package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Binder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service {

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_SHUFFLE = "ACTION_SHUFFLE";
    public static final String ACTION_REPLAY = "ACTION_REPLAY";
    public static final String ACTION_SEEK = "ACTION_SEEK";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_SONG_CHANGED = "com.example.myapplication.ACTION_SONG_CHANGED";
    public static final String ACTION_PLAYBACK_STATE_CHANGED = "ACTION_PLAYBACK_STATE_CHANGED";
    private static final String PREFS_NAME = "SleepTimerPrefs";
    private static final String KEY_TIMER_RUNNING = "timer_running";
    private static final String KEY_TIMER_END_TIME = "timer_end_time";

    private MediaPlayer mediaPlayer;
    static ArrayList<String> songPaths = new ArrayList<>(); // Initialize songPaths
    private boolean isShuffleOn = false;
    private boolean isReplayOn = false;

    public static boolean isPlaying = false;
    public static int currentSongPosition = 0;

    private final IBinder binder = new LocalBinder();
    private MusicNotificationManager notificationManager;

    private SharedPreferences preferences;
    private static MusicService instance;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            if (isReplayOn) {
                playSong(currentSongPosition);
            } else if (isShuffleOn) {
                shuffleAndPlay();
            } else {
                nextSong();
            }
        });
        preferences = getSharedPreferences("music_player_prefs", MODE_PRIVATE);
        notificationManager = new MusicNotificationManager(this);
        startForeground(MusicNotificationManager.NOTIFICATION_ID, notificationManager.createNotification());
        instance = this;
    }

    public static MusicService getInstance() {
        return instance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY:
                        ArrayList<String> paths = intent.getStringArrayListExtra("SONG_PATHS");
                        int position = intent.getIntExtra("CURRENT_SONG_POSITION", -1);
                        if (paths != null && position != -1 ) {
                            Log.d("123","7989"+paths+position);
                            setSongData(paths, position);
                        } else {
                            playMusic();
                        }
                        break;
                    case ACTION_PAUSE:
                        pauseMusic();
                        break;
                    case ACTION_NEXT:
                        nextSong();
                        break;
                    case ACTION_PREVIOUS:
                        previousSong();
                        break;
                    case ACTION_SHUFFLE:
                        toggleShuffle();
                        break;
                    case ACTION_REPLAY:
                        toggleReplay();
                        break;
                    case ACTION_SEEK:
                        int seekPosition = intent.getIntExtra("SEEK_POSITION", 0);
                        seekTo(seekPosition);
                        break;
                    case ACTION_STOP:
                        stopMusic();
                        break;
                }
                notificationManager.updateNotification(); // Update notification for each action
            }
        }
        return START_STICKY;
    }

    public void playSong(int position) {
        if (songPaths != null && position >= 0 && position < songPaths.size()) {
            String songPath = songPaths.get(position);
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnCompletionListener(mp -> {
                        if (isReplayOn) {
                            playSong(currentSongPosition);
                        } else if (isShuffleOn) {
                            shuffleAndPlay();
                        } else {
                            nextSong();
                        }
                    });
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.reset();  // Make sure MediaPlayer is in the correct state
                    mediaPlayer.setDataSource(songPath);
                    mediaPlayer.prepare();
                    if (preferences.contains(songPath)) {
                        int resumePosition = preferences.getInt(songPath, 0);
                        mediaPlayer.seekTo(resumePosition);  // Restore last played position
                    }
                }
                mediaPlayer.start();
                isPlaying = true;
                currentSongPosition = position;
                sendCurrentSongBroadcast();
                sendPlaybackStateBroadcast();
                notificationManager.updateNotification();
                preferences.edit().clear().apply();// Update notification with new song info
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void playMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (preferences.contains(songPaths.get(currentSongPosition))) {
                int resumePosition = preferences.getInt(songPaths.get(currentSongPosition), 0);
                mediaPlayer.seekTo(resumePosition);
            }
            mediaPlayer.start();
            isPlaying = true;
            sendPlaybackStateBroadcast();
            notificationManager.updateNotification(); // Update notification with play state
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            sendPlaybackStateBroadcast();

            // Store current song's position in SharedPreferences
            preferences.edit().putInt((songPaths.get(currentSongPosition)), mediaPlayer.getCurrentPosition()).apply();
            notificationManager.updateNotification(); // Update notification with pause state
        }
    }

    public void stopMusic() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isTimerRunning = prefs.getBoolean(KEY_TIMER_RUNNING, false);
        SharedPreferences.Editor editor = prefs.edit();
        if (isTimerRunning) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();

                isPlaying = false;
                sendPlaybackStateBroadcast();
                notificationManager.updateNotification();
                Log.d("1234MusicService", "123Music stopped successfully");  // Add this line
                editor.putBoolean(KEY_TIMER_RUNNING, false);
                editor.remove(KEY_TIMER_END_TIME);
                editor.apply();
            }
        }


    }

    public void nextSong() {
        if (isShuffleOn) {
            shuffleAndPlay();
        } else {
            currentSongPosition = (currentSongPosition + 1) % songPaths.size();
            playSong(currentSongPosition);
        }
    }

    public void previousSong() {
        if (isShuffleOn) {
            shuffleAndPlay();
        } else {
            currentSongPosition = (currentSongPosition - 1 + songPaths.size()) % songPaths.size();
            playSong(currentSongPosition);
        }
    }

    public void shuffleAndPlay() {
        int randomPosition = (int) (Math.random() * songPaths.size());
        playSong(randomPosition);
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void setSongData(ArrayList<String> paths, int position) {
        String newSongPath = paths.get(position); // Get the new song path

        // Check if the new song is the same as the currently playing song
        if (mediaPlayer != null && mediaPlayer.isPlaying() &&
                currentSongPosition == position && songPaths.get(currentSongPosition).equals(newSongPath)) {
            return; // Don't reset if playing the same song
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        this.songPaths = paths;
        this.currentSongPosition = position;

        Log.d("MusicService", "setSongData called with paths: " + paths + " and position: " + position);
        Log.d("MusicService", "Current song path: " + songPaths.get(currentSongPosition) + " New song path: " + newSongPath);

        playSong(position);
    }

    public static ArrayList<String> getSongPaths(){
        return songPaths;
    }

    public int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public void toggleShuffle() {
        isShuffleOn = !isShuffleOn;
        notificationManager.updateNotification(); // Update notification with shuffle state
    }

    public void toggleReplay() {
        isReplayOn = !isReplayOn;
        notificationManager.updateNotification(); // Update notification with replay state
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public boolean isShuffleOn() {
        return isShuffleOn;
    }

    public boolean isReplayOn() {
        return isReplayOn;
    }

    public static String getCurrentSongTitle() {
        if (songPaths != null && !songPaths.isEmpty() && currentSongPosition < songPaths.size()) {
            return new File(songPaths.get(currentSongPosition)).getName();
        }
        return "Unknown Title";
    }

    private void sendCurrentSongBroadcast() {
        Intent intent = new Intent();
        intent.setAction(ACTION_SONG_CHANGED);
        String currentSongTitle = getCurrentSongTitle();
        intent.putExtra("123CURRENT_SONG_TITLE", currentSongTitle);
        Log.d("MusicService", "123Sending Song Changed Broadcast: " + currentSongTitle); // Log the broadcast
        sendBroadcast(intent);
    }

    private void sendPlaybackStateBroadcast() {
        Intent intent = new Intent(ACTION_PLAYBACK_STATE_CHANGED);
        intent.putExtra("IS_PLAYING", isPlaying);
        sendBroadcast(intent);
    }

    public int getLastPlayedSongPosition() {
        String songPath = songPaths.get(currentSongPosition);  // Adjusted to use currentSongPosition
        return preferences.getInt(songPath, 0);
    }

    public String getCurrentSongArtist() {
        if (songPaths != null && !songPaths.isEmpty() && currentSongPosition < songPaths.size()) {
            String songPath = songPaths.get(currentSongPosition);
            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media.ARTIST},
                    MediaStore.Audio.Media.DATA + " = ?",
                    new String[]{songPath},
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                cursor.close();
                return artist;
            }
        }
        return "Unknown Artist";
    }

    public int getAudioSessionId() {
        if (mediaPlayer != null) {
            return mediaPlayer.getAudioSessionId();
        }
        return -1;
    }

//    public String getCurrentSongArtist() {
//        // Placeholder for artist name. You can modify this to get the actual artist name.
//        return "Artist Name";
//    }
}

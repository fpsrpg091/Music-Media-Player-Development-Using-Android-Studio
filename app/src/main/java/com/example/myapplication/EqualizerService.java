package com.example.myapplication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class EqualizerService extends Service {

    private Equalizer equalizer;
    private static final String TAG = "EqualizerService";
    private MusicService musicService;
    private SharedPreferences sharedPreferences;

    private int bassLevel;
    private int surroundLevel;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            setupEqualizer(); // Ensure equalizer is set up once service is bound
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("EqualizerPrefs", MODE_PRIVATE);
        // Bind to MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void saveEqualizerSettings(boolean isEnabled, int[] bandLevels, int bassLevel, int surroundLevel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("equalizerEnabled", isEnabled);
        for (int i = 0; i < bandLevels.length; i++) {
            editor.putInt("bandLevel_" + i, bandLevels[i]);
        }
        editor.putInt("bassLevel", bassLevel);
        editor.putInt("surroundLevel", surroundLevel);
        editor.apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            boolean isEnabled = intent.getBooleanExtra("equalizerEnabled", false);
            int[] bandLevels = intent.getIntArrayExtra("bandLevels");
            bassLevel = intent.getIntExtra("bassLevel", 0);
            surroundLevel = intent.getIntExtra("surroundLevel", 0);

            if ("UPDATE_EQUALIZER_SETTINGS".equals(action)) {
                if (bandLevels != null) {
                    saveEqualizerSettings(isEnabled, bandLevels, bassLevel, surroundLevel);
                    if (equalizer != null) {
                        if (equalizer.getEnabled() != isEnabled) {
                            equalizer.setEnabled(isEnabled);
                        }
                        for (short i = 0; i < bandLevels.length; i++) {
                            equalizer.setBandLevel(i, (short) bandLevels[i]);
                        }
                        applyBassAndSurroundLevels();  // Apply custom bass and surround levels
                    } else {
                        Log.e(TAG, "Equalizer is not initialized. Cannot update settings.");
                    }
                }
            } else {
                if (bandLevels != null) {
                    saveEqualizerSettings(isEnabled, bandLevels, bassLevel, surroundLevel);
                    if (equalizer != null) {
                        equalizer.setEnabled(isEnabled);
                        for (short i = 0; i < bandLevels.length; i++) {
                            equalizer.setBandLevel(i, (short) bandLevels[i]);
                        }
                        applyBassAndSurroundLevels();  // Apply custom bass and surround levels
                    } else {
                        Log.e(TAG, "Equalizer is not initialized. Cannot apply settings.");
                    }
                }
            }
        }

        return START_STICKY;
    }

    private void setupEqualizer() {
        if (musicService != null) {
            int audioSessionId = musicService.getAudioSessionId();
            Log.d(TAG, "Audio Session ID: " + audioSessionId);
            if (audioSessionId != -1) {
                if (equalizer != null) {
                    equalizer.release(); // Release previous instance
                }
                equalizer = new Equalizer(0, audioSessionId);
                equalizer.setEnabled(sharedPreferences.getBoolean("equalizerEnabled", false));

                // Restore band levels
                for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
                    int level = sharedPreferences.getInt("bandLevel_" + i, 0);
                    equalizer.setBandLevel(i, (short) level);
                }

                // Restore and apply bass and surround levels
                bassLevel = sharedPreferences.getInt("bassLevel", 0);
                surroundLevel = sharedPreferences.getInt("surroundLevel", 0);
                applyBassAndSurroundLevels();
            } else {
                Log.e(TAG, "Failed to initialize Equalizer: Invalid audio session ID");
                equalizer = null; // Ensure equalizer is set to null if initialization fails
            }
        }
    }

    private void applyBassAndSurroundLevels() {
        // Apply bass and surround levels to the equalizer
        // Adjust this method to match how bass and surround levels should be applied
        if (equalizer != null) {
            // Assuming these are custom bands or using a custom effect, modify accordingly
            // This is just a placeholder for your specific implementation
            equalizer.setBandLevel((short) 0, (short) bassLevel); // Example application
            equalizer.setBandLevel((short) 1, (short) surroundLevel); // Example application
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
        unbindService(serviceConnection);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

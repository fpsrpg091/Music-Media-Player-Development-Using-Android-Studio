package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class SleepTimerNotificationService extends Service {

    private static final String CHANNEL_ID = "SleepTimerChannel";
    private static final int NOTIFICATION_ID = 2;
    private static final String PREFS_NAME = "SleepTimerPrefs";
    private static final String KEY_TIMER_RUNNING = "timer_running";
    private static final String KEY_TIMER_END_TIME = "timer_end_time";

    private Handler handler;
    private Runnable updateTimeRunnable;
    private long endTimeMillis;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());

        // Retrieve the end time from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        endTimeMillis = prefs.getLong(KEY_TIMER_END_TIME, 0);

        createNotificationChannel();
        startCountdown();
    }

    // Handle cancel action from the notification
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ACTION_CANCEL_TIMER".equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Sleep Timer", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    private void startCountdown() {
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                long remainingTimeMillis = endTimeMillis - currentTimeMillis;

                if (remainingTimeMillis <= 0) {
                    // Timer has finished, stop the service
                    stopSelf();
                } else {
                    // Update notification with remaining time
                    long seconds = remainingTimeMillis / 1000;
                    long minutes = seconds / 60;
                    seconds = seconds % 60;

                    String timeRemaining = String.format("%02d:%02d", minutes, seconds);
                    updateNotification(timeRemaining);

                    // Update the countdown every second
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateTimeRunnable);
    }

    private void updateNotification(String timeRemaining) {
        Intent cancelIntent = new Intent(this, SleepTimerNotificationService.class);
        cancelIntent.setAction("ACTION_CANCEL_TIMER");
        PendingIntent cancelPendingIntent = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_bedtime_24)
                .setContentTitle("Sleep Timer")
                .setContentText("Time remaining: " + timeRemaining)
                .addAction(R.drawable.baseline_arrow_back_24, "Cancel Timer", cancelPendingIntent)
                .setOngoing(true);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimeRunnable);
        clearTimer();
    }

    private void clearTimer() {
        // Clear the countdown status and end time from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_TIMER_RUNNING, false);
        editor.remove(KEY_TIMER_END_TIME);
        editor.apply();

        // Optionally, stop any music playing
        Intent stopMusicIntent = new Intent(this, MusicService.class);
        stopMusicIntent.setAction(MusicService.ACTION_STOP);
        startService(stopMusicIntent);
    }

}


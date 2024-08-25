package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class SleepTimerService extends JobIntentService {

    private static final int JOB_ID = 1000;
    private static final String PREFS_NAME = "SleepTimerPrefs";
    private static final String KEY_TIMER_RUNNING = "timer_running";
    private static final String KEY_TIMER_END_TIME = "timer_end_time";

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SleepTimerService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent != null) {
            int totalTimeInSeconds = intent.getIntExtra("TOTAL_TIME", 0);

            Log.d("SleepTimerService", "123Timer started for " + totalTimeInSeconds + " seconds");

            long endTimeMillis = System.currentTimeMillis() + (totalTimeInSeconds * 1000L);

            // Save timer running status and end time
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_TIMER_END_TIME, endTimeMillis);
            editor.apply();

            try {
                Thread.sleep(totalTimeInSeconds * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Check if the timer is still running
            boolean isTimerRunning = prefs.getBoolean(KEY_TIMER_RUNNING, false);

            if (isTimerRunning) {
                Log.d("SleepTimerService", "123Sleep timer triggered");

                // Stop music
                Intent stopMusicIntent = new Intent(this, MusicService.class);
                stopMusicIntent.setAction(MusicService.ACTION_STOP);
                startService(stopMusicIntent);

                // Clear timer running status
//                editor.putBoolean(KEY_TIMER_RUNNING, false);
//                editor.remove(KEY_TIMER_END_TIME);
//                editor.apply();

                // Optionally, stop the service
                stopSelf();
            } else {
                Log.d("SleepTimerService", "123Timer was canceled, not stopping music.");
            }
        }
    }


}

package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

public class CountDownActivity extends Activity {

    private static final String PREFS_NAME = "SleepTimerPrefs";
    private static final String KEY_TIMER_RUNNING = "timer_running";
    private static final String KEY_TIMER_END_TIME = "timer_end_time";

    private TextView timeRemainingView;
    private Button cancelTimerButton;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private long endTimeMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        timeRemainingView = findViewById(R.id.time_remaining_view);
        cancelTimerButton = findViewById(R.id.cancel_timer_button);

        handler = new Handler(Looper.getMainLooper());

        // Retrieve the end time from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        endTimeMillis = prefs.getLong(KEY_TIMER_END_TIME, 0);

        if (endTimeMillis > System.currentTimeMillis()) {
            startCountdown();
        } else {
            Toast.makeText(this, "No active timer to display.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startCountdown() {
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                long remainingTimeMillis = endTimeMillis - currentTimeMillis;

                if (remainingTimeMillis <= 0) {
                    // Timer has finished
                    timeRemainingView.setText("00:00");
                    handler.removeCallbacks(updateTimeRunnable);
                    timerEnd();
                } else {
                    // Update UI with remaining time
                    long seconds = remainingTimeMillis / 1000;
                    long minutes = seconds / 60;
                    seconds = seconds % 60;
                    timeRemainingView.setText(String.format("%02d:%02d", minutes, seconds));

                    // Update the countdown every second
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateTimeRunnable);
    }

    public void stopCountdown(View view) {
        cancelTimer();
    }

    public void backToMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void timerEnd() {
        // Clear the countdown status and end time from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_TIMER_RUNNING, false);
        editor.remove(KEY_TIMER_END_TIME);
        editor.apply();

        // Stop the service if running
        Intent intent = new Intent(this, SleepTimerService.class);
        stopService(intent);
        finish();
    }

    private void cancelTimer() {
        // Clear the countdown status and end time from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_TIMER_RUNNING, false);
        editor.remove(KEY_TIMER_END_TIME);
        editor.apply();

        // Stop the SleepTimerNotificationService if running
        Intent stopServiceIntent = new Intent(this, SleepTimerNotificationService.class);
        stopService(stopServiceIntent);

        // Stop the service if running
        Intent intent = new Intent(this, SleepTimerService.class);
        stopService(intent);
        Toast.makeText(this, "Timer cancelled.", Toast.LENGTH_SHORT).show();
        finish();
    }
}


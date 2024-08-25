package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.myapplication.R;

public class SleepTimerActivity extends Activity {

    private NumberPicker numberPickerHours;
    private NumberPicker numberPickerMinutes;
    private NumberPicker numberPickerSeconds;
    private Button buttonSetTimer;
    private static final String PREFS_NAME = "SleepTimerPrefs";
    private static final String KEY_TIMER_RUNNING = "timer_running";
    private static final String KEY_TIMER_END_TIME = "timer_end_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_timer);

        numberPickerHours = findViewById(R.id.number_picker_hours);
        numberPickerMinutes = findViewById(R.id.number_picker_minutes);
        numberPickerSeconds = findViewById(R.id.number_picker_seconds);
        buttonSetTimer = findViewById(R.id.button_set_timer);

        // Set min and max values programmatically
        numberPickerHours.setMinValue(0);
        numberPickerHours.setMaxValue(23);

        numberPickerMinutes.setMinValue(0);
        numberPickerMinutes.setMaxValue(59);

        numberPickerSeconds.setMinValue(0);
        numberPickerSeconds.setMaxValue(59);

        buttonSetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save timer running status
                int hours = numberPickerHours.getValue();
                int minutes = numberPickerMinutes.getValue();
                int seconds = numberPickerSeconds.getValue();

                int totalTimeInSeconds = (hours * 3600) + (minutes * 60) + seconds;

                long endTimeMillis = System.currentTimeMillis() + (totalTimeInSeconds * 1000L);

                // Save timer running status and end time
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(KEY_TIMER_RUNNING, true);
                editor.putLong(KEY_TIMER_END_TIME, endTimeMillis);
                editor.apply();

                if (totalTimeInSeconds > 0) {
                    Intent intent = new Intent(SleepTimerActivity.this, SleepTimerService.class);
                    intent.putExtra("TOTAL_TIME", totalTimeInSeconds);
                    SleepTimerService.enqueueWork(SleepTimerActivity.this, intent);
                    // Start the SleepTimerNotificationService
                    Intent notificationServiceIntent = new Intent(SleepTimerActivity.this, SleepTimerNotificationService.class);
                    startService(notificationServiceIntent);

                    Toast.makeText(SleepTimerActivity.this, "Sleep Timer Set for " + hours + "h " + minutes + "m " + seconds + "s", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SleepTimerActivity.this, "Please set a valid time.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

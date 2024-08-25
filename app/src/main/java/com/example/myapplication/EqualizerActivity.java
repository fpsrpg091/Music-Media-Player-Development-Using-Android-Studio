package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class EqualizerActivity extends AppCompatActivity {

    private EqualizerGraphView equalizerGraphView;
    private SeekBar seekBar60Hz, seekBar230Hz, seekBar910Hz, seekBar4kHz, seekBar14kHz;
    private Switch switchEqualizer;
    private Button resetButton;
    private SeekBar seekBarBassControl, seekBarSurroundControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        equalizerGraphView = findViewById(R.id.equalizerGraphView);
        seekBar60Hz = findViewById(R.id.seekBar_60Hz);
        seekBar230Hz = findViewById(R.id.seekBar_230Hz);
        seekBar910Hz = findViewById(R.id.seekBar_910Hz);
        seekBar4kHz = findViewById(R.id.seekBar_4kHz);
        seekBar14kHz = findViewById(R.id.seekBar_14kHz);
        switchEqualizer = findViewById(R.id.switch_equalizer);
        resetButton = findViewById(R.id.reset_button);
        seekBarBassControl = findViewById(R.id.seekBar_BassControl);
        seekBarSurroundControl = findViewById(R.id.seekBar_SurroundControl);

        // Set SeekBars' visibility based on equalizer state
        setSeekBarsVisibility(false);

        seekBar60Hz.setMax(500);
        seekBar230Hz.setMax(500);
        seekBar910Hz.setMax(500);
        seekBar4kHz.setMax(500);
        seekBar14kHz.setMax(500);
        seekBarBassControl.setMax(1000); // Example max value, adjust as needed
        seekBarSurroundControl.setMax(1000); // Example max value, adjust as needed

        seekBar60Hz.setMin(-500);
        seekBar230Hz.setMin(-500);
        seekBar910Hz.setMin(-500);
        seekBar4kHz.setMin(-500);
        seekBar14kHz.setMin(-500);


        // Restore equalizer state from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("EqualizerPrefs", MODE_PRIVATE);
        boolean isEnabled = sharedPreferences.getBoolean("equalizerEnabled", false);
        int[] bandLevels = new int[5];
        for (int i = 0; i < bandLevels.length; i++) {
            bandLevels[i] = sharedPreferences.getInt("bandLevel_" + i, 0);
        }
        // Restore saved values
        int bassLevel = sharedPreferences.getInt("bassLevel", 0);
        int surroundLevel = sharedPreferences.getInt("surroundLevel", 0);

        // Update UI components based on restored state
        switchEqualizer.setChecked(isEnabled);
        setSeekBarsVisibility(isEnabled);  // Update visibility based on saved state
        seekBar60Hz.setProgress(bandLevels[0]);
        seekBar230Hz.setProgress(bandLevels[1]);
        seekBar910Hz.setProgress(bandLevels[2]);
        seekBar4kHz.setProgress(bandLevels[3]);
        seekBar14kHz.setProgress(bandLevels[4]);
        seekBarBassControl.setProgress(bassLevel);
        seekBarSurroundControl.setProgress(surroundLevel);

        updateEqualizerGraph();

        // Add change listeners to the SeekBars
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateEqualizerGraph();
                updateEqualizerSettings(); // Ensure equalizer settings are updated in real-time
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        seekBar60Hz.setOnSeekBarChangeListener(listener);
        seekBar230Hz.setOnSeekBarChangeListener(listener);
        seekBar910Hz.setOnSeekBarChangeListener(listener);
        seekBar4kHz.setOnSeekBarChangeListener(listener);
        seekBar14kHz.setOnSeekBarChangeListener(listener);
        seekBarBassControl.setOnSeekBarChangeListener(listener);
        seekBarSurroundControl.setOnSeekBarChangeListener(listener);
        // Update visibility when switch is toggled
        switchEqualizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setSeekBarsVisibility(isChecked);
            saveEqualizerSettings(isChecked, getBandLevels());
            Intent serviceIntent = new Intent(EqualizerActivity.this, EqualizerService.class);
            serviceIntent.putExtra("equalizerEnabled", isChecked);
            serviceIntent.putExtra("bandLevels", getBandLevels());
            if (isChecked) {
                startService(serviceIntent);
            } else {
                stopService(serviceIntent);
                resetSeekBars();
            }
        });

        // Reset button functionality
        resetButton.setOnClickListener(v -> {
            resetSeekBars();
            updateEqualizerGraph();
            updateEqualizerSettings(); // Update equalizer settings after reset
        });
    }

    private void setSeekBarsVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        seekBar60Hz.setVisibility(visibility);
        seekBar230Hz.setVisibility(visibility);
        seekBar910Hz.setVisibility(visibility);
        seekBar4kHz.setVisibility(visibility);
        seekBar14kHz.setVisibility(visibility);
        seekBarBassControl.setVisibility(visibility);
        seekBarSurroundControl.setVisibility(visibility);
        findViewById(R.id.equalizerGraphView).setVisibility(visibility);
        findViewById(R.id.seekBar_horizontal).setVisibility(visibility);
        findViewById(R.id.seekBar_vertical).setVisibility(visibility);
        resetButton.setVisibility(visibility);
    }

    private void saveEqualizerSettings(boolean isEnabled, int[] bandLevels) {
        SharedPreferences.Editor editor = getSharedPreferences("EqualizerPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("equalizerEnabled", isEnabled);
        for (int i = 0; i < bandLevels.length; i++) {
            editor.putInt("bandLevel_" + i, bandLevels[i]);
        }

        // Save Bass and Surround levels
        editor.putInt("bassLevel", seekBarBassControl.getProgress());
        editor.putInt("surroundLevel", seekBarSurroundControl.getProgress());

        editor.apply();
    }

    private int[] getBandLevels() {
        return new int[]{
                seekBar60Hz.getProgress(),
                seekBar230Hz.getProgress(),
                seekBar910Hz.getProgress(),
                seekBar4kHz.getProgress(),
                seekBar14kHz.getProgress()
        };
    }

    private void updateEqualizerGraph() {
        float[] amplitudes = new float[5];
        amplitudes[0] = seekBar60Hz.getProgress();
        amplitudes[1] = seekBar230Hz.getProgress();
        amplitudes[2] = seekBar910Hz.getProgress();
        amplitudes[3] = seekBar4kHz.getProgress();
        amplitudes[4] = seekBar14kHz.getProgress();
        equalizerGraphView.setAmplitudes(amplitudes);
    }

    private void updateEqualizerSettings() {
        Intent serviceIntent = new Intent(EqualizerActivity.this, EqualizerService.class);
        serviceIntent.setAction("UPDATE_EQUALIZER_SETTINGS");
        serviceIntent.putExtra("equalizerEnabled", switchEqualizer.isChecked());
        serviceIntent.putExtra("bandLevels", getBandLevels());

        // Add bass and surround control levels
        serviceIntent.putExtra("bassLevel", seekBarBassControl.getProgress());
        serviceIntent.putExtra("surroundLevel", seekBarSurroundControl.getProgress());

        startService(serviceIntent);
    }

    private void resetSeekBars() {
        seekBar60Hz.setProgress(0);
        seekBar230Hz.setProgress(0);
        seekBar910Hz.setProgress(0);
        seekBar4kHz.setProgress(0);
        seekBar14kHz.setProgress(0);

        seekBarBassControl.setProgress(0); // Reset Bass control
        seekBarSurroundControl.setProgress(0); // Reset Surround control
    }

}

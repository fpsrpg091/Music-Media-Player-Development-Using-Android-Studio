package com.example.myapplication;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.ui.gallery.PlaylistFragment;
import com.example.myapplication.ui.home.FragmentHome;
import com.example.myapplication.ui.home.HomeViewModel;
import com.example.myapplication.ui.slideshow.AlbumArtistFragment;
import com.example.myapplication.ui.slideshow.AlbumFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private List<String> songPaths;
    private TextView songNameMiniPlayer;
    private ImageView bottomAlbumArt;
    private ImageButton playPauseMiniPlayer, skipNextBottom, prevBackBottom;
    private SharedPreferences preferences;
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "SleepTimerPrefs";
    private static final String KEY_TIMER_RUNNING = "timer_running";

    private final BroadcastReceiver songChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.ACTION_SONG_CHANGED.equals(intent.getAction()) ||
                    MusicNotificationManager.ACTION_UPDATE_SONG_TITLE.equals(intent.getAction())) { // Listen for the new broadcast
                String currentSongTitle = intent.getStringExtra("CURRENT_SONG_TITLE");
                Log.d(TAG, "Song Changed Broadcast Received: " + currentSongTitle); // Log the received title
                if (currentSongTitle != null) {
                    // Ensure this update is on the main thread
                    runOnUiThread(() -> songNameMiniPlayer.setText(currentSongTitle));
                }
            }
        }
    };

    private final BroadcastReceiver playbackStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.ACTION_PLAYBACK_STATE_CHANGED.equals(intent.getAction())) {
                boolean isPlaying = intent.getBooleanExtra("IS_PLAYING", false);
                playPauseMiniPlayer.setImageResource(isPlaying ? R.drawable.player_pause : R.drawable.player_play);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Initialize the views
        songNameMiniPlayer = findViewById(R.id.songnameminiplayer);
        bottomAlbumArt = findViewById(R.id.bottomalbumart);
        playPauseMiniPlayer = findViewById(R.id.playpauseminiplayer);
        skipNextBottom = findViewById(R.id.skipnextbottom);
        prevBackBottom = findViewById(R.id.prevbackbottom);

        // Set up click listeners
        playPauseMiniPlayer.setOnClickListener(v -> togglePlayPause());
        skipNextBottom.setOnClickListener(v -> playNextSong());
        prevBackBottom.setOnClickListener(v -> playPreviousSong());

        // **New Code: Set up click listener for PlayerBottom**
        View playerBottom = findViewById(R.id.cardbottomplayer); // Replace with actual ID of PlayerBottom
        playerBottom.setOnClickListener(v -> openPlayerActivity());

        // Set up the ViewModel and observers
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getSongPaths().observe(this, paths -> {
            songPaths = paths;
            // Enable play button only if there are songs
            playPauseMiniPlayer.setEnabled(songPaths != null && !songPaths.isEmpty());
        });

        homeViewModel.getSongList().observe(this, firstSongName -> {
            if (firstSongName != null) {
                songNameMiniPlayer.setText(firstSongName.get(0));
            }
        });

        // Ensure the songs are loaded
        homeViewModel.loadSongs();

        IntentFilter playbackStateFilter = new IntentFilter();
        playbackStateFilter.addAction(MusicService.ACTION_PLAYBACK_STATE_CHANGED);
        registerReceiver(playbackStateReceiver, playbackStateFilter);


        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.ACTION_SONG_CHANGED);
        filter.addAction(MusicNotificationManager.ACTION_UPDATE_SONG_TITLE); // Register for the new action
        registerReceiver(songChangedReceiver, filter);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        MenuItem sleepTimerItem = menu.findItem(R.id.sleep_timer);

        MenuItem equalizerItem = menu.findItem(R.id.equalizer);

        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                NavHostFragment navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
                if (navHostFragment != null) {
                    Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
                    if (currentFragment instanceof FragmentHome) {
                        ((FragmentHome) currentFragment).filterSongs(newText);
                    }
                    if (currentFragment instanceof PlaylistFragment) {
                        ((PlaylistFragment) currentFragment).filterSongsOrPlaylist(newText);
                    }
                    if (currentFragment instanceof AlbumArtistFragment) {
                        ((AlbumArtistFragment) currentFragment).filterAlbumOrArtist(newText);
                    }

                }
                return true;
            }
        });
        searchItem.setVisible(true);

// Fix for the sleepTimerItem click listener
        sleepTimerItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Check if a timer is running
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean isTimerRunning = prefs.getBoolean(KEY_TIMER_RUNNING, false);

                if (isTimerRunning) {
                    // If a timer is already running, show the countdown page
                    Intent intent = new Intent(MainActivity.this, CountDownActivity.class);
                    startActivity(intent);
                } else {
                    // If no timer is running, show the SleepTimerActivity to set a new timer
                    Intent intent = new Intent(MainActivity.this, SleepTimerActivity.class);
                    startActivity(intent);
                }

                return true;
            }
        });

        equalizerItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Open the EqualizerActivity
                Intent intent = new Intent(MainActivity.this, EqualizerActivity.class);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void togglePlayPause() {
        Intent intent = new Intent(this, MusicService.class);
        if (MusicService.isPlaying) {
            intent.setAction(MusicService.ACTION_PAUSE);
            playPauseMiniPlayer.setImageResource(R.drawable.player_play); // Update to play icon
        } else {
            intent.setAction(MusicService.ACTION_PLAY);
            if (MusicService.songPaths == null || MusicService.songPaths.isEmpty()) {
                intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(songPaths));
                intent.putExtra("CURRENT_SONG_POSITION", MusicService.currentSongPosition);
            }
            playPauseMiniPlayer.setImageResource(R.drawable.player_pause); // Update to pause icon
        }
        ContextCompat.startForegroundService(this, intent);
    }

    private void playNextSong() {
        if (MusicService.songPaths == null || MusicService.songPaths.isEmpty()) {
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction(MusicService.ACTION_PLAY);
            intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(songPaths));
            intent.putExtra("CURRENT_SONG_POSITION", MusicService.currentSongPosition+1);
            playPauseMiniPlayer.setImageResource(R.drawable.player_pause); // Update to pause icon
            ContextCompat.startForegroundService(this, intent);
        }
        else{
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction(MusicService.ACTION_NEXT);
            ContextCompat.startForegroundService(this, intent);
        }

    }

    private void playPreviousSong() {
        if (MusicService.songPaths == null || MusicService.songPaths.isEmpty()) {
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction(MusicService.ACTION_PLAY);
            intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(songPaths));

            // Ensure the songPaths list is not empty before generating a random position
            if (songPaths != null && !songPaths.isEmpty()) {
                Random random = new Random();
                int randomPosition = random.nextInt(songPaths.size()); // Corrected logic
                intent.putExtra("CURRENT_SONG_POSITION", randomPosition);
            }

            playPauseMiniPlayer.setImageResource(R.drawable.player_pause); // Update to pause icon
            ContextCompat.startForegroundService(this, intent);
        } else {
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction(MusicService.ACTION_PREVIOUS);
            ContextCompat.startForegroundService(this, intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Re-fetch the current song title and playback state
        if (MusicService.isPlaying) {
            String currentSongTitle = MusicService.getCurrentSongTitle();
            if (currentSongTitle != null) {
                songNameMiniPlayer.setText(currentSongTitle);
            }
            playPauseMiniPlayer.setImageResource(R.drawable.player_pause); // Show pause button
        } else {
            playPauseMiniPlayer.setImageResource(R.drawable.player_play); // Show play button
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.ACTION_SONG_CHANGED);
        filter.addAction(MusicNotificationManager.ACTION_UPDATE_SONG_TITLE); // Include the action to update song title
        registerReceiver(songChangedReceiver, filter);
        Log.d(TAG, "Registered songChangedReceiver");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(songChangedReceiver);
        Log.d(TAG, "123Unregistered songChangedReceiver");
    }

    // **New Method: Open PlayerActivity with the current song data**
    // **Modified Method: Open PlayerActivity with the current song data**
    private void openPlayerActivity() {
        // Start MusicService if it's not running
        Intent serviceIntent = new Intent(this, MusicService.class);
        if (!isServiceRunning(MusicService.class)) {
            startService(serviceIntent);
        }

        // Assuming you have a reference to the MusicService instance
        ArrayList<String> songPaths = MusicService.getSongPaths();
        Log.d(TAG, "123Song data set"+songPaths);

        // Open PlayerActivity
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(songPaths));
        intent.putExtra("CURRENT_SONG_POSITION", MusicService.currentSongPosition);
        startActivity(intent);
    }


    // Helper method to check if the service is running
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

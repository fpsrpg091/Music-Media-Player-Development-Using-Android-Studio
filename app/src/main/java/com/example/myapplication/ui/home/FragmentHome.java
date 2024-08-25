package com.example.myapplication.ui.home;

import android.Manifest;
import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MusicService;
import com.example.myapplication.PlayerActivity;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment implements SongAdapter.OnSongClickListener {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private HomeViewModel homeViewModel;
    private List<String> songPaths;

    private static final int READ_EXTERNAL_STORAGE_REQUEST = 123;

    private TextView songNameMiniPlayer;
    private ImageView bottomAlbumArt;
    private ImageButton playPauseMiniPlayer, skipNextBottom, prevBackBottom;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(songAdapter);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getSongList().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> songs) {
                songAdapter.setSongList(songs);
                songAdapter.notifyDataSetChanged(); // Notify adapter of changes
            }
        });

        homeViewModel.getSongPaths().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> paths) {
                songPaths = paths;
            }
        });

        if (hasReadStoragePermission()) {
            homeViewModel.loadSongs();
        } else {
            requestReadStoragePermission();
        }

        return root;
    }

    public void filterSongs(String query) {
        if (songAdapter != null) {
            songAdapter.filter(query);
        }
    }

    private boolean hasReadStoragePermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                homeViewModel.loadSongs();
            }
        }
    }

    @Override
    public void onSongClick(int originalPosition, View itemView) {
        // Handle click animation
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                itemView.getResources().getColor(R.color.peach), itemView.getResources().getColor(R.color.peach));
        colorAnimation.setDuration(200); // milliseconds
        colorAnimation.addUpdateListener(animator -> itemView.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();

        // Reset color after animation
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                itemView.setBackgroundColor(itemView.getResources().getColor(android.R.color.transparent));
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        // Log originalPosition and songPaths for debugging
        Log.d("MusicPlayer", "Original Position: " + originalPosition);
        Log.d("MusicPlayer", "Song Paths: " + songPaths);

        // Ensure songPaths is up-to-date with current sorting
        // Get the song path based on the current sorted position
        String songPath = songPaths.get(originalPosition);

        // Log the selected song path for debugging
        Log.d("MusicPlayer", "Selected Song Path: " + songPath);
        SharedPreferences preferences = requireContext().getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE);

        // Get last played position
        int lastPlayedPosition = preferences.getInt(songPaths.get(originalPosition), 0);

        // Use an Intent to start the MusicService and pass the song data
        Intent intent = new Intent(getContext(), MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(songPaths));
        intent.putExtra("CURRENT_SONG_POSITION", originalPosition);
        intent.putExtra("LAST_PLAYED_POSITION", lastPlayedPosition); // Pass last played position

        ContextCompat.startForegroundService(getContext(), intent);

        // Optionally, start PlayerActivity if you need to display it
        intent = new Intent(getContext(), PlayerActivity.class);
        intent.putExtra("SONG_DATA", songPath);
        intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(songPaths));
        intent.putExtra("CURRENT_SONG_POSITION", originalPosition);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.sort_name) {
            homeViewModel.sortByName();
            return true;
        } else if (itemId == R.id.sort_date) {
            homeViewModel.sortByDate();
            return true;
        } else if (itemId == R.id.sort_size) {
            homeViewModel.sortBySize();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

}

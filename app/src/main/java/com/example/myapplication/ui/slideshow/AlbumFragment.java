package com.example.myapplication.ui.slideshow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MusicService;
import com.example.myapplication.PlayerActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.SongData;
import com.example.myapplication.ui.gallery.PlaylistAdapter;
import com.example.myapplication.ui.home.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment implements Refreshable {

    private HomeViewModel homeViewModel;
    private AlbumAdapter albumAdapter;
    private RecyclerView recyclerView;
    private AlbumArtistSongAdapter songAdapter;
    private GridView gridView;
    private List<String> currentSongs;  // Store current songs to pass to MusicService
    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Find the menu item you want to modify
        MenuItem sortDateItem = menu.findItem(R.id.sort_date);
        if (gridView.getVisibility() == View.VISIBLE) {
            // Set visibility based on your condition
            sortDateItem.setVisible(false);  // or false to hide it
        }
        else if (recyclerView.getVisibility() == View.VISIBLE) {
            // Set visibility based on your condition
            sortDateItem.setVisible(true);  // or false to hide it
        }
        super.onPrepareOptionsMenu(menu);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        gridView = view.findViewById(R.id.gridViewAlbum);
        albumAdapter = new AlbumAdapter(getContext(), null, this::onAlbumSelected);
        gridView.setAdapter(albumAdapter);

        recyclerView = view.findViewById(R.id.recyclerViewAlbum);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize with an empty list and set the click listener
        songAdapter = new AlbumArtistSongAdapter(new ArrayList<>(), this::onSongSelected);
        recyclerView.setAdapter(songAdapter);

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        homeViewModel.getSongAlbum().observe(getViewLifecycleOwner(), albumList -> {
            albumAdapter.updateAlbumList(albumList);
        });

        return view;
    }

    @Override
    public void refresh() {
        resetView();
        homeViewModel.getSongAlbum().observe(getViewLifecycleOwner(), albumList -> {
            albumAdapter.updateAlbumList(albumList);
        });
    }

    private void resetView() {
        gridView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void onAlbumSelected(String albumName) {
        gridView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        homeViewModel.getSongsByAlbum(albumName).observe(getViewLifecycleOwner(), songDataList -> {
            if (songDataList != null && !songDataList.isEmpty()) {
                currentSongs = new ArrayList<>();
                for (SongData songData : songDataList) {
                    currentSongs.addAll(songData.getPath());
                }
                songAdapter.updateSongs(songDataList);  // Update adapter with SongData
            }
        });
    }

    private void onSongSelected(int position) {
        if (currentSongs != null && !currentSongs.isEmpty()) {
            // Start MusicService with the song data
            Intent intent = new Intent(requireContext(), MusicService.class);
            intent.setAction(MusicService.ACTION_PLAY);
            intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(currentSongs));
            intent.putExtra("CURRENT_SONG_POSITION", position); // Pass the selected song position
            requireContext().startService(intent);

            // Optionally, start PlayerActivity if you need to display it
            intent = new Intent(getContext(), PlayerActivity.class);
            intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(currentSongs));
            intent.putExtra("CURRENT_SONG_POSITION", position);
            requireContext().startActivity(intent);
        }
    }

    public void filterSongsOrAlbum(String query) {

        if (gridView.getVisibility() == View.VISIBLE) {
            Log.d("123", "123filterSongs called with query: " + query);
            albumAdapter.filter(query);
        }
        else {
            songAdapter.filter(query);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (gridView.getVisibility() == View.VISIBLE) {

            if (itemId == R.id.sort_name){
                Log.d("123", "123sort_name VISIBLE for album " );
                albumAdapter.sortByName();
            }
            else if (itemId == R.id.sort_size) {
                Log.d("123", "123sort_size VISIBLE for album " );
                albumAdapter.sortBySize();
            }
        }
        else {
            if (itemId == R.id.sort_name){
                Log.d("123", "123sort_name VISIBLE for album song " );
                songAdapter.sortByName();
            }
            else if (itemId == R.id.sort_size) {
                Log.d("123", "123sort_size VISIBLE for album song " );
                songAdapter.sortBySize();
            }
            else if (itemId == R.id.sort_date) {
                Log.d("123", "123sort_size VISIBLE for artist song ");
                songAdapter.sortByDate();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}

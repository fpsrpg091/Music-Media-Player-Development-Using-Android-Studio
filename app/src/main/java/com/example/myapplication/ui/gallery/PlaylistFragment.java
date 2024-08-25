package com.example.myapplication.ui.gallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MusicService;
import com.example.myapplication.PlayerActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.Playlist;
import com.example.myapplication.data.Song;
import com.example.myapplication.databinding.FragmentPlaylistBinding;
import com.example.myapplication.ui.home.HomeViewModel;
import com.example.myapplication.ui.home.SongAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {

    private FragmentPlaylistBinding binding;
    private PlaylistViewModel playlistViewModel;
    private AddSongAdapter songAdapter;
    private boolean showingSongs = false; // Flag to check if displaying songs
    private HomeViewModel homeViewModel;
    private static final String TAG = "PlaylistFragment";
    private List<String> songPaths;
    private List<Long> songSize;
    private List<String> songDate;
    private Button buttonAddSong;
    private int currentPlaylistId = -1; // Initialize with an invalid ID
    FloatingActionButton fab;
    private boolean isNameAscending = true;// To track the selected item
    private boolean isSizeAscending = true;
    private boolean isDateAscending = true;

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class); // Initialize HomeViewModel
        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        buttonAddSong = root.findViewById(R.id.button_add_song_to_playlist);

        Button buttonGoToPlaylist = root.findViewById(R.id.button_go_to_playlist);

        RecyclerView recyclerView = binding.recyclerViewPlaylists;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        homeViewModel.getSongList().observe(getViewLifecycleOwner(), songs -> {
            Log.d(TAG, "Songs LiveData updated");
            if (showingSongs) {
                displayAllSongs(songs); // Display songs if showingSongs is true
                Log.d(TAG, "Songs LiveData updated"+songs);
            }
        });

        homeViewModel.getSongPaths().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> paths) {
                songPaths = paths;
            }
        });

        homeViewModel.getSongSize().observe(getViewLifecycleOwner(), new Observer<List<Long>>() {
            @Override
            public void onChanged(List<Long> size) {
                songSize = size;
            }
        });

        homeViewModel.getSongDate().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> date) {
                songDate = date;
            }
        });

        // Observe the playlists LiveData
        playlistViewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
            Log.d(TAG, "123Songs LiveData updated");
            displaySongs(songs);
        });
        playlistViewModel.getPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            Log.d(TAG, "123Playlists LiveData updated");
            displayPlaylists(playlists);
        });

        FloatingActionButton fab = root.findViewById(R.id.action_add_playlist);
        fab.setOnClickListener(v -> {
            if (showingSongs) {
                Log.d(TAG, "onCreateView: go to add song");
                fab.setVisibility(View.INVISIBLE);
                buttonAddSong.setVisibility(View.VISIBLE);
                addSongToPlaylistButton();
            } else {
                Log.d(TAG, "onCreateView: go to add playlist");
                showAddPlaylistDialog();
            }
        });
        buttonAddSong.setOnClickListener(v -> {
            Log.d(TAG, "buttontest clicked");
            addSelectedSongsToPlaylist();
        });

        buttonGoToPlaylist.setOnClickListener(v -> {
            buttonAddSong.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.VISIBLE);
            clearSongList(); // Clear the song list
            showPlaylists(); // Refresh playlist
        });
        return root;
    }

    // New method to handle adding a song to a playlist
    private void addSongToPlaylistButton() {
        Log.d(TAG, "addSongToPlaylist called"+songPaths);
        // Fetch songs from HomeViewModel
        homeViewModel.loadSongs();
        // Set showingSongs to true to display songs
        showingSongs = true;
        updateUiForSongs();

    }

    private void addSelectedSongsToPlaylist() {
        View root = binding.getRoot();
        FloatingActionButton fab = root.findViewById(R.id.action_add_playlist);
        if (songAdapter != null) {
            List<Integer> selectedPositions = songAdapter.getSelectedSongPositions(); // Updated to get positions
            if (!selectedPositions.isEmpty()) {
                List<String> selectedSongs = new ArrayList<>();
                List<String> selectedSongPaths = new ArrayList<>();
                List<Long> selectedSongSize = new ArrayList<>();
                List<String> selectedSongDate = new ArrayList<>();

                for (int position : selectedPositions) {
                    selectedSongs.add(songAdapter.getSongNameAtPosition(position));
                    selectedSongPaths.add(songPaths.get(position)); // Get path using position
                    selectedSongSize.add(songSize.get(position));
                    selectedSongDate.add(songDate.get(position));
                    Log.d(TAG, "123selectedSongs: " + selectedSongSize);
                    Log.d(TAG, "123selectedSongs: " + selectedSongDate);
                }

                playlistViewModel.addSongsToPlaylist(selectedSongs, selectedSongPaths,selectedSongSize,selectedSongDate, currentPlaylistId);
                Toast.makeText(requireContext(), "Songs added to playlist", Toast.LENGTH_SHORT).show();
                showingSongs = false;
                updateUiForSongs();
                buttonAddSong.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);
            } else{
                Toast.makeText(requireContext(), "No songs selected", Toast.LENGTH_SHORT).show();
                showingSongs = true;
            }
        }
    }

    private void displayAllSongs(List<String> songList) {
        Log.d(TAG, "123displayAllSongs called with " + songList.size() + " songs");

        songAdapter = new AddSongAdapter(songList, (position, itemView) -> {
            Log.d(TAG, "Song clicked");
            // Handle song click event
        });

        RecyclerView songRecyclerView = binding.recyclerViewPlaylists;
        songRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        songRecyclerView.setAdapter(songAdapter);
        songAdapter.notifyDataSetChanged();

    }

    private void showAddPlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create Playlist");

        final EditText input = new EditText(requireContext());
        input.setHint("Enter playlist name");
        builder.setView(input);

        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String playlistName = input.getText().toString().trim();
                if (playlistName.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a name for the playlist.", Toast.LENGTH_SHORT).show();
                } else {
                    playlistViewModel.addPlaylist(playlistName);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void displayPlaylists(List<Playlist> playlistList) {
        Log.d(TAG, "displayPlaylists called with " + playlistList.size() + " playlists");

        PlaylistAdapter adapter = new PlaylistAdapter(playlistList,
                playlist -> {
                    Log.d(TAG, "Playlist clicked: " + playlist.getName());
                    Log.d(TAG, "Playlist clicked: " + playlist.getId());
                    // Store the selected playlist ID
                    currentPlaylistId = playlist.getId();
                    // Load songs for the selected playlist
                    playlistViewModel.loadSongsForPlaylist(currentPlaylistId);
                    showingSongs = true;
                    updateUiForSongs();
                },
                playlist -> {
                    Log.d(TAG, "Playlist long-pressed: " + playlist.getName());
                    showPlaylistOptionsDialog(playlist);
                }
        );

        RecyclerView recyclerView = binding.recyclerViewPlaylists;
        recyclerView.setAdapter(adapter);
        showingSongs = false;
        updateUiForSongs();
    }

    private void showPlaylistOptionsDialog(Playlist playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select an Option");
        builder.setItems(new CharSequence[]{"Rename", "Delete"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    showRenamePlaylistDialog(playlist);
                    break;
                case 1:
                    deletePlaylist(playlist);
                    break;
            }
        });
        builder.show();
    }

    private void showRenamePlaylistDialog(Playlist playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rename Playlist");

        final EditText input = new EditText(requireContext());
        input.setText(playlist.getName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                playlistViewModel.renamePlaylist(playlist.getId(), newName);
                Toast.makeText(requireContext(), "Playlist renamed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Please enter a new name", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deletePlaylist(Playlist playlist) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete this playlist?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    playlistViewModel.deletePlaylist(playlist.getId());
                    Toast.makeText(requireContext(), "Playlist deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void displaySongs(List<Song> songList) {
        Log.d(TAG, "123displaySongs called with " + songList.size() + " songs");

        // Convert Song list to song names
        List<String> songNames = new ArrayList<>();
        for (Song song : songList) {
            songNames.add(song.getName());
        }

        // Create a new adapter if it doesn't exist
        Log.d(TAG, "123Creating new songAdapter");
        songAdapter = new AddSongAdapter(songNames, (position, itemView) -> {
            Log.d(TAG, "123song clicked");
            playSongFromPosition(position, songList);
        });

        songAdapter.setOnSongLongClickListener((position, itemView) -> {
            Song selectedSong = songList.get(position);
            showSongOptionsDialog(selectedSong);
        });

        // Set the adapter and layout manager
        RecyclerView songRecyclerView = binding.recyclerViewPlaylists;
        songRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        songRecyclerView.setAdapter(songAdapter);

        // Notify the adapter of the data change
        songAdapter.notifyDataSetChanged();

        showingSongs = true;
        updateUiForSongs();

        songAdapter.setShowCheckboxes(false);
    }


    private void showSongOptionsDialog(Song song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select an Option");
        builder.setItems(new CharSequence[]{"Delete"}, (dialog, which) -> {
            if (which == 0) {
                deleteSongFromPlaylist(song);
            }
        });
        builder.show();
    }

    private void deleteSongFromPlaylist(Song song) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Song")
                .setMessage("Are you sure you want to delete this song from the playlist?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    playlistViewModel.deleteSongFromPlaylist(song.getId(), currentPlaylistId);
                    Toast.makeText(requireContext(), "Song deleted from playlist", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void playSongFromPosition(int position, List<Song> songList) {
        // Create a list of song paths
        ArrayList<String> songPaths = new ArrayList<>();
        for (Song song : songList) {
            songPaths.add(song.getPath());
        }

        // Ensure songPaths is not empty and position is valid
        if (songPaths.isEmpty() || position < 0 || position >= songPaths.size()) {
            Log.e("MusicPlayer", "Invalid position or songPaths is empty");
            return;
        }

        // Log originalPosition and songPaths for debugging
        Log.d("MusicPlayer", "Original Position: " + position);
        Log.d("MusicPlayer", "Song Paths: " + songPaths);

        // Start MusicService with the song data
        Intent intent = new Intent(requireContext(), MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putStringArrayListExtra("SONG_PATHS", songPaths);
        intent.putExtra("CURRENT_SONG_POSITION", position); // Pass the selected song position
        requireContext().startService(intent);


        // Optionally, start PlayerActivity if you need to display it, work
        intent = new Intent(getContext(), PlayerActivity.class);
        intent.putExtra("SONG_DATA", songPaths);
        intent.putStringArrayListExtra("SONG_PATHS", new ArrayList<>(songPaths));
        intent.putExtra("CURRENT_SONG_POSITION", position);
        requireContext().startActivity(intent);

        Log.d("MusicPlayer", "SONG_DATA: " + songPaths);
        Log.d("MusicPlayer", "SONG_PATHS " + new ArrayList<>(songPaths));
        Log.d("MusicPlayer", "CURRENT_SONG_POSITION: " + position);
    }


    private void showPlaylists() {
        Log.d(TAG, "123showPlaylists called");
        playlistViewModel.loadPlaylists();
        showingSongs = false;
        updateUiForSongs();
    }

    private void clearSongList() {
        Log.d(TAG, "123Clearing song list");
        if (songAdapter != null) {
            songAdapter.setSongList(new ArrayList<>()); // Clear the list
        }
    }

    private void updateUiForSongs() {
        Button buttonGoToPlaylist = getView().findViewById(R.id.button_go_to_playlist);
        boolean isShowingSongs = showingSongs;
        buttonGoToPlaylist.setVisibility(isShowingSongs ? View.VISIBLE : View.GONE);
        Log.d(TAG, "123updateUiForSongs: buttonGoToPlaylist visibility = " + (isShowingSongs ? "VISIBLE" : "GONE"));
        Log.d(TAG, "123updateUiForSongs: buttonGoToPlaylist visibility showingSongs = "+ showingSongs);

    }

    public void filterSongsOrPlaylist(String query) {

        if (songAdapter != null && showingSongs) {
            Log.d("123"+ TAG, "123filterSongs called with query: " + query);
            songAdapter.filter(query);
        }
        else if (!showingSongs) {
            PlaylistAdapter adapter = (PlaylistAdapter) binding.recyclerViewPlaylists.getAdapter();
            adapter.filter(query);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.sort_name) {
            if (!showingSongs) {
                PlaylistAdapter adapter = (PlaylistAdapter) binding.recyclerViewPlaylists.getAdapter();
                adapter.sortByName();
            }
            else{
                if (isNameAscending) {
                    playlistViewModel.loadSongsForPlaylistSortedByNameASC(currentPlaylistId);
                }
                else{
                    playlistViewModel.loadSongsForPlaylistSortedByNameDESC(currentPlaylistId);
                }
                isNameAscending = !isNameAscending;
            }
            return true;
        } else if (itemId == R.id.sort_date) {
            if (!showingSongs) {
                PlaylistAdapter adapter = (PlaylistAdapter) binding.recyclerViewPlaylists.getAdapter();
                adapter.sortByDate();
            }
            else{
                if (isDateAscending) {
                    playlistViewModel.loadSongsForPlaylistSortedByDateASC(currentPlaylistId);
                }
                else{
                    playlistViewModel.loadSongsForPlaylistSortedByDateDESC(currentPlaylistId);
                }
                isDateAscending = !isDateAscending;
            }
            return true;
        } else if (itemId == R.id.sort_size) {
            if (!showingSongs) {
                PlaylistAdapter adapter = (PlaylistAdapter) binding.recyclerViewPlaylists.getAdapter();
                adapter.sortBySize();
            }
            else{
                if (isSizeAscending) {
                    playlistViewModel.loadSongsForPlaylistSortedBySizeASC(currentPlaylistId);
                }
                else{
                    playlistViewModel.loadSongsForPlaylistSortedBySizeDESC(currentPlaylistId);
                }
                isSizeAscending = !isSizeAscending;
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }
    private void logRecyclerViewState() {
        RecyclerView recyclerView = binding.recyclerViewPlaylists;
        Log.d(TAG, "123RecyclerView adapter: " + recyclerView.getAdapter());
        Log.d(TAG, "123RecyclerView item count: " + (recyclerView.getAdapter() != null ? recyclerView.getAdapter().getItemCount() : "No adapter"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

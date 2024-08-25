package com.example.myapplication.ui.gallery;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.Playlist;
import com.example.myapplication.data.Song;
import com.example.myapplication.data.SongDao;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlaylistViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Playlist>> playlists;
    private final MutableLiveData<List<Song>> songs;
    private final AppDatabase database;

    public PlaylistViewModel(Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);
        playlists = new MutableLiveData<>();
        songs = new MutableLiveData<>();
        loadPlaylists();
    }

    public LiveData<List<Playlist>> getPlaylists() {
        return playlists;
    }

    public LiveData<List<Song>> getSongs() {
        return songs;
    }

    public void addPlaylist(String playlistName) {
        new Thread(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateTime = sdf.format(new Date());
            Playlist playlist = new Playlist(playlistName, currentDateTime, 0);
            database.playlistDao().insert(playlist);
            loadPlaylists();
        }).start();
    }

    public void renamePlaylist(int playlistId, String newName) {
        new Thread(() -> {
            database.playlistDao().updatePlaylistName(playlistId, newName);
            loadPlaylists(); // Refresh the playlist after renaming
        }).start();
    }

    public void deletePlaylist(int playlistId) {
        new Thread(() -> {
            // Delete all songs associated with the playlist
            database.songDao().deleteSongsByPlaylistId(playlistId);
            // Delete the playlist itself
            database.playlistDao().deletePlaylistById(playlistId);
            // Reload the playlists to update the UI
            loadPlaylists();
        }).start();
    }

    public void loadSongsForPlaylist(int playlistId) {
        new Thread(() -> {
            List<Song> songList = database.songDao().getSongsForPlaylist(playlistId);
            songs.postValue(songList);
        }).start();
    }

    void loadPlaylists() {
        new Thread(() -> {
            List<Playlist> playlistList = database.playlistDao().getAllPlaylists();
            playlists.postValue(playlistList);
            Log.d("PlaylistViewModel", "Playlists loaded: " + playlistList + " playlists");
        }).start();
    }

    public void addSongsToPlaylist(List<String> songNames, List<String> songPaths, List<Long> songSize,List<String> songDate,int playlistId) {
        new Thread(() -> {
            SongDao songDao = database.songDao();
            for (int i = 0; i < songNames.size(); i++) {
                String songName = songNames.get(i);
                String songPath = songPaths.get(i);
                Long songSizes = songSize.get(i);
                String songDates = songDate.get(i);

                // Check if the song already exists
                if (songDao.songExists(songName, songPath, playlistId) == 0) {
                    Song song = new Song(songName, songPath,songSizes,songDates,playlistId); // Create Song with name, path, and playlistId
                    songDao.insert(song);
                    // Update the size of the playlist
                    int currentSize = database.playlistDao().getPlaylistSize(playlistId);
                    int newSize= currentSize + 1;
                    database.playlistDao().updatePlaylistSize(playlistId, newSize);
                }
            }
            // Optionally reload songs for the current playlist
            loadSongsForPlaylist(playlistId);
        }).start();
    }

    public void loadSongsForPlaylistSortedByNameASC(int playlistId) {
        new Thread(() -> {
            List<Song> songList = database.songDao().getSongsForPlaylistSortedByNameASC(playlistId);
            songs.postValue(songList);
        }).start();
    }

    public void loadSongsForPlaylistSortedByDateASC(int playlistId) {
        new Thread(() -> {
            List<Song> songList = database.songDao().getSongsForPlaylistSortedByDateASC(playlistId);
            songs.postValue(songList);
        }).start();
    }

    public void loadSongsForPlaylistSortedBySizeASC(int playlistId) {
        new Thread(() -> {
            List<Song> songList = database.songDao().getSongsForPlaylistSortedBySizeASC(playlistId);
            songs.postValue(songList);
        }).start();
    }

    public void loadSongsForPlaylistSortedByNameDESC(int playlistId) {
        new Thread(() -> {
            List<Song> songList = database.songDao().getSongsForPlaylistSortedByNameDESC(playlistId);
            songs.postValue(songList);
        }).start();
    }

    public void loadSongsForPlaylistSortedByDateDESC(int playlistId) {
        new Thread(() -> {
            List<Song> songList = database.songDao().getSongsForPlaylistSortedByDateDESC(playlistId);
            songs.postValue(songList);
        }).start();
    }

    public void loadSongsForPlaylistSortedBySizeDESC(int playlistId) {
        new Thread(() -> {
            List<Song> songList = database.songDao().getSongsForPlaylistSortedBySizeDESC(playlistId);
            songs.postValue(songList);
        }).start();
    }

    public void deleteSongFromPlaylist(int songId,int playlistId) {
        new Thread(() -> {
            // Delete all songs associated with the playlist
            database.songDao().deleteSongsByIdAndPlaylistId(songId,playlistId);
            int currentSize = database.playlistDao().getPlaylistSize(playlistId);
            int newSize= currentSize - 1;
            database.playlistDao().updatePlaylistSize(playlistId, newSize);
            // Reload the playlists to update the UI
            loadPlaylists();
        }).start();
    }
}

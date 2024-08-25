package com.example.myapplication.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert
    void insert(Playlist playlist);

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    void updatePlaylistName(int playlistId, String newName);

    @Query("SELECT size FROM playlists WHERE id = :playlistId")
    int getPlaylistSize(int playlistId);

    @Query("UPDATE playlists SET size = :newSize WHERE id = :playlistId")
    void updatePlaylistSize(int playlistId, int newSize);

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    void deletePlaylistById(int playlistId);

    @Query("SELECT * FROM playlists ORDER BY creation_date DESC")
    List<Playlist> getPlaylistsSortedByDate();

    @Query("SELECT * FROM playlists ORDER BY size DESC")
    List<Playlist> getPlaylistsSortedBySize();

    @Query("SELECT * FROM playlists")
    List<Playlist> getAllPlaylists();
}


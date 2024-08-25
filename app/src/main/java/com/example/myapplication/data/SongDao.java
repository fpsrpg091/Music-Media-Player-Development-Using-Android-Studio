package com.example.myapplication.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {

    @Insert
    void insert(Song song);

    @Query("SELECT COUNT(*) FROM songs WHERE name = :songName AND path = :songPath AND playlistId = :playlistId")
    int songExists(String songName, String songPath, int playlistId);

    @Query("SELECT * FROM songs WHERE playlistId = :playlistId")
    List<Song> getSongsForPlaylist(int playlistId);

    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY name ASC")
    List<Song> getSongsForPlaylistSortedByNameASC(int playlistId);

    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY date ASC")
    List<Song> getSongsForPlaylistSortedByDateASC(int playlistId);

    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY size ASC")
    List<Song> getSongsForPlaylistSortedBySizeASC(int playlistId);

    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY name DESC")
    List<Song> getSongsForPlaylistSortedByNameDESC(int playlistId);

    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY date DESC")
    List<Song> getSongsForPlaylistSortedByDateDESC(int playlistId);

    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY size DESC")
    List<Song> getSongsForPlaylistSortedBySizeDESC(int playlistId);

    @Query("DELETE FROM songs WHERE playlistId = :playlistId")
    void deleteSongsByPlaylistId(int playlistId);

    @Query("DELETE FROM songs WHERE ID = :songId and playlistId = :playlistId")
    void deleteSongsByIdAndPlaylistId(int songId,int playlistId);
}

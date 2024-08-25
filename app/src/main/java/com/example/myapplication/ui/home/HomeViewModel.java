package com.example.myapplication.ui.home;

import android.app.Application;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.ui.SongData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<String>> songList;
    private MutableLiveData<List<String>> songPaths;
    private MutableLiveData<List<Long>> songSize;
    private MutableLiveData<List<String>> songDate;
    private MutableLiveData<List<String>> songAlbum;
    private MutableLiveData<List<String>> songArtist;
    private MutableLiveData<String> firstSongName;
    private ContentObserver contentObserver;
    private boolean isNameAscending = true;
    private boolean isSizeAscending = true;
    private boolean isDateAscending = true;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is song fragment");
        songList = new MutableLiveData<>();
        songPaths = new MutableLiveData<>();
        firstSongName = new MutableLiveData<>();
        songSize = new MutableLiveData<>();  // Initialize songSize
        songDate = new MutableLiveData<>();  // Initialize songDate
        songAlbum = new MutableLiveData<>();
        songArtist = new MutableLiveData<>();

        // Initialize the content observer
        contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.d("HomeViewModel", "ContentObserver detected a change");
                // Reload songs when a change is detected
                fetchSongs();
            }
        };

        // Register the observer
        application.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
        );
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<String>> getSongList() {
        return songList;
    }

    public LiveData<List<String>> getSongPaths() {
        return songPaths;
    }

    public LiveData<List<Long>> getSongSize() {
        return songSize;
    }

    public LiveData<List<String>> getSongDate() {
        return songDate;
    }

    public LiveData<List<String>> getSongAlbum() {
        return songAlbum;
    }

    public LiveData<List<String>> getSongArtist() {
        return songArtist;
    }

    public LiveData<String> getFirstSongName() {
        return firstSongName;
    }

    public void loadSongs() {
        fetchSongs();
    }

    private void fetchSongs() {
        List<String> songs = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        List<Long> songSizes = new ArrayList<>();
        List<String> songDates = new ArrayList<>();  // List to hold formatted song dates
        List<String> songAlbums = new ArrayList<>();
        List<String> songArtists = new ArrayList<>();

        Cursor cursor = getApplication().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.SIZE,      // Column for song size
                        MediaStore.Audio.Media.DATE_ADDED, // Column for song date
                        MediaStore.Audio.Media.ALBUM
                },
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
                int dateAddedIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

                // Safety check for indices
                if (titleIndex != -1 && artistIndex != -1 && dataIndex != -1 && sizeIndex != -1 && dateAddedIndex != -1) {
                    String title = cursor.getString(titleIndex);
                    String artist = cursor.getString(artistIndex);
                    String data = cursor.getString(dataIndex);
                    long size = cursor.getLong(sizeIndex);
                    long dateAdded = cursor.getLong(dateAddedIndex) * 1000L; // Convert to milliseconds
                    String album = cursor.getString(albumIndex);
                    // Check if file exists
                    if (new File(data).exists()) {
                        songs.add(title + "\n" + artist);
                        paths.add(data);
                        songSizes.add(size);

                        // Convert Unix timestamp to formatted date string
                        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(dateAdded));
                        songDates.add(formattedDate);  // Add formatted date to the list
                        songAlbums.add(album);
                        songArtists.add(artist);
                    }
                } else {
                    Log.e("HomeViewModel", "Invalid column index");
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d("HomeViewModel", "Fetched " + songs.size() + " songs"+songAlbums+songArtists);
        songList.postValue(songs);
        songPaths.postValue(paths);
        songSize.postValue(songSizes);
        songDate.postValue(songDates);
        songAlbum.postValue(songAlbums);
        songArtist.postValue(songArtists);

        // Set the first song name
        if (!songs.isEmpty()) {
            firstSongName.postValue(songs.get(0));
        } else {
            firstSongName.postValue(null);
        }

        // You can now use the `songSizes` and `songDates` lists as needed
    }

    public void sortByName() {
        List<String> songs = new ArrayList<>(songList.getValue());
        List<String> paths = new ArrayList<>(songPaths.getValue());

        // Combine song names with paths
        List<Pair<String, String>> combinedList = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            combinedList.add(new Pair<>(songs.get(i), paths.get(i)));
        }

        // Sorting by name
        if (isNameAscending) {
            Collections.sort(combinedList, (p1, p2) -> p1.first.compareTo(p2.first));
        } else {
            Collections.sort(combinedList, (p1, p2) -> p2.first.compareTo(p1.first));
        }

        // Toggle sorting order
        isNameAscending = !isNameAscending;

        // Extract sorted data
        List<String> sortedSongs = new ArrayList<>();
        List<String> sortedPaths = new ArrayList<>();
        for (Pair<String, String> pair : combinedList) {
            sortedSongs.add(pair.first);
            sortedPaths.add(pair.second);
        }

        songList.postValue(sortedSongs);
        songPaths.postValue(sortedPaths);
    }

    public void sortBySize() {
        // Ensure paths and artists are not null
        List<String> paths = new ArrayList<>(songPaths.getValue() != null ? songPaths.getValue() : Collections.emptyList());
        List<String> artists = new ArrayList<>(songArtist.getValue() != null ? songArtist.getValue() : Collections.emptyList());

        // Check that paths and artists lists are of the same size
        if (paths.size() != artists.size()) {
            Log.e("Error", "Paths and artists lists are of different sizes.");
            return;
        }

        List<Song> songs = new ArrayList<>();

        // Create Song objects for each file path and corresponding artist
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            String artist = artists.get(i);
            File file = new File(path);

            if (file.exists()) {
                songs.add(new Song(file.getName(), artist, file.length(), file.lastModified(), path));
                Log.d("123", "Added song: " + file.getName() + ", Artist: " + artist);
            } else {
                Log.w("Warning", "File does not exist: " + path);
            }
        }

        // Sort songs by size
        if (isSizeAscending) {
            Collections.sort(songs, Comparator.comparingLong(Song::getSize));
        } else {
            Collections.sort(songs, (s1, s2) -> Long.compare(s2.getSize(), s1.getSize()));
        }

        // Toggle sorting order
        isSizeAscending = !isSizeAscending;

        // Update the song list and paths
        List<String> sortedSongs = new ArrayList<>();
        List<String> sortedPaths = new ArrayList<>();
        for (Song song : songs) {
            sortedSongs.add(song.getName() + "\n" + song.getArtist());
            sortedPaths.add(song.getPath());
        }

        songList.postValue(sortedSongs);
        songPaths.postValue(sortedPaths);

        // Debugging logs
        Log.d("Sorting", "Sorted by Size:");
        for (int i = 0; i < sortedSongs.size(); i++) {
            Log.d("Sorting", "Sorted Song: " + sortedSongs.get(i) + ", Path: " + sortedPaths.get(i));
        }
    }

    public void sortByDate() {
        List<String> paths = new ArrayList<>(songPaths.getValue());
        List<String> artists = new ArrayList<>(songArtist.getValue());

        // Ensure both lists are not null and have the same size
        if (paths == null || artists == null || paths.size() != artists.size()) {
            Log.e("Error", "Paths and artists lists are either null or have different sizes.");
            return;
        }

        List<Song> songs = new ArrayList<>();

        // Populate the songs list
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            String artist = artists.get(i);
            File file = new File(path);
            if (file.exists()) {
                songs.add(new Song(file.getName(), artist, file.length(), file.lastModified(), path));
                Log.d("123", "Added Song: " + file.getName() + ", Artist: " + artist);
            }
        }

        // Sort songs by date
        if (isDateAscending) {
            Collections.sort(songs, (s1, s2) -> Long.compare(s1.getDate(), s2.getDate()));
        } else {
            Collections.sort(songs, (s1, s2) -> Long.compare(s2.getDate(), s1.getDate()));
        }

        // Toggle sorting order
        isDateAscending = !isDateAscending;

        // Update the song list and paths
        List<String> sortedSongs = new ArrayList<>();
        List<String> sortedPaths = new ArrayList<>();
        for (Song song : songs) {
            sortedSongs.add(song.getName()+ "\n"+song.getArtist()); // Update this if you want to include artist info
            sortedPaths.add(song.getPath());
        }
        songList.postValue(sortedSongs);
        songPaths.postValue(sortedPaths);

        // Debugging logs
        Log.d("Sorting", "Sorted by Date:");
        for (int i = 0; i < sortedSongs.size(); i++) {
            Log.d("Sorting", "Sorted Song: " + sortedSongs.get(i) + ", Path: " + sortedPaths.get(i));
        }
    }

    // Define a Song class to hold song data for sorting
    private static class Song {
        private String name;
        private String artist;
        private long size;
        private long date;
        private String path;

        public Song(String name, String artist,long size, long date, String path) {
            this.name = name;
            this.artist = artist;
            this.size = size;
            this.date = date;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getArtist() {
            return artist;
        }

        public long getSize() {
            return size;
        }

        public long getDate() {
            return date;
        }

        public String getPath() {
            return path;
        }
    }

    public LiveData<List<SongData>> getSongsByAlbum(String albumName) {
        MutableLiveData<List<SongData>> songsByAlbum = new MutableLiveData<>();
        new Thread(() -> {
            List<SongData> songDataList = new ArrayList<>();
            Cursor cursor = getApplication().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.DATE_MODIFIED,
                            MediaStore.Audio.Media.SIZE
                    },
                    MediaStore.Audio.Media.ALBUM + "=?",
                    new String[]{albumName},
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String date = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    SongData songData = new SongData(title + "\n" + artist, Collections.singletonList(data), date, size);
                    songDataList.add(songData);
                } while (cursor.moveToNext());
                cursor.close();
            }
            songsByAlbum.postValue(songDataList);
        }).start();

        return songsByAlbum;
    }

    public LiveData<List<SongData>> getSongsByArtist(String artistName) {
        MutableLiveData<List<SongData>> songsByArtist = new MutableLiveData<>();
        new Thread(() -> {
            List<SongData> songDataList = new ArrayList<>();
            Cursor cursor = getApplication().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.DATE_MODIFIED,
                            MediaStore.Audio.Media.SIZE
                    },
                    MediaStore.Audio.Media.ARTIST + "=?",
                    new String[]{artistName},
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String date = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    SongData songData = new SongData(title + "\n" + artist, Collections.singletonList(data), date, size);
                    songDataList.add(songData);
                } while (cursor.moveToNext());
                cursor.close();
            }
            songsByArtist.postValue(songDataList);
        }).start();

        return songsByArtist;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        // Unregister the content observer
        getApplication().getContentResolver().unregisterContentObserver(contentObserver);
    }
}

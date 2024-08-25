package com.example.myapplication.ui.slideshow;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

// AlbumAdapter.java
public class AlbumAdapter extends BaseAdapter {

    private Context context;
    private List<String> albumList;
    private List<String> filteredAlbumList;
    private OnAlbumClickListener onAlbumClickListener;
    private Map<String, Integer> albumSizeMap; // Map to store artist and their song count

    private boolean isNameAscending = true; // Track sorting order for name
    private boolean isSizeAscending = false; // Track sorting order for size

    public AlbumAdapter(Context context, List<String> albumList, OnAlbumClickListener onAlbumClickListener) {
        this.context = context;
        this.albumList = albumList != null ? albumList : new ArrayList<>(); // Initialize with empty list if null
        this.filteredAlbumList = new ArrayList<>();
        this.albumSizeMap = new HashMap<>();
        this.onAlbumClickListener = onAlbumClickListener;
        updateAlbumSizeMap(); // Initialize size map
        updateFilteredAlbumList(); // Initialize filtered list
    }

    private void updateAlbumSizeMap() {
        albumSizeMap.clear();
        if (albumList != null) {
            for (String artist : albumList) {
                // Count the occurrences of each artist
                int count = Collections.frequency(albumList, artist);
                // Add to map
                albumSizeMap.put(artist, count);
            }
            // Log the artistSizeMap to debug
            Log.d("123", "Updated artistSizeMap: " + albumSizeMap);
        }
    }

    private void updateFilteredAlbumList() {
        Set<String> uniqueArtists = new HashSet<>(albumList);
        filteredAlbumList.clear();
        filteredAlbumList.addAll(uniqueArtists);
    }

    public void filter(String query) {
        updateFilteredAlbumList();
        List<String> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(filteredAlbumList);
        } else {
            for (String album : filteredAlbumList) {
                if (album.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(album);
                }
            }
        }
        filteredAlbumList = filteredList;
        notifyDataSetChanged();
    }

    public void sortByName() {
        if (isNameAscending) {
            Collections.sort(filteredAlbumList); // Ascending
        } else {
            Collections.sort(filteredAlbumList, Collections.reverseOrder()); // Descending
        }
        isNameAscending = !isNameAscending; // Toggle sorting order
        notifyDataSetChanged();
    }

    public void sortBySize() {
        if (isSizeAscending) {
            Collections.sort(filteredAlbumList, (a, b) -> {
                int sizeA = albumSizeMap.getOrDefault(a, 0);
                int sizeB = albumSizeMap.getOrDefault(b, 0);
                return Integer.compare(sizeA, sizeB); // Ascending
            });
        } else {
            Collections.sort(filteredAlbumList, (a, b) -> {
                int sizeA = albumSizeMap.getOrDefault(a, 0);
                int sizeB = albumSizeMap.getOrDefault(b, 0);
                return Integer.compare(sizeB, sizeA); // Descending
            });
        }
        isSizeAscending = !isSizeAscending; // Toggle sorting order
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (filteredAlbumList != null) ? filteredAlbumList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return filteredAlbumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        }

        TextView albumName = convertView.findViewById(R.id.album_title);
        albumName.setText(filteredAlbumList.get(position));


        // Set onClick listener to handle album clicks
        convertView.setOnClickListener(v -> onAlbumClickListener.onAlbumClick(filteredAlbumList.get(position)));

        return convertView;
    }

    public void updateAlbumList(List<String> newAlbumList) {
        this.albumList = new ArrayList<>(newAlbumList);
        updateAlbumSizeMap();
        updateFilteredAlbumList();
        notifyDataSetChanged();
    }

    public interface OnAlbumClickListener {
        void onAlbumClick(String albumName);
    }
}

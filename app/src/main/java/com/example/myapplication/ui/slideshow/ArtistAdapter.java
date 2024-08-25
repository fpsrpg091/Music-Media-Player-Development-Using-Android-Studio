package com.example.myapplication.ui.slideshow;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArtistAdapter extends BaseAdapter {

    private Context context;
    private List<String> artistList;
    private List<String> filteredArtistList;
    private OnArtistClickListener OnArtistClickListener;
    private Map<String, Integer> artistSizeMap; // Map to store artist and their song count

    private boolean isNameAscending = true; // Track sorting order for name
    private boolean isSizeAscending = false; // Track sorting order for size

    public ArtistAdapter(Context context, List<String> artistList, OnArtistClickListener OnArtistClickListener) {
        this.context = context;
        this.artistList = artistList != null ? artistList : new ArrayList<>(); // Initialize with empty list if null
        this.filteredArtistList = new ArrayList<>();
        this.artistSizeMap = new HashMap<>();
        this.OnArtistClickListener = OnArtistClickListener;
        updateArtistSizeMap(); // Initialize size map
        updateFilteredArtistList(); // Initialize filtered list
    }

    private void updateArtistSizeMap() {
        artistSizeMap.clear();
        if (artistList != null) {
            for (String artist : artistList) {
                // Count the occurrences of each artist
                int count = Collections.frequency(artistList, artist);
                // Add to map
                artistSizeMap.put(artist, count);
            }
            // Log the artistSizeMap to debug
            Log.d("123", "Updated artistSizeMap: " + artistSizeMap);
        }
    }

    private void updateFilteredArtistList() {
        Set<String> uniqueArtists = new HashSet<>(artistList);
        filteredArtistList.clear();
        filteredArtistList.addAll(uniqueArtists);
    }

    public void filter(String query) {
        updateFilteredArtistList();
        List<String> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(filteredArtistList);
        } else {
            for (String artist : filteredArtistList) {
                if (artist.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(artist);
                }
            }
        }
        filteredArtistList = filteredList;
        notifyDataSetChanged();
    }

    public void sortByName() {
        if (isNameAscending) {
            Collections.sort(filteredArtistList); // Ascending
        } else {
            Collections.sort(filteredArtistList, Collections.reverseOrder()); // Descending
        }
        isNameAscending = !isNameAscending; // Toggle sorting order
        notifyDataSetChanged();
    }

    public void sortBySize() {
        if (isSizeAscending) {
            Collections.sort(filteredArtistList, (a, b) -> {
                int sizeA = artistSizeMap.getOrDefault(a, 0);
                int sizeB = artistSizeMap.getOrDefault(b, 0);
                return Integer.compare(sizeA, sizeB); // Ascending
            });
        } else {
            Collections.sort(filteredArtistList, (a, b) -> {
                int sizeA = artistSizeMap.getOrDefault(a, 0);
                int sizeB = artistSizeMap.getOrDefault(b, 0);
                return Integer.compare(sizeB, sizeA); // Descending
            });
        }
        isSizeAscending = !isSizeAscending; // Toggle sorting order
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (filteredArtistList != null) ? filteredArtistList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return filteredArtistList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
        }

        TextView artistName = convertView.findViewById(R.id.artist_title);
        artistName.setText(filteredArtistList.get(position));

        // Set onClick listener to handle artist clicks
        convertView.setOnClickListener(v -> OnArtistClickListener.OnArtistClick(filteredArtistList.get(position)));

        return convertView;
    }

    public void updateArtistList(List<String> newArtistList) {
        this.artistList = new ArrayList<>(newArtistList);
        updateArtistSizeMap();
        updateFilteredArtistList();
        notifyDataSetChanged();
    }

    public interface OnArtistClickListener {
        void OnArtistClick(String artistname);
    }
}

package com.example.myapplication.ui.slideshow;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.SongData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlbumArtistSongAdapter extends RecyclerView.Adapter<AlbumArtistSongAdapter.ViewHolder> {

    private List<SongData> songs;
    private List<SongData> filteredSongList;
    private OnItemClickListener onItemClickListener;

    private boolean isNameAscending = true;
    private boolean isSizeAscending = true;
    private boolean isDateAscending = true;

    public AlbumArtistSongAdapter(List<SongData> songs, OnItemClickListener onItemClickListener) {
        this.songs = songs;
        this.filteredSongList = new ArrayList<>(songs);
        this.onItemClickListener = onItemClickListener;
    }

    public void filter(String query) {
        filteredSongList.clear();
        if (query.isEmpty()) {
            filteredSongList.addAll(songs);
        } else {
            for (SongData song : songs) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredSongList.add(song);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void sortByName() {
        if (isNameAscending) {
            Collections.sort(filteredSongList, Comparator.comparing(SongData::getTitle));
        } else {
            Collections.sort(filteredSongList, Comparator.comparing(SongData::getTitle).reversed());
        }
        isNameAscending = !isNameAscending;
        notifyDataSetChanged();
    }

    public void sortBySize() {
        if (isSizeAscending) {
            Collections.sort(filteredSongList, Comparator.comparingLong(SongData::getSize));
        } else {
            Collections.sort(filteredSongList, Comparator.comparingLong(SongData::getSize).reversed());
        }
        isSizeAscending = !isSizeAscending;
        notifyDataSetChanged();
    }

    public void sortByDate() {
        if (isDateAscending) {
            Collections.sort(filteredSongList, Comparator.comparing(SongData::getDate));
        } else {
            Collections.sort(filteredSongList, Comparator.comparing(SongData::getDate).reversed());
        }
        isDateAscending = !isDateAscending;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongData song = filteredSongList.get(position);
        holder.textView.setText(song.getTitle());

        // Set default background
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));

        holder.itemView.setOnClickListener(v -> {
            int originalPosition = songs.indexOf(song); // Get the original position

            // Change background color to peach
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.peach));

            // Delay to reset background color
            new Handler().postDelayed(() -> {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
            }, 100); // 100 milliseconds delay

            onItemClickListener.onItemClick(originalPosition);
        });
    }


    @Override
    public int getItemCount() {
        return filteredSongList.size();
    }

    public void updateSongs(List<SongData> newSongs) {
        this.songs.clear();
        this.songs.addAll(newSongs);
        this.filteredSongList = new ArrayList<>(newSongs);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.song_title);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}

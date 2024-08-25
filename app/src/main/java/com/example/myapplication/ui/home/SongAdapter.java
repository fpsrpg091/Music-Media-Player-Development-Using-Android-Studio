package com.example.myapplication.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<String> songList;
    private List<String> filteredSongList;
    private OnSongClickListener onSongClickListener;

    public SongAdapter(List<String> songList, OnSongClickListener onSongClickListener) {
        this.songList = songList;
        this.filteredSongList = new ArrayList<>(songList);
        this.onSongClickListener = onSongClickListener;
    }

    public void setSongList(List<String> songList) {
        this.songList = songList;
        this.filteredSongList = new ArrayList<>(songList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredSongList.clear();
        if (query.isEmpty()) {
            filteredSongList.addAll(songList);
        } else {
            for (String song : songList) {
                if (song.toLowerCase().contains(query.toLowerCase())) {
                    filteredSongList.add(song);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        String song = filteredSongList.get(position);
        holder.songTitle.setText(song);

        holder.itemView.setOnClickListener(v -> {
            if (onSongClickListener != null) {
                // Pass the position of the song in the original list to the listener
                int originalPosition = songList.indexOf(song);
                onSongClickListener.onSongClick(originalPosition, holder.itemView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredSongList != null ? filteredSongList.size() : 0;
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle;

        SongViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.song_title);
        }
    }

    public interface OnSongClickListener {
        void onSongClick(int originalPosition, View itemView);
    }
}

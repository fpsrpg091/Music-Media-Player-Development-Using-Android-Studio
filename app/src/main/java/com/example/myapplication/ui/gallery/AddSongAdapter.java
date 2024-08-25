package com.example.myapplication.ui.gallery;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class AddSongAdapter extends RecyclerView.Adapter<AddSongAdapter.SongViewHolder> {

    private List<String> songList;
    private List<String> filteredSongList;
    private com.example.myapplication.ui.home.SongAdapter.OnSongClickListener onSongClickListener;
    private boolean showCheckboxes = true; // Flag to control checkbox visibility
    private List<String> selectedSongs = new ArrayList<>(); // List to store selected songs
    private OnSongLongClickListener onSongLongClickListener;

    public AddSongAdapter(List<String> songList, com.example.myapplication.ui.home.SongAdapter.OnSongClickListener onSongClickListener) {
        this.songList = songList;
        this.filteredSongList = new ArrayList<>(songList);
        this.onSongClickListener = onSongClickListener;
    }

    public void setShowCheckboxes(boolean showCheckboxes) {
        this.showCheckboxes = showCheckboxes;
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedSongPositions() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < selectedSongs.size(); i++) {
            positions.add(songList.indexOf(selectedSongs.get(i)));
        }
        return positions;
    }

    public String getSongNameAtPosition(int position) {
        return songList.get(position);
    }

    public List<String> getSelectedSongs() {
        return selectedSongs;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        String song = filteredSongList.get(position);
        holder.songTitle.setText(song);

        // Show or hide the checkbox based on the flag
        holder.checkBox.setVisibility(showCheckboxes ? View.VISIBLE : View.INVISIBLE);

        // Set the checkbox state
        holder.checkBox.setOnCheckedChangeListener(null); // Temporarily remove the listener
        holder.checkBox.setChecked(selectedSongs.contains(song));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedSongs.add(song);
                holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.peach));
            } else {
                selectedSongs.remove(song);
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        // Highlight the item if selected
        if (holder.checkBox.isChecked()) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.peach));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Handle item click to toggle the checkbox or reset background color
        holder.itemView.setOnClickListener(v -> {
            if (showCheckboxes) {
                boolean isChecked = !holder.checkBox.isChecked(); // Toggle the checkbox state
                holder.checkBox.setChecked(isChecked); // This will trigger the OnCheckedChangeListener above
            } else {
                // If checkboxes are not visible, reset the background color after a short delay
                holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.peach));
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                }, 150); // Change back to transparent after 150 milliseconds
            }

            // Simple click animation
            holder.itemView.animate().setDuration(100).alpha(0.7f).withEndAction(() ->
                    holder.itemView.animate().setDuration(100).alpha(1f)
            );

            // Trigger the click listener if needed
            if (onSongClickListener != null) {
                int originalPosition = songList.indexOf(song);
                onSongClickListener.onSongClick(originalPosition, holder.itemView);
            }
        });

        // Handle item long click
        holder.itemView.setOnLongClickListener(v -> {
            if (onSongLongClickListener != null) {
                int originalPosition = songList.indexOf(song);
                onSongLongClickListener.onSongLongClick(originalPosition, holder.itemView);

                // Change the background color on long press
                holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.peach));

                // Simple long-click animation
                holder.itemView.animate().setDuration(100).scaleX(0.95f).scaleY(0.95f).withEndAction(() ->
                        holder.itemView.animate().setDuration(100).scaleX(1f).scaleY(1f)
                );

                // Delay to reset the background color to transparent
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.transparent));
                }, 150); // Change back to transparent after 150 milliseconds

                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return filteredSongList != null ? filteredSongList.size() : 0;
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle;
        CheckBox checkBox;

        SongViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.song_title);
            checkBox = itemView.findViewById(R.id.checkBox); // Find checkbox
        }
    }

    public interface OnSongClickListener {
        void onSongClick(int originalPosition, View itemView);
    }

    public interface OnSongLongClickListener {
        void onSongLongClick(int position, View itemView);
    }

    public void setOnSongLongClickListener(OnSongLongClickListener listener) {
        this.onSongLongClickListener = listener;
    }
}

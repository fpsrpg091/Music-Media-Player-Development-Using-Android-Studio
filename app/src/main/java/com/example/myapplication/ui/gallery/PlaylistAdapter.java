package com.example.myapplication.ui.gallery;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlistNames;
    private List<Playlist> filteredList;
    private final OnPlaylistClickListener onPlaylistClickListener;
    private final OnPlaylistLongClickListener onPlaylistLongClickListener;
    private int selectedPosition = -1;
    private boolean isNameAscending = true;// To track the selected item
    private boolean isSizeAscending = true;
    private boolean isDateAscending = true;

    public PlaylistAdapter(List<Playlist> playlistNames, OnPlaylistClickListener onPlaylistClickListener, OnPlaylistLongClickListener onPlaylistLongClickListener) {
        this.playlistNames = playlistNames;
        this.filteredList = new ArrayList<>(playlistNames);
        this.onPlaylistClickListener = onPlaylistClickListener;
        this.onPlaylistLongClickListener = onPlaylistLongClickListener;
    }

    public void setSongList(List<Playlist> songList) {
        this.playlistNames = playlistNames;
        this.filteredList = new ArrayList<>(playlistNames);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        this.filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(playlistNames);
        } else {
            for (Playlist playlist : playlistNames) {
                if (playlist.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(playlist);
                }
            }
        }
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = filteredList.get(position);
        holder.playlistTitle.setText(playlist.getName()+"\n"+playlist.getSize()+" Songs");

        // Change color based on the selected position
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.peach)); // Selected color
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Default color
        }

        holder.itemView.setOnClickListener(v -> {
            // Update the selected position and notify the adapter
            selectedPosition = position;
            notifyDataSetChanged(); // Refresh the list to update the background color

            // Trigger the click listener
            onPlaylistClickListener.onPlaylistClick(playlist);

            // Reset the color after the dialog is shown
            v.postDelayed(() -> {
                selectedPosition = -1;  // Reset the selected position
                notifyDataSetChanged(); // Refresh the list again to remove the background color
            }, 100); // Adjust delay as needed to match dialog display timing
        });

        holder.itemView.setOnLongClickListener(v -> {
            // Set the background color immediately on long press
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.peach)); // Long press color

            // Trigger the long click listener
            onPlaylistLongClickListener.onPlaylistLongClick(playlist);

            // Simple long-click animation
            holder.itemView.animate()
                    .setDuration(100)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .withEndAction(() -> {
                        // Reset the animation after scaling back
                        holder.itemView.animate()
                                .setDuration(100)
                                .scaleX(1f)
                                .scaleY(1f)
                                .withEndAction(() -> {
                                    // Delay the reset to allow user to see the change before reverting
                                    holder.itemView.postDelayed(() -> {
                                        // Reset the background color after the delay
                                        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                                    }, 100); // Adjust the delay as necessary
                                });
                    });

            return true;
        });

    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public interface OnPlaylistLongClickListener {
        void onPlaylistLongClick(Playlist playlist);
    }

    public void sortByName() {
        // Sorting by name
        if (isNameAscending) {
            Collections.sort(filteredList, (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        } else {
            Collections.sort(filteredList, (p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
        }

        // Toggle sorting order
        isNameAscending = !isNameAscending;
        notifyDataSetChanged();
    }

    public void sortByDate() {
        if (isDateAscending) {
            // Sort by creation date in ascending order
            Collections.sort(filteredList, (p1, p2) -> p1.getCreationDate().compareToIgnoreCase(p2.getCreationDate()));
        } else {
            // Sort by creation date in descending order
            Collections.sort(filteredList, (p1, p2) -> p2.getCreationDate().compareToIgnoreCase(p1.getCreationDate()));
        }

        // Toggle sorting order
        isDateAscending = !isDateAscending;
        notifyDataSetChanged();
    }

    public void sortBySize() {
        if (isSizeAscending) {
            // Sort by size in ascending order
            Collections.sort(filteredList, (p1, p2) -> Integer.compare(p1.getSize(), p2.getSize()));
        } else {
            // Sort by size in descending order
            Collections.sort(filteredList, (p1, p2) -> Integer.compare(p2.getSize(), p1.getSize()));
        }

        // Toggle sorting order
        isSizeAscending = !isSizeAscending;
        notifyDataSetChanged();
    }


    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView playlistTitle;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            playlistTitle = itemView.findViewById(R.id.Playlist_title);
        }
    }
}

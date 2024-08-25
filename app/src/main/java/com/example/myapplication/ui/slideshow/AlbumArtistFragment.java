package com.example.myapplication.ui.slideshow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentAlbumArtistBinding;

public class AlbumArtistFragment extends Fragment {

    private FragmentAlbumArtistBinding binding;
    private ViewPager2 viewPager;
    private AlbumArtistPagerAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlbumArtistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewPager = binding.viewPager;
        adapter = new AlbumArtistPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        // Set initial tab colors
        setTabColors(0);

        // Add Tab click listeners
        binding.albumTab.setOnClickListener(v -> {
            viewPager.setCurrentItem(0);
            setTabColors(0);
            // Refresh album list if the tab was clicked again
            refreshFragments();
        });
        binding.artistTab.setOnClickListener(v -> {
            viewPager.setCurrentItem(1);
            setTabColors(1);
            // Refresh artist list if the tab was clicked again
            refreshFragments();
        });

        // Listen to ViewPager page changes to keep tabs synchronized
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setTabColors(position);
            }
        });

        return root;
    }

    private void setTabColors(int selectedIndex) {
        if (selectedIndex == 0) {
            binding.albumTab.setBackgroundColor(getResources().getColor(R.color.peach));
            binding.artistTab.setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            binding.albumTab.setBackgroundColor(getResources().getColor(R.color.white));
            binding.artistTab.setBackgroundColor(getResources().getColor(R.color.peach));
        }
    }

    private void refreshFragments() {
        Fragment albumFragment = adapter.getFragment(0);
        Fragment artistFragment = adapter.getFragment(1);
        if (albumFragment instanceof Refreshable) {
            ((Refreshable) albumFragment).refresh();
        }
        if (artistFragment instanceof Refreshable) {
            ((Refreshable) artistFragment).refresh();
        }
    }

    public void filterAlbumOrArtist(String query) {
        int currentItem = viewPager.getCurrentItem();
        Fragment currentFragment = adapter.getFragment(currentItem);

        if (currentFragment instanceof AlbumFragment) {
            ((AlbumFragment) currentFragment).filterSongsOrAlbum(query);
        } else if (currentFragment instanceof ArtistFragment) {
            ((ArtistFragment) currentFragment).filterSongsOrArtist(query);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.qolzy.ui.add_post;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.qolzy.ui.add_post.add_post.AddPostEntityFragment;
import com.example.qolzy.ui.add_post.add_story.AddStoryFragment;

public class CreatePagerAdapter extends FragmentStateAdapter {
    public CreatePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new AddPostEntityFragment();
            case 1: return new AddStoryFragment();

            default: return new AddPostFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

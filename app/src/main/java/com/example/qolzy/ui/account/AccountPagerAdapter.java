package com.example.qolzy.ui.account;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.qolzy.data.model.User;
import com.example.qolzy.ui.account.tab.posts_history.PostsHistoryFragment;
import com.example.qolzy.ui.account.tab.reels_history.ReelsHistoryFragment;
import com.example.qolzy.ui.account.tab.stories_history.StoriesHistoryFragment;

public class AccountPagerAdapter extends FragmentStateAdapter {

    private User user;

    public AccountPagerAdapter(@NonNull FragmentActivity fragmentActivity, User user) {
        super(fragmentActivity);
        this.user = user;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return PostsHistoryFragment.newInstance(user.getId());
            case 1:
                return StoriesHistoryFragment.newInstance(user.getId());
            case 2:
//                return ReelsHistoryFragment.newInstance(user.getId());
            default:
                return PostsHistoryFragment.newInstance(user.getId());
        }
    }

    @Override
    public int getItemCount() {
        return 3; // 3 tab: Posts, Stories, Reels
    }
}


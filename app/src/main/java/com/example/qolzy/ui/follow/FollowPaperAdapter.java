package com.example.qolzy.ui.follow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.qolzy.data.model.User;
import com.example.qolzy.ui.follow.tab.follower.FollowerFragment;
import com.example.qolzy.ui.follow.tab.following.FollowingFragment;

public class FollowPaperAdapter extends FragmentStateAdapter {
    private User user;
    public FollowPaperAdapter(@NonNull FragmentActivity fragmentActivity, User user) {
        super(fragmentActivity);
        this.user = user;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return FollowerFragment.newInstance(user);
            case 1:
                return FollowingFragment.newInstance(user);
            default:
                return FollowerFragment.newInstance(user);
        }
    }


    @Override
    public int getItemCount() {
        return 2;
    }
}

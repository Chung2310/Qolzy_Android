package com.example.qolzy.ui.follow.tab.follower;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.data.model.FollowResponse;
import com.example.qolzy.data.model.User;
import com.example.qolzy.databinding.FragmentFollowerBinding;
import com.example.qolzy.ui.follow.tab.FollowAdapter;
import com.example.qolzy.ui.follow.tab.FollowViewModel;

import java.util.ArrayList;
import java.util.List;

public class FollowerFragment extends Fragment {

    private FollowViewModel mViewModel;
    private FragmentFollowerBinding binding;
    private FollowAdapter followAdapter;
    private List<FollowResponse> userList = new ArrayList<>();
    private User user;
    private int page = 0, size = 10;

    public static FollowerFragment newInstance(User user) {
        FollowerFragment fragment = new FollowerFragment();
        Bundle args = new Bundle();
        args.putSerializable("USER", user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFollowerBinding.inflate(inflater, container, false);

        followAdapter = new FollowAdapter(userList, getContext());
        binding.recyclerViewFollower.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFollower.setAdapter(followAdapter);

        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("USER");
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FollowViewModel.class);

        mViewModel.getFollowerByUserId(user.getId(), page, size);

        mViewModel.getUserListLiveData().observe(getViewLifecycleOwner(), response ->{
            followAdapter.updateFollow(response);
        });
    }

}
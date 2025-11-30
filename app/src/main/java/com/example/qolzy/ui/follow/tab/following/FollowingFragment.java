package com.example.qolzy.ui.follow.tab.following;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.R;
import com.example.qolzy.data.model.FollowResponse;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentFollowerBinding;
import com.example.qolzy.databinding.FragmentFollowingBinding;
import com.example.qolzy.ui.account.AccountFragment;
import com.example.qolzy.ui.account.AccountViewModel;
import com.example.qolzy.ui.follow.tab.FollowAdapter;
import com.example.qolzy.ui.follow.tab.FollowViewModel;
import com.example.qolzy.ui.home.HomeViewModel;
import com.example.qolzy.ui.message.DetailMessageFragment;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {

    private FollowViewModel mViewModel;
    private HomeViewModel homeViewModel;
    private AccountViewModel accountViewModel;
    private FragmentFollowingBinding binding;
    private FollowAdapter followAdapter;
    private List<FollowResponse> userList = new ArrayList<>();
    private User user;
    private Long userId;
    private UserRepository userRepository;
    private int page = 0, size = 10;

    public static FollowingFragment newInstance(User user) {
        FollowingFragment fragment = new FollowingFragment();
        Bundle args = new Bundle();
        args.putSerializable("USER", user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFollowingBinding.inflate(inflater, container, false);

        userRepository = new UserRepository(getContext());
        userId = (long) userRepository.getUserId();

        followAdapter = new FollowAdapter(userList, getContext());
        binding.recyclerViewFollowing.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFollowing.setAdapter(followAdapter);

        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("USER");
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FollowViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        mViewModel.getFollowingByUserId(user.getId(), page, size);

        mViewModel.getUserListLiveData().observe(getViewLifecycleOwner(), response ->{
            followAdapter.updateFollow(response);
        });

        followAdapter.setOnFollowItemActionListner(new FollowAdapter.OnFollowItemActionListner() {
            @Override
            public void onAvatarClicked(User user) {
                openAccountFragment(user, true);
            }

            @Override
            public void onUserNameClicked(User user) {
                openAccountFragment(user, true);
            }

            @Override
            public void onNameClicked(User user) {
                openAccountFragment(user, true);
            }

            @Override
            public void onMessageClicked(User user) {
                openMessageFragment(user);
            }

            @Override
            public void onUnFollow(User user) {
                homeViewModel.toggleFollow(userId ,user.getId());
            }
        });
    }

    public void openAccountFragment(User user, Boolean followByCurrentUser){
        AccountFragment fragment = new AccountFragment();

        Bundle args = new Bundle();
        args.putSerializable("USER", user);
        args.putBoolean("followByCurrentUser", followByCurrentUser);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

    }

    public void openMessageFragment(User user){
        DetailMessageFragment fragment = new DetailMessageFragment();

        accountViewModel.createContact(userId, user.getId());

        Bundle args = new Bundle();
        args.putSerializable("contact", user);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

    }
}
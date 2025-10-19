package com.example.qolzy.ui.account.tab.posts_history;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.R;
import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.User;
import com.example.qolzy.databinding.FragmentPostsHistoryBinding;
import com.example.qolzy.ui.post.PostAdapter;
import com.example.qolzy.util.Utils;
import com.google.android.exoplayer2.ExoPlayer;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PostsHistoryFragment extends Fragment {
    private static final String ARG_USER_ID = "userId";
    private Long userId;
    private FragmentPostsHistoryBinding binding;
    private PostsHistoryViewModel mViewModel;
    private PostHistoryAdapter postAdapter;
    private List<Post> posts = new ArrayList<>();
    private int page = 0, size = 10;
    private ExoPlayer exoPlayer;

    public static PostsHistoryFragment newInstance(Long id) {
        PostsHistoryFragment fragment = new PostsHistoryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        if (getArguments() != null) {
            userId = getArguments().getLong(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostsHistoryBinding.inflate(inflater, container, false);

        postAdapter = new PostHistoryAdapter(requireContext(),posts, exoPlayer);
        binding.recyclerViewPostsHistory.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewPostsHistory.setAdapter(postAdapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PostsHistoryViewModel.class);

        if (userId != null) {
            mViewModel.getPostsHistoryByUserId(userId, page, size);
        }

        mViewModel.getPostsLiveData().observe(getViewLifecycleOwner(), postsLive -> {
            postAdapter.updatePosts(postsLive);
        });
    }
}

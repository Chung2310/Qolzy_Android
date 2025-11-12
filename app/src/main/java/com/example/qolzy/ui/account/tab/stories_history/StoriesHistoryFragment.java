package com.example.qolzy.ui.account.tab.stories_history;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.data.model.Story;
import com.example.qolzy.databinding.FragmentStoriesHistoryBinding;
import com.example.qolzy.ui.story.StoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class StoriesHistoryFragment extends Fragment {
    private static final String ARG_USER_ID = "userId";
    private Long userId;
    private FragmentStoriesHistoryBinding binding;
    private StoriesHistoryViewModel mViewModel;
    private StoryAdapter storyAdapter;
    private List<Story> stories = new ArrayList<>();
    private int page = 0; int size = 10;

    public static StoriesHistoryFragment newInstance(Long id) {
        StoriesHistoryFragment fragment = new StoriesHistoryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getLong(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStoriesHistoryBinding.inflate(inflater, container, false);

        storyAdapter = new StoryAdapter(stories, requireContext());
        binding.recyclerViewStoriesHistory.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewStoriesHistory.setAdapter(storyAdapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(StoriesHistoryViewModel.class);

        if(userId != null){
            mViewModel.getStoriesHistory(userId, page, size);
        }

        mViewModel.getStoriesLiveData().observe(getViewLifecycleOwner(), storiesLive ->{
            storyAdapter.updateStories(storiesLive);
        });
    }

}
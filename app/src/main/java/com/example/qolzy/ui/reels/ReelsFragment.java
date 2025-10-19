package com.example.qolzy.ui.reels;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.R;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentReelsBinding;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

public class ReelsFragment extends Fragment {

    private ReelsViewModel mViewModel;
    private FragmentReelsBinding binding;
    private ReelsAdapter adapter;
    private List<Reel> reelsList = new ArrayList<>();
    private UserRepository userRepository;
    private ExoPlayer exoPlayer;
    private int page =0, size = 10;

    public static ReelsFragment newInstance() {
        return new ReelsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReelsBinding.inflate(inflater, container, false);
        userRepository = new UserRepository(requireContext());
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ReelsViewModel.class);

        adapter = new ReelsAdapter(requireContext(), reelsList, exoPlayer);
        binding.recyclerReels.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false));
        binding.recyclerReels.setAdapter(adapter);

        mViewModel.getReels((long)userRepository.getUserId(), page, size);

        mViewModel.getReelsLiveData().observe(getViewLifecycleOwner(), response -> {
            adapter.updatePosts(response);
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        detachPlayerFromViews();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        detachPlayerFromViews();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void detachPlayerFromViews() {
        // Tách player khỏi tất cả PlayerView trong RecyclerView
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerReels.getLayoutManager();
        if (layoutManager != null) {
            for (int i = 0; i < binding.recyclerReels.getChildCount(); i++) {
                View child = binding.recyclerReels.getChildAt(i);
                PlayerView playerView = child.findViewById(R.id.playerView);
                if (playerView != null) {
                    playerView.setPlayer(null);
                }
            }
        }
    }


}
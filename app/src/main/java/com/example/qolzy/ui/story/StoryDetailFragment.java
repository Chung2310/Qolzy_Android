package com.example.qolzy.ui.story;

import androidx.lifecycle.ViewModelProvider;

import android.animation.ObjectAnimator;
import android.media.browse.MediaBrowser;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.qolzy.R;
import com.example.qolzy.data.model.Story;
import com.example.qolzy.databinding.FragmentStoryDetailBinding;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

public class StoryDetailFragment extends Fragment {
    private StoryDetailViewModel mViewModel;
    private FragmentStoryDetailBinding binding;
    private List<Story> storyList = new ArrayList<>();
    private int currentIndex = 0;
    private ImageView storyImage;
    private PlayerView storyVideo;
    private SimpleExoPlayer player;
    private LinearLayout progressContainer;
    private Handler handler = new Handler();
    private boolean isPaused = false;

    public static StoryDetailFragment newInstance() {
        return new StoryDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStoryDetailBinding.inflate(inflater,container,false);
        if (getArguments() != null) {
            storyList = (List<Story>) getArguments().getSerializable("story_list");
            Long storyId = getArguments().getLong("story_id");
            for (int i = 0; i < storyList.size(); i++) {
                if (storyList.get(i).getId().equals(storyId)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        return binding.getRoot();
    }

    private Runnable nextStoryRunnable = new Runnable() {
        @Override
        public void run() {
            showNextStory();
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(StoryDetailViewModel.class);
        // TODO: Use the ViewModel
    }

    private void initProgressBars() {
        progressContainer.removeAllViews();
        for (int i = 0; i < storyList.size(); i++) {
            ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            params.setMargins(4, 0, 4, 0);
            pb.setLayoutParams(params);
            pb.setMax(1000);
            pb.setProgress(i < currentIndex ? 1000 : 0);
            progressContainer.addView(pb);
        }
    }

    private void showStory(int index) {
        if (index < 0 || index >= storyList.size()) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        resetPlayer();
        Story story = storyList.get(index);

        for (int i = 0; i < progressContainer.getChildCount(); i++) {
            ProgressBar pb = (ProgressBar) progressContainer.getChildAt(i);
            pb.setProgress(i < index ? 1000 : 0);
        }

        // Phát video
        player = new SimpleExoPlayer.Builder(requireContext()).build();
        storyVideo.setPlayer(player);
        MediaItem item = MediaItem.fromUri(story.getMedia());
        player.setMediaItem(item);
        player.prepare();
        player.play();

        // Thanh tiến trình chạy
        animateProgress(index, story.getDuration());
    }

    private void animateProgress(int index, int durationSec) {
        ProgressBar pb = (ProgressBar) progressContainer.getChildAt(index);
        ObjectAnimator animator = ObjectAnimator.ofInt(pb, "progress", 0, 1000);
        animator.setDuration(durationSec * 1000L);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();

        handler.postDelayed(nextStoryRunnable, durationSec * 1000L);
    }

    private void showNextStory() {
        if (currentIndex < storyList.size() - 1) {
            currentIndex++;
            showStory(currentIndex);
        } else {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void showPreviousStory() {
        if (currentIndex > 0) {
            currentIndex--;
            showStory(currentIndex);
        }
    }

    private void resetPlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        handler.removeCallbacks(nextStoryRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        if (player != null) player.pause();
        handler.removeCallbacks(nextStoryRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPaused) {
            isPaused = false;
            if (player != null) player.play();
            handler.postDelayed(nextStoryRunnable, storyList.get(currentIndex).getDuration() * 1000L);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetPlayer();
    }
}
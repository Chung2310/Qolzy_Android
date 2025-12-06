package com.example.qolzy.ui.reels;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.qolzy.R;
import com.example.qolzy.data.model.Reel;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentReelsBinding;
import com.example.qolzy.ui.account.AccountFragment;
import com.example.qolzy.ui.comment.CommentsBottomSheet;
import com.example.qolzy.ui.home.HomeViewModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

public class ReelsFragment extends Fragment {

    private ReelsViewModel viewModel;
    private FragmentReelsBinding binding;
    private ReelsAdapter adapter;
    private final List<Reel> reelsList = new ArrayList<>();

    private UserRepository userRepository;
    private ExoPlayer exoPlayer;

    private int page = 0;
    private int size = 5;
    private HomeViewModel homeViewModel;
    private PagerSnapHelper snapHelper;

    public static ReelsFragment newInstance() {
        return new ReelsFragment();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentReelsBinding.inflate(inflater, container, false);
         snapHelper = new PagerSnapHelper();
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);

        binding.recyclerReels.setLayoutManager(layoutManager);
        binding.recyclerReels.setHasFixedSize(true);
        binding.recyclerReels.setItemViewCacheSize(3);


        snapHelper.attachToRecyclerView(binding.recyclerReels);
        userRepository = new UserRepository(requireContext());

        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);

        setupRecyclerView();

        return binding.getRoot();
    }

    private void setupRecyclerView() {

        adapter = new ReelsAdapter(requireContext(), reelsList, exoPlayer);
        binding.recyclerReels.setAdapter(adapter);

        binding.recyclerReels.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Lấy snapView
                    View snapView = snapHelper.findSnapView(
                            ((LinearLayoutManager) recyclerView.getLayoutManager())
                    );
                    if (snapView != null) {
                        int position = recyclerView.getLayoutManager().getPosition(snapView);
                        adapter.playVideoAtPosition(position);
                    }
                }
            }
        });



    }

    private void playSnapPosition() {
        if (binding == null) return;

        LinearLayoutManager layoutManager =
                (LinearLayoutManager) binding.recyclerReels.getLayoutManager();
        if (layoutManager == null) return;

        View snapView = snapHelper.findSnapView(layoutManager);
        if (snapView == null) return;

        int position = layoutManager.getPosition(snapView);
        adapter.playVideoAtPosition(position);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel = new ViewModelProvider(this).get(ReelsViewModel.class);

        viewModel.getReels(
                (long) userRepository.getUserId(),
                page,
                size
        );

        viewModel.getReelsLiveData().observe(
                getViewLifecycleOwner(),
                reels -> {
                    if (reels == null || reels.isEmpty()) return;

                    adapter.updatePosts(reels);

                    // Tự động play item đầu tiên khi load xong
                    binding.recyclerReels.post(() -> adapter.playVideoAtPosition(0));
                }
        );

        adapter.setOnReelsActionListener(new ReelsAdapter.OnReelsActionListener() {
            @Override
            public void onLikeClicked(Long reelId) {
                homeViewModel.toggleLike("reel", (long) userRepository.getUserId(), reelId);
            }

            @Override
            public void onCommentClicked(Long reelId) {
                CommentsBottomSheet bottomSheet = new CommentsBottomSheet();
                Bundle args = new Bundle();
                args.putLong("postId", reelId);
                args.putString("mode", "reel");
                bottomSheet.setArguments(args);
                bottomSheet.show(getChildFragmentManager(), "CommentsBottomSheet");
            }

            @Override
            public void onUsernameClicked(User user, Boolean followByCurrentUser) {
                openAccountFragment(user, followByCurrentUser);
            }

            @Override
            public void onAvatarClicked(User user, Boolean followByCurrentUser) {
                openAccountFragment(user, followByCurrentUser);
            }
        });
    }

    public void openAccountFragment(User user, Boolean followByCurrentUser) {
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
    /* ================= LIFECYCLE ================= */

    @Override
    public void onResume() {
        super.onResume();
        binding.recyclerReels.post(this::playSnapPosition);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding.recyclerReels.setAdapter(null);

        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }

        binding = null;
    }

}

package com.example.qolzy.ui.home;

import static com.example.qolzy.R.id.nav_host_fragment;
import static com.example.qolzy.R.id.recyclerPosts;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.qolzy.R;
import com.example.qolzy.data.model.Contact;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.Story;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentHomeBinding;
import com.example.qolzy.ui.account.AccountFragment;
import com.example.qolzy.ui.comment.CommentsBottomSheet;
import com.example.qolzy.ui.message.ContactFragment;
import com.example.qolzy.ui.post.PostAdapter;
import com.example.qolzy.ui.story.StoryAdapter;
import com.example.qolzy.ui.story.StoryDetailFragment;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;

    private FragmentHomeBinding binding;

    private UserRepository userRepository;

    private int page = 0, size = 30;

    private int userId;
    private PostAdapter postAdapter;
    private StoryAdapter storyAdapter;
    private ExoPlayer exoPlayer;
    private LinearLayoutManager linearLayoutManager;
    private List<Post> posts;
    private PlayerView currentPlayerView;
    private int currentPlayingPosition = RecyclerView.NO_POSITION;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        initAutoPlayListener();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(HomeViewModel.class);

        userRepository = new UserRepository(requireContext());

        postAdapter = new PostAdapter(new ArrayList<>(), getContext(), exoPlayer);
        storyAdapter = new StoryAdapter(new ArrayList<>(), getContext());

        linearLayoutManager = new LinearLayoutManager(getContext());
        binding.recyclerPosts.setLayoutManager(linearLayoutManager);
        binding.recyclerPosts.setAdapter(postAdapter);

        binding.recyclerStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL , false));
        binding.recyclerStories.setAdapter(storyAdapter);

        postAdapter.setOnPostActionListener(new PostAdapter.OnPostActionListener() {
            @Override
            public void onLikeClicked(Long postId) {
                mViewModel.toggleLike("post", Long.parseLong(String.valueOf(userId)), postId);
                // TODO: gọi API update like ở đây
            }

            @Override
            public void onCommentClicked(Long postId) {

                CommentsBottomSheet bottomSheet = new CommentsBottomSheet();

                // Truyền postId qua BottomSheet
                Bundle args = new Bundle();
                args.putLong("postId", postId);
                bottomSheet.setArguments(args);

                // Mở BottomSheet trong Fragment
                bottomSheet.show(getChildFragmentManager(), "CommentsBottomSheet");
            }

            @Override
            public void onUsernameClicked(User user) {
                openAccountFragment(user);
            }

            @Override
            public void onAvatarClicked(User user) {
                openAccountFragment(user);
            }

            @Override
            public void onFollowClicked(Long followingId) {
                mViewModel.toggleFollow((long)userId,followingId);
            }
        });

        storyAdapter.setOnStoryActionListener(new StoryAdapter.OnStoryActionListener() {
            @Override
            public void onClicked(Long storyId, List<Story> stories) {
                StoryDetailFragment storyDetailFragment = new StoryDetailFragment();

                // Tạo Bundle để truyền dữ liệu
                Bundle bundle = new Bundle();
                bundle.putSerializable("story_list", (Serializable) stories);
                bundle.putLong("story_id", storyId);

                // Gắn bundle vào fragment
                storyDetailFragment.setArguments(bundle);

                // Chuyển fragment
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, storyDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }

        });

        ProgressBar progressBar = binding.progressBar;

        mViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Log.d("HomeFragment", msg+"");
            }
        });

        mViewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if(posts.size() > 0){
                postAdapter.updatePosts(posts);
                progressBar.setVisibility(View.GONE);
            }
            else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Không tải được bài viết", Toast.LENGTH_SHORT).show();
            }
        });

        mViewModel.getStoriesLiveData().observe(getViewLifecycleOwner(), stories -> {
            if (posts.size() > 0){
                storyAdapter.updateStories(stories);
                Log.d("HomeFragment", "Size story: " + stories.size()+" ");
            }
        });
        userId = userRepository.getUserId();

        mViewModel.getPosts(page,size,userId);
        mViewModel.getStory(userId);


        binding.btnMessager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactFragment contactFragment = new ContactFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, contactFragment)
                        .addToBackStack(null)
                        .commit();

            }
        });

    }

    private void initAutoPlayListener() {
        binding.recyclerPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    autoPlayVisibleVideo();
                }
            }
        });
    }

    private void autoPlayVisibleVideo() {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        int targetPos = -1;
        float maxVisibleArea = 0f;

        // tìm bài viết nào hiển thị rõ nhất trên màn hình
        for (int i = firstVisible; i <= lastVisible; i++) {
            View child = linearLayoutManager.findViewByPosition(i);
            if (child != null) {
                float visibleHeight = getVisibleHeightPercent(child);
                if (visibleHeight > maxVisibleArea) {
                    maxVisibleArea = visibleHeight;
                    targetPos = i;
                }
            }
        }

        if (targetPos != -1 && targetPos != currentPlayingPosition) {
            playVideoAtPosition(targetPos);
        }
    }

    private float getVisibleHeightPercent(View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        float visibleHeight = rect.height();
        return visibleHeight / view.getHeight();
    }

    private void playVideoAtPosition(int position) {
        // Dừng video hiện tại nếu đang phát
        if (currentPlayerView != null) {
            currentPlayerView.setPlayer(null);
            currentPlayerView = null;
        }

        // Lấy ViewHolder của bài post tại vị trí cần phát
        RecyclerView.ViewHolder vh = binding.recyclerPosts.findViewHolderForAdapterPosition(position);
        if (!(vh instanceof PostAdapter.PostViewHolder)) return;

        PostAdapter.PostViewHolder postHolder = (PostAdapter.PostViewHolder) vh;
        ViewPager2 pager = postHolder.itemView.findViewById(R.id.viewPagerMedia);

        // Lấy vị trí media hiện tại trong ViewPager (đang hiển thị)
        int currentMediaIndex = pager.getCurrentItem();

        // Lấy RecyclerView bên trong ViewPager2
        RecyclerView innerRv = (RecyclerView) pager.getChildAt(0);
        if (innerRv == null) return;

        // Lấy View đang hiển thị (chính là media hiện tại)
        View currentMediaView = innerRv.getLayoutManager().findViewByPosition(currentMediaIndex);
        if (currentMediaView == null) return;

        // Tìm PlayerView bên trong media hiện tại (nếu là video)
        PlayerView playerView = currentMediaView.findViewById(R.id.playerView);
        if (playerView == null) {
            // Nếu không có PlayerView → media là ảnh → không phát video
            exoPlayer.pause();
            currentPlayingPosition = RecyclerView.NO_POSITION;
            return;
        }

        // Gán player và phát
        currentPlayerView = playerView;
        currentPlayerView.setPlayer(exoPlayer);
        exoPlayer.setPlayWhenReady(true);

        currentPlayingPosition = position;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }


    public void openAccountFragment(User user){
        AccountFragment fragment = new AccountFragment();

// truyền userId qua Bundle
        Bundle args = new Bundle();
        args.putSerializable("USER", user);
        fragment.setArguments(args);

// mở fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // fragment_container là id FrameLayout chứa fragment
                .addToBackStack(null)
                .commit();

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
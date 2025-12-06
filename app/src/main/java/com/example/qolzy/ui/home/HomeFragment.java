package com.example.qolzy.ui.home;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.qolzy.R;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.Story;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentHomeBinding;
import com.example.qolzy.ui.account.AccountFragment;
import com.example.qolzy.ui.comment.CommentsBottomSheet;
import com.example.qolzy.ui.message.ContactFragment;
import com.example.qolzy.ui.notification.NotificationFragment;
import com.example.qolzy.ui.post.PostAdapter;
import com.example.qolzy.ui.story.StoryAdapter;
import com.example.qolzy.ui.story.StoryDetailFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    private FragmentHomeBinding binding;
    private UserRepository userRepository;

    private int page = 0, size = 10;
    private int userId;

    private PostAdapter postAdapter;
    private StoryAdapter storyAdapter;
    private LinearLayoutManager linearLayoutManager;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
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
        userId = userRepository.getUserId();

        // Load initial data
        mViewModel.getPosts(page, size, userId);
        mViewModel.getStory(userId);

        // Setup adapters
        postAdapter = new PostAdapter(new ArrayList<>(), getContext());
        storyAdapter = new StoryAdapter(new ArrayList<>(), getContext());

        // RecyclerView Posts
        binding.recyclerPosts.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        binding.recyclerPosts.setLayoutManager(linearLayoutManager);
        binding.recyclerPosts.setAdapter(postAdapter);

        // RecyclerView Stories
//        binding.recyclerStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        binding.recyclerStories.setAdapter(storyAdapter);

        // Post actions
        postAdapter.setOnPostActionListener(new PostAdapter.OnPostActionListener() {
            @Override
            public void onLikeClicked(Long postId) {
                mViewModel.toggleLike("post", (long) userId, postId);
            }

            @Override
            public void onCommentClicked(Long postId) {
                CommentsBottomSheet bottomSheet = new CommentsBottomSheet();
                Bundle args = new Bundle();
                args.putLong("postId", postId);
                args.putString("mode", "post");
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

            @Override
            public void onFollowClicked(Long followingId) {
                mViewModel.toggleFollow((long) userId, followingId);
            }
        });

        // Story actions
        storyAdapter.setOnStoryActionListener((storyId, stories) -> {
            StoryDetailFragment storyDetailFragment = new StoryDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("story_list", (Serializable) stories);
            bundle.putLong("story_id", storyId);
            storyDetailFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, storyDetailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Buttons
        binding.btnMessager.setOnClickListener(v -> {
            ContactFragment contactFragment = new ContactFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, contactFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnNotification.setOnClickListener(v -> {
            NotificationFragment notificationFragment = new NotificationFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, notificationFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Scroll listener for pagination
        binding.recyclerPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return;

                int visible = linearLayoutManager.getChildCount();
                int total = linearLayoutManager.getItemCount();
                int first = linearLayoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visible + first) >= total) {
                        loadMorePosts();
                    }
                }
            }
        });



        // Observers
        mViewModel.getPostsLiveData().observe(getViewLifecycleOwner(), newPosts -> {
            isLoading = false;
            binding.progressBar.setVisibility(View.GONE);

            if (newPosts == null || newPosts.isEmpty()) {
                isLastPage = true;
                return;
            }

            if (page == 0) {
                postAdapter.updatePosts(newPosts);
            } else {
                postAdapter.addPosts(newPosts);
                Log.d("scroll", "size list load more: "+newPosts.size());
            }
            binding.progressBar.setVisibility(View.GONE);
            if (newPosts.size() < size) {
                isLastPage = true;
            }
        });

        mViewModel.getStoriesLiveData().observe(getViewLifecycleOwner(), stories -> {
            if (stories != null && !stories.isEmpty()) {
                storyAdapter.updateStories(stories);
                Log.d("HomeFragment", "Size story: " + stories.size());
            }
        });

        mViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Log.d("HomeFragment", msg + "");
            }
        });
    }

    private void loadMorePosts() {
        Log.d("scroll", "load more");
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        page++;
        binding.progressBar.setVisibility(View.VISIBLE);

        mViewModel.getPosts(page, size, userId);
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
}

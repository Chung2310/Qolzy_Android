package com.example.qolzy.ui.home;

import static com.example.qolzy.R.id.nav_host_fragment;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.qolzy.R;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.Story;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentHomeBinding;
import com.example.qolzy.ui.account.AccountFragment;
import com.example.qolzy.ui.comment.CommentsBottomSheet;
import com.example.qolzy.ui.post.PostAdapter;
import com.example.qolzy.ui.story.StoryAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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

    private List<Post> posts;

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

        postAdapter = new PostAdapter(new ArrayList<>(), getContext());
        storyAdapter = new StoryAdapter(new ArrayList<>(), getContext());

        binding.recyclerPosts.setLayoutManager(new LinearLayoutManager(getContext()));
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
        });

        storyAdapter.setOnStoryActionListener(new StoryAdapter.OnStoryActionListener() {
            @Override
            public void onClicked(Long storyId) {

            }
        });

        mViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Log.d("HomeFragment", msg+"");
            }
        });

        mViewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if(posts.size() > 0){
                postAdapter.updatePosts(posts);
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

    }

    @Override
    public void onPause() {
        super.onPause();
        // Dừng tất cả video khi rời màn hình
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
}
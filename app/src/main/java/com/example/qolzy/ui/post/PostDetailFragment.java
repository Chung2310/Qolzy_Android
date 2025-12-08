package com.example.qolzy.ui.post;

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
import com.example.qolzy.data.model.Post;
import com.example.qolzy.databinding.FragmentPostDetail2Binding;
import com.example.qolzy.databinding.FragmentPostDetailBinding;
import com.example.qolzy.ui.comment.CommentsBottomSheet;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends Fragment {

    private PostDetailViewModel mViewModel;
    private FragmentPostDetail2Binding binding;
    private PostAdapter adapter;
    private List<Post> posts = new ArrayList<>();
    private String action;
    private LinearLayoutManager linearLayoutManager;

    public static PostDetailFragment newInstance() {
        return new PostDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetail2Binding.inflate(inflater, container, false);

        linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new PostAdapter(posts, getContext());
        binding.recyclerViewPostDetail.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        Bundle args = getArguments();
        String from = args.getString("from");
        if(from.equals("notification")){
            Long id = args.getLong("actionId");
            action = args.getString("action");
            if(action.equals("commet")){
                CommentsBottomSheet bottomSheet = new CommentsBottomSheet();
                Bundle args1 = new Bundle();
                args1.putLong("postId", posts.get(0).getId());
                args1.putString("mode", "post");
                bottomSheet.setArguments(args);
                bottomSheet.show(getChildFragmentManager(), "CommentsBottomSheet");
            }
        }
        else {


        }



    }

}
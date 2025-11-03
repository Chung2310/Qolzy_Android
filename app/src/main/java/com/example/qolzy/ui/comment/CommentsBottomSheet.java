package com.example.qolzy.ui.comment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qolzy.R;
import com.example.qolzy.data.model.Comment;
import com.example.qolzy.data.model.CommentRequest;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentsBottomSheet extends BottomSheetDialogFragment {
    private RecyclerView recyclerComments;
    private EditText edtComment;
    private ImageButton btnSend;
    private CommentAdapter adapter;
    private List<Comment> comments = new ArrayList<>();
    private CommentViewModel commentViewModel;
    private int page = 0, size = 5, pageReply = 0, sizeReply = 10;
    private Long userId;
    private UserRepository userRepository;
    private long postId;
    private Long replyParentId = null;
    private String replyUserName = null;
    private int level = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_comment, container, false);

        commentViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(CommentViewModel.class);

        userRepository = new UserRepository(requireContext());
        userId = (long) userRepository.getUserId();

        recyclerComments = view.findViewById(R.id.recyclerComments);
        edtComment = view.findViewById(R.id.edtComment);
        btnSend = view.findViewById(R.id.btnSend);

        adapter = new CommentAdapter(requireContext(), comments, new CommentAdapter.OnCommentActionListener() {
            @Override
            public void onLikeClicked(Long commentId) {
                commentViewModel.toggleLike(commentId, userId);
            }

            @Override
            public void onCommentReplyClicked(Comment comment, User user) {
                setReplyText(edtComment, user.getLastName());

                replyParentId = comment.getId();
                replyUserName = user.getLastName();
                level = comment.getLevel()+1;
            }

            @Override
            public void onUserNameClicked(Long userId) {
                // TODO: chuyển sang profile
            }

            @Override
            public void onAvatarClicked(Long userId) {
                // TODO: chuyển sang profile
            }

            @Override
            public void onExtendClicked(Long parentCommentId) {
                // Gọi API lấy replies cho comment này
                commentViewModel.loadRepliesByParent(parentCommentId, userId, pageReply, sizeReply);
            }
        });

        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerComments.setAdapter(adapter);

        // Nhận postId truyền từ adapter
        postId = getArguments() != null ? getArguments().getLong("postId") : -1;
        if (postId != -1) {
            commentViewModel.loadCommentParent(postId, userId, page, size);
        }

        btnSend.setOnClickListener(v -> {
            String content = edtComment.getText().toString().trim();
            Comment comment = new Comment();

            if (!content.isEmpty()) {
                CommentRequest commentRequest = new CommentRequest();

                // Nếu là trả lời thì bỏ phần @Tên khỏi nội dung gửi đi
                if (replyParentId != null) {
                    String prefix = "@" + replyUserName;
                    if (content.startsWith(prefix)) {
                        content = content.substring(prefix.length()).trim();
                    }
                    commentRequest.setParentId(replyParentId);
                    comment.setLevel(level);
                } else {
                    commentRequest.setParentId(null); // comment gốc
                }

                Log.d("CommentsBottomSheet", userId.toString());

                commentRequest.setContent(content);
                commentRequest.setParentId(replyParentId);
                commentRequest.setUserId(userId);
                commentRequest.setPostId(postId);

                commentViewModel.createCommentParent(commentRequest);

                // Reset
                edtComment.setText("");
                hideKeyboard(v);
                replyParentId = null;
                replyUserName = null;

                commentViewModel.loadCommentParent(postId, userId, page,size);

            }
        });


        // ✅ Observe 1 lần duy nhất
        setupObservers();

        return view;
    }

    private void setupObservers() {
        // Comment cha
        commentViewModel.getCommentListLiveData().observe(getViewLifecycleOwner(), commentList -> {
            if (commentList != null) {
                adapter.updateComments(commentList);
            }
        });

        // Thông báo tạo comment thành công
        commentViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), msg -> {
            Log.d("CommentsBottomSheet", msg +"");
        });

        // Replies
        commentViewModel.getRepliesMapLiveData().observe(getViewLifecycleOwner(), repliesMap -> {
            if (repliesMap != null) {
                for (Map.Entry<Long, List<Comment>> entry : repliesMap.entrySet()) {
                    Long parentId = entry.getKey();
                    List<Comment> replies = entry.getValue();
                    adapter.insertReplies(parentId, replies);
                }
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setReplyText(EditText edtComment, String userName) {
        String prefix = "@" + userName + " ";
        SpannableStringBuilder ssb = new SpannableStringBuilder(prefix);

        // Đặt bold cho đoạn @userName
        ssb.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                prefix.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Gán vào EditText
        edtComment.setText(ssb);
        edtComment.setSelection(ssb.length()); // đặt con trỏ sau @userName
    }

}

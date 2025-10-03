package com.example.qolzy.ui.comment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.data.model.Comment;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.User;
import com.example.qolzy.util.Utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private List<Comment> comments;
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onLikeClicked(Long commentId);
        void onCommentReplyClicked(Comment comment, User user);
        void onUserNameClicked(Long userId);
        void onAvatarClicked(Long userId);
        void onExtendClicked(Long commentId);
    }

    public CommentAdapter(Context context, List<Comment> comments, OnCommentActionListener listener) {
        this.context = context;
        this.comments = comments != null ? comments : new ArrayList<>();
        this.listener = listener;
    }

    public void updateComments(List<Comment> newComments) {
        this.comments.clear();
        if (newComments != null) {
            this.comments.addAll(newComments);
        }
        notifyDataSetChanged();
    }

    public void insertReplies(Long parentId, List<Comment> replies) {
        int parentIndex = findCommentIndex(parentId);
        if (parentIndex == -1 || replies == null || replies.isEmpty()) return;

        List<Comment> filtered = new ArrayList<>();
        for (Comment reply : replies) {
            if (reply == null) continue;
            if (reply.getParenId() != null && reply.getParenId().equals(parentId)) {
                // tránh trùng lặp
                boolean exists = false;
                for (Comment c : comments) {
                    if (c != null && c.getId() != null && c.getId().equals(reply.getId())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) filtered.add(reply);
            }
        }

        if (!filtered.isEmpty()) {
            comments.addAll(parentIndex + 1, filtered);
            notifyItemRangeInserted(parentIndex + 1, filtered.size());
        }
    }

    private int findCommentIndex(Long parentId) {
        if (parentId == null) return -1;
        for (int i = 0; i < comments.size(); i++) {
            Comment c = comments.get(i);
            if (c != null && parentId.equals(c.getId())) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        if (comment == null) return;

        // user name
        String firstName = comment.getUserComment() != null ? comment.getUserComment().getFirstName() : "";
        String lastName = comment.getUserComment() != null ? comment.getUserComment().getLastName() : "";
        String fullName;
        if(firstName == null){
            fullName = (lastName).trim();
        }
        else fullName = (firstName + " " + lastName).trim();
        holder.txtUserName.setText(TextUtils.isEmpty(fullName) ? "Người dùng" : fullName);

        // time
        if (!TextUtils.isEmpty(comment.getCreatedAt())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    holder.txtTime.setText(getTimeAgo(comment.getCreatedAt()) + " ");
                } catch (Exception e) {
                    holder.txtTime.setText(comment.getCreatedAt() + " ");
                }
            } else {
                holder.txtTime.setText(comment.getCreatedAt() + " ");
            }
        } else {
            holder.txtTime.setText("");
        }

        // likes
        holder.tvLikes.setText(comment.getLikes() + " ");
        holder.btnLike.setImageResource(comment.isLikedByCurrentUser() ? R.drawable.love1 : R.drawable.love);

        // avatar
        String avatarUrl = null;
        if (comment.getUserComment() != null && !TextUtils.isEmpty(comment.getUserComment().getAvatarUrl())) {
            if (comment.getUserComment().getAvatarUrl().contains("https")) {
                avatarUrl = comment.getUserComment().getAvatarUrl();
            } else {
                avatarUrl = Utils.BASE_URL.replace("/api/", "") + "avatar/" + comment.getUserComment().getAvatarUrl();
            }
        }

        Glide.with(context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.imgAvatar);

        // content
        holder.txtContent.setText(!TextUtils.isEmpty(comment.getContent()) ? comment.getContent() : "(Không có nội dung)");

        // indent theo level
        int paddingStart = comment.getLevel() * 50;
        holder.itemView.setPadding(paddingStart,
                holder.itemView.getPaddingTop(),
                holder.itemView.getPaddingRight(),
                holder.itemView.getPaddingBottom());

        // see replies
        holder.btnSeeReplies.setVisibility(comment.getCountComment() > 0 ? View.VISIBLE : View.GONE);

        // event like
        holder.btnLike.setOnClickListener(v -> handleLikeClick(comment, holder));

        // event see replies
        holder.btnSeeReplies.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExtendClicked(comment.getId());
                holder.btnSeeReplies.setVisibility(View.GONE);
            }
        });

        holder.btnReply.setOnClickListener(v -> {
            if(listener != null) {
                listener.onCommentReplyClicked(comment, comment.getUserComment());
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnLike;
        TextView txtUserName, txtContent, btnReply, btnSeeReplies, txtTime, tvLikes;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnLike = itemView.findViewById(R.id.btnLike);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtContent = itemView.findViewById(R.id.txtContent);
            btnReply = itemView.findViewById(R.id.btnReply);
            btnSeeReplies = itemView.findViewById(R.id.btnSeeReplies);
            txtTime = itemView.findViewById(R.id.txtTime);
            tvLikes = itemView.findViewById(R.id.tvLikes);
        }
    }

    private void handleLikeClick(Comment comment, CommentAdapter.CommentViewHolder holder) {
        if (comment == null) return;

        boolean isLiked = comment.isLikedByCurrentUser();
        int current = comment.getLikes();

        if (!isLiked) {
            comment.setLikedByCurrentUser(true);
            comment.setLikes(current + 1);
            holder.btnLike.setImageResource(R.drawable.love1);
            animateButtonPop(holder.btnLike);
        } else {
            comment.setLikedByCurrentUser(false);
            comment.setLikes(Math.max(0, current - 1));
            holder.btnLike.setImageResource(R.drawable.love);
        }

        holder.tvLikes.setText(comment.getLikes() + " ");

        if (listener != null && comment.getId() != null) {
            listener.onLikeClicked(comment.getId());
        }
    }

    public static String getTimeAgo(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) return "";
        try {
            LocalDateTime postTime = null;
            LocalDateTime now;
            long seconds = 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                postTime = LocalDateTime.parse(timeStr);
                now = LocalDateTime.now();
                Duration duration = Duration.between(postTime, now);
                seconds = duration.getSeconds();
            }

            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long months = days / 30;

            if (seconds < 60) {
                return "vừa xong";
            } else if (minutes < 60) {
                return minutes + " phút trước";
            } else if (hours < 24) {
                return hours + " giờ trước";
            } else if (days < 30) {
                return days + " ngày trước";
            } else {
                return months + " tháng trước";
            }
        } catch (Exception e) {
            return timeStr;
        }
    }

    private void animateButtonPop(View v) {
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(v, View.SCALE_X, 1.2f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(v, View.SCALE_Y, 1.2f);
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(v, View.SCALE_X, 1f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(v, View.SCALE_Y, 1f);

        scaleXUp.setDuration(100);
        scaleYUp.setDuration(100);
        scaleXDown.setStartDelay(100);
        scaleYDown.setStartDelay(100);
        scaleXDown.setDuration(100);
        scaleYDown.setDuration(100);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleXUp, scaleYUp);
        set.play(scaleXDown).after(scaleXUp);
        set.play(scaleYDown).after(scaleYUp);
        set.start();
    }
}

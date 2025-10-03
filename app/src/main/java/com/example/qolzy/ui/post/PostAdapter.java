package com.example.qolzy.ui.post;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.User;
import com.example.qolzy.util.Utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>();
    private Context context;

    public interface OnPostActionListener {
        void onLikeClicked(Long postId);
        void onCommentClicked(Long postId);
        void onUsernameClicked(User user);
        void onAvatarClicked(User user);
    }

    private OnPostActionListener listener;

    public void setOnPostActionListener(OnPostActionListener listener) {
        this.listener = listener;
    }

    public PostAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    public void updatePosts(List<Post> newPosts) {
        this.posts.clear();
        this.posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        // Hiển thị tên
        String fullName;
        if (post.getUser().getFirstName() == null) {
            fullName = post.getUser().getLastName();
        } else {
            fullName = post.getUser().getFirstName() + " " + post.getUser().getLastName();
        }
        holder.tvUsername.setText(fullName);
        holder.tvLikes.setText(post.getLikes() + " lượt thích");

        // Caption
        if (post.getContent() == null) {
            holder.tvCaption.setText(fullName + " ");
        } else {
            holder.tvCaption.setText(fullName + " " + post.getContent());
        }

        // Thời gian
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tvTime.setText(getTimeAgo(post.getCreateAt()));
        } else {
            holder.tvTime.setText(post.getCreateAt());
        }

        // Avatar
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String postAvatarUrl = post.getUser().getAvatarUrl().contains("https")
                ? post.getUser().getAvatarUrl()
                : fixedUrl + "avatar/" + post.getUser().getAvatarUrl();

        Log.d("AvatarUrl", postAvatarUrl);
        Glide.with(context)
                .load(postAvatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.imgAvatar);

        // Media
        MediaAdapter mediaAdapter = new MediaAdapter(context, post.getMedias());
        holder.viewPagerMedia.setAdapter(mediaAdapter);

        // Trạng thái like ban đầu
        holder.btnLike.setImageResource(post.getLikedByCurrentUser() ? R.drawable.love1 : R.drawable.love);

        // Set click listener
        holder.btnLike.setOnClickListener(v -> handleLikeClick(post, holder));
        holder.btnComment.setOnClickListener(v -> handleCommentClick(post));

        holder.imgAvatar.setOnClickListener(v -> handleUserClick(post.getUser()));
        holder.tvUsername.setOnClickListener(v -> handleUserClick(post.getUser()));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // -------------------------------
    // Các hàm xử lý riêng
    // -------------------------------
    private void handleLikeClick(Post post, PostViewHolder holder) {
        boolean isLiked = post.getLikedByCurrentUser();
        int current = post.getLikes();

        if (!isLiked) {
            post.setLikedByCurrentUser(true);
            post.setLikes(current + 1);
            holder.btnLike.setImageResource(R.drawable.love1);
            animateButtonPop(holder.btnLike);
        } else {
            post.setLikedByCurrentUser(false);
            post.setLikes(Math.max(0, current - 1));
            holder.btnLike.setImageResource(R.drawable.love);
        }

        holder.tvLikes.setText(post.getLikes() + " lượt thích");

        if (listener != null) {
            listener.onLikeClicked(post.getId());
        }
    }

    private void handleCommentClick(Post post) {
        if (listener != null) {
            listener.onCommentClicked(post.getId());
        }
    }

    private void handleUserClick(User user) {
        if (listener != null) {
            listener.onUsernameClicked(user);
            listener.onAvatarClicked(user);
        }
    }

    // -------------------------------
    // ViewHolder
    // -------------------------------
    public class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvUsername;
        public TextView tvLikes;
        TextView tvCaption;
        TextView tvTime;
        ImageView btnMenu;
        public ImageView btnLike;
        ImageView btnComment;
        ImageView btnShare;
        ViewPager2 viewPagerMedia;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            viewPagerMedia = itemView.findViewById(R.id.viewPagerMedia);
        }
    }

    // -------------------------------
    // Helper functions
    // -------------------------------
    public static String getTimeAgo(String timeStr) {
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

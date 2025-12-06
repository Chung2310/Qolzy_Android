package com.example.qolzy.ui.reels;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qolzy.R;
import com.example.qolzy.data.model.Reel;
import com.example.qolzy.data.model.User;
import com.example.qolzy.util.Utils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ReelsViewHolder> {

    private final Context context;
    private final List<Reel> reels;
    private final ExoPlayer exoPlayer;

    private int currentPlayPosition = RecyclerView.NO_POSITION;
    private ReelsViewHolder currentHolder;
    private Player.Listener currentPlayerListener;

    public interface OnReelsActionListener {
        void onLikeClicked(Long reelId);
        void onCommentClicked(Long reelId);
        void onUsernameClicked(User user, Boolean followByCurrentUser);
        void onAvatarClicked(User user,Boolean followByCurrentUser);

    }

    private OnReelsActionListener listener;

    public void setOnReelsActionListener(OnReelsActionListener listener){
        this.listener = listener;
    }

    public ReelsAdapter(Context context, List<Reel> reels, ExoPlayer exoPlayer) {
        this.context = context;
        this.reels = reels;
        this.exoPlayer = exoPlayer;
    }

    public void updatePosts(List<Reel> newReels) {
        this.reels.clear();
        this.reels.addAll(newReels);
        currentPlayPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    /**
     * Chỉ play video tại vị trí position.
     */
    public void playVideoAtPosition(int position) {
        if (position < 0 || position >= reels.size()) return;
        if (position == currentPlayPosition) return;

        int oldPosition = currentHolder == null ? -1 : currentHolder.getBindingAdapterPosition();

        // detach holder cũ
        if (currentHolder != null) {
            if (currentHolder.playerView.getPlayer() == exoPlayer) {
                currentHolder.playerView.setPlayer(null);
            }
        }

        // remove listener cũ
        if (currentPlayerListener != null) {
            exoPlayer.removeListener(currentPlayerListener);
            currentPlayerListener = null;
        }

        currentPlayPosition = position;

        // notify only old and new item
        if (oldPosition != -1) notifyItemChanged(oldPosition);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ReelsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reels_video, parent, false);
        return new ReelsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelsViewHolder holder, int position) {
        Reel reel = reels.get(position);

        holder.txtUsername.setText(reel.getUser().getLastName());
        holder.txtCaption.setText(reel.getContent());

        holder.btnLike.setImageResource(reel.isLikeByCurrentUser() ? R.drawable.love1 : R.drawable.love);

        // Avatar
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String avatarUrl = reel.getUser().getAvatarUrl().contains("https")
                ? reel.getUser().getAvatarUrl()
                : fixedUrl + reel.getUser().getAvatarUrl();

        Glide.with(context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .into(holder.imgAvatar);

        // Chỉ attach player cho holder đang active
        if (position == currentPlayPosition) {
            attachPlayerToHolder(holder, reel);
        } else {
            // holder không active
            if (holder.playerView.getPlayer() == exoPlayer) {
                holder.playerView.setPlayer(null);
            }
            holder.progressBar.setVisibility(View.GONE);
        }

        holder.tvLike.setText(reel.getLikes() +"");

        holder.tvComment.setText(reel.getComment()+"");

        holder.imgMusicAvatar.setVisibility(View.GONE);

        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onLikeClicked(reel.getId());
                boolean isLiked = reel.isLikeByCurrentUser();
                int current = reel.getLikes();

                if (!isLiked) {
                    reel.setLikeByCurrentUser(true);
                    reel.setLikes(current + 1);
                    holder.btnLike.setImageResource(R.drawable.love1);
                    animateButtonPop(holder.btnLike);
                } else {
                    reel.setLikeByCurrentUser(false);
                    reel.setLikes(Math.max(0, current - 1));
                    holder.btnLike.setImageResource(R.drawable.love);
                }

                holder.tvLike.setText(reel.getLikes() + " lượt thích");

                if (listener != null) {
                    listener.onLikeClicked(reel.getId());
                }
            }
        });

        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCommentClicked(reel.getId());
            }
        });
        holder.imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onAvatarClicked(reel.getUser(), reel.getUser().getFollowByCurrentUser());
            }
        });

        holder.txtUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onUsernameClicked(reel.getUser(), reel.getUser().getFollowByCurrentUser());
            }
        });
    }

    @Override
    public int getItemCount() {
        return reels.size();
    }

    @Override
    public void onViewRecycled(@NonNull ReelsViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.playerView.getPlayer() == exoPlayer) {
            holder.playerView.setPlayer(null);
            try {
                exoPlayer.stop();
                exoPlayer.clearMediaItems();
            } catch (Exception ignored) {}
            if (currentPlayerListener != null) {
                exoPlayer.removeListener(currentPlayerListener);
                currentPlayerListener = null;
            }
            if (currentHolder == holder) currentHolder = null;
        }
    }

    private void attachPlayerToHolder(ReelsViewHolder holder, Reel reel) {
        if (currentHolder != null && currentHolder != holder) {
            if (currentHolder.playerView.getPlayer() == exoPlayer) {
                currentHolder.playerView.setPlayer(null);
            }
        }

        currentHolder = holder;

        holder.playerView.setPlayer(exoPlayer);

        // remove listener cũ
        if (currentPlayerListener != null) {
            exoPlayer.removeListener(currentPlayerListener);
        }

        // tạo listener mới để cập nhật progressBar
        currentPlayerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                } else {
                    holder.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (!isPlaying) holder.progressBar.setVisibility(View.GONE);
            }
        };
        exoPlayer.addListener(currentPlayerListener);

        // Cấu hình media
        if (reel.getMedia() != null && !reel.getMedia().isEmpty()) {
            String videoUrl = Utils.BASE_URL.replace("/api/", "") + reel.getMedia();
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            try {
                exoPlayer.stop();
                exoPlayer.clearMediaItems();
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
                exoPlayer.prepare();
                exoPlayer.setPlayWhenReady(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.progressBar.setVisibility(View.GONE);
            try {
                exoPlayer.stop();
                exoPlayer.clearMediaItems();
            } catch (Exception ignored) {}
        }
    }

    static class ReelsViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        ProgressBar progressBar;
        TextView txtUsername, txtCaption, txtMusic, tvLike, tvComment, tvShare;
        ImageView imgAvatar, imgMusicAvatar, btnLike, btnComment, btnShare;

        public ReelsViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            progressBar = itemView.findViewById(R.id.progressBar);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtCaption = itemView.findViewById(R.id.txtCaption);
            txtMusic = itemView.findViewById(R.id.txtMusic);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgMusicAvatar = itemView.findViewById(R.id.imgMusicAvatar);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            tvLike = itemView.findViewById(R.id.tvLike);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvShare = itemView.findViewById(R.id.tvShare);
            playerView.setUseController(false);
        }
    }

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

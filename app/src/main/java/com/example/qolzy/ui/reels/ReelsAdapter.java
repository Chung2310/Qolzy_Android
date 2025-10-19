package com.example.qolzy.ui.reels;

import android.content.Context;
import android.media.browse.MediaBrowser;
import android.net.Uri;
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
import com.example.qolzy.custom_view.CustomVideoView;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.util.Utils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;


public class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ReelsViewHolder> {
    private final Context context;
    private final List<Reel> reels;
    private final ExoPlayer exoPlayer;

    // Track holder hiện tại đang attach player
    private ReelsViewHolder currentHolder;

    public ReelsAdapter(Context context, List<Reel> reels, ExoPlayer exoPlayer) {
        this.context = context;
        this.reels = reels;
        this.exoPlayer = exoPlayer;
    }

    public void updatePosts(List<Reel> newReels) {
        this.reels.clear();
        this.reels.addAll(newReels);
        notifyDataSetChanged();
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
        holder.bind(reel);

        // Attach player chỉ cho holder hiện tại
        attachPlayerToHolder(holder, reel);
    }

    @Override
    public int getItemCount() {
        return reels.size();
    }

    @Override
    public void onViewRecycled(@NonNull ReelsViewHolder holder) {
        super.onViewRecycled(holder);
        if(holder.playerView.getPlayer() != null){
            holder.playerView.setPlayer(null);
        }
    }

    private void attachPlayerToHolder(ReelsViewHolder holder, Reel reel) {
        // Detach khỏi holder cũ
        if (currentHolder != null && currentHolder != holder) {
            currentHolder.playerView.setPlayer(null);
        }

        currentHolder = holder;
        holder.playerView.setPlayer(exoPlayer);

        // Phát video
        if (reel.getMedia() != null && !reel.getMedia().isEmpty()) {
            String videoUrl = Utils.BASE_URL.replace("/api/", "") + reel.getMedia();
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));

            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    static class ReelsViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        ProgressBar progressBar;
        TextView txtUsername, txtCaption, txtMusic;
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
        }

        public void bind(Reel reel) {
            txtUsername.setText(reel.getUser().getLastName());
            txtCaption.setText(reel.getContent());

            Glide.with(itemView.getContext()).load(Utils.BASE_URL + reel.getUser().getAvatarUrl()).into(imgAvatar);
            Glide.with(itemView.getContext()).load(Utils.BASE_URL + reel.getUser().getAvatarUrl()).into(imgMusicAvatar);
        }
    }
}


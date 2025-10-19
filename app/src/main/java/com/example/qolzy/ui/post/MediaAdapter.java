package com.example.qolzy.ui.post;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qolzy.R;
import com.example.qolzy.data.model.PostMedia;
import com.example.qolzy.util.Utils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<PostMedia> mediaList;
    private final ExoPlayer exoPlayer;
    private int currentVideoIndex = -1; // mặc định chưa play
    private int playingPosition = 0;   // item đang attach player

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    public MediaAdapter(Context context, List<PostMedia> mediaList, ExoPlayer exoPlayer) {
        this.context = context;
        this.mediaList = mediaList;
        this.exoPlayer = exoPlayer;
    }

    @Override
    public int getItemViewType(int position) {
        String url = mediaList.get(position).getUrl();
        if (url.endsWith(".mp4") || url.endsWith(".3gp") || url.endsWith(".mkv")) {
            return TYPE_VIDEO;
        } else {
            return TYPE_IMAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_VIDEO) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_media_video, parent, false);
            return new VideoViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_media_image, parent, false);
            return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostMedia postMedia = mediaList.get(position);
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");

        if (holder instanceof ImageViewHolder) {
            Glide.with(context)
                    .load(fixedUrl + postMedia.getUrl())
                    .placeholder(R.drawable.error_image) // ảnh tạm khi đang load
                    .error(R.drawable.error_image)
                    .into(((ImageViewHolder) holder).imageView);

        } else if (holder instanceof VideoViewHolder) {
            VideoViewHolder videoHolder = (VideoViewHolder) holder;
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_BUFFERING) {
                        videoHolder.progressBar.setVisibility(View.VISIBLE);
                    } else if (state == Player.STATE_READY || state == Player.STATE_ENDED) {
                        videoHolder.progressBar.setVisibility(View.GONE);
                    }
                }
            });
            // chỉ attach player cho item đang phát
            if (position == playingPosition) {
                videoHolder.playerView.setPlayer(exoPlayer);
            } else {
                videoHolder.playerView.setPlayer(null);
            }

        }

    }

    public void playVideoAt(int mediaPosition) {
        if (mediaPosition < 0 || mediaPosition >= mediaList.size()) return;

        PostMedia media = mediaList.get(mediaPosition);
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");

        if (media.getUrl().endsWith(".mp4") || media.getUrl().endsWith(".3gp") || media.getUrl().endsWith(".mkv")) {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(fixedUrl + media.getUrl())));
            exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);

            currentVideoIndex = mediaPosition;
            playingPosition = mediaPosition;

            // refresh để gắn player đúng item
            notifyDataSetChanged();
        } else {
            exoPlayer.pause();
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).playerView.setPlayer(null);
        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgMedia);
        }
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        ProgressBar progressBar;
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
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
    private int currentPlayingPosition = -1; // index video đang play

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    public MediaAdapter(Context context, List<PostMedia> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
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

        if (holder instanceof ImageViewHolder) {
            String imageUrl = Utils.BASE_URL + "medias/image?fileName=" + postMedia.getUrl();
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(((ImageViewHolder) holder).imageView);

        } else if (holder instanceof VideoViewHolder) {
            VideoViewHolder videoHolder = (VideoViewHolder) holder;
            String videoUrl = Utils.BASE_URL + "medias/video?fileName=" + postMedia.getUrl();

            videoHolder.progressBar.setVisibility(View.VISIBLE);

            // Tạo player riêng
            ExoPlayer player = new ExoPlayer.Builder(context).build();
            videoHolder.playerView.setPlayer(player);

            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            player.setMediaItem(mediaItem);

            // Đảm bảo PlayerView đã layout trước khi prepare
            videoHolder.playerView.post(() -> {
                player.prepare();
                if (position == currentPlayingPosition) {
                    player.setPlayWhenReady(true);
                }
            });

            // Listener hiển thị ProgressBar
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_BUFFERING) {
                        videoHolder.progressBar.setVisibility(View.VISIBLE);
                    } else if (state == Player.STATE_READY) {
                        videoHolder.progressBar.setVisibility(View.GONE);
                        if (position == currentPlayingPosition) {
                            player.setPlayWhenReady(true);
                        }
                    } else if (state == Player.STATE_ENDED) {
                        player.seekTo(0);
                        player.setPlayWhenReady(false);
                    }
                }
            });

            videoHolder.player = player;
        }
    }

    public void playVideoAt(int position) {
        // Pause video cũ
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            notifyItemChanged(currentPlayingPosition);
        }
        currentPlayingPosition = position;

    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VideoViewHolder) {
            VideoViewHolder vh = (VideoViewHolder) holder;
            if (vh.player != null) {
                vh.player.release();
                vh.player = null;
            }
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
        ExoPlayer player;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

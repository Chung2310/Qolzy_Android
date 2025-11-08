package com.example.qolzy.ui.post;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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
    private final ExoPlayer sharedPlayer;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    public MediaAdapter(Context context, List<PostMedia> mediaList, ExoPlayer sharedPlayer) {
        this.context = context;
        this.mediaList = mediaList;
        this.sharedPlayer = sharedPlayer;
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
                    .load(postMedia.getUrl().startsWith("http") ? postMedia.getUrl() : fixedUrl + postMedia.getUrl())
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(((ImageViewHolder) holder).imageView);

        } else if (holder instanceof VideoViewHolder) {
            VideoViewHolder videoHolder = (VideoViewHolder) holder;

            String videoUrl = postMedia.getUrl().startsWith("http")
                    ? postMedia.getUrl()
                    : fixedUrl + postMedia.getUrl();

            Log.d("VideoURL", videoUrl);

            // Tạo player riêng cho từng video
            videoHolder.playerView.setPlayer(sharedPlayer);
            videoHolder.progressBar.setVisibility(View.VISIBLE);

            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            sharedPlayer.setMediaItem(mediaItem);
            sharedPlayer.prepare();
            sharedPlayer.setPlayWhenReady(true); // không auto-play

            // Theo dõi trạng thái buffer/loading
            sharedPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_BUFFERING) {
                        videoHolder.progressBar.setVisibility(View.VISIBLE);
                    } else if (state == Player.STATE_READY) {
                        videoHolder.progressBar.setVisibility(View.GONE);
                    } else if (state == Player.STATE_ENDED) {
                        sharedPlayer.seekTo(0);
                        sharedPlayer.setPlayWhenReady(false);
                    }
                }
            });

            // Lưu lại player để giải phóng khi view bị recycle
            videoHolder.player = sharedPlayer;
        }
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
        ExoPlayer player; // player riêng cho từng video
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

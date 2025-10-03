package com.example.qolzy.ui.post;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qolzy.R;
import com.example.qolzy.data.model.PostMedia;
import com.example.qolzy.util.Utils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<PostMedia> mediaList;

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
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");

        if (holder instanceof ImageViewHolder) {
            Glide.with(context)
                    .load(fixedUrl + postMedia.getUrl())
                    .into(((ImageViewHolder) holder).imageView);

        } else if (holder instanceof VideoViewHolder) {
            VideoViewHolder videoHolder = (VideoViewHolder) holder;

            // Tạo ExoPlayer cho từng video
            ExoPlayer player = new ExoPlayer.Builder(context).build();
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(fixedUrl + postMedia.getUrl()));
            player.setMediaItem(mediaItem);
            player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE); // loop video
            player.prepare();
            player.setPlayWhenReady(true);

            videoHolder.playerView.setPlayer(player);
        }
    }
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VideoViewHolder) {
            PlayerView playerView = ((VideoViewHolder) holder).playerView;
            if (playerView.getPlayer() != null) {
                playerView.getPlayer().release();
                playerView.setPlayer(null);
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
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
        }
    }
}

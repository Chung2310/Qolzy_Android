package com.example.qolzy.ui.add_post.post_detail;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qolzy.R;

import java.util.List;

public class PostPreviewAdapter extends RecyclerView.Adapter<PostPreviewAdapter.ViewHolder> {

    private final List<String> uris;
    private final List<Boolean> isVideos;

    public PostPreviewAdapter(List<String> uris, List<Boolean> isVideos) {
        this.uris = uris;
        this.isVideos = isVideos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_preview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String uriStr = uris.get(position);
        boolean isVideo = isVideos.get(position);
        Uri uri = Uri.parse(uriStr);

        if (isVideo) {
            holder.img.setVisibility(View.GONE);
            holder.video.setVisibility(View.VISIBLE);
            holder.video.setVideoURI(uri);
            holder.video.start();
        } else {
            holder.video.setVisibility(View.GONE);
            holder.img.setVisibility(View.VISIBLE);
            holder.img.setImageURI(uri);
        }
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        VideoView video;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPreviewItem);
            video = itemView.findViewById(R.id.videoPreviewItem);
        }
    }
}

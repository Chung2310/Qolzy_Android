package com.example.qolzy.ui.add_post;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qolzy.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final List<Uri> uris;
    private final List<Boolean> isVideos;
    private final OnItemClickListener listener; // 👉 dùng interface custom
    private final List<Uri> selected = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(Uri uri, boolean isVideo, boolean isSelected);
    }

    public GalleryAdapter(List<Uri> items, List<Boolean> isVideoFlags, OnItemClickListener listener) {
        this.uris = items;
        this.isVideos = isVideoFlags;
        this.listener = listener; // 👉 không ép kiểu nữa
    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.ViewHolder holder, int position) {
        Uri uri = uris.get(position);
        boolean isVideo = isVideos.get(position);

        // load thumbnail bằng Glide cho cả ảnh + video
        Glide.with(holder.img.getContext())
                .load(uri)
                .centerCrop()
                .into(holder.img);

        // hiển thị icon play nếu là video
        if (isVideo) {
            holder.icVideo.setVisibility(View.VISIBLE);
        } else {
            holder.icVideo.setVisibility(View.GONE);
        }

        // highlight khi được chọn
        if (selected.contains(uri)) {
            holder.overlay.setVisibility(View.VISIBLE);
        } else {
            holder.overlay.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            boolean isSelected;
            if (selected.contains(uri)) {
                selected.remove(uri);
                holder.overlay.setVisibility(View.GONE);
                isSelected = false;
            } else {
                selected.add(uri);
                holder.overlay.setVisibility(View.VISIBLE);
                isSelected = true;
            }
            listener.onItemClick(uri, isVideo, isSelected);
        });

    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img, icVideo;
        View overlay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgItem);
            overlay = itemView.findViewById(R.id.overlay);
            icVideo = itemView.findViewById(R.id.icVideo);
        }
    }
}

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

import java.util.ArrayList;

public class DetailMediaPagerAdapter extends RecyclerView.Adapter<DetailMediaPagerAdapter.ViewHolder> {

    private final ArrayList<String> uris;
    private final ArrayList<Boolean> isVideos;

    public DetailMediaPagerAdapter(ArrayList<String> uris, ArrayList<Boolean> isVideos) {
        this.uris = uris;
        this.isVideos = isVideos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = Uri.parse(uris.get(position));
        boolean video = isVideos.get(position);

        if (video) {
            holder.imageView.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
            holder.videoView.setVideoURI(uri);
            holder.videoView.start();
        } else {
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageURI(uri);
        }
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        VideoView videoView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgDetailItem);
            videoView = itemView.findViewById(R.id.videoDetailItem);
        }
    }
}

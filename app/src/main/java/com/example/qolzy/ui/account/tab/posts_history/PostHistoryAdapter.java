package com.example.qolzy.ui.account.tab.posts_history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.qolzy.R;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.ui.post.MediaAdapter;
import com.google.android.exoplayer2.ExoPlayer;

import java.util.List;

public class PostHistoryAdapter extends RecyclerView.Adapter<PostHistoryAdapter.PostHistoryViewHolder> {
    private Context context;
    private List<Post> posts;
    private final ExoPlayer exoPlayer;

    public PostHistoryAdapter(Context context, List<Post> posts, ExoPlayer exoPlayer) {
        this.context = context;
        this.posts = posts;
        this.exoPlayer = exoPlayer;
    }

    public void updatePosts(List<Post> newPosts) {
        this.posts.clear();
        this.posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostHistoryAdapter.PostHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_history, parent, false);
        return new PostHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHistoryAdapter.PostHistoryViewHolder holder, int position) {
        Post post = posts.get(position);
        MediaAdapter mediaAdapter = new MediaAdapter(context, post.getMedias());
        holder.viewPagerMedia.setAdapter(mediaAdapter);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostHistoryViewHolder extends RecyclerView.ViewHolder{
        ViewPager2 viewPagerMedia;
        public PostHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPagerMedia = itemView.findViewById(R.id.viewPagerMedia);
        }
    }

}

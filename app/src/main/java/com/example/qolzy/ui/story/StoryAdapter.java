package com.example.qolzy.ui.story;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.Story;
import com.example.qolzy.ui.post.PostAdapter;
import com.example.qolzy.util.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    private Context context;
    private List<Story> stories;

    private OnStoryActionListener listener;


    public StoryAdapter( List<Story> stories, Context context) {
        this.context = context;
        this.stories = stories;
    }

    public interface OnStoryActionListener {
        void onClicked(Long storyId, List<Story> stories);
    }

    public void setOnStoryActionListener(OnStoryActionListener listener) {
        this.listener = listener;
    }

    public void updateStories(List<Story> stories) {
        this.stories.clear();
        this.stories.addAll(stories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryAdapter.StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.StoryViewHolder holder, int position) {
        Story story = stories.get(position);

        holder.txtUserNameStory.setText(story.getUser().getUserName()+"");
        // Avatar
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String postAvatarUrl = story.getUser().getAvatarUrl().contains("https")
                ? story.getUser().getAvatarUrl()
                : fixedUrl + "avatar/" + story.getUser().getAvatarUrl();

        Log.d("AvatarUrl", postAvatarUrl);
        Glide.with(context)
                .load(postAvatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.circleImageView);

        holder.itemStory.setOnClickListener(v ->{
            if(listener != null){
                listener.onClicked(story.getId(), stories);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder{
        LinearLayout itemStory;
        CircleImageView circleImageView;
        TextView txtUserNameStory;
        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.circleImageViewStory);
            txtUserNameStory = itemView.findViewById(R.id.txtUserNameStory);
            itemStory = itemView.findViewById(R.id.itemStory);
        }
    }
}

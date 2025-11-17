package com.example.qolzy.ui.follow.tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.data.model.FollowResponse;
import com.example.qolzy.data.model.User;
import com.example.qolzy.util.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.FollowViewHolder> {
    private List<FollowResponse> userList;
    private Context context;

    public FollowAdapter(List<FollowResponse> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    public void updateFollow(List<FollowResponse> newFollows){
        this.userList.addAll(newFollows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
        FollowResponse followResponse = userList.get(position);
        User user = followResponse.getFollowing();
        holder.btnMessager.setVisibility(View.VISIBLE);
        holder.btnExtend.setVisibility(View.VISIBLE);

        String newUrl = Utils.BASE_URL.replace("/api/", "/");
        String postAvatarUrl = null;

        if (user.getAvatarUrl() != null) {
            postAvatarUrl = user.getAvatarUrl().contains("https")
                    ? user.getAvatarUrl()
                    : newUrl + "avatar/" + user.getAvatarUrl();
        }

        Glide.with(context)
                .load(postAvatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.imgAvatarFollow);

        String displayName = (user.getFirstName() == null)
                ? user.getLastName()
                : user.getFirstName() + " " + user.getLastName();

        holder.tvNameFollow.setText(displayName != null ? displayName : "Unknown");
        holder.tvUserNameFollow.setText(user.getUserName() != null ? user.getUserName() : "Unknown");


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class FollowViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView imgAvatarFollow;
        private TextView tvNameFollow, tvUserNameFollow;
        private AppCompatButton btnMessager;
        private ImageButton btnExtend;
        public FollowViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarFollow = itemView.findViewById(R.id.imgAvatarSearch);
            tvNameFollow = itemView.findViewById(R.id.tvNameSearch);
            tvUserNameFollow = itemView.findViewById(R.id.tvUserNameSearch);
            btnMessager = itemView.findViewById(R.id.btnMessager);
            btnExtend = itemView.findViewById(R.id.btnExtend);
        }
    }
}

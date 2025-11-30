package com.example.qolzy.ui.search;

import android.content.Context;
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
import com.example.qolzy.data.model.User;
import com.example.qolzy.util.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private List<User> userList;
    private Context context;
    private OnSearchUserActionListener listener;

    public SearchAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    public interface OnSearchUserActionListener {
        void onClicked(User user,Boolean followByCurrentUser);
    }

    public void setOnSearchActionListener(OnSearchUserActionListener listener) {
        this.listener = listener;
    }

    public void updateUserSearch(List<User> newUsers) {
        this.userList.clear();
        this.userList.addAll(newUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchViewHolder holder, int position) {
        User user = userList.get(position);

        String newUrl = Utils.BASE_URL.replace("/api/", "");
        String postAvatarUrl = null;

        if (user.getAvatarUrl() != null) {
            postAvatarUrl = user.getAvatarUrl().contains("https")
                    ? user.getAvatarUrl()
                    : newUrl  + user.getAvatarUrl();
        }

        Glide.with(context)
                .load(postAvatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.imgAvatarSearch);

        String displayName = (user.getFirstName() == null)
                ? user.getLastName()
                : user.getFirstName() + " " + user.getLastName();

        holder.tvNameSearch.setText(displayName != null ? displayName : "Unknown");
        holder.tvUserNameSearch.setText(user.getUserName() != null ? user.getUserName() : "Unknown");

        holder.contactMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClicked(user, user.getFollowByCurrentUser());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView imgAvatarSearch;
        private TextView tvNameSearch, tvUserNameSearch;
        private LinearLayout contactMainLayout;
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarSearch = itemView.findViewById(R.id.imgAvatarSearch);
            tvNameSearch = itemView.findViewById(R.id.tvNameSearch);
            tvUserNameSearch = itemView.findViewById(R.id.tvUserNameSearch);
            contactMainLayout = itemView.findViewById(R.id.contactMainLayout);
        }
    }
}

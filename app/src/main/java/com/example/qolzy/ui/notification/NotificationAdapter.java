package com.example.qolzy.ui.notification;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.data.model.Notification;
import com.example.qolzy.util.Utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private Context context;

    public NotificationAdapter(List<Notification> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications.clear();
        this.notifications.addAll(newNotifications);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tvTime.setText(getTimeAgo(notification.getCreatedAt()));
        } else {
            holder.tvTime.setText(notification.getCreatedAt());
        }

        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String postAvatarUrl = notification.getSender().getAvatarUrl().contains("https")
                ? notification.getSender().getAvatarUrl()
                : fixedUrl  + notification.getSender().getAvatarUrl();

        Log.d("AvatarUrl", postAvatarUrl);
        Glide.with(context)
                .load(postAvatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.imgAvatar);

        String messageNotification = "";
        if ("follow".equals(notification.getType())) {
            messageNotification = notification.getSender().getUserName() + " đã theo dõi bạn";
        } else if ("comment".equals(notification.getType())) {
            messageNotification = notification.getSender().getUserName() + " đã bình luận về bài viết của bạn";
        } else if ("comment-reply".equals(notification.getType())) {
            messageNotification = notification.getSender().getUserName() + " đã trả lời bình luận của bạn";
        } else if ("post".equals(notification.getType())){
            messageNotification = notification.getSender().getUserName() + " đã thích bài viết của bạn";
        }

        holder.tvTitle.setText(messageNotification+"");
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imgAvatar;
        private TextView tvTitle,tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    public static String getTimeAgo(String timeStr) {
        LocalDateTime postTime = null;
        LocalDateTime now;
        long seconds = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            postTime = LocalDateTime.parse(timeStr);
            now = LocalDateTime.now();
            Duration duration = Duration.between(postTime, now);
            seconds = duration.getSeconds();
        }

        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;

        if (seconds < 60) {
            return "vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days < 30) {
            return days + " ngày trước";
        } else {
            return months + " tháng trước";
        }
    }
}

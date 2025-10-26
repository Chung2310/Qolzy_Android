package com.example.qolzy.ui.message;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.qolzy.R;
import com.example.qolzy.data.model.Message;
import com.example.qolzy.data.model.User;
import com.example.qolzy.util.Utils;
import java.util.ArrayList;
import java.util.List;
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<Message> messages;
    private Long currentUserId;
    private OnActionMessageListener listener;
    public MessageAdapter(List<Message> messages ,Context context,Long currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.messages = messages;
    }

    public interface OnActionMessageListener {
        void onClickAvatar(User user);
        void onLongClickMessage(Long messageId);
    }

    public void setListener(OnActionMessageListener listener) {
        this.listener = listener;
    }

    public void updateMessages(List<Message> newMessages) {
        messages.clear(); Log.d("MessageAdapter", newMessages.size() +"");
        messages.addAll(newMessages); notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Log.d("MessageAdapter", "Load message "+ position); Message message = messages.get(position);
        if (message == null) return; boolean isSender = message.getSender().getId().equals(currentUserId);
        if (isSender) {
            // Hiển thị phần gửi
            holder.layoutSender.setVisibility(View.VISIBLE);
            holder.layoutReceiver.setVisibility(View.GONE);
            holder.tvMessageSender.setText(message.getContent());
            String avatarUrl = message.getSender().getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(context) .load(avatarUrl.startsWith("https") ? avatarUrl : Utils.BASE_URL + avatarUrl)
                        .into(holder.imgAvatarSender);
            }
        } else {
            // Hiển thị phần nhận
            holder.layoutReceiver.setVisibility(View.VISIBLE);
            holder.layoutSender.setVisibility(View.GONE);
            holder.tvMessageReceiver.setText(message.getContent());
            String avatarUrl = message.getSender().getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(context) .load(avatarUrl.startsWith("https") ? avatarUrl : Utils.BASE_URL + avatarUrl)
                        .into(holder.imgAvatarReceiver);
            }
        }
    }

    @Override public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutSender, layoutReceiver;
        TextView tvMessageSender, tvMessageReceiver;
        ImageView imgAvatarSender, imgAvatarReceiver;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView); layoutSender = itemView.findViewById(R.id.layoutSender);
            layoutReceiver = itemView.findViewById(R.id.layoutReceiver);
            tvMessageSender = itemView.findViewById(R.id.tvMessageSender);
            tvMessageReceiver = itemView.findViewById(R.id.tvMessageReceiver);
            imgAvatarSender = itemView.findViewById(R.id.imgAvatarSender);
            imgAvatarReceiver = itemView.findViewById(R.id.imgAvatarReceiver);
        }
    }
}
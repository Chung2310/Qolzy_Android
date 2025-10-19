package com.example.qolzy.ui.message;

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
import com.example.qolzy.data.model.Message;
import com.example.qolzy.data.model.User;
import com.example.qolzy.util.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> messages;
    private Context context;
    private Long currentUserId;
    private OnActionMessageListener listener;
    private static final int TYPE_SENDER = 1;
    private static final int TYPE_RECEIVER = 2;

    public interface OnActionMessageListener{
        void onClickAvatar(User user);
        void onLongClickMessage(Long messageId);
    }

    public void setListener(OnActionMessageListener listener) {
        this.listener = listener;
    }

    public MessageAdapter(List<Message> messages, Context context, Long currentUserId) {
        this.messages = messages;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    public void updateMessages(List<Message> newMessages){
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSender().getId().equals(currentUserId)) {
            return TYPE_SENDER;
        } else {
            return TYPE_RECEIVER;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENDER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_right, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_left, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getItemViewType() == TYPE_SENDER) {
            ((SenderViewHolder) holder).tvMessageSender.setText(message.getContent());

            ((SenderViewHolder) holder).itemMessageSender.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClickMessage(message.getId());
                    return true;
                }
            });
        } else {
            ((ReceiverViewHolder) holder).tvMessageReceiver.setText(message.getContent());

            String fixedUrl = Utils.BASE_URL.replace("/api/", "");
            String postAvatarUrl = message.getReceiver().getAvatarUrl().contains("https")
                    ? message.getReceiver().getAvatarUrl()
                    : fixedUrl + "avatar/" + message.getReceiver().getAvatarUrl();

            Log.d("AvatarUrl", postAvatarUrl);
            Glide.with(context)
                    .load(postAvatarUrl)
                    .placeholder(R.drawable.ic_android_black_24dp)
                    .error(R.drawable.user)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .into(((ReceiverViewHolder) holder).imgDetailItemMessageReceiver);

            ((ReceiverViewHolder) holder).imgDetailItemMessageReceiver.setOnClickListener(
                    v -> listener.onClickAvatar(message.getReceiver()));

            ((ReceiverViewHolder) holder).itemMessageReceiver.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClickMessage(message.getId());
                    return true;
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemMessageSender;
        TextView tvMessageSender;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageSender = itemView.findViewById(R.id.tvMessageSender);
            itemMessageSender = itemView.findViewById(R.id.itemMessageSender);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgDetailItemMessageReceiver;
        TextView tvMessageReceiver;
        LinearLayout itemMessageReceiver;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageReceiver= itemView.findViewById(R.id.tvMessageReceiver);
            imgDetailItemMessageReceiver = itemView.findViewById(R.id.imgDetailItemMessageReceiver);
            itemMessageReceiver = itemView.findViewById(R.id.itemMessageReceiver);
        }
    }
}

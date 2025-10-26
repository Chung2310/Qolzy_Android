package com.example.qolzy.ui.message;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.data.model.Contact;
import com.example.qolzy.data.model.User;
import com.example.qolzy.util.Utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context context;
    private List<Contact> contacts;

    public interface OnContactActionListener {
        void onClicked(User contact);
    }

    private OnContactActionListener listener;

    public void setOnContactActionListener(OnContactActionListener listener) {
        this.listener = listener;
    }

    public ContactAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    public void updateContacts(List<Contact> newContacts){
        this.contacts.clear();
        this.contacts.addAll(newContacts);
        notifyDataSetChanged();
    }

    public void updateContactsSearch(List<Contact> newContacts){
        this.contacts.clear();
        this.contacts.addAll(newContacts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contacts, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);

        String fullName;
        if (contact.getUserContact().getFirstName() == null) {
            fullName = contact.getUserContact().getLastName();
        } else {
            fullName = contact.getUserContact().getFirstName() + " " + contact.getUserContact().getLastName();
        }
        holder.tvName.setText(fullName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tvTime.setText(getTimeAgo(contact.getLastTime()));
        } else {
            holder.tvTime.setText(contact.getLastTime());
        }

        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String postAvatarUrl = contact.getUserContact().getAvatarUrl().contains("https")
                ? contact.getUserContact().getAvatarUrl()
                : fixedUrl + "avatar/" + contact.getUserContact().getAvatarUrl();

        Log.d("AvatarUrl", postAvatarUrl);
        Glide.with(context)
                .load(postAvatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.imgAvatar);

        if(contact.isCurrentUserLastMessage()){
            holder.tvLastMsg.setText(shortenText("Bạn: "+contact.getLastMessage(), 20));
        } else {
            holder.tvLastMsg.setText(shortenText(contact.getLastMessage(), 20));
        }

        holder.itemContactLayout.setOnClickListener(v-> listener.onClicked(contact.getUserContact()));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgAvatar;
        private ConstraintLayout itemContactLayout;
        private TextView tvName, tvLastMsg, tvTime, tvUnread;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMsg = itemView.findViewById(R.id.tvLastMsg);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnread = itemView.findViewById(R.id.tvUnread);
            itemContactLayout = itemView.findViewById(R.id.contactMainLayout);
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

    public static String shortenText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

}

package com.example.qolzy.ui.music;

import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qolzy.R;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private List<MusicItem> musicList;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private int playingPosition = -1;

    // callback
    public interface OnMusicActionListener {
        void onMusicClick(MusicItem item); // click vào item -> trả ra ngoài Fragment
    }

    private OnMusicActionListener actionListener;

    public void setOnMusicActionListener(OnMusicActionListener listener) {
        this.actionListener = listener;
    }

    public MusicAdapter(List<MusicItem> musicList) {
        if (musicList != null) {
            this.musicList = musicList;
        } else {
            this.musicList = new ArrayList<>();
        }
    }

    public void updateMusics(List<MusicItem> list) {
        this.musicList.clear();
        this.musicList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicItem item = musicList.get(position);

        holder.title.setText(item.getName());
        holder.subtitle.setText("By: " + item.getArtistName() + " | " + item.getDuration() + "s");

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_music)
                .into(holder.cover);

        // Hiển thị icon play/pause
        if (playingPosition == position) {
            holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }

        // Play/Pause khi bấm nút
        holder.btnPlayPause.setOnClickListener(v -> {
            if (playingPosition == position) {
                stopMusic();
                notifyItemChanged(position);
            } else {
                playMusic(item, position);
            }
        });

        // Click vào toàn bộ item -> callback ra Fragment
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onMusicClick(item);
            }
        });
    }

    private void playMusic(MusicItem item, int position) {
        stopMusic();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(item.getAudioUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                playingPosition = position;
                notifyDataSetChanged();

                // chỉ phát 10 giây preview
                handler.postDelayed(() -> {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        stopMusic();
                        notifyItemChanged(position);
                    }
                }, 10000);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                stopMusic();
                notifyItemChanged(position);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        playingPosition = -1;
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, subtitle;
        ImageButton btnPlayPause;

        MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.imgCover);
            title = itemView.findViewById(R.id.tvTitle);
            subtitle = itemView.findViewById(R.id.tvSubtitle);
            btnPlayPause = itemView.findViewById(R.id.btnPlayPause);
        }
    }
}

package com.example.qolzy.data.model;

import com.example.qolzy.ui.music.MusicItem;

public class CreatePostRequest {
    private String content;
    private MusicItem music;
    private Long userId;

    public CreatePostRequest(String content, MusicItem music, Long userId) {
        this.content = content;
        this.music = music;
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MusicItem getMusic() {
        return music;
    }

    public void setMusic(MusicItem music) {
        this.music = music;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

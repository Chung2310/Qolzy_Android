package com.example.qolzy.ui.reels;

import com.example.qolzy.data.model.User;

public class Reel {
    private Long id;
    private User user;
    private String content;
    private String media;
    private String createAt;
    private int likes;
    private int comment;
    private int views;
    private boolean isLikeReel = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isLikeReel() {
        return isLikeReel;
    }

    public void setLikeReel(boolean likeReel) {
        isLikeReel = likeReel;
    }
}

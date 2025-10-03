package com.example.qolzy.data.model;

import java.util.List;

public class Post {
    private Long id;
    private User user;
    private int likes;
    private String content;
    private String musicId;
    private int comments;
    private List<PostMedia> medias;
    private String createAt;
    private Boolean likedByCurrentUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public String getCreateAt() {
        return createAt;
    }

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public List<PostMedia> getMedias() {
        return medias;
    }

    public void setMedias(List<PostMedia> medias) {
        this.medias = medias;
    }
}

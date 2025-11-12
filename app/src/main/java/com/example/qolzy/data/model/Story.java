package com.example.qolzy.data.model;

public class Story {
    private Long id;
    private User user;
    private PostMedia medias;
    private int duration;
    private String createAt;

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

    public PostMedia getMedias() {
        return medias;
    }

    public void setMedias(PostMedia medias) {
        this.medias = medias;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

package com.example.qolzy.data.model;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private User receiver;
    private User sender;
    private String content;
    private String createdAt;

    public Message() {
    }

    public Message(Long id, User receiver, User sender, String content, String createdAt) {
        this.id = id;
        this.receiver = receiver;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

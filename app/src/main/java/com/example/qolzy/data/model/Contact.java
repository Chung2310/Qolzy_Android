package com.example.qolzy.data.model;

public class Contact {
    private Long id;
    private User userContact;
    private String lastMessage;
    private String lastTime;

    private boolean isCurrentUserLastMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUserContact() {
        return userContact;
    }

    public void setUserContact(User userContact) {
        this.userContact = userContact;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public boolean isCurrentUserLastMessage() {
        return isCurrentUserLastMessage;
    }

    public void setCurrentUserLastMessage(boolean currentUserLastMessage) {
        isCurrentUserLastMessage = currentUserLastMessage;
    }
}

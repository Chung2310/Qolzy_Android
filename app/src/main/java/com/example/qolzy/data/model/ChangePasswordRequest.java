package com.example.qolzy.data.model;


public class ChangePasswordRequest {
    private Long id;
    private String oldPassowrd;
    private String newPassowrd;

    public ChangePasswordRequest(Long id, String oldPassowrd, String newPassowrd) {
        this.id = id;
        this.oldPassowrd = oldPassowrd;
        this.newPassowrd = newPassowrd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOldPassowrd() {
        return oldPassowrd;
    }

    public void setOldPassowrd(String oldPassowrd) {
        this.oldPassowrd = oldPassowrd;
    }

    public String getNewPassowrd() {
        return newPassowrd;
    }

    public void setNewPassowrd(String newPassowrd) {
        this.newPassowrd = newPassowrd;
    }
}

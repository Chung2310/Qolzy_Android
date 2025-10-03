package com.example.qolzy.data.model;

public class LoginRequestFirebase {
    private String idToken;

    public LoginRequestFirebase(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}

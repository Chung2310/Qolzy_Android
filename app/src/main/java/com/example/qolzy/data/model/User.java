package com.example.qolzy.data.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String token;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String refreshToken;
    private String userName;
    private int postCount;
    private Long followersCount;
    private Long followingCount;
    private String bio;
    private String createAt;

    public User(Long id, String firstName, String lastName, String token, String email,
                String phone, String address, String avatarUrl, String refreshToken,
                String userName, int postCount, Long followersCount, Long followingCount,
                String createAt, String bio) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.avatarUrl = avatarUrl;
        this.refreshToken = refreshToken;
        this.userName = userName;
        this.postCount = postCount;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.createAt = createAt;
        this.bio = bio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }
}

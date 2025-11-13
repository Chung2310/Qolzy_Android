package com.example.qolzy.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.qolzy.data.model.User;

public class UserRepository {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_FIRST_NAME = "UserFirstName";
    private static final String KEY_LAST_NAME = "UserLastName";
    private static final String KEY_ID = "UserID";
    private static final String KEY_EMAIL = "UserEmail";
    private static final String KEY_TOKEN_ACCESS = "UserTokenAccess";
    private static final String KEY_TOKEN_REFRESH = "UserTokenRefresh";
    private static final String KEY_PHONE = "UserPhone";
    private static final String KEY_ADDRESS = "UserAddress";
    private static final String KEY_CREATE_AT = "UserCreateAt";
    private static final String KEY_AVATAR_URL = "UserAvatarUrl";
    private static final String KEY_USER_NAME = "UserName";
    private static final String KEY_POST_COUNT = "PostCount";
    private static final String KEY_FOLLOWING_COUNT = "UserFollowing";
    private static final String KEY_FOLLOWER_COUNT = "UserFollower";

    private final SharedPreferences prefs;

    public UserRepository(Context context){
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(User user){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_ID, user.getId());
        editor.putString(KEY_FIRST_NAME, user.getFirstName());
        editor.putString(KEY_LAST_NAME, user.getLastName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putString(KEY_TOKEN_ACCESS, user.getToken());
        editor.putString(KEY_TOKEN_REFRESH, user.getRefreshToken());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_AVATAR_URL, user.getAvatarUrl());
        editor.putLong(KEY_FOLLOWER_COUNT, user.getFollowersCount());
        editor.putLong(KEY_FOLLOWING_COUNT, user.getFollowingCount());
        editor.putString(KEY_CREATE_AT, user.getCreateAt());
        editor.putString(KEY_USER_NAME, user.getUserName());
        editor.putInt(KEY_POST_COUNT, user.getPostCount());

        // Thêm log cho tất cả trường
        Log.d("UserPrefs", "Saving user:");
        Log.d("UserPrefs", "ID: " + user.getId());
        Log.d("UserPrefs", "First Name: " + user.getFirstName());
        Log.d("UserPrefs", "Last Name: " + user.getLastName());
        Log.d("UserPrefs", "Email: " + user.getEmail());
        Log.d("UserPrefs", "Address: " + user.getAddress());
        Log.d("UserPrefs", "Access Token: " + user.getToken());
        Log.d("UserPrefs", "Refresh Token: " + user.getRefreshToken());
        Log.d("UserPrefs", "Phone: " + user.getPhone());
        Log.d("UserPrefs", "Avatar URL: " + user.getAvatarUrl());
        Log.d("UserPrefs", "Following Count: " + user.getFollowingCount());
        Log.d("UserPrefs", "Follower Count: " + user.getFollowersCount());
        Log.d("UserPrefs", "Create At: " + user.getCreateAt());
        Log.d("UserPrefs", "Post Count :" + user.getPostCount());
        Log.d("UserPrefs", "User Mame : " + user.getUserName());

        editor.apply();
    }


    public User getUser(){
        Long id = prefs.getLong(KEY_ID, 0);
        String firstName = prefs.getString(KEY_FIRST_NAME, null);
        String lastName = prefs.getString(KEY_LAST_NAME, null);
        String email = prefs.getString(KEY_EMAIL, null);
        String address = prefs.getString(KEY_ADDRESS, null);
        String accessToken = prefs.getString(KEY_TOKEN_ACCESS, null);
        String refreshToken = prefs.getString(KEY_TOKEN_REFRESH, null);
        String phone = prefs.getString(KEY_PHONE, null);
        String avatar = prefs.getString(KEY_AVATAR_URL, null);
        String userName = prefs.getString(KEY_USER_NAME, null);
        int postCount = prefs.getInt(KEY_POST_COUNT, 0);
        Long follower = prefs.getLong(KEY_FOLLOWER_COUNT, 0);
        Long following = prefs.getLong(KEY_FOLLOWING_COUNT, 0);
        String createAt = prefs.getString(KEY_CREATE_AT, null);

        Log.d("UserPrefs", "Loaded user:");
        Log.d("UserPrefs", "ID: " + id);
        Log.d("UserPrefs", "First Name: " + firstName);
        Log.d("UserPrefs", "Last Name: " + lastName);
        Log.d("UserPrefs", "Email: " + email);
        Log.d("UserPrefs", "Address: " + address);
        Log.d("UserPrefs", "Access Token: " + accessToken);
        Log.d("UserPrefs", "Refresh Token: " + refreshToken);
        Log.d("UserPrefs", "Phone: " + phone);
        Log.d("UserPrefs", "Avatar URL: " + avatar);
        Log.d("UserPrefs", "Post Count: " + postCount);
        Log.d("UserPrefs", "User Name: " + userName);
        Log.d("UserPrefs", "Following Count: " + following);
        Log.d("UserPrefs", "Follower Count: " + follower);

        return new User(id, firstName, lastName, accessToken ,email, phone, address, avatar, 
                refreshToken,userName,postCount, follower, following, createAt);
    }

    public int getUserId(){
        int id = (int) prefs.getLong(KEY_ID, 0);
        Log.d("UserPrefs", "ID: " + id);
        return id;
    }

    public String getAccessToken(){
        return prefs.getString(KEY_TOKEN_ACCESS, null);
    }

    public String getRefreshToken(){
        return prefs.getString(KEY_TOKEN_REFRESH, null);
    }

    public void clearUser(){
        prefs.edit().clear().apply();
    }

    public String getUserName(){
        return prefs.getString(KEY_USER_NAME, null);
    }

}

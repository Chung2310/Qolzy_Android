package com.example.qolzy.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.qolzy.data.model.User;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserSearchRepository {
    private static final String PREF_NAME = "search_history_pref";
    private static final String KEY_HISTORY = "search_history";
    private static final int MAX_SIZE = 5;
    private SharedPreferences prefs;
    private Gson gson;

    public UserSearchRepository(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Lấy danh sách lịch sử
    public List<User> getHistory() {
        String json = prefs.getString(KEY_HISTORY, "[]");
        Type type = new TypeToken<List<User>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Thêm một User vào lịch sử
    public void addUser(User user) {
        List<User> list = getHistory();

        // Nếu User đã tồn tại → xoá để thêm lại lên đầu
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == user.getId()) {
                list.remove(i);
                break;
            }
        }

        // Nếu đủ 5 thì xoá phần tử cũ nhất
        if (list.size() == MAX_SIZE) {
            list.remove(0);
        }

        // Thêm user mới vào cuối
        list.add(user);

        // Lưu lại
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply();
    }
}

package com.example.qolzy;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class QolzyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo Firebase ở đây
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
    }
}

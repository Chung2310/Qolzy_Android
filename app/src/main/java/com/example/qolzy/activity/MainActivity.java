package com.example.qolzy.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.qolzy.R;
import com.example.qolzy.databinding.ActivityMainBinding;
import com.example.qolzy.ui.account.AccountFragment;
import com.example.qolzy.ui.add_post.AddPostFragment;
import com.example.qolzy.ui.home.HomeFragment;
import com.example.qolzy.ui.reels.ReelsFragment;
import com.example.qolzy.ui.search.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int REQ_NOTIFY = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        requestNotificationPermissionIfNeeded();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            }
            else if (id == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            }
            else if (id == R.id.nav_add_post) {
                selectedFragment = new AddPostFragment();
            }
            else if (id == R.id.nav_reels) {
                selectedFragment = new ReelsFragment();
            }
            else if (id == R.id.nav_account) {
                selectedFragment = new AccountFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

    }

    public void switchToHome() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    public void setBottomNavigationVisibility(boolean isVisible) {
        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        // Chỉ cần từ Android 13 (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFY
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_NOTIFY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d("Permission", "✅ Notification permission granted");

            } else {
                Log.w("Permission", "❌ Notification permission denied");
            }
        }
    }
}

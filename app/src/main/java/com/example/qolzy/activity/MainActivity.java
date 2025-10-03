package com.example.qolzy.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

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


}

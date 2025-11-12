package com.example.qolzy.ui.add_post;

import static android.app.Activity.RESULT_OK;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.qolzy.R;
import com.example.qolzy.databinding.FragmentAddPostBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayoutMediator;

public class AddPostFragment extends Fragment {

    private FragmentAddPostBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPostBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        CreatePagerAdapter adapter = new CreatePagerAdapter(requireActivity());
        binding.viewPagerCreate.setAdapter(adapter);

        new TabLayoutMediator(binding.tabCreateType, binding.viewPagerCreate,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("Bài viết"); break;
                        case 1: tab.setText("Tin"); break;
                    }
                }).attach();
    }
}
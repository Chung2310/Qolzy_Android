package com.example.qolzy.ui.follow;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.R;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.model.User;
import com.example.qolzy.databinding.FragmentFollowBinding;
import com.example.qolzy.ui.account.AccountPagerAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

public class FollowFragment extends Fragment {

    private FollowViewModel mViewModel;
    private FragmentFollowBinding binding;
    private User user;

    public static FollowFragment newInstance(User user) {
        FollowFragment fragment = new FollowFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("USER", user);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFollowBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("USER");
        }
        setupViewPager();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FollowViewModel.class);

        binding.toolbarFollow.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });


    }

    private void setupViewPager() {
        // Adapter nên xử lý user có thể null (nếu chưa load xong)
        FollowPaperAdapter adapter = new FollowPaperAdapter(requireActivity(), user);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Người theo dõi");
                            break;
                        case 1:
                            tab.setText("Đang theo dõi");
                            break;
                    }
                }).attach();

        binding.viewPager.setOffscreenPageLimit(3);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ẩn BottomNavigation khi vào Fragment này
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Hiện lại khi rời khỏi Fragment
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(true);
    }
}